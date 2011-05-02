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

import org.apache.catalina.Context
import org.apache.catalina.Engine
import org.apache.catalina.Host
import org.apache.catalina.Wrapper
import org.apache.catalina.connector.Connector
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.startup.Embedded

/**
 * Tomcat 6x server implementation.
 *
 * @author Benjamin Muschko
 */
class Tomcat6xServer implements TomcatServer {
    Embedded server
    Context context

    public Tomcat6xServer() {
        this.server = new Embedded()
    }

    @Override
    def getEmbedded() {
        server
    }

    @Override
    void setHome(String home) {
        server.setCatalinaHome(home)
    }

    @Override
    void setRealm(realm) {
        server.setRealm(realm)
    }

    @Override
    Context getContext() {
        context
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        Context context = server.createContext(fullContextPath, webAppPath)
        this.context = context
    }

    @Override
    void createLoader(ClassLoader classLoader) {
        context.setLoader(new WebappLoader(classLoader))
    }

    @Override
    void configureContainer(int port, String uriEncoding) {
        Host localHost = server.createHost('localHost', new File('.').absolutePath)
        localHost.addChild(context)

        // Create engine
        addEngineToServer(localHost)

        // Create HTTP connector
        addConnectorToServer(port, uriEncoding)
    }

    /**
     * Adds engine to server
     *
     * @param localHost host
     */
    void addEngineToServer(Host localHost) {
        Engine engine = server.createEngine()

        engine.with {
            setName 'localEngine'
            addChild localHost
            setDefaultHost localHost.name
        }

        server.addEngine(engine)
    }

    /**
     * Adds connector to server
     */
    void addConnectorToServer(int port, String uriEncoding) {
        Connector httpConnector = server.createConnector((InetAddress) null, port, false)
        httpConnector.setURIEncoding uriEncoding ? uriEncoding : 'UTF-8'
        server.addConnector(httpConnector)
    }

    /**
     * Configures default web XML if provided. Otherwise, set it up programmatically.
     */
    @Override
    void configureDefaultWebXml(File webDefaultXml) {
        if(webDefaultXml) {
            context.setDefaultWebXml(webDefaultXml.absolutePath)
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
        Wrapper defaultServlet = context.createWrapper()

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
        Wrapper jspServlet = context.createWrapper()

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
    void setConfigFile(File configFile) {
        if(configFile) {
            context.setConfigFile(configFile.canonicalPath)
        }
    }

    @Override
    void start() {
        server.start()
    }

    @Override
    void stop() {
        server.stop()
    }
}
