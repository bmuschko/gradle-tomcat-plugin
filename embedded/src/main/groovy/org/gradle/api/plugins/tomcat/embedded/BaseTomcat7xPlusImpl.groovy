package org.gradle.api.plugins.tomcat.embedded

import java.lang.reflect.Constructor

/**
 * Base Tomcat 7x and higher implementation.
 *
 * @author Benjamin Muschko
 * @author Andrey Bloschetsov
 */
abstract class BaseTomcat7xPlusImpl extends BaseTomcatServerImpl {
    @Override
    String getServerClassName() {
        'org.apache.catalina.startup.Tomcat'
    }

    @Override
    void setHome(String home) {
        tomcat.baseDir = home
    }

    @Override
    void createContext(String fullContextPath, String webAppPath) {
        def context = tomcat.addWebapp(null, fullContextPath, webAppPath)
        context.unpackWAR = false
        this.context = context
    }

    @Override
    void createLoader(ClassLoader classLoader) {
        Class webappLoader = classLoader.loadClass('org.apache.catalina.loader.WebappLoader')
        this.context.loader = webappLoader.newInstance(classLoader)
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
    void configureAjpConnector(int port, String uriEncoding, String protocolHandlerClassName) {
        def ajpConnector = createConnector(protocolHandlerClassName, uriEncoding)
        ajpConnector.port = port
        tomcat.service.addConnector ajpConnector
    }

    @Override
    void configureHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystoreFile, String keyPassword) {
        def httpsConnector = createHttpsConnector(port, uriEncoding, protocolHandlerClassName, keystoreFile, keyPassword)
        tomcat.service.addConnector httpsConnector
    }

    @Override
    void configureHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystoreFile, String keyPassword, File truststoreFile, String trustPassword, String clientAuth) {
        def httpsConnector = createHttpsConnector(port, uriEncoding, protocolHandlerClassName, keystoreFile, keyPassword)
        httpsConnector.setAttribute('truststoreFile', truststoreFile.canonicalPath)
        httpsConnector.setAttribute('truststorePass', trustPassword)
        httpsConnector.setAttribute('clientAuth', clientAuth)
        tomcat.service.addConnector httpsConnector
    }

    private createHttpsConnector(int port, String uriEncoding, String protocolHandlerClassName, File keystore, String keyPassword) {
        def httpsConnector = createConnector(protocolHandlerClassName, uriEncoding)
        httpsConnector.scheme = 'https'
        httpsConnector.secure = true
        httpsConnector.port = port
        httpsConnector.setAttribute('SSLEnabled', 'true')
        httpsConnector.setAttribute('keystoreFile', keystore.canonicalPath)
        httpsConnector.setAttribute('keystorePass', keyPassword)
        httpsConnector
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
}
