package org.gradle.api.plugins.tomcat.extension

/**
 * Defines a TomcatUser task convention.
 *
 * @author Nykolas Lima
 */
class TomcatUser implements Serializable {

    String username
    String password
    def roles = [] as String[]
    
}
