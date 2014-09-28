package org.gradle.api.plugins.tomcat.embedded.fixture

import org.gradle.api.plugins.tomcat.embedded.TomcatServer
import org.gradle.api.plugins.tomcat.embedded.TomcatUser
import org.gradle.api.plugins.tomcat.embedded.TomcatVersion
import org.gradle.util.AvailablePortFinder
import spock.lang.Specification

abstract class EmbeddedTomcatIntegrationTest extends Specification {
    TomcatServer tomcatServer
    Integer port

    def setup() {
        tomcatServer = createTomcatServer()
        tomcatServer.home = getTomcatHomeDir().canonicalPath
        port = reservePort()
    }

    private Integer reservePort() {
        AvailablePortFinder availablePortFinder = AvailablePortFinder.createPrivate()
        availablePortFinder.nextAvailable
    }

    void verifyCreatedSocket() {
        new Socket(InetAddress.getByName('localhost'), port)
    }

    protected abstract TomcatServer createTomcatServer()
    protected abstract File getTomcatHomeDir()
    protected abstract void configureTomcatServer()
    protected abstract TomcatVersion getTomcatVersion()

    def "Indicates correct version"() {
        expect:
           tomcatServer.version == getTomcatVersion()
    }

    def "Can start server"() {
        when:
            configureTomcatServer()
            tomcatServer.start()
        then:
            verifyCreatedSocket()
        cleanup:
            tomcatServer.stop()
    }

    def "Can start server with authentication user"() {
        when:
            configureTomcatServer()
            addUsers()
            tomcatServer.start()
        then:
            verifyCreatedSocket()
        cleanup:
            tomcatServer.stop()
    }

    private void addUsers() {
        tomcatServer.configureUser(new TomcatUser(username: 'nykolaslima', password: '123456', roles: ['developer', 'admin']))
        tomcatServer.configureUser(new TomcatUser(username: 'bmuschko', password: 'abcdef', roles: ['manager']))
        tomcatServer.configureUser(new TomcatUser(username: 'unprivileged', password: 'pwd'))
        tomcatServer.configureUser(new TomcatUser(username: 'unprivileged', password: 'pwd', roles: null))
    }
}
