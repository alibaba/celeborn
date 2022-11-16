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

package com.aliyun.emr.rss.service.deploy.master.clustermeta;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.emr.rss.common.RssConf;
import com.aliyun.emr.rss.common.meta.WorkerInfo;
import com.aliyun.emr.rss.common.rpc.RpcEnv;
import com.aliyun.emr.rss.service.deploy.master.metrics.AppDiskUsageMetric;

public class SingleMasterMetaManager extends AbstractMetaManager {
  private static final Logger LOG = LoggerFactory.getLogger(SingleMasterMetaManager.class);

  public SingleMasterMetaManager(RpcEnv rpcEnv, RssConf conf) {
    this.rpcEnv = rpcEnv;
    this.conf = conf;
    this.appDiskUsageMetric=new AppDiskUsageMetric(conf);
  }

  @Override
  public void handleRequestSlots(
      String shuffleKey,
      String hostName,
      Map<WorkerInfo, Integer> workerToAllocatedSlots,
      String requestId) {
    updateRequestSlotsMeta(shuffleKey, hostName, null);
    synchronized (workers) {
      for (WorkerInfo workerInfo : workerToAllocatedSlots.keySet()) {
        workerInfo.allocateSlots(shuffleKey, workerToAllocatedSlots.get(workerInfo));
      }
    }
  }

  @Override
  public void handleReleaseSlots(String shuffleKey, List<String> workerIds,
                                 List<Integer> slots, String requestId) {
    updateReleaseSlotsMeta(shuffleKey, workerIds, slots);
  }

  @Override
  public void handleUnRegisterShuffle(String shuffleKey, String requestId) {
    updateUnregisterShuffleMeta(shuffleKey);
  }

  @Override
  public void handleAppHeartbeat(String appId, long time, String requestId) {
    updateAppHeartBeatMeta(appId, time);
  }

  @Override
  public void handleAppLost(String appId, String requestId) {
    updateAppLostMeta(appId);
  }

  @Override
  public void handleWorkerLost(String host, int rpcPort,
    int pushPort, int fetchPort, int replicatePort, String requestId) {
    updateWorkerLostMeta(host, rpcPort, pushPort, fetchPort, replicatePort);
  }

  @Override
  public void handleWorkerHeartBeat(String host, int rpcPort, int pushPort, int fetchPort,
      int replicatePort, int numSlots, long time,
      Map<String, Long> shuffleDiskUsage, String requestId) {
    updateWorkerHeartBeatMeta(host, rpcPort, pushPort, fetchPort, replicatePort,
            numSlots, time, shuffleDiskUsage);
  }

  @Override
  public void handleRegisterWorker(String host, int rpcPort, int pushPort, int fetchPort,
    int replicatePort, int numSlots, String requestId) {
    updateRegisterWorkerMeta(host, rpcPort, pushPort, fetchPort, replicatePort, numSlots);
  }

  @Override
  public void handleReportWorkerFailure(List<WorkerInfo> failedNodes, String requestId) {
    updateBlacklistByReportWorkerFailure(failedNodes);
  }
}
