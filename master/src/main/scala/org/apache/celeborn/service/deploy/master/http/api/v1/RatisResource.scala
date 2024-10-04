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

package org.apache.celeborn.service.deploy.master.http.api.v1

import javax.ws.rs.{Consumes, Path, POST, Produces}
import javax.ws.rs.core.MediaType

import scala.collection.JavaConverters._

import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.ratis.proto.RaftProtos.RaftPeerRole
import org.apache.ratis.protocol.{LeaderElectionManagementRequest, RaftPeer, RaftPeerId, SetConfigurationRequest, SnapshotManagementRequest, TransferLeadershipRequest}
import org.apache.ratis.rpc.CallId

import org.apache.celeborn.common.internal.Logging
import org.apache.celeborn.rest.v1.model.{HandleResponse, RatisElectionTransferRequest, RatisPeerAddRequest, RatisPeerRemoveRequest, RatisPeerSetPriorityRequest}
import org.apache.celeborn.server.common.http.api.ApiRequestContext
import org.apache.celeborn.service.deploy.master.Master
import org.apache.celeborn.service.deploy.master.clustermeta.ha.{HAMasterMetaManager, HARaftServer}
import org.apache.celeborn.service.deploy.master.http.api.MasterHttpResourceUtils._

@Tag(name = "Ratis")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class RatisResource extends ApiRequestContext with Logging {
  private def master = httpService.asInstanceOf[Master]
  private def ratisServer = master.statusSystem.asInstanceOf[HAMasterMetaManager].getRatisServer

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Transfer the group leader to the specified server.")
  @POST
  @Path("/election/transfer")
  def electionTransfer(request: RatisElectionTransferRequest): HandleResponse =
    ensureMasterIsLeader(master) {
      transferLeadership(request.getPeerAddress)
    }

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Make the group leader step down its leadership.")
  @POST
  @Path("/election/step_down")
  def electionStepDown(): HandleResponse = ensureMasterIsLeader(master) {
    transferLeadership(null)
  }

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Pause leader election at the current server." +
      " Then, the current server would not start a leader election.")
  @POST
  @Path("/election/pause")
  def electionPause(): HandleResponse = ensureMasterHAEnabled(master) {
    applyElectionOp(new LeaderElectionManagementRequest.Pause)
  }

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Resume leader election at the current server.")
  @POST
  @Path("/election/resume")
  def electionResume(): HandleResponse = ensureMasterHAEnabled(master) {
    applyElectionOp(new LeaderElectionManagementRequest.Resume)
  }

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Trigger the current server to take snapshot.")
  @POST
  @Path("/snapshot/create")
  def createSnapshot(): HandleResponse = ensureMasterHAEnabled(master) {
    val request = SnapshotManagementRequest.newCreate(
      ratisServer.getClientId,
      ratisServer.getServer.getId,
      ratisServer.getGroupId,
      CallId.getAndIncrement(),
      HARaftServer.REQUEST_TIMEOUT_MS)
    val reply = ratisServer.getServer.snapshotManagement(request)
    if (reply.isSuccess) {
      new HandleResponse().success(true).message(
        s"Successfully create snapshot at $localServerAddress.")
    } else {
      new HandleResponse().success(false).message(
        s"Failed to create snapshot at $localServerAddress. $reply")
    }
  }

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Add new peers to the raft group.")
  @POST
  @Path("/peer/add")
  def peerAdd(request: RatisPeerAddRequest): HandleResponse = {
    val remaining = getPeersWithRole(RaftPeerRole.FOLLOWER)
    val adding = request.getPeers.asScala.map { peer =>
      RaftPeer.newBuilder()
        .setId(peer.getId)
        .setAddress(peer.getAddress)
        .setPriority(0)
        .build()
    }

    val peers = (remaining ++ adding).distinct
    val listeners = getPeersWithRole(RaftPeerRole.LISTENER)

    logInfo(
      s"Adding peers ${adding.map(_.getId).mkString(",")} to group ${ratisServer.getGroupInfo}.")
    logInfo(s"New peers: ${peers.map(_.getId).mkString(",")}")
    logInfo(s"New listeners: ${listeners.map(_.getId).mkString(",")}")

    val reply = ratisServer.getServer.setConfiguration(
      new SetConfigurationRequest(
        ratisServer.getClientId,
        ratisServer.getServer.getId,
        ratisServer.getGroupId,
        CallId.getAndIncrement(),
        peers.asJava,
        listeners.asJava))

    if (reply.isSuccess) {
      new HandleResponse().success(true).message(
        s"Successfully added peers ${peers.map(_.getId).mkString(",")} to group ${ratisServer.getGroupInfo}.")
    } else {
      new HandleResponse().success(false).message(
        s"Failed to add peers ${peers.map(_.getId).mkString(
          ",")} to group ${ratisServer.getGroupInfo}. $reply")
    }
  }

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Remove peers from the raft group.")
  @POST
  @Path("/peer/remove")
  def peerRemove(request: RatisPeerRemoveRequest): HandleResponse = {
    val removing = request.getPeerAddresses.asScala.map(getRaftPeerId)

    val peers = getPeersWithRole(RaftPeerRole.FOLLOWER)
      .filterNot(peer => removing.contains(peer.getId))
    val listeners = getPeersWithRole(RaftPeerRole.LISTENER)
      .filterNot(peer => removing.contains(peer.getId))

    logInfo(s"Removing peers ${removing.mkString(",")} to group ${ratisServer.getGroupInfo}.")
    logInfo(s"New peers: ${peers.map(_.getId).mkString(",")}")
    logInfo(s"New listeners: ${listeners.map(_.getId).mkString(",")}")

    val reply = ratisServer.getServer.setConfiguration(
      new SetConfigurationRequest(
        ratisServer.getClientId,
        ratisServer.getServer.getId,
        ratisServer.getGroupId,
        CallId.getAndIncrement(),
        peers.asJava,
        listeners.asJava))

    if (reply.isSuccess) {
      new HandleResponse().success(true).message(
        s"Successfully added peers ${peers.map(_.getId).mkString(",")} to group ${ratisServer.getGroupInfo}.")
    } else {
      new HandleResponse().success(false).message(
        s"Failed to add peers ${peers.map(_.getId).mkString(
          ",")} to group ${ratisServer.getGroupInfo}. $reply")
    }
  }

  @ApiResponse(
    responseCode = "200",
    content = Array(new Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = new Schema(implementation = classOf[HandleResponse]))),
    description = "Set the priority of the peers in the raft group.")
  @POST
  @Path("/peer/set_priority")
  def peerSetPriority(request: RatisPeerSetPriorityRequest): HandleResponse = {
    val peers = getPeersWithRole(RaftPeerRole.FOLLOWER).map { peer =>
      val newPriority = request.getAddressPriorities.get(peer.getAddress)
      val priority: Int = if (newPriority != null) newPriority else peer.getPriority
      RaftPeer.newBuilder(peer).setPriority(priority).build()
    }
    val listeners = getPeersWithRole(RaftPeerRole.LISTENER)

    val reply = ratisServer.getServer.setConfiguration(
      new SetConfigurationRequest(
        ratisServer.getClientId,
        ratisServer.getServer.getId,
        ratisServer.getGroupId,
        CallId.getAndIncrement(),
        peers.asJava,
        listeners.asJava))

    if (reply.isSuccess) {
      new HandleResponse().success(true).message(
        s"Successfully set priority of peers ${peers.map(_.getId).mkString(",")} to group ${ratisServer.getGroupInfo}.")
    } else {
      new HandleResponse().success(false).message(
        s"Failed to set priority of peers ${peers.map(_.getId).mkString(
          ",")} to group ${ratisServer.getGroupInfo}. $reply")
    }
  }

  private def transferLeadership(peerAddress: String): HandleResponse = {
    val newLeaderId = Option(peerAddress).map(getRaftPeerId).orNull
    val op =
      if (newLeaderId == null) s"step down leader $localServerAddress"
      else s"transfer leadership from $localServerAddress to $peerAddress"
    val request = new TransferLeadershipRequest(
      ratisServer.getClientId,
      ratisServer.getServer.getId,
      ratisServer.getGroupId,
      CallId.getAndIncrement(),
      newLeaderId,
      HARaftServer.REQUEST_TIMEOUT_MS)
    val reply = ratisServer.getServer.transferLeadership(request)
    if (reply.isSuccess) {
      new HandleResponse().success(true).message(s"Successfully $op.")
    } else {
      new HandleResponse().success(false).message(s"Failed to $op: $reply")
    }
  }

  private def applyElectionOp(op: LeaderElectionManagementRequest.Op): HandleResponse = {
    val request = new LeaderElectionManagementRequest(
      ratisServer.getClientId,
      ratisServer.getServer.getId,
      ratisServer.getGroupId,
      CallId.getAndIncrement(),
      op)
    val reply = ratisServer.getServer.leaderElectionManagement(request)
    if (reply.isSuccess) {
      new HandleResponse().success(true).message(
        s"Successfully applied election $op $localServerAddress.")
    } else {
      new HandleResponse().success(false).message(
        s"Failed to apply election $op $localServerAddress. $reply")
    }
  }

  private lazy val localServerAddress = ratisServer.getLocalNode.ratisEndpoint

  private def getRaftPeerId(peerAddress: String): RaftPeerId = {
    val groupInfo =
      master.statusSystem.asInstanceOf[HAMasterMetaManager].getRatisServer.getGroupInfo
    groupInfo.getCommitInfos.asScala.filter(peer => peer.getServer.getAddress == peerAddress)
      .map(peer => RaftPeerId.valueOf(peer.getServer.getId)).headOption.getOrElse(
        throw new IllegalArgumentException(s"Peer $peerAddress not found in group: $groupInfo"))
  }

  private def getPeersWithRole(role: RaftPeerRole): Seq[RaftPeer] = {
    val groupInfo = ratisServer.getGroupInfo
    val conf = groupInfo.getConf.orElse(null)
    if (conf == null) return Seq.empty
    val targets = if (role == RaftPeerRole.LISTENER) conf.getListenersList else conf.getPeersList
    groupInfo.getGroup.getPeers.asScala.filter(peer => targets.contains(peer.getId)).toSeq
  }
}
