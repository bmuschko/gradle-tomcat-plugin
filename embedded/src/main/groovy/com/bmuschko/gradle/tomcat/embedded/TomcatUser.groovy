package com.bmuschko.gradle.tomcat.embedded

import groovy.transform.Canonical

/**
 * Defines a Tomcat user.
 */
@Canonical
class TomcatUser implements Serializable {
    String username
    String password
    List<String> roles = []
}
