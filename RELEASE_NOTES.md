### Version 2.8.0 (UNRELEASED)

* Add option to set the ajp secret. 

### Version 2.7.0 (June 29, 2021)

* Add jspFiles and failOnError parameter for passing to the jspc ant task - [Pull Request 195](https://github.com/bmuschko/gradle-tomcat-plugin/pull/195).

### Version 2.6.0 (June 23, 2021)

* Gradle 6.2.2 shows deprecation warning regarding jasperAttributes - [Issue 194](https://github.com/bmuschko/gradle-tomcat-plugin/issues/194).
* tomcatRun fails with Gradle 7.0 due to @Internal annotation on private getter - [Issue 200](https://github.com/bmuschko/gradle-tomcat-plugin/issues/200).

### Version 2.5 (March 24, 2018)

* Stop using deprecated SourceSet method - [Issue 168](https://github.com/bmuschko/gradle-tomcat-plugin/issues/168).
* Upgrade to Gradle Wrapper 4.6.

### Version 2.4.2 (November 23, 2017)

* Jasper task adheres to up to date checking - [Pull Request 165](https://github.com/bmuschko/gradle-tomcat-plugin/pull/165).
* Upgrade to Gradle Wrapper 4.3.1.

### Version 2.4.1 (November 5, 2017)

* Fix initialization error NoClassDefFoundError for Jasper class - [Issue 162](https://github.com/bmuschko/gradle-tomcat-plugin/issues/162).

### Version 2.4 (November 4, 2017)

* Add Tomcat 9.x support.
* Upgrade to Gradle Wrapper 4.2.

### Version 2.3 (June 23, 2017)

* Add Tomcat 8.5.x support.
* `TomcatVersion` enum values have changed to reflect minor version.
* Upgrade to Gradle Wrapper 4.0.

### Version 2.2.5 (June 2, 2016)

* Remove use of Gradle internal methods.
* Upgrade to Gradle Wrapper 2.13.

### Version 2.2.4 (December 7, 2015)

* TomcatStop should wait for the server to stop - [Pull Request 137](https://github.com/bmuschko/gradle-tomcat-plugin/pull/137).
* Upgrade to Gradle Wrapper 2.9.

### Version 2.2.3 (October 23, 2015)

* Fix for JasperInitializer in Tomcat 8 embedded mode - [Pull Request 133](https://github.com/bmuschko/gradle-tomcat-plugin/pull/133).
* Upgrade to Gradle Wrapper 2.8.

### Version 2.2.2 (May 16, 2015)

* Remove ShutdownHook to avoid memory leak - [Pull Request 109](https://github.com/bmuschko/gradle-tomcat-plugin/pull/109).

### Version 2.2.1 (May 9, 2015)

* Upgrade to Gradle Wrapper 2.4.
* Renamed property `additionalRuntimeJar` to `additionalRuntimeResources` to better express its intent - [Issue 41](https://github.com/bmuschko/gradle-tomcat-plugin/issues/41).

### Version 2.2 (May 3, 2015)

* Upgrade to Gradle Wrapper 2.3.
* Register LifecycleListener that listens for AFTER_START event to determine when Tomcat is up and running to ensure that
 integration and/or functional tests are only started when web application under tests is ready.
* Added properties `contextPath` and `daemon` to extension.
* Only add web resource if it actually exists - [Issue 116](https://github.com/bmuschko/gradle-tomcat-plugin/issues/116).

### Version 2.1 (February 22, 2015)

* Proper classloader isolation from Gradle core - [Issue 45](https://github.com/bmuschko/gradle-tomcat-plugin/issues/45).

### Version 2.0 (October 4, 2014)

* Upgrade to Gradle Wrapper 2.1.
* Changed package name to `com.bmuschko.gradle.tomcat`.
* Changed group ID to `com.bmuschko`.
* Adapted plugin IDs to be compatible with Gradle's plugin portal.

### Version 1.2.5 (September 28, 2014)

* Allow for declaring users - [Pull Request 109](https://github.com/bmuschko/gradle-tomcat-plugin/pull/109).

### Version 1.2.4 (June 21, 2014)

* Configure classes directory and web app source directory in base plugin. This will reduce the amount of configuration required by plugin consumers - [Issue 86](https://github.com/bmuschko/gradle-tomcat-plugin/issues/86).
* Support for creating SSL keystore with Java 8 - [Issue 98](https://github.com/bmuschko/gradle-tomcat-plugin/issues/98).

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
