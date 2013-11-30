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

/**
 * Tomcat 6x server implementation.
 *
 * @author Benjamin Muschko
 */
class Tomcat6xServer extends BaseTomcatServerImpl {
    @Override
    String getServerClassName() {
        'org.apache.catalina.startup.Embedded'
    }

    @Override
    TomcatVersion getVersion() {
        TomcatVersion.VERSION_6X
    }

    @Override
    void setHome(String home) {
        tomcat.catalinaHome = home
    }

    @Override
    void setRealm(realm) {
        tomcat.realm = realm
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        def context = tomcat.createContext(fullContextPath, webAppPath)
        context.unpackWAR = false
        this.context = context
    }

    @Override
    void createLoader(ClassLoader classLoader) {
        context.loader = tomcat.createLoader(classLoader)
    }

    @Override
    void configureContainer() {
        def localHost = tomcat.createHost('localHost', new File('.').absolutePath)
        localHost.addChild(context)

        // Create engine
        addEngineToServer(localHost)
    }

    @Override
    void configureHttpConnector(int port, String uriEncoding, String protocolHandlerClassName) {
        def httpConnector = createConnector(port, uriEncoding, protocolHandlerClassName)
        tomcat.addConnector httpConnector
    }

    @Override
    void configureAjpConnector(int port, String uriEncoding, String protocolHandlerClassName) {
        def ajpConnector = createConnector(port, uriEncoding, protocolHandlerClassName)
        tomcat.addConnector ajpConnector
    }

    @Override
    void configureHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystoreFile, String keyPassword) {
        def httpsConnector = createHttpsConnector(port, uriEncoding, protocolHandlerClassName, keystoreFile, keyPassword)
        tomcat.addConnector httpsConnector
    }

    @Override
    void configureHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystoreFile, String keyPassword, File truststoreFile, String trustPassword, String clientAuth) {
        def httpsConnector = createHttpsConnector(port, uriEncoding, protocolHandlerClassName, keystoreFile, keyPassword)
        httpsConnector.setAttribute('truststoreFile', truststoreFile.canonicalPath)
        httpsConnector.setAttribute('truststorePass', trustPassword)
        httpsConnector.setAttribute('clientAuth', clientAuth)
        tomcat.addConnector httpsConnector
    }

    private createHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystore, String keyPassword) {
        def httpsConnector = createConnector(port, uriEncoding, protocolHandlerClassName)
        httpsConnector.scheme = 'https'
        httpsConnector.secure = true
        httpsConnector.setProperty('SSLEnabled', 'true')
        httpsConnector.setAttribute('keystore', keystore.canonicalPath)
        httpsConnector.setAttribute('keystorePass', keyPassword)
        httpsConnector
    }

    private createConnector(int port, String uriEncoding, String protocolHandlerClassName) {
        def connector = tomcat.createConnector((InetAddress) null, port, protocolHandlerClassName)
        connector.URIEncoding =  uriEncoding ? uriEncoding : 'UTF-8'
        connector
    }

    /**
     * Adds engine to server
     *
     * @param localHost host
     */
    void addEngineToServer(localHost) {
        def engine = tomcat.createEngine()

        engine.with {
            setName 'localEngine'
            addChild localHost
            setDefaultHost localHost.name
        }

        tomcat.addEngine(engine)
    }

    /**
     * Configures default web XML if provided. Otherwise, set it up programmatically.
     */
    @Override
    void configureDefaultWebXml(File webDefaultXml) {
        if(webDefaultXml) {
            context.defaultWebXml = webDefaultXml.absolutePath
        }
        else {
            configureDefaultServlet()
            configureJspServlet()
        }
    }

    /**
     * Configures default servlet and adds it to the context.
     */
    void configureDefaultServlet() {
        def defaultServlet = context.createWrapper()

        defaultServlet.with {
            setName 'default'
            setServletClass 'org.apache.catalina.servlets.DefaultServlet'
            addInitParameter 'debug', '0'
            addInitParameter 'listings', 'false'
            setLoadOnStartup 1
        }

        context.with {
            addChild defaultServlet
            addServletMapping '/', 'default'
        }
    }

    /**
     * Configures JSP servlet and adds it to the context.
     */
    void configureJspServlet() {
        def jspServlet = context.createWrapper()

        jspServlet.with {
            setName 'jsp'
            setServletClass 'org.apache.jasper.servlet.JspServlet'
            addInitParameter 'fork', 'false'
            addInitParameter 'xpoweredBy', 'false'
            setLoadOnStartup 3
        }

        context.with {
            addChild jspServlet
            addServletMapping '*.jsp', 'jsp'
            addServletMapping '*.jspx', 'jsp'
        }
    }

    @Override
    void setConfigFile(URL configFile) {
        if(configFile) {
            context.configFile = configFile.toURI().path
        }
    }
}
