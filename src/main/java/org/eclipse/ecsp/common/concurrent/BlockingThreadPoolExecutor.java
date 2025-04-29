/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and\
 * limitations under the License.
 *
 * <p>SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.ecsp.common.concurrent;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A custom implementation of {@link ThreadPoolExecutor} that uses a
 * {@link Semaphore} to limit the number of tasks that can be submitted to the
 * executor at any given time. This ensures that the thread pool does not become
 * overwhelmed with tasks.
 *
 * <p>
 * The {@code BlockingThreadPoolExecutor} blocks the calling thread when the
 * semaphore limit is reached, waiting until a permit becomes available before
 * allowing the task to be submitted. This is particularly useful in scenarios
 * where task submission needs to be throttled to prevent resource exhaustion.
 * </p>
 *
 * <p>
 * Key features of this implementation:
 * </p>
 * <ul>
 * <li>Uses a {@link Semaphore} to control the number of concurrent tasks.</li>
 * <li>Retries task submission in case of {@link RejectedExecutionException} due
 * to race conditions in the underlying {@link ThreadPoolExecutor}.</li>
 * <li>Releases semaphore permits after task execution, ensuring proper resource
 * management.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * BlockingThreadPoolExecutor executor = new BlockingThreadPoolExecutor(4, 10, 60, TimeUnit.SECONDS);
 * executor.execute(() -> {
 *     // Task logic here
 * });
 * executor.shutdown();
 * }</pre>
 *
 * @see ThreadPoolExecutor
 * @see Semaphore
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

    private Semaphore semaphore;
    private static final int RETRY_COUNT = 2;
    private static final int SLEEP_TIME_MILLIS = 100;

    /**
     * A custom thread pool executor that blocks when the maximum pool size is reached.
     * This class extends the ThreadPoolExecutor and uses a semaphore to limit the number
     * of concurrent threads to the specified maximum pool size.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maxPoolSize the maximum number of threads allowed in the pool.
     * @param keepAliveTime the maximum time that excess idle threads will wait for new tasks
     *                      before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument.
     */
    public BlockingThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maxPoolSize, keepAliveTime, unit, new SynchronousQueue<Runnable>());
        this.semaphore = new Semaphore(maxPoolSize);
    }

    /**
     * Executes the given task in the thread pool. This method ensures that the task
     * is executed only after acquiring a permit from the semaphore, which limits
     * the number of concurrent tasks being executed. If the thread pool rejects the
     * task due to being overloaded, it retries execution until successful.
     * 
     * <p>In case of a {@link RejectedExecutionException}, the method retries the
     * execution, introducing a delay after a certain number of retries to avoid
     * excessive CPU usage. If a runtime exception or error occurs during task
     * execution, the semaphore permit is released before propagating the exception.
     *
     * @param task the {@link Runnable} task to be executed
     * @throws RuntimeException if the task execution fails due to a runtime
     *                          exception
     * @throws Error            if the task execution fails due to an error
     */
    @Override
    public void execute(Runnable task) {
        boolean acquired = false;

        do {
            try {
                // System.out.println(getInfo() + "acquire() " + task);
                semaphore.acquire();
                acquired = true;
            } catch (InterruptedException e) {
                // System.out.println(getInfo() + "acquire() failed " + task);
            }
        } while (!acquired);

        try {
            // System.out.println(getInfo() + "execute() " + task);

            long retryCounter = 0;
            while (true) {
                try {
                    super.execute(task);
                    break;
                } catch (RejectedExecutionException e) {
                    // there is no way to know when ThreadPoolExecutor releases thread, so we have
                    // to deal with race conditions... Retrying
                    retryCounter++;
                    if (retryCounter > RETRY_COUNT) {
                        try {
                            // System.out.println(getInfo()+ "pool is busy. Waiting... " + task);
                            Thread.sleep(SLEEP_TIME_MILLIS);
                        } catch (InterruptedException ie) {
                            // do nothing
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            // specifically, handle RejectedExecutionException
            // System.out.println(getInfo() + "release() RE " + task);
            semaphore.release();
            throw e;
        } catch (Error e) {
            // System.out.println(getInfo() + "release() E " + task);
            semaphore.release();
            throw e;
        }
    }

    /**
     * This method is invoked after the execution of a Runnable task. It is used to
     * perform any necessary cleanup or post-processing.
     *
     * @param r the runnable that has completed execution
     * @param t the exception that caused termination, or {@code null} if execution
     *          completed normally
     * 
     *          This implementation releases a permit from the semaphore to signal
     *          that a thread has completed its task and is available for reuse.
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        // System.out.println(getInfo()+"release() "+r);
        semaphore.release();
    }
}