package com.bmuschko.gradle.tomcat.embedded

import groovy.transform.Canonical

/**
 * Defines a Tomcat user.
 *
 * @author Nykolas Lima
 */
@Canonical
class TomcatUser implements Serializable {
    String username
    String password
    List<String> roles = []
}
