package org.gradle.api.plugins.tomcat.internal

/**
 * Store type representation.
 *
 * @author Benjamin Muschko
 */
enum StoreType {
    TRUST('TrustStore'), KEY('KeyStore')

    private final String description

    private StoreType(String description) {
        this.description = description
    }

    String getDescription() {
        return description
    }
}
