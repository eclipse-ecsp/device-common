package org.eclipse.ecsp.common.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * this class is based on Executors.DefaultThreadFactory implementation.
 * but allows to add name to the pool
 *
 * @author Sergey
 */
public class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;

    public NamedThreadFactory(String factoryName) {
        this(factoryName, true);
    }

    /**
     * Constructs a new {@code NamedThreadFactory} with the specified factory name
     * and daemon status.
     *
     * @param factoryName the name prefix for threads created by this factory.
     *                    Each thread's name will be suffixed with a unique number.
     * @param daemon      whether the threads created by this factory should be
     *                    daemon threads. Daemon threads do not prevent the JVM
     *                    from exiting when the program finishes.
     */
    public NamedThreadFactory(String factoryName, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = factoryName + "-";
        this.daemon = daemon;
    }

    /**
     * Creates a new thread with a specified runnable task.
     * The thread is created with a name that includes a prefix and a unique number,
     * and is associated with a thread group. The thread's daemon status and priority
     * are also set based on the factory's configuration.
     *
     * @param r the runnable task to be executed by the new thread
     * @return the newly created thread
     */
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(daemon);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}
