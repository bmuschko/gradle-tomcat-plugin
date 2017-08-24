package com.bmuschko.gradle.tomcat.embedded

import groovy.transform.Canonical

/**
 * Defines a Tomcat object.
 *
 * @author Eric Chauvin
 */
@Canonical
abstract class TomcatObject implements Serializable {
    String className
    Map<String, Object> attributes
}
