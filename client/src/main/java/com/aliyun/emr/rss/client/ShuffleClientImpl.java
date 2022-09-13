/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.emr.rss.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.emr.rss.client.compress.Compressor;
import com.aliyun.emr.rss.client.read.RssInputStream;
import com.aliyun.emr.rss.client.write.DataBatches;
import com.aliyun.emr.rss.client.write.PushState;
import com.aliyun.emr.rss.common.RssConf;
import com.aliyun.emr.rss.common.network.TransportContext;
import com.aliyun.emr.rss.common.network.buffer.NettyManagedBuffer;
import com.aliyun.emr.rss.common.network.client.RpcResponseCallback;
import com.aliyun.emr.rss.common.network.client.TransportClient;
import com.aliyun.emr.rss.common.network.client.TransportClientFactory;
import com.aliyun.emr.rss.common.network.protocol.PushData;
import com.aliyun.emr.rss.common.network.protocol.PushMergedData;
import com.aliyun.emr.rss.common.network.server.BaseMessageHandler;
import com.aliyun.emr.rss.common.network.util.TransportConf;
import com.aliyun.emr.rss.common.protocol.PartitionLocation;
import com.aliyun.emr.rss.common.protocol.RpcNameConstants;
import com.aliyun.emr.rss.common.protocol.TransportModuleConstants;
import com.aliyun.emr.rss.common.protocol.message.ControlMessages;
import com.aliyun.emr.rss.common.protocol.message.ControlMessages.*;
import com.aliyun.emr.rss.common.protocol.message.StatusCode;
import com.aliyun.emr.rss.common.rpc.RpcAddress;
import com.aliyun.emr.rss.common.rpc.RpcEndpointRef;
import com.aliyun.emr.rss.common.rpc.RpcEnv;
import com.aliyun.emr.rss.common.unsafe.Platform;
import com.aliyun.emr.rss.common.util.ThreadUtils;
import com.aliyun.emr.rss.common.util.Utils;

public class ShuffleClientImpl extends ShuffleClient {
  private static final Logger logger = LoggerFactory.getLogger(ShuffleClientImpl.class);

  private static final byte MASTER_MODE = PartitionLocation.Mode.MASTER.mode();

  private static final Random rand = new Random();

  private final RssConf conf;
  private final int registerShuffleMaxRetries;
  private final long registerShuffleRetryWait;
  private final int maxInFlight;
  private final int pushBufferSize;

  private final RpcEnv rpcEnv;

  private RpcEndpointRef driverRssMetaService;

  protected TransportClientFactory dataClientFactory;

  private InetAddress ia = null;

  // key: shuffleId, value: (partitionId, PartitionLocation)
  private final Map<Integer, ConcurrentHashMap<Integer, PartitionLocation>> reducePartitionMap =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Integer, Set<String>> mapperEndMap = new ConcurrentHashMap<>();

  // key: shuffleId-mapId-attemptId
  private final Map<String, PushState> pushStates = new ConcurrentHashMap<>();

  private final ExecutorService pushDataRetryPool;

  private final ExecutorService partitionSplitPool;
  private final Map<Integer, Set<Integer>> splitting = new ConcurrentHashMap<>();

  ThreadLocal<Compressor> compressorThreadLocal =
      new ThreadLocal<Compressor>() {
        @Override
        protected Compressor initialValue() {
          return Compressor.getCompressor(conf);
        }
      };

  private static class ReduceFileGroups {
    final PartitionLocation[][] partitionGroups;
    final int[] mapAttempts;

    ReduceFileGroups(PartitionLocation[][] partitionGroups, int[] mapAttempts) {
      this.partitionGroups = partitionGroups;
      this.mapAttempts = mapAttempts;
    }
  }
  // key: shuffleId
  private final Map<Integer, ReduceFileGroups> reduceFileGroupsMap = new ConcurrentHashMap<>();

  public ShuffleClientImpl(RssConf conf) {
    super();
    this.conf = conf;
    registerShuffleMaxRetries = RssConf.registerShuffleMaxRetry(conf);
    registerShuffleRetryWait = RssConf.registerShuffleRetryWait(conf);
    maxInFlight = RssConf.pushDataMaxReqsInFlight(conf);
    pushBufferSize = RssConf.pushDataBufferSize(conf);

    // init rpc env and master endpointRef
    rpcEnv = RpcEnv.create("ShuffleClient", Utils.localHostName(), 0, conf);

    TransportConf dataTransportConf =
        Utils.fromRssConf(
            conf, TransportModuleConstants.DATA_MODULE, conf.getInt("rss.data.io.threads", 8));
    TransportContext context =
        new TransportContext(dataTransportConf, new BaseMessageHandler(), true);
    dataClientFactory = context.createClientFactory();

    int retryThreadNum = RssConf.pushDataRetryThreadNum(conf);
    pushDataRetryPool = ThreadUtils.newDaemonCachedThreadPool("Retry-Sender", retryThreadNum, 60);

    int splitPoolSize = RssConf.clientSplitPoolSize(conf);
    partitionSplitPool = ThreadUtils.newDaemonCachedThreadPool("Shuffle-Split", splitPoolSize, 60);
  }

  private void submitRetryPushData(
      String applicationId,
      int shuffleId,
      int mapId,
      int attemptId,
      byte[] body,
      int batchId,
      PartitionLocation loc,
      RpcResponseCallback callback,
      PushState pushState,
      StatusCode cause) {
    int partitionId = loc.getId();
    if (!revive(
        applicationId, shuffleId, mapId, attemptId, partitionId, loc.getEpoch(), loc, cause)) {
      callback.onFailure(new IOException("Revive Failed"));
    } else if (mapperEnded(shuffleId, mapId, attemptId)) {
      logger.debug(
          "Retrying push data, but the mapper(map {} attempt {}) has ended.", mapId, attemptId);
      pushState.inFlightBatches.remove(batchId);
    } else {
      PartitionLocation newLoc = reducePartitionMap.get(shuffleId).get(partitionId);
      logger.info("Revive success, new location for reduce {} is {}.", partitionId, newLoc);
      try {
        TransportClient client =
            dataClientFactory.createClient(newLoc.getHost(), newLoc.getPushPort(), partitionId);
        NettyManagedBuffer newBuffer = new NettyManagedBuffer(Unpooled.wrappedBuffer(body));
        String shuffleKey = Utils.makeShuffleKey(applicationId, shuffleId);

        PushData newPushData =
            new PushData(MASTER_MODE, shuffleKey, newLoc.getUniqueId(), newBuffer);
        ChannelFuture future = client.pushData(newPushData, callback);
        pushState.addFuture(batchId, future);
      } catch (Exception ex) {
        logger.warn(
            "Exception raised while pushing data for shuffle {} map {} attempt {}" + " batch {}.",
            shuffleId,
            mapId,
            attemptId,
            batchId,
            ex);
        callback.onFailure(ex);
      }
    }
  }

  private void submitRetryPushMergedData(
      PushState pushState,
      String applicationId,
      int shuffleId,
      int mapId,
      int attemptId,
      ArrayList<DataBatches.DataBatch> batches,
      boolean revived,
      StatusCode cause) {
    HashMap<String, DataBatches> newDataBatchesMap = new HashMap<>();
    for (DataBatches.DataBatch batch : batches) {
      int partitionId = batch.loc.getId();
      if (!revive(
          applicationId,
          shuffleId,
          mapId,
          attemptId,
          partitionId,
          batch.loc.getEpoch(),
          batch.loc,
          cause)) {
        pushState.exception.compareAndSet(
            null,
            new IOException("Revive Failed in retry push merged data for location: " + batch.loc));
        return;
      } else if (mapperEnded(shuffleId, mapId, attemptId)) {
        logger.debug(
            "Retrying push data, but the mapper(map {} attempt {}) has ended.", mapId, attemptId);
      } else {
        PartitionLocation newLoc = reducePartitionMap.get(shuffleId).get(partitionId);
        logger.info("Revive success, new location for reduce {} is {}.", partitionId, newLoc);
        DataBatches newDataBatches =
            newDataBatchesMap.computeIfAbsent(genAddressPair(newLoc), (s) -> new DataBatches());
        newDataBatches.addDataBatch(newLoc, batch.batchId, batch.body);
      }
    }

    for (Map.Entry<String, DataBatches> entry : newDataBatchesMap.entrySet()) {
      String addressPair = entry.getKey();
      DataBatches newDataBatches = entry.getValue();
      String[] tokens = addressPair.split("-");
      doPushMergedData(
          tokens[0],
          applicationId,
          shuffleId,
          mapId,
          attemptId,
          newDataBatches.requireBatches(),
          pushState,
          revived);
    }
  }

  private String genAddressPair(PartitionLocation loc) {
    String addressPair;
    if (loc.getPeer() != null) {
      addressPair = loc.hostAndPushPort() + "-" + loc.getPeer().hostAndPushPort();
    } else {
      addressPair = loc.hostAndPushPort();
    }
    return addressPair;
  }

  private ConcurrentHashMap<Integer, PartitionLocation> registerShuffle(
      String appId, int shuffleId, int numMappers, int numPartitions) {
    int numRetries = registerShuffleMaxRetries;
    while (numRetries > 0) {
      try {
        RegisterShuffleResponse response =
            driverRssMetaService.askSync(
                new RegisterShuffle(appId, shuffleId, numMappers, numPartitions),
                ClassTag$.MODULE$.apply(RegisterShuffleResponse.class));

        if (response.status().equals(StatusCode.SUCCESS)) {
          ConcurrentHashMap<Integer, PartitionLocation> result = new ConcurrentHashMap<>();
          for (int i = 0; i < response.partitionLocations().size(); i++) {
            PartitionLocation partitionLoc = response.partitionLocations().get(i);
            result.put(partitionLoc.getId(), partitionLoc);
          }
          return result;
        } else if (response.status().equals(StatusCode.SLOT_NOT_AVAILABLE)) {
          logger.warn(
              "LifecycleManager request slots return {}, retry again, remain retry times {}",
              StatusCode.SLOT_NOT_AVAILABLE,
              numRetries - 1);
        } else {
          logger.error(
              "LifecycleManager request slots return {}, retry again, remain retry times {}",
              StatusCode.FAILED,
              numRetries - 1);
        }
      } catch (Exception e) {
        logger.error(
            "Exception raised while registering shuffle {} with {} mapper and {} partitions.",
            shuffleId,
            numMappers,
            numPartitions,
            e);
        break;
      }

      try {
        TimeUnit.SECONDS.sleep(registerShuffleRetryWait);
      } catch (InterruptedException e) {
        break;
      }
      numRetries--;
    }

    return null;
  }

  private void limitMaxInFlight(String mapKey, PushState pushState, int limit) throws IOException {
    if (pushState.exception.get() != null) {
      throw pushState.exception.get();
    }

    ConcurrentHashMap<Integer, PartitionLocation> inFlightBatches = pushState.inFlightBatches;
    long timeoutMs = RssConf.limitInFlightTimeoutMs(conf);
    long delta = RssConf.limitInFlightSleepDeltaMs(conf);
    long times = timeoutMs / delta;
    try {
      while (times > 0) {
        if (inFlightBatches.size() <= limit) {
          break;
        }
        if (pushState.exception.get() != null) {
          throw pushState.exception.get();
        }
        Thread.sleep(delta);
        times--;
      }
    } catch (InterruptedException e) {
      pushState.exception.set(new IOException(e));
    }

    if (times <= 0) {
      logger.error(
          "After waiting for {} ms, there are still {} batches in flight for map {}, "
              + "which exceeds the limit {}.",
          timeoutMs,
          inFlightBatches.size(),
          mapKey,
          limit);
      logger.error("Map: {} in flight batches: {}", mapKey, inFlightBatches);
      throw new IOException("wait timeout for task " + mapKey, pushState.exception.get());
    }
    if (pushState.exception.get() != null) {
      throw pushState.exception.get();
    }
  }

  private boolean waitRevivedLocation(
      ConcurrentHashMap<Integer, PartitionLocation> map, int partitionId, int epoch) {
    PartitionLocation currentLocation = map.get(partitionId);
    if (currentLocation != null && currentLocation.getEpoch() > epoch) {
      return true;
    }

    long sleepTimeMs = rand.nextInt(50);
    if (sleepTimeMs > 30) {
      try {
        TimeUnit.MILLISECONDS.sleep(sleepTimeMs);
      } catch (InterruptedException e) {
        logger.warn("Wait revived location interrupted", e);
        Thread.currentThread().interrupt();
      }
    }

    currentLocation = map.get(partitionId);
    return currentLocation != null && currentLocation.getEpoch() > epoch;
  }

  private boolean revive(
      String applicationId,
      int shuffleId,
      int mapId,
      int attemptId,
      int partitionId,
      int epoch,
      PartitionLocation oldLocation,
      StatusCode cause) {
    ConcurrentHashMap<Integer, PartitionLocation> map = reducePartitionMap.get(shuffleId);
    if (waitRevivedLocation(map, partitionId, epoch)) {
      logger.debug(
          "Has already revived for shuffle {} map {} reduce {} epoch {},"
              + " just return(Assume revive successfully).",
          shuffleId,
          mapId,
          partitionId,
          epoch);
      return true;
    }
    String mapKey = Utils.makeMapKey(shuffleId, mapId, attemptId);
    if (mapperEnded(shuffleId, mapId, attemptId)) {
      logger.debug(
          "The mapper(shuffle {} map {}) has already ended, just return(Assume"
              + " revive successfully).",
          shuffleId,
          mapId);
      return true;
    }

    try {
      ChangeLocationResponse response =
          driverRssMetaService.askSync(
              new Revive(
                  applicationId,
                  shuffleId,
                  mapId,
                  attemptId,
                  partitionId,
                  epoch,
                  oldLocation,
                  cause),
              ClassTag$.MODULE$.apply(ChangeLocationResponse.class));
      // per partitionKey only serve single PartitionLocation in Client Cache.
      if (response.status().equals(StatusCode.SUCCESS)) {
        map.put(partitionId, response.partition());
        return true;
      } else if (response.status().equals(StatusCode.MAP_ENDED)) {
        mapperEndMap.computeIfAbsent(shuffleId, (id) -> ConcurrentHashMap.newKeySet()).add(mapKey);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      logger.error(
          "Exception raised while reviving for shuffle {} reduce {} epoch {}.",
          shuffleId,
          partitionId,
          epoch,
          e);
      return false;
    }
  }

  public int pushOrMergeData(
      String applicationId,
      int shuffleId,
      int mapId,
      int attemptId,
      int partitionId,
      byte[] data,
      int offset,
      int length,
      int numMappers,
      int numPartitions,
      boolean doPush)
      throws IOException {
    // mapKey
    final String mapKey = Utils.makeMapKey(shuffleId, mapId, attemptId);
    final String shuffleKey = Utils.makeShuffleKey(applicationId, shuffleId);
    // return if shuffle stage already ended
    if (mapperEnded(shuffleId, mapId, attemptId)) {
      logger.debug(
          "The mapper(shuffle {} map {} attempt {}) has already ended while" + " pushing data.",
          shuffleId,
          mapId,
          attemptId);
      PushState pushState = pushStates.get(mapKey);
      if (pushState != null) {
        pushState.cancelFutures();
      }
      return 0;
    }
    // register shuffle if not registered
    final ConcurrentHashMap<Integer, PartitionLocation> map =
        reducePartitionMap.computeIfAbsent(
            shuffleId,
            (id) -> registerShuffle(applicationId, shuffleId, numMappers, numPartitions));

    if (map == null) {
      throw new IOException("Register shuffle failed for shuffle " + shuffleKey);
    }

    // get location
    if (!map.containsKey(partitionId)
        && !revive(
            applicationId,
            shuffleId,
            mapId,
            attemptId,
            partitionId,
            0,
            null,
            StatusCode.PUSH_DATA_FAIL_NON_CRITICAL_CAUSE)) {
      throw new IOException(
          "Revive for shuffle " + shuffleKey + " partitionId " + partitionId + " failed.");
    }

    if (mapperEnded(shuffleId, mapId, attemptId)) {
      logger.debug(
          "The mapper(shuffle {} map {} attempt {}) has already ended while" + " pushing data.",
          shuffleId,
          mapId,
          attemptId);
      PushState pushState = pushStates.get(mapKey);
      if (pushState != null) {
        pushState.cancelFutures();
      }
      return 0;
    }

    final PartitionLocation loc = map.get(partitionId);
    if (loc == null) {
      throw new IOException(
          "Partition location for shuffle "
              + shuffleKey
              + " partitionId "
              + partitionId
              + " is NULL!");
    }

    PushState pushState = pushStates.computeIfAbsent(mapKey, (s) -> new PushState(conf));

    // increment batchId
    final int nextBatchId = pushState.batchId.addAndGet(1);

    // compress data
    final Compressor compressor = compressorThreadLocal.get();
    compressor.compress(data, offset, length);

    final int compressedTotalSize = compressor.getCompressedTotalSize();
    final int BATCH_HEADER_SIZE = 4 * 4;
    final byte[] body = new byte[BATCH_HEADER_SIZE + compressedTotalSize];
    Platform.putInt(body, Platform.BYTE_ARRAY_OFFSET, mapId);
    Platform.putInt(body, Platform.BYTE_ARRAY_OFFSET + 4, attemptId);
    Platform.putInt(body, Platform.BYTE_ARRAY_OFFSET + 8, nextBatchId);
    Platform.putInt(body, Platform.BYTE_ARRAY_OFFSET + 12, compressedTotalSize);
    System.arraycopy(
        compressor.getCompressedBuffer(), 0, body, BATCH_HEADER_SIZE, compressedTotalSize);

    if (doPush) {
      logger.debug(
          "Do push data for app {} shuffle {} map {} attempt {} reduce {} batch {}.",
          applicationId,
          shuffleId,
          mapId,
          attemptId,
          partitionId,
          nextBatchId);
      // check limit
      limitMaxInFlight(mapKey, pushState, maxInFlight);

      // add inFlight requests
      pushState.inFlightBatches.put(nextBatchId, loc);

      // build PushData request
      NettyManagedBuffer buffer = new NettyManagedBuffer(Unpooled.wrappedBuffer(body));
      PushData pushData = new PushData(MASTER_MODE, shuffleKey, loc.getUniqueId(), buffer);

      // build callback
      RpcResponseCallback callback =
          new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
              pushState.inFlightBatches.remove(nextBatchId);
              if (response.remaining() > 0 && response.get() == StatusCode.STAGE_ENDED.getValue()) {
                mapperEndMap
                    .computeIfAbsent(shuffleId, (id) -> ConcurrentHashMap.newKeySet())
                    .add(mapKey);
              }
              pushState.removeFuture(nextBatchId);
              logger.debug(
                  "Push data to {}:{} success for map {} attempt {} batch {}.",
                  loc.getHost(),
                  loc.getPushPort(),
                  mapId,
                  attemptId,
                  nextBatchId);
            }

            @Override
            public void onFailure(Throwable e) {
              pushState.exception.compareAndSet(
                  null, new IOException("Revived PushData failed!", e));
              pushState.removeFuture(nextBatchId);
              logger.error(
                  "Push data to {}:{} failed for map {} attempt {} batch {}.",
                  loc.getHost(),
                  loc.getPushPort(),
                  mapId,
                  attemptId,
                  nextBatchId,
                  e);
            }
          };

      RpcResponseCallback wrappedCallback =
          new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
              if (response.remaining() > 0) {
                byte reason = response.get();
                if (reason == StatusCode.SOFT_SPLIT.getValue()) {
                  logger.debug(
                      "Push data split required for map {} attempt {} batch {}",
                      mapId,
                      attemptId,
                      nextBatchId);
                  splitPartition(shuffleId, partitionId, applicationId, loc);
                  callback.onSuccess(response);
                } else if (reason == StatusCode.HARD_SPLIT.getValue()) {
                  logger.debug(
                      "Push data split for map {} attempt {} batch {}.",
                      mapId,
                      attemptId,
                      nextBatchId);
                  pushDataRetryPool.submit(
                      () ->
                          submitRetryPushData(
                              applicationId,
                              shuffleId,
                              mapId,
                              attemptId,
                              body,
                              nextBatchId,
                              loc,
                              this,
                              pushState,
                              StatusCode.HARD_SPLIT));
                } else {
                  response.rewind();
                  callback.onSuccess(response);
                }
              } else {
                callback.onSuccess(response);
              }
            }

            @Override
            public void onFailure(Throwable e) {
              if (pushState.exception.get() != null) {
                return;
              }
              logger.error(
                  "Push data to {}:{} failed for map {} attempt {} batch {}.",
                  loc.getHost(),
                  loc.getPushPort(),
                  mapId,
                  attemptId,
                  nextBatchId,
                  e);
              // async retry push data
              if (!mapperEnded(shuffleId, mapId, attemptId)) {
                pushDataRetryPool.submit(
                    () ->
                        submitRetryPushData(
                            applicationId,
                            shuffleId,
                            mapId,
                            attemptId,
                            body,
                            nextBatchId,
                            loc,
                            callback,
                            pushState,
                            getPushDataFailCause(e.getMessage())));
              } else {
                pushState.inFlightBatches.remove(nextBatchId);
                logger.info(
                    "Mapper shuffleId:{} mapId:{} attempt:{} already ended, remove batchId:{}.",
                    shuffleId,
                    mapId,
                    attemptId,
                    nextBatchId);
              }
            }
          };

      // do push data
      try {
        TransportClient client =
            dataClientFactory.createClient(loc.getHost(), loc.getPushPort(), partitionId);
        ChannelFuture future = client.pushData(pushData, wrappedCallback);
        pushState.addFuture(nextBatchId, future);
      } catch (Exception e) {
        logger.warn("PushData failed", e);
        wrappedCallback.onFailure(
            new Exception(getPushDataFailCause(e.getMessage()).toString(), e));
      }
    } else {
      // add batch data
      logger.debug("Merge batch {}.", nextBatchId);
      String addressPair = genAddressPair(loc);
      boolean shoudPush = pushState.addBatchData(addressPair, loc, nextBatchId, body);
      if (shoudPush) {
        limitMaxInFlight(mapKey, pushState, maxInFlight);
        DataBatches dataBatches = pushState.takeDataBatches(addressPair);
        doPushMergedData(
            addressPair.split("-")[0],
            applicationId,
            shuffleId,
            mapId,
            attemptId,
            dataBatches.requireBatches(),
            pushState,
            false);
      }
    }

    return body.length;
  }

  private void splitPartition(
      int shuffleId, int partitionId, String applicationId, PartitionLocation loc) {
    Set<Integer> splittingSet =
        splitting.computeIfAbsent(shuffleId, integer -> ConcurrentHashMap.newKeySet());
    synchronized (splittingSet) {
      if (splittingSet.contains(partitionId)) {
        logger.debug(
            "shuffle {} partitionId {} is splitting, skip split request ", shuffleId, partitionId);
        return;
      }
      splittingSet.add(partitionId);
    }

    ConcurrentHashMap<Integer, PartitionLocation> currentShuffleLocs =
        reducePartitionMap.get(shuffleId);

    ShuffleClientHelper.sendShuffleSplitAsync(
        driverRssMetaService,
        new PartitionSplit(applicationId, shuffleId, partitionId, loc.getEpoch(), loc),
        partitionSplitPool,
        splittingSet,
        partitionId,
        shuffleId,
        currentShuffleLocs);
  }

  @Override
  public int pushData(
      String applicationId,
      int shuffleId,
      int mapId,
      int attemptId,
      int partitionId,
      byte[] data,
      int offset,
      int length,
      int numMappers,
      int numPartitions)
      throws IOException {
    return pushOrMergeData(
        applicationId,
        shuffleId,
        mapId,
        attemptId,
        partitionId,
        data,
        offset,
        length,
        numMappers,
        numPartitions,
        true);
  }

  @Override
  public void prepareForMergeData(int shuffleId, int mapId, int attemptId) throws IOException {
    final String mapKey = Utils.makeMapKey(shuffleId, mapId, attemptId);
    PushState pushState = pushStates.get(mapKey);
    if (pushState != null) {
      limitMaxInFlight(mapKey, pushState, 0);
    }
  }

  @Override
  public int mergeData(
      String applicationId,
      int shuffleId,
      int mapId,
      int attemptId,
      int partitionId,
      byte[] data,
      int offset,
      int length,
      int numMappers,
      int numPartitions)
      throws IOException {
    return pushOrMergeData(
        applicationId,
        shuffleId,
        mapId,
        attemptId,
        partitionId,
        data,
        offset,
        length,
        numMappers,
        numPartitions,
        false);
  }

  public void pushMergedData(String applicationId, int shuffleId, int mapId, int attemptId)
      throws IOException {
    final String mapKey = Utils.makeMapKey(shuffleId, mapId, attemptId);
    PushState pushState = pushStates.get(mapKey);
    if (pushState == null) {
      return;
    }
    ArrayList<Map.Entry<String, DataBatches>> batchesArr =
        new ArrayList<>(pushState.batchesMap.entrySet());
    while (!batchesArr.isEmpty()) {
      limitMaxInFlight(mapKey, pushState, maxInFlight);
      Map.Entry<String, DataBatches> entry = batchesArr.get(rand.nextInt(batchesArr.size()));
      ArrayList<DataBatches.DataBatch> batches = entry.getValue().requireBatches(pushBufferSize);
      if (entry.getValue().getTotalSize() == 0) {
        batchesArr.remove(entry);
      }
      String[] tokens = entry.getKey().split("-");
      doPushMergedData(
          tokens[0], applicationId, shuffleId, mapId, attemptId, batches, pushState, false);
    }
  }

  private void doPushMergedData(
      String hostPort,
      String applicationId,
      int shuffleId,
      int mapId,
      int attemptId,
      ArrayList<DataBatches.DataBatch> batches,
      PushState pushState,
      boolean revived) {
    final String[] splits = hostPort.split(":");
    final String host = splits[0];
    final int port = Integer.parseInt(splits[1]);

    int groupedBatchId = pushState.batchId.addAndGet(1);
    pushState.inFlightBatches.put(groupedBatchId, batches.get(0).loc);

    final int numBatches = batches.size();
    final String[] partitionUniqueIds = new String[numBatches];
    final int[] offsets = new int[numBatches];
    final int[] batchIds = new int[numBatches];
    int currentSize = 0;
    CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
    for (int i = 0; i < numBatches; i++) {
      DataBatches.DataBatch batch = batches.get(i);
      partitionUniqueIds[i] = batch.loc.getUniqueId();
      offsets[i] = currentSize;
      batchIds[i] = batch.batchId;
      currentSize += batch.body.length;
      byteBuf.addComponent(true, Unpooled.wrappedBuffer(batch.body));
    }
    NettyManagedBuffer buffer = new NettyManagedBuffer(byteBuf);
    String shuffleKey = Utils.makeShuffleKey(applicationId, shuffleId);
    PushMergedData mergedData =
        new PushMergedData(MASTER_MODE, shuffleKey, partitionUniqueIds, offsets, buffer);

    RpcResponseCallback callback =
        new RpcResponseCallback() {
          @Override
          public void onSuccess(ByteBuffer response) {
            logger.debug(
                "Push data success for map {} attempt {} grouped batch {}.",
                mapId,
                attemptId,
                groupedBatchId);
            pushState.inFlightBatches.remove(groupedBatchId);
            if (response.remaining() > 0 && response.get() == StatusCode.STAGE_ENDED.getValue()) {
              mapperEndMap
                  .computeIfAbsent(shuffleId, (id) -> ConcurrentHashMap.newKeySet())
                  .add(Utils.makeMapKey(shuffleId, mapId, attemptId));
            }
          }

          @Override
          public void onFailure(Throwable e) {
            String errorMsg =
                (revived ? "Revived push" : "Push")
                    + " merged data to "
                    + host
                    + ":"
                    + port
                    + " failed for map "
                    + mapId
                    + " attempt "
                    + attemptId
                    + " batches "
                    + Arrays.toString(batchIds)
                    + ".";
            pushState.exception.compareAndSet(null, new IOException(errorMsg, e));
            if (logger.isDebugEnabled()) {
              for (int batchId : batchIds) {
                logger.debug(
                    "Push data failed for map {} attempt {} batch {}.", mapId, attemptId, batchId);
              }
            }
          }
        };

    RpcResponseCallback wrappedCallback =
        new RpcResponseCallback() {
          @Override
          public void onSuccess(ByteBuffer response) {
            callback.onSuccess(response);
          }

          @Override
          public void onFailure(Throwable e) {
            if (pushState.exception.get() != null) {
              return;
            }
            if (revived) {
              callback.onFailure(e);
              return;
            }
            logger.error(
                (revived ? "Revived push" : "Push")
                    + " merged data to "
                    + host
                    + ":"
                    + port
                    + " failed for map "
                    + mapId
                    + " attempt "
                    + attemptId
                    + " batches "
                    + Arrays.toString(batchIds)
                    + ".",
                e);
            pushState.inFlightBatches.remove(groupedBatchId);
            if (!mapperEnded(shuffleId, mapId, attemptId)) {
              pushDataRetryPool.submit(
                  () ->
                      submitRetryPushMergedData(
                          pushState,
                          applicationId,
                          shuffleId,
                          mapId,
                          attemptId,
                          batches,
                          true,
                          getPushDataFailCause(e.getMessage())));
            }
          }
        };

    // do push merged data
    try {
      TransportClient client = dataClientFactory.createClient(host, port);
      client.pushMergedData(mergedData, wrappedCallback);
    } catch (Exception e) {
      logger.warn("PushMergedData failed", e);
      wrappedCallback.onFailure(new Exception(getPushDataFailCause(e.getMessage()).toString(), e));
    }
  }

  @Override
  public void mapperEnd(
      String applicationId, int shuffleId, int mapId, int attemptId, int numMappers)
      throws IOException {
    final String mapKey = Utils.makeMapKey(shuffleId, mapId, attemptId);
    PushState pushState = pushStates.computeIfAbsent(mapKey, (s) -> new PushState(conf));

    try {
      limitMaxInFlight(mapKey, pushState, 0);

      MapperEndResponse response =
          driverRssMetaService.<MapperEndResponse>askSync(
              new MapperEnd(applicationId, shuffleId, mapId, attemptId, numMappers),
              ClassTag$.MODULE$.<MapperEndResponse>apply(MapperEndResponse.class));
      if (response.status() != StatusCode.SUCCESS) {
        throw new IOException("MapperEnd failed! StatusCode: " + response.status());
      }
    } finally {
      pushStates.remove(mapKey);
    }
  }

  @Override
  public void cleanup(String applicationId, int shuffleId, int mapId, int attemptId) {
    final String mapKey = Utils.makeMapKey(shuffleId, mapId, attemptId);
    PushState pushState = pushStates.remove(mapKey);
    if (pushState != null) {
      pushState.exception.compareAndSet(null, new IOException("Cleaned Up"));
      pushState.cancelFutures();
    }
  }

  @Override
  public boolean unregisterShuffle(String applicationId, int shuffleId, boolean isDriver) {
    if (isDriver) {
      try {
        driverRssMetaService.send(
            new UnregisterShuffle(applicationId, shuffleId, ControlMessages.ZERO_UUID()));
      } catch (Exception e) {
        // If some exceptions need to be ignored, they shouldn't be logged as error-level,
        // otherwise it will mislead users.
        logger.warn("Send UnregisterShuffle failed, ignore.", e);
      }
    }

    // clear status
    reducePartitionMap.remove(shuffleId);
    reduceFileGroupsMap.remove(shuffleId);
    mapperEndMap.remove(shuffleId);
    splitting.remove(shuffleId);

    logger.info("Unregistered shuffle {}.", shuffleId);
    return true;
  }

  @Override
  public RssInputStream readPartition(
      String applicationId, int shuffleId, int partitionId, int attemptNumber) throws IOException {
    return readPartition(
        applicationId, shuffleId, partitionId, attemptNumber, 0, Integer.MAX_VALUE);
  }

  @Override
  public RssInputStream readPartition(
      String applicationId,
      int shuffleId,
      int partitionId,
      int attemptNumber,
      int startMapIndex,
      int endMapIndex)
      throws IOException {
    String shuffleKey = Utils.makeShuffleKey(applicationId, shuffleId);
    ReduceFileGroups fileGroups =
        reduceFileGroupsMap.computeIfAbsent(
            shuffleId,
            (id) -> {
              try {
                if (driverRssMetaService == null) {
                  logger.warn("Driver endpoint is null!");
                  return null;
                }

                GetReducerFileGroup getReducerFileGroup =
                    new GetReducerFileGroup(applicationId, shuffleId);
                ClassTag<GetReducerFileGroupResponse> classTag =
                    ClassTag$.MODULE$.apply(GetReducerFileGroupResponse.class);

                GetReducerFileGroupResponse response =
                    driverRssMetaService.<GetReducerFileGroupResponse>askSync(
                        getReducerFileGroup, classTag);

                if (response.status() == StatusCode.SUCCESS) {
                  return new ReduceFileGroups(response.fileGroup(), response.attempts());
                } else if (response.status() == StatusCode.STAGE_END_TIME_OUT) {
                  logger.warn(
                      "Request {} return {} for {}",
                      getReducerFileGroup,
                      StatusCode.STAGE_END_TIME_OUT.toString(),
                      shuffleKey);
                } else if (response.status() == StatusCode.SHUFFLE_DATA_LOST) {
                  logger.warn(
                      "Request {} return {} for {}",
                      getReducerFileGroup,
                      StatusCode.SHUFFLE_DATA_LOST.toString(),
                      shuffleKey);
                }
              } catch (Exception e) {
                logger.error(
                    "Exception raised while call GetReducerFileGroup for " + shuffleKey + ".", e);
              }
              return null;
            });

    if (fileGroups == null) {
      String msg = "Shuffle data lost for shuffle " + shuffleId + " reduce " + partitionId + "!";
      logger.error(msg);
      throw new IOException(msg);
    } else if (fileGroups.partitionGroups.length == 0) {
      logger.warn("Shuffle data is empty for shuffle {} reduce {}.", shuffleId, partitionId);
      return RssInputStream.empty();
    } else {
      return RssInputStream.create(
          conf,
          dataClientFactory,
          shuffleKey,
          fileGroups.partitionGroups[partitionId],
          fileGroups.mapAttempts,
          attemptNumber,
          startMapIndex,
          endMapIndex);
    }
  }

  @Override
  public void shutDown() {
    if (null != rpcEnv) {
      rpcEnv.shutdown();
    }
    if (null != dataClientFactory) {
      dataClientFactory.close();
    }
    if (null != pushDataRetryPool) {
      pushDataRetryPool.shutdown();
    }
    if (null != partitionSplitPool) {
      partitionSplitPool.shutdown();
    }
    if (null != driverRssMetaService) {
      driverRssMetaService = null;
    }
    logger.warn("Shuffle client has been shutdown!");
  }

  @Override
  public void setupMetaServiceRef(String host, int port) {
    driverRssMetaService =
        rpcEnv.setupEndpointRef(new RpcAddress(host, port), RpcNameConstants.RSS_METASERVICE_EP);
  }

  @Override
  public void setupMetaServiceRef(RpcEndpointRef endpointRef) {
    driverRssMetaService = endpointRef;
  }

  private synchronized String getLocalHost() {
    if (ia == null) {
      try {
        ia = InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
        logger.error("Unknown host", e);
        return null;
      }
    }
    return ia.getHostName();
  }

  private boolean mapperEnded(int shuffleId, int mapId, int attemptId) {
    return mapperEndMap.containsKey(shuffleId)
        && mapperEndMap.get(shuffleId).contains(Utils.makeMapKey(shuffleId, mapId, attemptId));
  }

  private StatusCode getPushDataFailCause(String message) {
    logger.info("[getPushDataFailCause] message: " + message);
    StatusCode cause;
    if (StatusCode.PUSH_DATA_FAIL_SLAVE.getMessage().equals(message)) {
      cause = StatusCode.PUSH_DATA_FAIL_SLAVE;
    } else if (StatusCode.PUSH_DATA_FAIL_MAIN.getMessage().equals(message)
        || connectFail(message)) {
      cause = StatusCode.PUSH_DATA_FAIL_MAIN;
    } else {
      cause = StatusCode.PUSH_DATA_FAIL_NON_CRITICAL_CAUSE;
    }
    return cause;
  }

  private boolean connectFail(String message) {
    return (message.startsWith("Connection from ") && message.endsWith(" closed"))
        || (message.equals("Connection reset by peer"))
        || (message.startsWith("Failed to send RPC "));
  }
}
