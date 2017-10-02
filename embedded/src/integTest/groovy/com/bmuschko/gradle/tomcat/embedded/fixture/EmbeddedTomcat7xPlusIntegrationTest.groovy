package com.bmuschko.gradle.tomcat.embedded.fixture

abstract class EmbeddedTomcat7xPlusIntegrationTest extends EmbeddedTomcatIntegrationTest {
    @Override
    protected void configureTomcatServer() {
        tomcatServer.embedded.getHost()
        tomcatServer.embedded.port = port
    }
}
