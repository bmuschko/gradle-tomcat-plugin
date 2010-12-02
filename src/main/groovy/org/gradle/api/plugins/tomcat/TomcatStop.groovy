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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Stops the embedded Tomcat web container.
 *
 * @author Benjamin Muschko 
 */
class TomcatStop extends ConventionTask {
    static Logger logger = LoggerFactory.getLogger(TomcatStop.class)
    private Integer stopPort
    private String stopKey

    @TaskAction
    public void stop() {
        if(!getStopPort()) {
            throw new InvalidUserDataException("Please specify a valid port")
        }
        if(!getStopKey()) {
            throw new InvalidUserDataException("Please specify a valid stopKey")
        }

        try {
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), getStopPort())
            s.setSoLinger(false, 0)

            OutputStream out = s.getOutputStream()
            out.write((getStopKey() + "\r\nstop\r\n").getBytes())
            out.flush()
            s.close()
        }
        catch(ConnectException e) {
            logger.info "Tomcat not running!"
        }
        catch(Exception e) {
            logger.error "Exception during stopping", e
        }
    }

    /**
     * Returns port to listen to stop Tomcat on sending stop command.
     */
    public Integer getStopPort() {
        return stopPort;
    }

    /**
     * Sets port to listen to stop Tomcat on sending stop command.
     */
    public void setStopPort(Integer stopPort) {
        this.stopPort = stopPort;
    }

    /**
     * Returns stop key.
     *
     * @see #setStopKey(String)
     */
    public String getStopKey() {
        return stopKey;
    }

    /**
     * Sets key to provide when stopping Tomcat.
     */
    public void setStopKey(String stopKey) {
        this.stopKey = stopKey;
    }
}
