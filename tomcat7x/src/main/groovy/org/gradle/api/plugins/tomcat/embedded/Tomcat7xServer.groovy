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
 * Tomcat 7x server implementation.
 *
 * @author Benjamin Muschko
 */
class Tomcat7xServer implements TomcatServer {
    final tomcat
    def context

    public Tomcat7xServer() {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        Class serverClass = classLoader.loadClass('org.apache.catalina.startup.Tomcat')
        this.tomcat = serverClass.newInstance()
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
    void configureContainer(int port, String uriEncoding) {
        tomcat.port = port
        tomcat.connector.URIEncoding = uriEncoding

        // Enable JNDI naming by default
        tomcat.enableNaming()
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
        tomcat.start()
    }

    @Override
    void stop() {
        tomcat.stop()
    }
}
