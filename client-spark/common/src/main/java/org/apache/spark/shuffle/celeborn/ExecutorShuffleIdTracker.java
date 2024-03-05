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

package org.apache.spark.shuffle.celeborn;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.celeborn.client.ShuffleClient;
import org.apache.celeborn.common.util.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorShuffleIdTracker {
  private static final Logger logger = LoggerFactory.getLogger(ExecutorShuffleIdTracker.class);

  // track appShuffleId -> shuffleId Set in executor for cleanup
  private ConcurrentHashMap<Integer, HashSet<Integer>> shuffleIdMap =
      JavaUtils.newConcurrentHashMap();

  public void track(int appShuffleId, int shuffleId) {
    logger.info("ExecutorShuffleIdTracker#track appShuffleId {}, shuffleId {}",
      appShuffleId,
      shuffleId);
    HashSet<Integer> shuffleIds = shuffleIdMap.computeIfAbsent(appShuffleId, id -> new HashSet<>());

    synchronized (shuffleIds) {
      shuffleIds.add(shuffleId);
    }
  }

  public void unregisterAppShuffleId(ShuffleClient shuffleClient, int appShuffleId) {
    HashSet<Integer> shuffleIds = shuffleIdMap.remove(appShuffleId);
    logger.info("inside ExecutorShuffleIdTracker#unregisterAppShuffleId, appShuffleId{}, empty {} ",
      appShuffleId,
      shuffleIds == null);
    if (shuffleIds != null) {
      synchronized (shuffleIds) {
        shuffleIds.forEach(shuffleClient::cleanupShuffle);
      }
    }
  }
}
