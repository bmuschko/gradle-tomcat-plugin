/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.plugins.tomcat.internal

import org.apache.catalina.Context
import org.apache.catalina.Loader
import org.apache.catalina.loader.WebappLoader
import org.apache.catalina.startup.Tomcat

/**
 * Tomcat 7x server implementation.
 *
 * @author Benjamin Muschko
 */
class Tomcat7xServer implements TomcatServer {
    Tomcat tomcat
    Context context
    Loader loader

    public Tomcat7xServer() {
        this.tomcat = new Tomcat()
    }

    @Override
    def getEmbedded() {
        tomcat
    }

    @Override
    void setHome(String home) {
        tomcat.setBaseDir(home)
    }

    @Override
    void setRealm(realm) {
        tomcat.setDefaultRealm(realm)
    }

    @Override
    Context getContext() {
        context
    }

    @Override
    Loader createLoader(ClassLoader classLoader) {
        loader = new WebappLoader(classLoader)
    }

    @Override
    Loader getLoader() {
        loader
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        Context context = tomcat.addWebapp(null, fullContextPath, webAppPath)
        this.context = context
    }

    @Override
    void configureContainer(int port, String uriEncoding) {
        tomcat.setPort(port)
        tomcat.connector.setURIEncoding(uriEncoding)

        // Enable JNDI naming by default
        tomcat.enableNaming()
    }

    @Override
    void configureDefaultWebXml(File webDefaultXml) {
        if(webDefaultXml) {
            context.setDefaultWebXml(webDefaultXml.absolutePath)
        }
    }

    @Override
    void setConfigFile(File configFile) {
        if(configFile) {
            context.setConfigFile(configFile.toURI().toURL())
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
