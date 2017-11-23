package com.bmuschko.gradle.tomcat.embedded

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch

/**
 * Implementation of common Tomcat server logic.
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
    void addStartUpLifecycleListener(CountDownLatch startupBarrier, boolean daemon) {
        def afterStartEventLifecycleListener = java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread().contextClassLoader,
                [loadClass('org.apache.catalina.LifecycleListener')] as Class[], new AfterStartEventLifecycleListener(startupBarrier, daemon))
        addLifecycleListener(afterStartEventLifecycleListener)
    }

    @Override
    void start() {
        stopped = false
        tomcat.start()
    }

    @Override
    void stop() {
        context?.stop()
        context?.destroy()

        if(!stopped) {
            tomcat.stop()
            stopped = true
        }

        tomcat.destroy()
    }

    @Override
    boolean isStopped() {
        stopped
    }

    private class AfterStartEventLifecycleListener implements InvocationHandler {
        private final Logger logger = Logging.getLogger(AfterStartEventLifecycleListener)
        private final CountDownLatch startupBarrier
        private final boolean daemon

        AfterStartEventLifecycleListener(CountDownLatch startupBarrier, boolean daemon) {
            this.startupBarrier = startupBarrier
            this.daemon = daemon
        }

        @Override
        Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName()

            if(methodName == 'lifecycleEvent') {
                def event = args[0]

                if(event.type == 'after_start') {
                    logger.quiet 'Started Tomcat Server'
                    logger.quiet "The Server is running at http://localhost:${httpConnector.port}${context.path}"

                    if(daemon) {
                        startupBarrier.countDown()
                    }
                }
            }
        }
    }

    abstract void addLifecycleListener(lifecycleListener)
    abstract Object getHttpConnector()
}
