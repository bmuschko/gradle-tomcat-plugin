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
package com.bmuschko.gradle.tomcat.extension

import com.bmuschko.gradle.tomcat.embedded.TomcatUser

/**
 * Defines Tomcat plugin extension.
 */
class TomcatPluginExtension {
    final static String DEFAULT_PROTOCOL_HANDLER = 'org.apache.coyote.http11.Http11Protocol'
    final static String DEFAULT_AJP_PROTOCOL_HANDLER = 'org.apache.coyote.ajp.AjpProtocol'

    Integer httpPort = 8080
    Integer httpsPort = 8443
    Integer stopPort = 8081
    Integer ajpPort = 8009
    String stopKey = 'stopKey'
    String contextPath
    Boolean enableSSL = Boolean.FALSE
    Boolean daemon = Boolean.FALSE
    String httpProtocol = DEFAULT_PROTOCOL_HANDLER
    String httpsProtocol = DEFAULT_PROTOCOL_HANDLER
    String ajpProtocol = DEFAULT_AJP_PROTOCOL_HANDLER
    Boolean ajpSecretRequired = Boolean.TRUE
    String ajpSecret
    TomcatJasperConvention jasper = new TomcatJasperConvention()
    List<TomcatUser> users = []

    def jasper(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = jasper
        closure()
    }
    
    def users(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = users
        closure()
    }
    
    def user(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        TomcatUser user = new TomcatUser()
        closure.delegate = user
        users << user
        closure()
    }
}
