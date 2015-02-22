/*
 * Copyright 2011 the original author or authors.
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
package com.bmuschko.gradle.tomcat.embedded.factory

import spock.lang.Specification

import static org.spockframework.util.Assert.fail

class TomcatSpecVersionManifestReaderTest extends Specification {
    File manifestDir
    File manifestFile
    ManifestReader manifestReader = new TomcatSpecVersionManifestReader()

    def setup() {
        manifestDir = new File('build/META-INF')

        if(!manifestDir.mkdirs()) {
            fail('Unable to create integration test directory.')
        }

        manifestFile = new File(manifestDir, 'MANIFEST.MF')

        if(!manifestFile.createNewFile()) {
            fail('Unable to create manifest file.')
        }
    }

    def cleanup() {
        if(!manifestFile.delete()) {
            fail('Unable to delete manifest file.')
        }

        if(!manifestDir.deleteDir()) {
            fail('Unable to delete integration test directory.')
        }
    }

    def "Throws exception for null URL"() {
        when:
        manifestReader.readAttributeValue(null)

        then:
        thrown(MalformedURLException)
    }

    def "Throws exception for unresolvable URL"() {
        when:
            URL manifestURL = new URL('file:/myurl/test')
            manifestReader.readAttributeValue(manifestURL)

        then:
            thrown(FileNotFoundException)
    }

    def "Can read specification version if defined"() {
        when:
            manifestFile <<
                """Manifest-Version: 1.0
Ant-Version: Apache Ant 1.8.4
Created-By: 1.7.0_40-b43 (Oracle Corporation)
Specification-Title: Apache Tomcat
Specification-Version: 8.0
Specification-Vendor: Apache Software Foundation
Implementation-Title: Apache Tomcat
Implementation-Version: 8.0.0-RC5
Implementation-Vendor: Apache Software Foundation
X-Compile-Source-JDK: 1.7
X-Compile-Target-JDK: 1.7"""
            String specVersion = manifestReader.readAttributeValue(manifestFile.toURI().toURL())

        then:
            specVersion == '8.0'
    }

    def "Can't read specification version if specification title attribute is not available"() {
        when:
            manifestFile <<
                """Manifest-Version: 1.0
Ant-Version: Apache Ant 1.8.4
Created-By: 1.7.0_40-b43 (Oracle Corporation)
Specification-Version: 8.0
Specification-Vendor: Apache Software Foundation
Implementation-Title: Apache Tomcat
Implementation-Version: 8.0.0-RC5
Implementation-Vendor: Apache Software Foundation
X-Compile-Source-JDK: 1.7
X-Compile-Target-JDK: 1.7"""
            String specVersion = manifestReader.readAttributeValue(manifestFile.toURI().toURL())

        then:
            !specVersion
    }

    def "Can't read specification version if attribute is not available"() {
        when:
            manifestFile <<
                """Manifest-Version: 1.0
Ant-Version: Apache Ant 1.8.4
Created-By: 1.7.0_40-b43 (Oracle Corporation)
Specification-Title: Apache Tomcat
Specification-Vendor: Apache Software Foundation
Implementation-Title: Apache Tomcat
Implementation-Version: 8.0.0-RC5
Implementation-Vendor: Apache Software Foundation
X-Compile-Source-JDK: 1.7
X-Compile-Target-JDK: 1.7"""
            String specVersion = manifestReader.readAttributeValue(manifestFile.toURI().toURL())

        then:
            !specVersion
    }
}
