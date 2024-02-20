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
package org.apache.celeborn.common.rpc

import org.apache.celeborn.common.identity.UserIdentifier
import org.apache.celeborn.common.network.sasl.{ApplicationRegistry, SaslCredentials}
import org.apache.celeborn.common.network.sasl.registration.RegistrationInfo

/**
 * Represents the rpc context, combining both client and server contexts.
 *
 * @param clientRpcContext Optional client rpc context.
 * @param serverRpcContext Optional server rpc context.
 */
private[celeborn] case class RpcContext(
    clientRpcContext: Option[ClientRpcContext] = None,
    serverRpcContext: Option[ServerRpcContext] = None)

/**
 * Represents the client RPC context.
 * @param appId     The application id.
 * @param saslCredentials sasl credentials.
 * @param addRegistrationBootstrap Whether to add registration bootstrap.
 */
private[celeborn] case class ClientRpcContext(
    appId: String,
    saslCredentials: SaslCredentials,
    addRegistrationBootstrap: Boolean = false,
    registrationInfo: RegistrationInfo = null,
    userIdentifier: UserIdentifier = null,
    authEnabled: Boolean = false)

/**
 * Represents the server RPC context.
 * @param applicationRegistry  The application registry.
 * @param addRegistrationBootstrap  Whether to add registration bootstrap.
 */
private[celeborn] case class ServerRpcContext(
    applicationRegistry: ApplicationRegistry,
    addRegistrationBootstrap: Boolean = false,
    authEnabled: Boolean = false)

/**
 * Builder for [[ClientRpcContext]].
 */
private[celeborn] class ClientRpcContextBuilder {
  private var saslUser: String = _
  private var saslPassword: String = _
  private var appId: String = _
  private var userIdentifier: UserIdentifier = _
  private var addRegistrationBootstrap: Boolean = false
  private var authEnabled: Boolean = false
  private var registrationInfo: RegistrationInfo = _

  def withSaslUser(user: String): ClientRpcContextBuilder = {
    this.saslUser = user
    this
  }

  def withSaslPassword(password: String): ClientRpcContextBuilder = {
    this.saslPassword = password
    this
  }

  def withAppId(appId: String): ClientRpcContextBuilder = {
    this.appId = appId
    this
  }

  def withUserIdentifier(userIdentifier: UserIdentifier): ClientRpcContextBuilder = {
    this.userIdentifier = userIdentifier
    this
  }

  def withAddRegistrationBootstrap(addRegistrationBootstrap: Boolean): ClientRpcContextBuilder = {
    this.addRegistrationBootstrap = addRegistrationBootstrap
    this
  }

  def withAuthEnabled(authEnabled: Boolean): ClientRpcContextBuilder = {
    this.authEnabled = authEnabled
    this
  }

  def withRegistrationInfo(registrationInfo: RegistrationInfo): ClientRpcContextBuilder = {
    this.registrationInfo = registrationInfo
    this
  }

  def build(): ClientRpcContext = {
    if (saslUser == null || saslPassword == null) {
      throw new IllegalArgumentException("Sasl user/password is not set.")
    }
    if (appId == null) {
      throw new IllegalArgumentException("App id is not set.")
    }
    if (userIdentifier == null) {
      throw new IllegalArgumentException("User identifier is not set.")
    }
    if (addRegistrationBootstrap && registrationInfo == null) {
      throw new IllegalArgumentException("Registration info is not set.")
    }
    ClientRpcContext(
      appId,
      new SaslCredentials(saslUser, saslPassword),
      addRegistrationBootstrap,
      registrationInfo,
      userIdentifier,
      authEnabled)
  }
}

/**
 * Builder for [[ServerRpcContext]].
 */
private[celeborn] class ServerRpcContextBuilder {
  private var appRegistry: ApplicationRegistry = _
  private var addRegistrationBootstrap: Boolean = false
  private var authEnabled: Boolean = false

  def withApplicationRegistry(appRegistry: ApplicationRegistry): ServerRpcContextBuilder = {
    this.appRegistry = appRegistry
    this
  }

  def withAddRegistrationBootstrap(addRegistrationBootstrap: Boolean): ServerRpcContextBuilder = {
    this.addRegistrationBootstrap = addRegistrationBootstrap
    this
  }

  def withAuthEnabled(authEnabled: Boolean): ServerRpcContextBuilder = {
    this.authEnabled = authEnabled
    this
  }

  def build(): ServerRpcContext = {
    if (appRegistry == null) {
      throw new IllegalArgumentException("Secret registry is not set.")
    }
    ServerRpcContext(
      appRegistry,
      addRegistrationBootstrap,
      authEnabled)
  }
}

/**
 * Builder for [[RpcContext]].
 */
private[celeborn] class RpcContextBuilder {
  private var clientRpcContext: Option[ClientRpcContext] = None
  private var serverRpcContext: Option[ServerRpcContext] = None

  def withClientSaslContext(context: ClientRpcContext): RpcContextBuilder = {
    this.clientRpcContext = Some(context)
    this
  }

  def withServerRpcContext(context: ServerRpcContext): RpcContextBuilder = {
    this.serverRpcContext = Some(context)
    this
  }

  def build(): RpcContext = {
    if (clientRpcContext.nonEmpty && serverRpcContext.nonEmpty) {
      throw new IllegalArgumentException("Both client and server sasl context cannot be set.")
    }
    RpcContext(clientRpcContext, serverRpcContext)
  }
}