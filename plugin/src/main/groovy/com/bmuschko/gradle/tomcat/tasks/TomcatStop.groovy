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
package com.bmuschko.gradle.tomcat.tasks

import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Stops the embedded Tomcat web container.
 *
 * @author Benjamin Muschko 
 */
class TomcatStop extends TomcatRunAlways {
    /**
     * The TCP port which Tomcat should listen for admin requests. Defaults to 8081.
     */
    @Input
    Integer stopPort = 8081

    /**
     * The key to pass to Tomcat when requesting it to stop. Defaults to "stopKey".
     */
    @Input
    String stopKey = 'stopKey'

    @TaskAction
    void stop() {
        if(!getStopPort()) {
            throw new InvalidUserDataException('Please specify a valid port')
        }
        if(!getStopKey()) {
            throw new InvalidUserDataException('Please specify a valid stopKey')
        }

        try {
            Socket s = new Socket(InetAddress.getByName('127.0.0.1'), getStopPort())
            s.setSoLinger(false, 0)

            OutputStream out = s.outputStream
            out.write((getStopKey() + '\r\nstop\r\n').bytes)
            out.flush()
            // Wait for the socket to be closed after the server is stopped.
            // No response is actually written, so this always returns -1.
            s.inputStream.read()
            s.close()
        }
        catch(ConnectException e) {
            logger.info 'Tomcat not running!'
        }
        catch(Exception e) {
            logger.error 'Exception during stopping', e
        }
    }
}
