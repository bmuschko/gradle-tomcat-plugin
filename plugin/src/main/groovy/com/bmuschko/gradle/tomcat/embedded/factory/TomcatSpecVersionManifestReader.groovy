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
package com.bmuschko.gradle.tomcat.embedded.factory

import java.util.jar.Manifest

/**
 * Tomcat specification version manifest reader.
 */
class TomcatSpecVersionManifestReader implements ManifestReader {
    static final String ATTR_SPEC_TITLE = 'Apache Tomcat'
    static final String ATTR_SPEC_VERSION = 'Specification-Version'

    @Override
    String readAttributeValue(URL manifestUrl) {
        if(!manifestUrl) {
            throw new MalformedURLException('Provided URL is null')
        }

        InputStream inputStream = manifestUrl.openStream()

        try {
            Manifest manifest = new Manifest(inputStream)
            def attributes = manifest.mainAttributes

            if(attributes.getValue('Specification-Title') == ATTR_SPEC_TITLE) {
                return attributes.getValue(ATTR_SPEC_VERSION)
            }
        }
        finally {
            try {
                inputStream?.close()
            }
            catch(all) {}
        }
    }
}
