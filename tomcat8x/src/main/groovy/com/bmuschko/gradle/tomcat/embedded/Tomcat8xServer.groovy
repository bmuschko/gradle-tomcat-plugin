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

import java.lang.reflect.Constructor

/**
 * Tomcat 8x server implementation.
 *
 * @author Benjamin Muschko
 * @author Andrey Bloschetsov
 */
class Tomcat8xServer extends BaseTomcat7xPlusImpl {
    @Override
    TomcatVersion getVersion() {
        TomcatVersion.VERSION_8X
    }

    @Override
    void setRealm(realm) {
        tomcat.engine.realm = realm
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        super.createContext(fullContextPath, webAppPath)
        Class standardRootClass = loadClass('org.apache.catalina.webresources.StandardRoot')
        Class contextClass = loadClass('org.apache.catalina.Context')
        Constructor constructor = standardRootClass.getConstructor(contextClass)
        context.resources = constructor.newInstance(context)
        Class jasperInitializer = loadClass('org.apache.jasper.servlet.JasperInitializer')
        context.addServletContainerInitializer(jasperInitializer.newInstance(), null);
    }

    @Override
    void addWebappResource(File resource) {
        context.resources.createWebResourceSet(getResourceSetType('PRE'), '/WEB-INF/classes', resource.toURI().toURL(), '/')
    }

    def getResourceSetType(String name) {
        Class resourceSetTypeClass = loadClass('org.apache.catalina.WebResourceRoot$ResourceSetType')
        resourceSetTypeClass.enumConstants.find { it.name() == name }
    }
}
