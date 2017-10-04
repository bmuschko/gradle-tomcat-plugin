package com.bmuschko.gradle.tomcat.embedded.fixture

abstract class EmbeddedTomcat9xPlusIntegrationTest extends EmbeddedTomcatIntegrationTest {
    @Override
    protected void configureTomcatServer() {
        tomcatServer.embedded.getHost()
        tomcatServer.embedded.port = port
        tomcatServer.configureHttpConnector(port, "UTF-8", "org.apache.coyote.http11.Http11Nio2Protocol")
    }
}
