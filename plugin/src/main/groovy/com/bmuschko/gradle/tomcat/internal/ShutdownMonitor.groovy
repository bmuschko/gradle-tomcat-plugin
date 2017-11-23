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
package com.bmuschko.gradle.tomcat.internal

import groovy.util.logging.Slf4j

/**
 * Monitor that keeps thread running until stop command got issued.
 */
@Slf4j
class ShutdownMonitor extends Thread {
    final int port
    final String key
    final server
    final boolean daemon
    ServerSocket serverSocket

    public ShutdownMonitor(int port, String key, server, boolean daemon) {
        if(port <= 0) {
            throw new IllegalStateException('Bad stop port')
        }

        this.port = port
        this.key = key
        this.server = server
        this.daemon = daemon

        if(daemon) {
            setDaemon(true)
        }

        setName('TomcatPluginShutdownMonitor')
        serverSocket = new ServerSocket(port, 1, InetAddress.getByName('127.0.0.1'))
        serverSocket.reuseAddress = true
    }

    @Override
    void run() {
        Socket socket = serverSocket.accept()
        socket.setSoLinger(false, 0)
        socket.keepAlive = true

        while(!server.stopped) {
            try {
                LineNumberReader lin = new LineNumberReader(new InputStreamReader(socket.inputStream))

                String keyCmd = lin.readLine()

                if(key && !(key == keyCmd)) {
                    continue
                }

                String cmd = lin.readLine()

                if('stop' == cmd) {
                    log.info 'Shutting down server'

                    try {
                        log.info 'Stopping server'
                        server.stop()
                    }
                    catch(Exception e) {
                        log.error 'Exception when stopping server', e
                    }
                }
            }
            catch(Exception e) {
                log.error 'Exception in shutdown monitor', e
                System.exit(1)
            }
        }

        try {
            socket.close()
            serverSocket.close()
        }
        catch(Exception e) {
            log.error 'Exception when stopping server', e
            System.exit(1)
        }
    }
}
