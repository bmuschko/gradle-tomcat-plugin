/*
 * Copyright 2012 the original author or authors.
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

import java.util.logging.*

/**
 * Logging handler support.
 */
class LoggingHandler {
    /**
     * Adds file handler to JDK root logger for closure execution.
     *
     * @param outputFile Output file
     * @param append Append
     * @param level Log level
     * @param c Closure
     */
    static void withJdkFileLogger(File outputFile, boolean append, Level level, Closure c) {
        if(outputFile) {
            Logger logger
            Handler fileHandler

            try {
                logger = Logger.getLogger('')
                fileHandler = createFileHandler(outputFile.canonicalPath, append, level)
                logger.addHandler(fileHandler)
                c()
            }
            finally {
                try {
                    fileHandler?.close()
                    logger?.removeHandler(fileHandler)
                }
                catch(SecurityException e) {
                    // do nothing
                }
            }
        }
        else {
            c()
        }
    }

    /**
     * Creates file handler.
     *
     * @param pattern Pattern
     * @param append Append
     * @param level Log level
     * @return File handler
     */
    private static FileHandler createFileHandler(String pattern, boolean append, Level level) {
        Handler fileHandler = new FileHandler(pattern, append)
        fileHandler.formatter = new SimpleFormatter()
        fileHandler.level = level
        fileHandler.encoding = 'UTF-8'
        fileHandler
    }
}
