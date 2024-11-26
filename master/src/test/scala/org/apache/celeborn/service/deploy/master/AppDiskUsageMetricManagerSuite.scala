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

package org.apache.celeborn.service.deploy.master

import java.util

import scala.util.Random

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.funsuite.AnyFunSuite

import org.apache.celeborn.common.CelebornConf
import org.apache.celeborn.common.identity.UserIdentifier
import org.apache.celeborn.common.internal.Logging
import org.apache.celeborn.common.meta.{AppDiskUsageSnapShot, DiskInfo, WorkerInfo}
import org.apache.celeborn.common.quota.ResourceConsumption
import org.apache.celeborn.service.deploy.master.clustermeta.ha.HAMasterMetaManager

class AppDiskUsageMetricManagerSuite extends AnyFunSuite
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Logging {
  val WORKER1 = new WorkerInfo("host1", 111, 112, 113, 114, 115)
  val WORKER2 = new WorkerInfo("host2", 211, 212, 213, 214, 215)
  val WORKER3 = new WorkerInfo("host3", 311, 312, 313, 314, 315)

  def verifySnapShotOutput(snapShot: AppDiskUsageSnapShot, capacity: Int, appCount: Int): Unit = {
    val topNItemsEstimatedUsage = snapShot.topNItems
      .filter(usage => usage != null)
      .map(_.estimatedUsage)

    assert(snapShot.topItemCount == capacity)
    assert(topNItemsEstimatedUsage.length == appCount)
    assert(topNItemsEstimatedUsage sameElements topNItemsEstimatedUsage.sorted.reverse)
  }

  test("test snapshot ordering") {
    val snapShot = new AppDiskUsageSnapShot(50)
    val rand = new Random()
    for (i <- 1 to 5) {
      snapShot.updateAppDiskUsage(s"app-${i}", rand.nextInt(100000000) + 1)
    }

    verifySnapShotOutput(snapShot, 50, 5)
  }

  test("test snapshot ordering with capacity") {
    val snapShot = new AppDiskUsageSnapShot(50)
    val rand = new Random()
    for (i <- 1 to 60) {
      snapShot.updateAppDiskUsage(s"app-${i}", rand.nextInt(100000000) + 1)
    }

    verifySnapShotOutput(snapShot, 50, 50)
  }

  test("test snapshot ordering with duplicate entries") {
    val snapShot = new AppDiskUsageSnapShot(50)
    val rand = new Random()
    for (i <- 1 to 10) {
      snapShot.updateAppDiskUsage(s"app-${i}", rand.nextInt(100000000) + 1)
    }
    for (i <- 1 to 10) {
      snapShot.updateAppDiskUsage(s"app-${i}", rand.nextInt(100000000) + 1000000000)
    }

    verifySnapShotOutput(snapShot, 50, 10)
  }

  test("test app usage snapshot") {
    Thread.sleep(5000)

    val conf = new CelebornConf()
    conf.set(CelebornConf.METRICS_APP_TOP_DISK_USAGE_WINDOW_SIZE.key, "5")
    conf.set(CelebornConf.METRICS_APP_TOP_DISK_USAGE_INTERVAL.key, "2s")
    val masterStatusSystem = new HAMasterMetaManager(null, conf)
    val usageMetric = new AppDiskUsageMetricManager(conf, masterStatusSystem)

    masterStatusSystem.appHeartbeatTime.put("app1", System.currentTimeMillis())
    masterStatusSystem.appHeartbeatTime.put("app2", System.currentTimeMillis())
    masterStatusSystem.appHeartbeatTime.put("app3", System.currentTimeMillis())
    masterStatusSystem.appHeartbeatTime.put("app4", System.currentTimeMillis())
    masterStatusSystem.appHeartbeatTime.put("app5", System.currentTimeMillis())
    masterStatusSystem.appHeartbeatTime.put("app6", System.currentTimeMillis())

    val map1 = new util.HashMap[String, java.lang.Long]()
    map1.put("app1", 2874371)
    map1.put("app2", 43452)
    map1.put("app3", 2134526)
    map1.put("app4", 23465463)
    map1.put("app5", 132456)
    map1.put("app6", 6535635)
    val workerInfo = new WorkerInfo(
      "host1",
      1,
      2,
      3,
      4,
      5,
      new util.HashMap[String, DiskInfo](),
      new util.HashMap[UserIdentifier, ResourceConsumption](),
      map1)
    masterStatusSystem.workersMap.put(workerInfo.toUniqueId(), workerInfo)
    usageMetric.update()
    println(usageMetric.summary())
    Thread.sleep(2000)

    map1.clear()
    map1.put("app1", 374524)
    map1.put("app2", 5234665)
    map1.put("app3", 24453)
    map1.put("app4", 2345637)
    map1.put("app5", 4534)
    map1.put("app6", 5357)
    workerInfo.updateThenGetAppDiskUsage(map1)
    usageMetric.update()
    println(usageMetric.summary())
    Thread.sleep(2000)

    map1.clear()
    map1.put("app1", 12343)
    map1.put("app2", 3456565)
    map1.put("app3", 4345)
    map1.put("app4", 35245268)
    map1.put("app5", 45367)
    map1.put("app6", 64345)
    workerInfo.updateThenGetAppDiskUsage(map1)
    usageMetric.update()
    println(usageMetric.summary())
    Thread.sleep(2500)
    println(usageMetric.summary())
  }
}