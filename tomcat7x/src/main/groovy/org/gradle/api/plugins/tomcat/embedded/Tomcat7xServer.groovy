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
package org.gradle.api.plugins.tomcat.embedded

import java.lang.reflect.Constructor

/**
 * Tomcat 7x server implementation.
 *
 * @author Benjamin Muschko
 */
class Tomcat7xServer implements TomcatServer {
    final tomcat
    def context
    private boolean stopped

    public Tomcat7xServer() {
        Class serverClass = loadClass('org.apache.catalina.startup.Tomcat')
        this.tomcat = serverClass.newInstance()
    }

    private Class loadClass(String className) {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        classLoader.loadClass(className)
    }

    @Override
    TomcatVersion getVersion() {
        TomcatVersion.VERSION_7X
    }

    @Override
    def getEmbedded() {
        tomcat
    }

    @Override
    void setHome(String home) {
        tomcat.baseDir = home
    }

    @Override
    void setRealm(realm) {
        tomcat.defaultRealm = realm
    }

    @Override
    def getContext() {
        context
    }

    @Override
    void createLoader(ClassLoader classLoader) {
        Class webappLoader = classLoader.loadClass('org.apache.catalina.loader.WebappLoader')
        context.loader = webappLoader.newInstance(classLoader)
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        def context = tomcat.addWebapp(null, fullContextPath, webAppPath)
        context.unpackWAR = false
        this.context = context
    }

    @Override
    void configureContainer() {
        // Enable JNDI naming by default
        tomcat.enableNaming()
    }

    @Override
    void configureHttpConnector(int port, String uriEncoding, String protocolHandlerClassName) {
        def httpConnector = createConnector(protocolHandlerClassName, uriEncoding)
        httpConnector.port = port

        // Remove default connector and add new one
        tomcat.service.removeConnector tomcat.connector
        tomcat.service.addConnector httpConnector
     }

    @Override
    void configureHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, String keystore, String keyPassword) {
        def httpsConnector = createConnector(protocolHandlerClassName, uriEncoding)
        httpsConnector.scheme = 'https'
        httpsConnector.secure = true
        httpsConnector.port = port
        httpsConnector.setAttribute('SSLEnabled', 'true')
        httpsConnector.setAttribute('keystoreFile', keystore)
        httpsConnector.setAttribute('keystorePass', keyPassword)
        tomcat.service.addConnector httpsConnector
    }

    private createConnector(String protocolHandlerClassName, String uriEncoding) {
        Class connectorClass = loadClass('org.apache.catalina.connector.Connector')
        Constructor constructor = connectorClass.getConstructor([String] as Class[])
        def connector = constructor.newInstance([protocolHandlerClassName] as Object[])
        connector.URIEncoding = uriEncoding
        connector
    }

    @Override
    void configureDefaultWebXml(File webDefaultXml) {
        if(webDefaultXml) {
            context.defaultWebXml = webDefaultXml.absolutePath
        }
    }

    @Override
    void setConfigFile(URL configFile) {
        if(configFile) {
            context.configFile = configFile
        }
    }

    @Override
    void start() {
        stopped = false
        tomcat.start()
    }

    @Override
    void stop() {
        stopped = true
        tomcat.stop()
        tomcat.destroy()
    }

    @Override
    boolean isStopped() {
        stopped
    }
}
