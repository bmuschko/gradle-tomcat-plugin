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

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.plugins.tomcat.internal.StoreType

import java.util.logging.Level

/**
 * SSL keystore representation.
 *
 * @author Benjamin Muschko
 */
class SSLKeystore {
    File keystore = new File("$project.buildDir/tmp/ssl/keystore")
    String keyPassword
    
    /**
     * Initializes SSL keystore parameters.
     *
     * @return SSL keystore parameters
     */
    public static SSLKeystore createSSLKeystore() {
        Logger(this).info 'Generating temporary SSL keystore'
        final String keyPassword = 'gradleTomcat'
        new SSLKeystore(keyPassword: keyPassword)
    }

    /**
     * Creates SSL certificate.
     *
     * @param sslKeystore SSL keystore parameters
     */
    public void createSSLCertificate(SSLKeystore sslKeystore) {
        logger.info 'Creating SSL certificate'

        prepareKeystoreDirectory(sslKeystore)

        if(sslKeystore.keystore.exists()) {
            if(getPreserveSSLKey()) {
                return
            }

            sslKeystore.keystore.delete()
        }

        invokeKeyTool(sslKeystore)

        logger.info 'Created SSL certificate'
    }
    
    /**
     * Prepares keystore directory.
     *
     * @param sslKeystore SSL keystore
     */
    private void prepareKeystoreDirectory(SSLKeystore sslKeystore) {
        final File keystoreFile = sslKeystore.keystore

        if(!keystoreFile.parentFile.exists() && !keystoreFile.parentFile.mkdirs()) {
            throw new GradleException("Unable to create keystore directory: $keystoreFile.parentFile.canonicalPath")
        }
    }
    
    /**
     * Invokes keytool to create SSL certificate.
     *
     * @param sslKeystore SSL keystore
     */
    private void invokeKeyTool(SSLKeystore sslKeystore) {
        String[] keytoolArgs = ["-genkey", "-alias", "localhost", "-dname",
                "CN=localhost,OU=Test,O=Test,C=US", "-keyalg", "RSA",
                "-validity", "365", "-storepass", "key", "-keystore",
                sslKeystore.keystore, "-storepass", sslKeystore.keyPassword,
                "-keypass", sslKeystore.keyPassword]
        Class<?> keyToolClass

        try {
            // Java 8
            keyToolClass = Class.forName('sun.security.tools.keytool.Main')
        } catch(ClassNotFoundException e) {
            try {
                // Before java 8
                keyToolClass = Class.forName('sun.security.tools.KeyTool')
            } catch (ClassNotFoundException ex) {
                // Ibm java version
                keyToolClass = Class.forName('com.ibm.crypto.tools.KeyTool')
            }
        }

        keyToolClass.main(keytoolArgs)
    }
    
    /**
     * Validates that the necessary parameters have been provided for the specified key/trust store.
     *
     * @param file The file representing the store
     * @param password The password to the store
     * @param storeType identifies whether the store is a KeyStore or TrustStore
     */
    public static void validateStore(File file, String password, StoreType storeType) {
        if(!file ^ !password) {
            throw new InvalidUserDataException('If you want to provide a ${storeType.description} then password and file may not be null or blank')
        }
        else if(file && password) {
            if(!file.exists()) {
                throw new InvalidUserDataException("${storeType.description} file does not exist at location ${file.canonicalPath}")
            }
            else {
                Logger(this).info "${storeType.description} file = ${file}"
            }
        }
    }
}
