/*
 * Copyright 2013 the original author or authors.
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

import static org.spockframework.util.Assert.fail
import spock.lang.Specification

/**
 * Tomcat 7x server test.
 *
 * @author Benjamin Muschko
 */
class Tomcat7xServerIntegrationTest extends Specification {
    TomcatServer tomcatServer = new Tomcat7xServer()

    def setup() {
        tomcatServer.home = new File(System.properties['user.home'], 'tmp/tomcat7xHome').canonicalPath
    }

    def "Indicates correct version"() {
        expect:
            tomcatServer.version == TomcatVersion.VERSION_7X
    }

    def "Can start server"() {
        setup:
            Integer port = 8080
        expect:
            try {
                new Socket(InetAddress.getByName('localhost'), port)
                fail("The port $port is already in use.")
            }
            catch(ConnectException e) {}
        when:
            tomcatServer.embedded.getHost()
            tomcatServer.embedded.port = port
            tomcatServer.start()
        then:
            new Socket(InetAddress.getByName('localhost'), port)
        cleanup:
            tomcatServer.stop()
    }
    
    def "Can start server with authentication user"() {
	setup:
	    Integer port = 8080
	expect:
	    try {
		new Socket(InetAddress.getByName('localhost'), port)
		fail("The port $port is already in use.")
	    }
	    catch(ConnectException e) {}
	when:
	    tomcatServer.embedded.getHost()
	    tomcatServer.embedded.port = port
	    tomcatServer.embedded.addUser("nykolaslima", "123456");
	    tomcatServer.embedded.addRole("nykolaslima", "role");
	    tomcatServer.start()
	then:
	    new Socket(InetAddress.getByName('localhost'), port)
	cleanup:
	    tomcatServer.stop()
    }
}
