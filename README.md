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
            classpath 'bmuschko:gradle-tomcat-plugin:0.9.5'
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
* `tomcatJasper`: Runs the JSP compiler ([Jasper](http://tomcat.apache.org/tomcat-7.0-doc/jasper-howto.html)) and turns JSP pages into Java source using.

## Project layout

The Tomcat plugin uses the same layout as the War plugin.

## Convention properties

The Tomcat plugin defines the following convention properties:

* `httpPort`: The TCP port which Tomcat should listen for HTTP requests on (defaults to `8080`).
* `httpsPort`: The TCP port which Tomcat should listen for HTTPS requests on (defaults to `8443`).
* `stopPort`: The TCP port which Tomcat should listen for admin requests on (defaults to `8081`).
* `stopKey`: The key to pass to Tomcat when requesting it to stop (defaults to `null`).
* `enableSSL`: Determines whether the HTTPS connector should be created (defaults to `false`).
* `keystoreFile`: The keystore file to use for SSL, if enabled (by default, a keystore will be generated).
* `keystorePass`: The keystore password to use for SSL, if enabled.
* `httpProtocol`: The HTTP protocol handler class name to be used (defaults to `org.apache.coyote.http11.Http11Protocol`).
* `httpsProtocol`: The HTTPS protocol handler class name to be used (defaults to `org.apache.coyote.http11.Http11Protocol`).

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
* `outputFile`: The file to write Tomcat log messages to. If the file already exists new messages will be appended.

The following example shows how to change the default HTTP/HTTPS ports for the task `tomcatRun`. To enable SSL we set the
convention property `enableSSL` to `true`. Furthermore, we declare a custom context file.

### Example

    tomcatRun {
        httpPort = 8090
        httpsPort = 8091
        enableSSL = true
        configFile = file('context.xml')
    }

To configure the Jasper compiler task you can choose to set the following properties within the `jasper` closure:

* `validateXml`: Determines whether `web.xml` should be validated (defaults to `false`).
* `uriroot`: The web application root directory (defaults to `src/main/webapp`).
* `webXmlFragment`: The generated web XML fragment file to be referenced by your `web.xml` file.
* `outputDir`: The output directory the compiled JSPs will end up in (defaults to `build/jasper`).
* `classdebuginfo`: Should the class file be compiled with debugging information (defaults to `true`).
* `compiler`: Which compiler Ant should use to compile JSP pages. See the Ant documentation for more information. If the value is not set, then the default Eclipse JDT Java compiler will be used instead of using Ant. No default value.
* `compilerSourceVM`: What JDK version are the source files compatible with (defaults to `1.6`).
* `compilerTargetVM`: What JDK version are the generated files compatible with (defaults to `1.6`).
* `poolingEnabled`: Determines whether tag handler pooling is enabled. This is a compilation option. It will not alter the behaviour of JSPs that have already been compiled (defaults to `true`).
* `errorOnUseBeanInvalidClassAttribute`: Should Jasper issue an error when the value of the class attribute in an useBean action is not a valid bean class (defaults to `true`).
* `genStringAsCharArray`: Should text strings be generated as char arrays, to improve performance in some cases (defaults to `false`).
* `ieClassId`: The class-id value to be sent to Internet Explorer when using `<jsp:plugin>` tags (defaults to `clsid:8AD9C840-044E-11D1-B3E9-00805F499D93`).
* `javaEncoding`: Java file encoding to use for generating java source files (defaults to `UTF8`).
* `trimSpaces`: Should white spaces in template text between actions or directives be trimmed (defaults to `false`).
* `xpoweredBy`: Determines whether X-Powered-By response header is added by generated servlet (defaults to `false`).

### Example

    jasper {
        validateXml = true
        webXmlFragment = file("$webAppDir/WEB-INF/generated_web.xml")
        outputDir = file("$webAppDir/WEB-INF/src")
    }

## System properties

The convention properties can be overridden by system properties:

* `tomcat.http.port`: Overrides the convention property `httpPort`.
* `tomcat.https.port`: Overrides the convention property `httpsPort`.
* `tomcat.stop.port`: Overrides the convention property `stopPort`.
* `tomcat.stop.key`: Overrides the convention property `stopKey`.
* `tomcat.enable.ssl`: Overrides the convention property `enableSSL`.
* `tomcat.http.protocol`: Overrides the convention property `httpProtocol`.
* `tomcat.https.protocol`: Overrides the convention property `httpsProtocol`.

## FAQ

**I get a compile exception when calling a JSP. Is there something I am missing?**

The exception you might see is probably similar to this one: `org.apache.jasper.JasperException: Unable to compile class for JSP`.
Tomcat 7.x requires you to have [Eclipse ECJ 3.6.x](http://www.eclipse.org/jdt/core/) in your the classpath. However, this
version of the dependency does not exist in Maven Central. You'll have to download that dependency and put it in your own
repository or define a repository on your local disk where you can drop it in. Here's an example:

    repositories {
         flatDir name: 'localRepository', dirs: 'lib'
    }

<br>
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

<br>
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

<br>
**My Tomcat container needs to use a JNDI datasource. How do I set up my project?**

First of all you got to make sure to declare the connection pool dependency using the `tomcat` configuration.

    def tomcatVersion = '6.0.35'
    tomcat "org.apache.tomcat:dbcp:${tomcatVersion}"

If you decide to go with the default settings place your `context.xml` in the directory `src/main/webapp/META-INF`. To
set a custom location you can use the convention property `configFile`. Here's an example on how to set it for the tasks
`tomcatRun` and `tomcatRunWar`.

    [tomcatRun, tomcatRunWar]*.configFile = file('context.xml')

Please refer to the [Tomcat documentation](http://tomcat.apache.org/tomcat-7.0-doc/config/context.html#Defining_a_context) for a list
of context attributes. The following example shows how to set up a MySQL JNDI datasource.

    <?xml version="1.0" encoding="UTF-8"?>
    <Context>
        <Resource name="jdbc/mydatabase"
                  auth="Container"
                  type="javax.sql.DataSource"
                  username="superuser"
                  password="secretpasswd"
                  driverClassName="com.mysql.jdbc.Driver"
                  url="jdbc:mysql://localhost:3306/mydb"
                  validationQuery="select 1"
                  maxActive="10"
                  maxIdle="4"/>
    </Context>

<br>
**How do I use byte code swap technologies like JRebel with the plugin?**

The configuration is usually product-specific. Please refer to the product's documentation on how to set it up for your project.
The following section describes how to set up Gradle and the plugin with [JRebel](http://zeroturnaround.com/jrebel/).
First of all download [JRebel](http://zeroturnaround.com/jrebel/current/), install it on your machine and set up the [license](http://zeroturnaround.com/reference-manual/install.html#install-1.3).
To tell JRebel which directory to scan for changed byte code you need to create a [rebel.xml](file://localhost/Users/benjamin/dev/tools/jrebel/doc/app.html#app) file. In
your web module place the file under `build/classes/main` so it can be loaded by the Tomcat plugin. For creating the configuration of the file
the [Gradle JRebel plugin](http://zeroturnaround.com/blog/jrebel-gradle-plugin-beta/) comes in handy. It's not required
to use the plugin. You can also decide to create the configuration by hand. Keep in mind that `gradle clean` will delete the file. 
For setting up JRebel in a multi-module project scenario please refer to the documentation. The following code snippet shows an example
`rebel.xml` file.

    <?xml version="1.0" encoding="UTF-8"?>
    <application xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.zeroturnaround.com"
                xsi:schemaLocation="http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd">
        <classpath>
            <dir name="/Users/ben/dev/projects/mywebproject/build/classes/main">
            </dir>
        </classpath>

        <web>
            <link target="/">
                <dir name="/Users/ben/dev/projects/mywebproject/src/main/webapp">
                </dir>
            </link>
        </web>
    </application>

Edit your Gradle startup script and add the following line to it to tell Gradle to [use the JRebel agent](http://zeroturnaround.com/reference-manual/server.html#server-4.5.36).
Please make sure to set the environment variable `REBEL_HOME` that points to your JRebel installation directory.

    JAVA_OPTS="-javaagent:$REBEL_HOME/jrebel.jar $JAVA_OPTS"

On startup of your web module using `gradle tomcatRun` you should see information about the JRebel license being used and
the directories being scanned for changes. For our example `rebel.xml` file it would look like this:

    JRebel: Directory '/Users/ben/dev/projects/mywebproject/build/classes/main' will be monitored for changes.
    JRebel: Directory '/Users/ben/dev/projects/mywebproject/src/main/webapp' will be monitored for changes.

If a file has been recompiled JRebel indicates this by writing it to the console like this:

    JRebel: Reloading class 'de.muschko.web.controller.TestController'.

<br>
**In need to run in-container integration tests as part of my build. What needs to be done?**

Usually unit and integration tests are kept separate by convention. One convention could be to name the test source
files differently e.g. integration tests always end with the suffix `IntegrationTest`, unit test files end with `Test`.
By doing that you can run them separately. For running the integration tests you will want to run the Tomcat task as daemon
thread and shut it down once your tests are done. The following example demonstrates how to set up a Gradle task that provides this
functionality. Of course this is only one way of doing it.

    [tomcatRun, tomcatStop]*.stopPort = 8081
    [tomcatRun, tomcatStop]*.stopKey = 'stopKey'

    task integrationTest(type: Test) {
        include '**/*IntegrationTest.*'

        doFirst {
            tomcatRun.daemon = true
            tomcatRun.execute()
        }

        doLast {
            tomcatStop.execute()
        }
    }

    test {
        exclude '**/*IntegrationTest.*'
    }