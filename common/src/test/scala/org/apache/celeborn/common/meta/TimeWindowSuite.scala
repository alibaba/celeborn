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

package org.apache.celeborn.common.meta

import org.apache.celeborn.CelebornFunSuite
import org.apache.celeborn.common.CelebornConf
import org.apache.celeborn.common.identity.UserIdentifier
import org.apache.celeborn.common.quota.ResourceConsumption
import org.apache.celeborn.common.rpc.netty.{NettyRpcEndpointRef, NettyRpcEnv}
import org.apache.celeborn.common.rpc._
import org.apache.celeborn.common.util.ThreadUtils
import org.junit.Assert.{assertEquals, assertNotEquals, assertNotNull}

import java.util
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ConcurrentHashMap, Future, ThreadLocalRandom}
import java.util.{Map => jMap}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.reflect.ClassTag

class TimeWindowSuite extends CelebornFunSuite {

  test("test TimeWindow") {
    val tw = new TimeWindow(2, 2)
    tw.update(10)
    tw.getAverage()
    assert(tw.index == 0)
    tw.update(10)
    tw.update(10)
    tw.getAverage()
    assert(tw.index == 1)
    assert(tw.timeWindow(0) == (20, 2))
    tw.update(5)
    tw.update(5)
    tw.update(5)
    val avg = tw.getAverage()
    assert(tw.timeWindow(1) == (15, 3))
    assert(tw.index == 0)
    assert(avg == 7)
  }
}
