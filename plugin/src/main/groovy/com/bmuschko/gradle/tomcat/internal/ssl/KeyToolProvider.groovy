/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bmuschko.gradle.tomcat.internal.ssl

@Singleton
class KeyToolProvider {
    Class getImplementationClass() {
        try {
            return KeyToolImplementation.JAVA_8.implementationClass
        }
        catch(ClassNotFoundException e) {
            try {
                return KeyToolImplementation.PRE_JAVA_8.implementationClass
            }
            catch(ClassNotFoundException ex) {
                return KeyToolImplementation.IBM_JAVA.implementationClass
            }
        }
    }

    private enum KeyToolImplementation {
        JAVA_8('sun.security.tools.keytool.Main'),
        PRE_JAVA_8('sun.security.tools.KeyTool'),
        IBM_JAVA('com.ibm.crypto.tools.KeyTool')

        private final String clazz

        private KeyToolImplementation(String clazz) {
            this.clazz = clazz
        }

        Class getImplementationClass() {
            Class.forName(clazz)
        }
    }
}
