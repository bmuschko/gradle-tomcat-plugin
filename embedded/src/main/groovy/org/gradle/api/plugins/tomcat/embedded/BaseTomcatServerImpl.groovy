package org.gradle.api.plugins.tomcat.embedded

/**
 * Implementation of common Tomcat server logic.
 *
 * @author Andrey Bloschetsov
 */
abstract class BaseTomcatServerImpl implements TomcatServer {
    final tomcat
    def context
    private boolean stopped

    public BaseTomcatServerImpl() {
        Class serverClass = loadClass(getServerClassName())
        this.tomcat = serverClass.newInstance()
    }

    Class loadClass(String className) {
        ClassLoader classLoader = Thread.currentThread().contextClassLoader
        classLoader.loadClass(className)
    }

    @Override
    def getEmbedded() {
        tomcat
    }

    @Override
    def getContext() {
        context
    }

    @Override
    void start() {
        stopped = false
        tomcat.start()
    }

    @Override
    void stop() {
        stopped = true
        tomcat.stop()
        tomcat.destroy()
    }

    @Override
    boolean isStopped() {
        stopped
    }
}
