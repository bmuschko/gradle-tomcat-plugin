package com.bmuschko.gradle.tomcat.internal

import com.bmuschko.gradle.tomcat.internal.ssl.SSLKeyStore
import com.bmuschko.gradle.tomcat.internal.ssl.SSLKeyStoreImpl
import spock.lang.Specification

import static org.spockframework.util.Assert.fail

class SSLKeyStoreImplTest extends Specification {
    SSLKeyStore sslKeyStore = new SSLKeyStoreImpl()
    File keyStoreFile = new File("build/tmp/tests/keystore")

    def setup() {
        deleteExistingKeyStoreFile()
    }

    def cleanup() {
        deleteExistingKeyStoreFile()
    }

    private void deleteExistingKeyStoreFile() {
        if(keyStoreFile.exists()) {
            boolean success = keyStoreFile.delete()

            if(!success) {
                fail("Unable to delete directory '$keyStoreFile.canonicalPath'.")
            }
        }
    }

    def "Can create SSL certificate from scratch"() {
        when:
            sslKeyStore.createSSLCertificate(keyStoreFile, 'somePwd', false)
        then:
            keyStoreFile.exists()
    }

    def "Doesn't recreate existing SSL certificate if preserved"() {
        when:
            sslKeyStore.createSSLCertificate(keyStoreFile, 'somePwd', false)
        then:
            keyStoreFile.exists()
        when:
            sslKeyStore.createSSLCertificate(keyStoreFile, 'somePwd', true)
        then:
            keyStoreFile.exists()
    }

    def "Deletes and recreates existing SSL certificate if not preserved"() {
        when:
            sslKeyStore.createSSLCertificate(keyStoreFile, 'somePwd', false)
        then:
            keyStoreFile.exists()
        when:
            sslKeyStore.createSSLCertificate(keyStoreFile, 'somePwd', false)
        then:
            keyStoreFile.exists()
    }
}
