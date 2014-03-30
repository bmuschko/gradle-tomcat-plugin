### Version 1.2.3 (March 29, 2014)

* Tasks should never be up-to-date - [Issue 90](https://github.com/bmuschko/gradle-tomcat-plugin/issues/90).

### Version 1.2.2 (March 15, 2014)

* Tomcat doesn't seem to shutdown gracefully when exiting after `tomcatRun` - [Issue 21](https://github.com/bmuschko/gradle-tomcat-plugin/issues/21).

### Version 1.2.1 (March 15, 2014)

* Allow Jasper task to use attribute `validateXml` or `validateTld` - [Issue 87](https://github.com/bmuschko/gradle-tomcat-plugin/issues/87).

### Version 1.2 (March 7, 2014)

* Broke out Tomcat base plugin.
* Moved tasks into subpackage `org.gradle.api.plugins.tomcat.tasks`.
* Updated documentation.
* Logging lock file isn't closed if container is stopped in daemon mode - [Issue 48](https://github.com/bmuschko/gradle-tomcat-plugin/issues/48).
* Preconfigure property `webAppClasspath` for custom task with type TomcatRun - [Issue 86](https://github.com/bmuschko/gradle-tomcat-plugin/issues/86).

### Version 1.1 (March 5, 2014)

* Removed "magic" system properties - [Issue 84](https://github.com/bmuschko/gradle-tomcat-plugin/issues/84).
* Allow non-existent `classes` directory - [Issue 51](https://github.com/bmuschko/gradle-tomcat-plugin/issues/51).
* Allow non-existent `webapp` directory - [Issue 80](https://github.com/bmuschko/gradle-tomcat-plugin/issues/80).
* Upgrade to Gradle Wrapper 1.11.

### Version 1.0 (October 19, 2013)

* Support for configuring AJP connector - [Pull Request 57](https://github.com/bmuschko/gradle-tomcat-plugin/pull/57).
* Allow preserving existing SSL key - [Pull Request 59](https://github.com/bmuschko/gradle-tomcat-plugin/pull/59).
* Added capability for user to specify truststore file - [Pull Request 61](https://github.com/bmuschko/gradle-tomcat-plugin/pull/61).
* The plugin is now published to Bintray. Maven Central is not going to be supported anymore.

### Version 0.9.9 (August 14, 2013)

* Resolve `ConfigurationContainer.add()` deprecation warning - [Pull Request 55](https://github.com/bmuschko/gradle-tomcat-plugin/pull/55).
* Upgrade to Gradle Wrapper 1.7.

### Version 0.9.8 (March 30, 2013)

* Call tomcat.destroy() when stopping Tomcat to avoid Gradle daemon to exit [Pull Request 42](https://github.com/bmuschko/gradle-tomcat-plugin/pull/42).

### Version 0.9.7 (February 17, 2013)

* Exposed convention property `addWebXmlMappings` for Jasper task - [Pull Request 38](https://github.com/bmuschko/gradle-tomcat-plugin/pull/38).
* Upgrade to Gradle Wrapper 1.4.

### Version 0.9.6 (January 19, 2013)

* Using Gradle logger instance from `DefaultTask`.
* Upgrade to Gradle Wrapper 1.3.

### Version 0.9.5 (September 30, 2012)

* Exposed convention properties for configuring external SSL keystore - [Pull Request 35](https://github.com/bmuschko/gradle-tomcat-plugin/pull/35).

### Version 0.9.4 (August 12, 2012)

* ConfigFile not taken correctly if its path contains spaces with Tomcat 6 - [Issue 30](https://github.com/bmuschko/gradle-tomcat-plugin/issues/30).

### Version 0.9.3 (July 17, 2012)

* Expose property to configure writing the Tomcat logs to a file - [Issue 28](https://github.com/bmuschko/gradle-tomcat-plugin/issues/28).
* Upgrade to Gradle Wrapper 1.0.

### Version 0.9.2 (May 7, 2012)

* Correctly scan for `@HandleTypes` to support Servlet 3.0 applications without `web.xml` - [Issue 14](https://github.com/bmuschko/gradle-tomcat-plugin/issues/14).

### Version 0.9.1 (March 4, 2012)

* Allow the task `tomcatRunWar` to pick up source file changes on subsequent calls - [Issue 23](https://github.com/bmuschko/gradle-tomcat-plugin/issues/23).

### Version 0.9 (February 19, 2012)

* Provided Jasper task to validate/compile JSPs - [Issue 22](https://github.com/bmuschko/gradle-tomcat-plugin/issues/22).

### Version 0.8.3 (February 9, 2012)

* Fixed root context path issues - [Issue 20](https://github.com/bmuschko/gradle-tomcat-plugin/issues/20).

### Version 0.8.2 (January 8, 2012)

* Allow Tomcat protocol handler class to be set - [Issue 17](https://github.com/bmuschko/gradle-tomcat-plugin/issues/17).
* Added example of setting up in-container integration tests.

### Version 0.8.1 (November 22, 2011)

* Removed reference to Gradle classpath variable - [Issue 12](https://github.com/bmuschko/gradle-tomcat-plugin/issues/12).
Instead used parent last ClassLoader strategy.
* Upgrade to Gradle Wrapper 1.0-m6.

### Version 0.8 (October 22, 2011)

* Introduced configuration for Tomcat libraries. **Note: Do not configure them in the `buildscript` closure anymore!**
* All tasks now extend `org.gradle.api.DefaultTask`.
* Support for exposing a HTTPS connector - [Issue 10](https://github.com/bmuschko/gradle-tomcat-plugin/issues/10).

### Version 0.7 (May 3, 2011)

* Support Tomcat 7 - [Issue 7](https://github.com/bmuschko/gradle-tomcat-plugin/issues#issue/7).
* Separated code into modules.
* Embedded Tomcat runs in its own classloader to avoid library conflicts with provided Gradle plugins.
* Added unit tests.

### Version 0.6 (March 30, 2011)

* Support configurable context.xml - [Issue 6](https://github.com/bmuschko/gradle-tomcat-plugin/issues#issue/6).

### Version 0.5 (March 10, 2011)

* Support for running Tomcat as daemon - [Issue 5](https://github.com/bmuschko/gradle-tomcat-plugin/issues#issue/5).

### Version 0.4 (March 5, 2011)

* Support configurable root context path - [Issue 4](https://github.com/bmuschko/gradle-tomcat-plugin/issues#issue/4).
* Added license file.

### Version 0.3 (December 19, 2010)

* Added optional configuration property `URIEncoding`.
* Set default VCS to Git for Gradle IDEA plugin.

### Version 0.2 (December 8, 2010)

* Added optional configuration property `additionalRuntimeJars`.
* Refactored some code.
* Wrote some unit tests.

### Version 0.1 (December 2, 2010)

* Initial release.