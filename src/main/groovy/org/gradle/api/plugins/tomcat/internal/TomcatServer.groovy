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
import org.apache.catalina.Realm

/**
 * Tomcat server interface.
 *
 * @author Benjamin Muschko
 */
interface TomcatServer {
    def getEmbedded()
    void setHome(String home)
    void setRealm(Realm realm)
    Context getContext()
    Loader createLoader(ClassLoader classLoader)
    Loader getLoader()
    void createContext(String fullContextPath, String webAppPath)
    void configureContainer(int port, String uriEncoding)
    void configureDefaultWebXml(File webDefaultXml)
    void setConfigFile(File configFile)
    void start()
    void stop()
}