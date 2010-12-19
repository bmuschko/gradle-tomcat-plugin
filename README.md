# Gradle Tomcat plugin

The plugin provides deployment capabilities of web applications to an embedded Tomcat web container in any given
Gradle build. It extends the War plugin. At the moment only Tomcat version 6.x is supported.

## Usage

To use the Tomcat plugin, include in your build script:

    apply plugin: 'tomcat'

The plugin JAR and depending Tomcat runtime libraries need to be defined in the classpath of your build script. You can
either get the plugin from the GitHub download section or upload it to your local repository. The following code snippet
shows an example:

    buildscript {
	    repositories {
		    add(new org.apache.ivy.plugins.resolver.URLResolver()) {
    		    name = "GitHub"
    		    addArtifactPattern 'http://cloud.github.com/downloads/bmuschko/gradle-tomcat-plugin/[module]-[revision].[ext]'
  		    }
            mavenCentral()
        }

	    dependencies {
		    def tomcatVersion = '6.0.29'
            classpath "org.apache.tomcat:catalina:${tomcatVersion}",
                      "org.apache.tomcat:coyote:${tomcatVersion}",
                      "org.apache.tomcat:jasper:${tomcatVersion}"
            classpath ':gradle-tomcat-plugin:0.3'
        }
    }

## Tasks

The Tomcat plugin defines the following tasks:

* `tomcatRun`: Starts a Tomcat instance and deploys the exploded web application to it.
* `tomcatRunWar`: Starts a Tomcat instance and deploys the WAR to it.
* `tomcatStop`: Stops the Tomcat instance.

## Project layout

The Tomcat plugin uses the same layout as the War plugin.

## Convention properties

The Tomcat plugin defines the following convention properties:

* `httpPort`: The TCP port which Tomcat should listen for HTTP requests on (defaults to 8080).
* `stopPort`: The TCP port which Tomcat should listen for admin requests on (defaults to 8081).
* `stopKey`: The key to pass to Tomcat when requesting it to stop (defaults to null).

These properties are provided by a TomcatPluginConvention convention object. Furthermore, you can define the following
optional properties:

* `contextPath`: The URL context path your web application will be registered under (defaults to WAR name).
* `webDefaultXml`: The default web.xml. If it doesn't get defined an instance of `org.apache.catalina.servlets.DefaultServlet`
and `org.apache.jasper.servlet.JspServlet` will be set up.
* `additionalRuntimeJars`: Defines additional runtime JARs that are not provided by the web application.
* `URIEncoding`: Specifies the character encoding used to decode the URI bytes by the HTTP Connector (defaults to 'UTF-8').

## System properties

The convention properties can be overridden by system properties:

* `tomcat.http.port`: Overrides the convention property `httpPort`.
* `tomcat.stop.port`: Overrides the convention property `stopPort`.
* `tomcat.stop.key`: Overrides the convention property `stopKey`.