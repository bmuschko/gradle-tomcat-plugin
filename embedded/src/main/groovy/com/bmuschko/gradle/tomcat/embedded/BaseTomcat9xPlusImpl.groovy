package com.bmuschko.gradle.tomcat.embedded
/**
 * Base Tomcat 8x and higher implementation.
 */
abstract class BaseTomcat9xPlusImpl extends BaseTomcat8xPlusImpl {
    @Override
    void configureAjpConnector(int port, String uriEncoding, String protocolHandlerClassName) {
        def ajpConnector = createConnector(protocolHandlerClassName, uriEncoding)
        ajpConnector.port = port
        def ajpProtocol = ajpConnector.getProtocolHandler()
        ajpProtocol.secretRequired = false
        tomcat.service.addConnector ajpConnector
    }
}
