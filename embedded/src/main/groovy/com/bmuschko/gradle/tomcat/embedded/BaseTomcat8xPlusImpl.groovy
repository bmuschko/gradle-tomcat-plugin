package com.bmuschko.gradle.tomcat.embedded

import java.lang.reflect.Constructor

/**
 * Base Tomcat 8x and higher implementation.
 */
abstract class BaseTomcat8xPlusImpl extends BaseTomcat7xPlusImpl {
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
        context.addServletContainerInitializer(jasperInitializer.newInstance(), null)
    }

    @Override
    void addWebappResource(File resource) {
        context.resources.createWebResourceSet(getResourceSetType('PRE'), '/WEB-INF/classes', resource.toURI().toURL(), '/')
    }

    def getResourceSetType(String name) {
        Class resourceSetTypeClass = loadClass('org.apache.catalina.WebResourceRoot$ResourceSetType')
        resourceSetTypeClass.enumConstants.find { it.name() == name }
    }

    protected void addWebappAdditionalFile(String webAppMountPoint, File file) {
        if (file.exists()) {
            context.resources.createWebResourceSet(getResourceSetType('PRE'),
                    webAppMountPoint, file.toURI().toURL(), '/')
        }
    }
}
