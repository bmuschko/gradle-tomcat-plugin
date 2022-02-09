package com.bmuschko.gradle.tomcat.embedded

import groovy.transform.Canonical

/**
 * Defines an additional resource.
 */
@Canonical
class Resource implements Serializable {
    File path
    String mountpoint
}
