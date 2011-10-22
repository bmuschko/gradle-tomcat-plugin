# Gradle Tomcat plugin

![Tomcat Logo](http://tomcat.apache.org/images/tomcat.gif)

The plugin provides deployment capabilities of web applications to an embedded Tomcat web container in any given
Gradle build. It extends the [War plugin](http://www.gradle.org/war_plugin.html). At the moment the Tomcat versions 6.x
and 7.x are supported.

## Usage

To use the Tomcat plugin, include in your build script:

    apply plugin: 'tomcat'

The plugin JAR needs to be defined in the classpath of your build script. You can either get the plugin from the GitHub
download section or upload it to your local repository. Additionally, the Tomcat runtime libraries need to be added to the configuration
`tomcat`. At the moment the Tomcat versions 6.x and 7.x are supported by the plugin. Make sure you don't mix up Tomcat libraries
of different versions.

    buildscript {
        repositories {
            add(new org.apache.ivy.plugins.resolver.URLResolver()) {
                name = 'GitHub'
                addArtifactPattern 'http://cloud.github.com/downloads/[organisation]/[module]/[module]-[revision].[ext]'
            }
        }

        dependencies {
            classpath 'bmuschko:gradle-tomcat-plugin:0.8'
        }
    }

**Tomcat 6.x:**

    repositories {
        mavenCentral()
    }

    dependencies {
        def tomcatVersion = '6.0.29'
        tomcat "org.apache.tomcat:catalina:${tomcatVersion}",
               "org.apache.tomcat:coyote:${tomcatVersion}",
               "org.apache.tomcat:jasper:${tomcatVersion}"
    }

**Tomcat 7.x:**

    repositories {
        mavenCentral()
    }

    dependencies {
        def tomcatVersion = '7.0.11'
        tomcat "org.apache.tomcat.embed:tomcat-embed-core:${tomcatVersion}",
               "org.apache.tomcat.embed:tomcat-embed-logging-juli:${tomcatVersion}"
        tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:${tomcatVersion}") {
            exclude group: 'org.eclipse.jdt.core.compiler', module: 'ecj'
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
* `httpsPort`: The TCP port which Tomcat should listen for HTTPS requests on (defaults to 8443).
* `stopPort`: The TCP port which Tomcat should listen for admin requests on (defaults to 8081).
* `stopKey`: The key to pass to Tomcat when requesting it to stop (defaults to null).
* `enableSSL`: Determines whether the HTTPS connector should be created (defaults to false).

These properties are provided by a TomcatPluginConvention convention object. Furthermore, you can define the following
optional properties:

* `contextPath`: The URL context path your web application will be registered under (defaults to WAR name).
* `webDefaultXml`: The default web.xml. If it doesn't get defined an instance of `org.apache.catalina.servlets.DefaultServlet`
and `org.apache.jasper.servlet.JspServlet` will be set up.
* `additionalRuntimeJars`: Defines additional runtime JARs that are not provided by the web application.
* `URIEncoding`: Specifies the character encoding used to decode the URI bytes by the HTTP Connector (defaults to 'UTF-8').
* `daemon`: Specifies whether the Tomcat server should run in the background. When true, this task completes as soon as the
server has started. When false, this task blocks until the Tomcat server is stopped (defaults to false).
* `configFile`: The path to the Tomcat context XML file (defaults to `src/main/webapp/META-INF/context.xml` for `tomcatRun`,
defaults to `META-INF/context.xml` within the WAR for `tomcatRunWar`).

## System properties

The convention properties can be overridden by system properties:

* `tomcat.http.port`: Overrides the convention property `httpPort`.
* `tomcat.https.port`: Overrides the convention property `httpsPort`.
* `tomcat.stop.port`: Overrides the convention property `stopPort`.
* `tomcat.stop.key`: Overrides the convention property `stopKey`.
* `tomcat.enable.ssl`: Overrides the convention property `enableSSL`.

## FAQ

**I get a compile exception when calling a JSP. Is there something I am missing?**

The exception you might see is probably similar to this one: `org.apache.jasper.JasperException: Unable to compile class for JSP`.
Tomcat 7.x requires you to have [Eclipse ECJ 3.6.x](http://www.eclipse.org/jdt/core/) in your the classpath. However, this
version of the dependency does not exist in Maven Central. You'll have to download that dependency and put it in your own
repository or define a repository on your local disk where you can drop it in. Here's an example:

    repositories {
         flatDir name: 'localRepository', dirs: 'lib'
    }

**Why do I get a `java.lang.ClassCastException` on `javax.servlet.Servlet`?**

Tomcat is very sensitive to having multiple versions of the dependencies `javax.servlet:servlet-api` and `javax.servlet:jsp-api`
in its classpath. By default they already get pulled in as transitive dependencies of the embedded Tomcat libraries. The
exception you might see looks similar to this one:

    java.lang.ClassCastException: org.springframework.web.servlet.DispatcherServlet cannot be cast to javax.servlet.Servlet
            at org.apache.catalina.core.StandardWrapper.loadServlet(StandardWrapper.java:1062)
            at org.apache.catalina.core.StandardWrapper.load(StandardWrapper.java:1010)
            at org.apache.catalina.core.StandardContext.loadOnStartup(StandardContext.java:4935)
            at org.apache.catalina.core.StandardContext$3.call(StandardContext.java:5262)
            at org.apache.catalina.core.StandardContext$3.call(StandardContext.java:5257)
            at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
            at java.util.concurrent.FutureTask.run(FutureTask.java:138)
            at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
            at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
            at java.lang.Thread.run(Thread.java:662)

To fix this make sure you define your JSP and Servlet module dependencies with the scope `providedCompile` like this:

    providedCompile 'javax.servlet:servlet-api:2.5',
                    'javax.servlet:jsp-api:2.0'

**How do I remote debug my Tomcat started up by the plugin?**

If you want to be able to debug your application remotely you have to set the following JVM options in your `GRADLE_OPTS`
environment variable before starting up the container. The port number you choose is up to you.

    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

Tomcat will then listen on the specified port for incoming remote debugging connections. When starting up the container
you should see the following message:

    Listening for transport dt_socket at address: 5005

Check your IDE documentation on how to configure connecting to the remote debugging port.

* [IntelliJ Remote Run/Debug Configuration](http://www.jetbrains.com/idea/webhelp/run-debug-configuration-remote.html)
* [Eclipse Remote Debugging](http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Fconcepts%2Fcremdbug.htm)

