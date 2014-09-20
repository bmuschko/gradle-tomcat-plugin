package org.gradle.api.plugins.tomcat.embedded

/**
 * Implementation of common Tomcat server logic.
 *
 * @author Benjamin Muschko
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
    void addWebappResource(File resource) {
        context.loader.addRepository(resource.toURI().toURL().toString())
    }

    @Override
    void start() {
        stopped = false
        tomcat.start()
    }

    @Override
    void stop() {
        stopped = true
        context?.stop()
        context?.destroy()
        tomcat.stop()
        tomcat.destroy()
    }

    @Override
    boolean isStopped() {
        stopped
    }
    
    @Override
    void configureUser(String username, String password, String group) {
	tomcat.addUser(username, password);
	tomcat.addRole(username, group);
    } 
}
