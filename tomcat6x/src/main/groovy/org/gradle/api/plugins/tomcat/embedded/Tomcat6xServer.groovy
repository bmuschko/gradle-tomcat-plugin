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
class Tomcat6xServer implements TomcatServer {
    final embedded
    def context

    public Tomcat6xServer() {
        Class serverClass = loadClass('org.apache.catalina.startup.Embedded')
        this.embedded = serverClass.newInstance()
    }

    private Class loadClass(String className) {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        classLoader.loadClass(className)
    }

    @Override
    def getEmbedded() {
        embedded
    }

    @Override
    void setHome(String home) {
        embedded.catalinaHome = home
    }

    @Override
    void setRealm(realm) {
        embedded.realm = realm
    }

    @Override
    def getContext() {
        context
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        context = embedded.createContext(fullContextPath, webAppPath)
    }

    @Override
    void createLoader(ClassLoader classLoader) {
        context.loader = embedded.createLoader(classLoader)
    }

    @Override
    void configureContainer() {
        def localHost = embedded.createHost('localHost', new File('.').absolutePath)
        localHost.addChild(context)

        // Create engine
        addEngineToServer(localHost)
    }

    @Override
    void configureHttpConnector(int port, String uriEncoding) {
        def httpConnector = embedded.createConnector((InetAddress) null, port, false)
        httpConnector.URIEncoding =  uriEncoding ? uriEncoding : 'UTF-8'
        embedded.addConnector(httpConnector)
    }

    @Override
    void configureHttpsConnector(int port, String uriEncoding, String keystore, String keyPassword) {
        def httpsConnector = loadClass('org.apache.catalina.connector.Connector').newInstance()
        httpsConnector.scheme = 'https'
        httpsConnector.secure = true
        httpsConnector.port = port
        httpsConnector.setProperty('SSLEnabled', 'true')
        httpsConnector.setAttribute('keystore', keystore)
        httpsConnector.setAttribute('keystorePass', keyPassword)
        httpsConnector.URIEncoding = uriEncoding
        embedded.addConnector httpsConnector
    }

    /**
     * Adds engine to server
     *
     * @param localHost host
     */
    void addEngineToServer(localHost) {
        def engine = embedded.createEngine()

        engine.with {
            setName 'localEngine'
            addChild localHost
            setDefaultHost localHost.name
        }

        embedded.addEngine(engine)
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
            context.configFile = configFile.path
        }
    }

    @Override
    void start() {
        embedded.start()
    }

    @Override
    void stop() {
        embedded.stop()
    }
}
