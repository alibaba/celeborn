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

import javax.ws.rs.core.MediaType

import com.google.common.io.Files

import org.apache.celeborn.common.CelebornConf
import org.apache.celeborn.common.util.{CelebornExitKind, Utils}
import org.apache.celeborn.server.common.HttpService
import org.apache.celeborn.server.common.http.ApiBaseResourceSuite

class ApiMasterResourceSuite extends ApiBaseResourceSuite {
  private var master: Master = _

  override protected def httpService: HttpService = master

  def getTmpDir(): String = {
    val tmpDir = Files.createTempDir()
    tmpDir.deleteOnExit()
    tmpDir.getAbsolutePath
  }

  override def beforeAll(): Unit = {
    val conf = new CelebornConf()
    val randomMasterPort = Utils.selectRandomPort(1024, 65535)
    val randomHttpPort = randomMasterPort + 1
    conf.set(CelebornConf.HA_ENABLED.key, "false")
    conf.set(CelebornConf.HA_MASTER_RATIS_STORAGE_DIR.key, getTmpDir())
    conf.set(CelebornConf.WORKER_STORAGE_DIRS.key, getTmpDir())
    conf.set(CelebornConf.METRICS_ENABLED.key, "true")
    conf.set(CelebornConf.MASTER_HTTP_HOST.key, "127.0.0.1")
    conf.set(CelebornConf.MASTER_HTTP_PORT.key, randomHttpPort.toString)

    val args = Array("-h", "localhost", "-p", randomMasterPort.toString)

    val masterArgs = new MasterArguments(args, conf)
    master = new Master(conf, masterArgs)
    new Thread() {
      override def run(): Unit = {
        master.initialize()
      }
    }.start()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    master.stop(CelebornExitKind.EXIT_IMMEDIATELY)
    master.rpcEnv.shutdown()
  }

  test("masterGroupInfo") {
    val response = webTarget.path("masterGroupInfo").request(MediaType.TEXT_PLAIN).get()
    assert(200 == response.getStatus)
  }

  test("lostWorkers") {
    val response = webTarget.path("lostWorkers").request(MediaType.TEXT_PLAIN).get()
    assert(200 == response.getStatus)
  }

  test("excludedWorkers") {
    val response = webTarget.path("excludedWorkers").request(MediaType.TEXT_PLAIN).get()
    assert(200 == response.getStatus)
  }

  test("shutdownWorkers") {
    val response = webTarget.path("shutdownWorkers").request(MediaType.TEXT_PLAIN).get()
    assert(200 == response.getStatus)
  }

  test("hostnames") {
    val response = webTarget.path("hostnames").request(MediaType.TEXT_PLAIN).get()
    assert(200 == response.getStatus)
  }

  test("sendWorkerEvent") {
    val response = webTarget.path("sendWorkerEvent")
      .request(MediaType.TEXT_PLAIN)
      .post(null)
    assert(200 == response.getStatus)
  }

  test("workerEventInfo") {
    val response = webTarget.path("workerEventInfo").request(MediaType.TEXT_PLAIN).get()
    assert(200 == response.getStatus)
  }

  test("exclude") {
    val response = webTarget.path("exclude").request(MediaType.TEXT_PLAIN).post(null)
    assert(200 == response.getStatus)
  }
}