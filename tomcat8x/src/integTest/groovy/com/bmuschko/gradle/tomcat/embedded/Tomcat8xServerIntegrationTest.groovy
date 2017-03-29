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
package com.bmuschko.gradle.tomcat.embedded

import com.bmuschko.gradle.tomcat.embedded.fixture.EmbeddedTomcatIntegrationTest

/**
 * Tomcat 8x server test.
 *
 * @author Benjamin Muschko
 */
class Tomcat8xServerIntegrationTest extends EmbeddedTomcatIntegrationTest {
    @Override
    protected TomcatServer createTomcatServer() {
        new Tomcat8xServer()
    }

    @Override
    protected File getTomcatHomeDir() {
        temporaryFolder.newFolder('tomcat8xHome')
    }

    @Override
    protected void configureTomcatServer() {
        tomcatServer.embedded.getHost()
        tomcatServer.embedded.port = port
    }

    @Override
    protected TomcatVersion getTomcatVersion() {
        TomcatVersion.VERSION_8_0_X
    }
}
