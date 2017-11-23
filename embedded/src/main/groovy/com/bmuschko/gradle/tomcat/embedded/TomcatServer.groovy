/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmuschko.gradle.tomcat.embedded

import java.util.concurrent.CountDownLatch

/**
 * Tomcat server interface.
 */
interface TomcatServer {
    String getServerClassName()
    TomcatVersion getVersion()
    def getEmbedded()
    void setHome(String home)
    void setRealm(realm)
    def getContext()
    void createLoader(ClassLoader classLoader)
    void createContext(String fullContextPath, String webAppPath)
    void configureContainer()
    void configureHttpConnector(int port, String uriEncoding, String protocolHandlerClassName)
    void configureAjpConnector(int port, String uriEncoding, String protocolHandlerClassName)
    void configureHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystoreFile, String keyPassword)
    void configureHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystoreFile, String keyPassword, File truststoreFile, String trustPassword, String clientAuth)
    void configureDefaultWebXml(File webDefaultXml)
    void configureUser(TomcatUser user)
    void setConfigFile(URL configFile)
    void addWebappResource(File resource)
    void addStartUpLifecycleListener(CountDownLatch startupBarrier, boolean daemon)
    void start()
    void stop()
    boolean isStopped()
}