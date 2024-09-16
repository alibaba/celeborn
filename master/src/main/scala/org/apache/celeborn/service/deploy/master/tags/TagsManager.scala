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

package org.apache.celeborn.service.deploy.master.tags

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters.{asScalaIteratorConverter, mapAsScalaConcurrentMapConverter}

import org.apache.celeborn.common.internal.Logging
import org.apache.celeborn.common.meta.WorkerInfo
import org.apache.celeborn.service.deploy.master.tags.TagsManager.{Tag, TagsStore, WorkerId}

object TagsManager {
  type Tag = String
  type WorkerId = String

  import java.util.{Set => JSet}
  type TagsStore = ConcurrentHashMap[Tag, JSet[WorkerId]]
}

class TagsManager extends Logging {
  private val tagStore = new TagsStore()

  def getTaggedWorkers(tag: Tag, workers: List[WorkerInfo]): List[WorkerInfo] = {
    val workersForTag = tagStore.get(tag)
    if (workersForTag == null) {
      logWarning(s"Tag $tag not found in cluster")
      return List.empty
    }
    workers.filter(worker => workersForTag.contains(worker.host))
  }

  def addTagToWorker(tag: Tag, worker: WorkerInfo): Unit = {
    val workerId = worker.host
    val workers = tagStore.computeIfAbsent(
      tag,
      (_: Tag) => ConcurrentHashMap.newKeySet[WorkerId]())

    logInfo(s"Adding Tag $tag to worker $workerId")
    workers.add(workerId)
  }

  def removeTagFromWorker(tag: Tag, worker: WorkerInfo): Unit = {
    val workerId = worker.host
    val workers = tagStore.get(tag)

    if (workers != null && workers.contains(workerId)) {
      logInfo(s"Removing Tag $tag from worker $workerId")
      workers.remove(workerId)
    } else {
      logWarning(s"Tag $tag not found for worker $workerId")
    }
  }

  def getTagsForWorker(worker: WorkerInfo): Set[Tag] = {
    tagStore.asScala.filter(_._2.contains(worker.host)).keySet.toSet
  }

  def removeTagFromCluster(tag: Tag): Unit = {
    val workers = tagStore.remove(tag)
    if (workers != null) {
      logInfo(s"Removed Tag $tag from cluster with workers ${workers.toArray.mkString(", ")}")
    } else {
      logWarning(s"Tag $tag not found in cluster and thus can not be removed")
    }
  }

  def getTagsForCluster: Set[Tag] = {
    tagStore.keySet().iterator().asScala.toSet
  }
}
