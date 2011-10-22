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
package org.gradle.api.plugins.tomcat

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Stops the embedded Tomcat web container.
 *
 * @author Benjamin Muschko 
 */
class TomcatStop extends DefaultTask {
    static final Logger LOGGER = LoggerFactory.getLogger(TomcatStop.class)
    Integer stopPort
    String stopKey

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
            s.close()
        }
        catch(ConnectException e) {
            LOGGER.info 'Tomcat not running!'
        }
        catch(Exception e) {
            LOGGER.error 'Exception during stopping', e
        }
    }

    /**
     * Returns port to listen to stop Tomcat on sending stop command.
     *
     * @return Stop port
     */
    Integer getStopPort() {
        Integer stopPortSystemProperty = TomcatSystemProperty.getStopPort()
        stopPortSystemProperty ?: stopPort
    }

    /**
     * Returns stop key.
     *
     * @return Stop key
     */
    String getStopKey() {
        String stopKeySystemProperty = TomcatSystemProperty.getStopKey()
        stopKeySystemProperty ?: stopKey
    }
}
