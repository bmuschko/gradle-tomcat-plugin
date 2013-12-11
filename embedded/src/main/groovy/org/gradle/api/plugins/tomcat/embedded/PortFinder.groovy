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

class PortFinder {
    static final int MIN_PORT_NUMBER = 1024
    static final int MAX_PORT_NUMBER = 49151

    static int findFreePort() {
        for(i in MIN_PORT_NUMBER..MAX_PORT_NUMBER) {
            if(available(i)) {
                return i
            }

            throw new RuntimeException("Could not find an available port between $MIN_PORT_NUMBER and $MAX_PORT_NUMBER")
        }
    }

    private static boolean available(final int port) {
        ServerSocket serverSocket = null
        DatagramSocket dataSocket = null

        try {
            serverSocket = new ServerSocket(port)
            serverSocket.reuseAddress = true
            dataSocket = new DatagramSocket(port)
            dataSocket.reuseAddress = true
            return true
        }
        catch(IOException e) {
            return false
        }
        finally {
            dataSocket?.close()
            serverSocket?.close()
        }
    }
}