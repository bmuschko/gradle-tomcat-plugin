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
package com.bmuschko.gradle.tomcat.internal.ssl

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * SSL keystore representation.
 */
class SSLKeyStoreImpl implements SSLKeyStore {
    private final static Logger logger = Logging.getLogger(SSLKeyStoreImpl)

    /**
     * Creates SSL certificate.
     *
     * @param keyStoreFile Keystore file
     * @param keyStorePassword Keystore password
     * @param preserve Indicates whether existing keystore file should be preserved or recreated
     */
    @Override
    public void createSSLCertificate(File keyStoreFile, String keyStorePassword, boolean preserve) {
        logger.info 'Creating SSL certificate'

        prepareKeystoreDirectory(keyStoreFile)

        if(keyStoreFile.exists()) {
            if(preserve) {
                return
            }

            boolean success = keyStoreFile.delete()

            if(!success) {
                throw new IOException("Unable to delete existing keystore file '$keyStoreFile.canonicalPath'")
            }
        }

        invokeKeyTool(keyStoreFile, keyStorePassword)

        logger.info 'Created SSL certificate'
    }
    
    /**
     * Prepares keystore directory.
     *
     * @param keyStoreFile Keystore file
     */
    private void prepareKeystoreDirectory(File keyStoreFile) {
        if(!keyStoreFile.parentFile.exists() && !keyStoreFile.parentFile.mkdirs()) {
            throw new IOException("Unable to create keystore directory '$keyStoreFile.parentFile.canonicalPath'")
        }
    }
    
    /**
     * Invokes keytool to create SSL certificate.
     *
     * @param keyStoreFile Keystore file
     * @param keyStorePassword Keystore password
     */
    private void invokeKeyTool(File keyStoreFile, String keyStorePassword) {
        String[] keytoolArgs = ["-genkey", "-alias", "localhost", "-dname",
                "CN=localhost,OU=Test,O=Test,C=US", "-keyalg", "RSA",
                "-validity", "365", "-storepass", "key", "-keystore",
                keyStoreFile, "-storepass", keyStorePassword,
                "-keypass", keyStorePassword]
        Class keyToolClass = KeyToolProvider.instance.implementationClass
        keyToolClass.main(keytoolArgs)
    }
}
