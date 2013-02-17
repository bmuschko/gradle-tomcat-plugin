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