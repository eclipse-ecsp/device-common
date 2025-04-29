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

package org.eclipse.ecsp.common.log4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Requirements for this class came from Aha Async Service. In this project we
 * have multiple JMS listeners. All these listeners write into the same log file
 * according to log4j configuration. As a result it is very hard to filter out
 * log messages that happened for particular JMS listener. To solve this problem
 * new log4j appender has been created that can create different log file names
 * based on thread context. In this case JMS listener is responsible for setting
 * appropriate thread context.
 * 
 *
 */

public class DynamicDailyRollingFileAppender implements Appender, OptionHandler {

    private DailyRollingFileAppender defaultAppender = new DailyRollingFileAppender();
    private Map<String, DailyRollingFileAppender> appenders = new HashMap<String, DailyRollingFileAppender>();

    private static ThreadLocal<String> dynamicName = new ThreadLocal<String>();

    private String replaceInFile;
    private String fileTemplate;
    
    private static final int APPENDER_SIZE_LIMIT = 100;

    /**
     * Sets a dynamic name for the current thread's context.
     * If the provided name is empty or null, the dynamic name is removed.
     * Otherwise, the provided name is set as the dynamic name for the thread.
     *
     * @param name The dynamic name to set. If null or empty, the dynamic name is cleared.
     */
    public static void setDynamicName(String name) {
        if (StringUtils.isEmpty(name)) {
            dynamicName.remove();
        } else {
            dynamicName.set(name);
        }
    }

    public void setReplaceInFile(String value) {
        this.replaceInFile = value;
    }

    public String getReplaceInFile() {
        return replaceInFile;
    }

    /**
     * Appends a logging event to the appropriate appender based on the dynamic name.
     *
     * <p>
     * This method determines the appender to use based on the thread-local dynamic name.
     * If no dynamic name is set or the replacement pattern is null, the default appender
     * is used. Otherwise, it retrieves or creates a new {@link DailyRollingFileAppender}
     * for the given dynamic name. If the number of appenders exceeds the defined limit,
     * the default appender is used instead.
     * </p>
     *
     * @param event the logging event to be appended
     */
    @Override
    public void doAppend(LoggingEvent event) {
        String name = dynamicName.get();

        if (name == null || replaceInFile == null) {
            defaultAppender.doAppend(event);
            return;
        }

        DailyRollingFileAppender appender = null;
        synchronized (appenders) {
            appender = appenders.get(name);
            if (appender == null) {
                appender = createNewAppender(name);
                if (appenders.size() < APPENDER_SIZE_LIMIT) {
                    appenders.put(name, appender);
                } else {
                    System.out.println("TOO MANY " + getClass().getName()
                            + " HAVE BEEN CREATED (limit 100). Using default appender for name: " + name);
                    appender = defaultAppender;
                }
            }
        }
        appender.doAppend(event);
    }

    /**
     * Creates a new instance of {@link DailyRollingFileAppender} with the specified name.
     * The new appender is configured based on the properties of the default appender.
     *
     * @param name The name to replace in the file template and to configure the new appender.
     * @return A new {@link DailyRollingFileAppender} instance configured with the specified name.
     */
    private DailyRollingFileAppender createNewAppender(String name) {
        DailyRollingFileAppender appender = new DailyRollingFileAppender();
        String fileName = this.fileTemplate.replace(this.replaceInFile, name);
        appender.setFile(fileName);

        appender.setThreshold(defaultAppender.getThreshold());
        appender.setEncoding(defaultAppender.getEncoding());
        appender.setImmediateFlush(defaultAppender.getImmediateFlush());
        appender.setBufferSize(defaultAppender.getBufferSize());
        appender.setBufferedIO(defaultAppender.getBufferedIO());
        appender.setAppend(defaultAppender.getAppend());
        appender.setDatePattern(defaultAppender.getDatePattern());

        appender.setErrorHandler(defaultAppender.getErrorHandler());
        if (defaultAppender.requiresLayout()) {
            appender.setLayout(defaultAppender.getLayout());
        }
        appender.setName(defaultAppender.getName());

        appender.activateOptions();
        return appender;
    }

    /**
     * Activates the options for the appender. This method checks if dynamic file
     * naming can be used and updates the file name template accordingly. If dynamic
     * names are allowed, it initializes the default file name by replacing the
     * specified placeholder in the file template. Finally, it delegates the
     * activation of options to the default appender.
     */
    @Override
    public void activateOptions() {
        if (canUseDynamicNames()) {

            fileTemplate = defaultAppender.getFile();
            // initializing default name
            defaultAppender.setFile(fileTemplate.replace(replaceInFile, ""));
        }
        defaultAppender.activateOptions();

    }

    /**
     * Determines if dynamic file names can be used for the appender.
     *
     * @return {@code true} if dynamic file names can be used; {@code false} otherwise.
     *         Dynamic file names can be used if:
     *         - The 'replaceInFile' string is not empty.
     *         - The default appender's file path is not empty.
     *         - The 'replaceInFile' string is present in the default appender's file path.
     */
    private boolean canUseDynamicNames() {
        return !StringUtils.isEmpty(replaceInFile) && !StringUtils.isEmpty(defaultAppender.getFile())
                && defaultAppender.getFile().indexOf(replaceInFile) >= 0;
    }

    /**
     * Adds a filter to the default appender.
     * Filters are used to determine whether a logging event should be handled
     * by this appender based on specific criteria.
     *
     * @param newFilter the filter to be added to the default appender.
     */
    @Override
    public void addFilter(Filter newFilter) {
        defaultAppender.addFilter(newFilter);
    }

    /**
     * Retrieves the filter associated with the default appender.
     *
     * @return the {@link Filter} instance currently set for the default appender.
     */
    @Override
    public Filter getFilter() {
        return defaultAppender.getFilter();
    }

    /**
     * Removes all the filters from the default appender.
     * This method clears any previously set filters, ensuring that no filtering
     * is applied to the logging events handled by the default appender.
     */
    @Override
    public void clearFilters() {
        defaultAppender.clearFilters();
    }

    /**
     * Closes the current appender and all associated daily rolling file appenders.
     * This method ensures that resources held by the default appender and any
     * appenders in the collection are properly released.
     */
    @Override
    public void close() {
        defaultAppender.close();
        Collection<DailyRollingFileAppender> values = appenders.values();
        for (DailyRollingFileAppender appender : values) {
            appender.close();
        }
    }

    /**
     * Sets the {@link ErrorHandler} for the default appender.
     *
     * @param errorHandler the {@link ErrorHandler} to be set for handling errors.
     */
    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        defaultAppender.setErrorHandler(errorHandler);
    }

    /**
     * Retrieves the error handler associated with the default appender.
     *
     * @return the {@link ErrorHandler} instance used by the default appender.
     */
    @Override
    public ErrorHandler getErrorHandler() {
        return defaultAppender.getErrorHandler();
    }

    /**
     * Sets the layout for the default appender.
     *
     * @param layout the layout to be set for the default appender. This defines
     *               the format in which log messages will be written.
     */
    @Override
    public void setLayout(Layout layout) {
        defaultAppender.setLayout(layout);
    }

    /**
     * Retrieves the layout associated with the default appender.
     *
     * @return the {@link Layout} instance used by the default appender.
     */
    @Override
    public Layout getLayout() {
        return defaultAppender.getLayout();
    }

    /**
     * Sets the name of the appender.
     *
     * @param name the name to set for the appender
     */
    @Override
    public void setName(String name) {
        defaultAppender.setName(name);
    }

    /**
     * Retrieves the name of the default appender.
     *
     * @return the name of the default appender as a {@code String}.
     */
    @Override
    public String getName() {
        return defaultAppender.getName();
    }

    /**
     * Indicates whether a layout is required for this appender.
     *
     * @return {@code true} if a layout is required, {@code false} otherwise.
     */
    @Override
    public boolean requiresLayout() {
        return defaultAppender.requiresLayout();
    }

    
    /**
     * Sets the date pattern for the default appender.
     * The date pattern determines the naming convention for the log files
     * based on the date and time.
     *
     * @param pattern the date pattern to be used, typically in a format
     *                such as "'.'yyyy-MM-dd" or similar.
     */
    public void setDatePattern(String pattern) {
        defaultAppender.setDatePattern(pattern);
    }

    /**
     * Retrieves the date pattern used by the default appender.
     *
     * @return the date pattern as a {@code String}.
     */
    public String getDatePattern() {
        return defaultAppender.getDatePattern();
    }

    /**
     * Sets the file path for the default appender.
     *
     * @param file the path of the file to which the logs will be written.
     */
    public void setFile(String file) {
        defaultAppender.setFile(file);
    }

    /**
     * Retrieves the file path of the log file currently being used by the default appender.
     *
     * @return The file path of the log file as a {@code String}.
     */
    public String getFile() {
        return defaultAppender.getFile();
    }

    /**
     * Sets whether the appender should append to the existing file or overwrite it.
     *
     * @param flag {@code true} to enable appending to the file, 
     *             {@code false} to overwrite the file.
     */
    public void setAppend(boolean flag) {
        defaultAppender.setAppend(flag);
    }

    /**
     * Retrieves the current append mode of the default appender.
     *
     * @return {@code true} if the appender is in append mode, 
     *         {@code false} otherwise.
     */
    public boolean getAppend() {
        return defaultAppender.getAppend();
    }

    /**
     * Sets whether buffered IO is enabled for the appender.
     *
     * @param bufferedIo {@code true} to enable buffered IO, {@code false} to disable it.
     */
    public void setBufferedIo(boolean bufferedIo) {
        defaultAppender.setBufferedIO(bufferedIo);
    }

    /**
     * Retrieves the buffered IO setting of the default appender.
     *
     * @return {@code true} if buffered IO is enabled; {@code false} otherwise.
     */
    public boolean getBufferedIo() {
        return defaultAppender.getBufferedIO();
    }

    /**
     * Sets the buffer size for the default appender.
     *
     * @param bufferSize the size of the buffer to be set, in bytes.
     */
    public void setBufferSize(int bufferSize) {
        defaultAppender.setBufferSize(bufferSize);
    }

    /**
     * Retrieves the buffer size used by the default appender.
     *
     * @return the buffer size in bytes.
     */
    public int getBufferSize() {
        return defaultAppender.getBufferSize();
    }

    /**
     * Sets whether the appender should immediately flush the output stream
     * after each log event. When set to {@code true}, the appender will flush
     * the stream immediately, ensuring that log events are written to the
     * destination without delay. When set to {@code false}, the appender may
     * buffer log events for performance reasons.
     *
     * @param value {@code true} to enable immediate flushing, {@code false} to disable it.
     */
    public void setImmediateFlush(boolean value) {
        defaultAppender.setImmediateFlush(value);
    }

    /**
     * Retrieves the immediate flush setting of the default appender.
     *
     * @return {@code true} if the appender is configured to flush output immediately 
     *         after each write operation, {@code false} otherwise.
     */
    public boolean getImmediateFlush() {
        return defaultAppender.getImmediateFlush();
    }

    /**
     * Sets the character encoding for the appender.
     *
     * @param value the name of the character encoding to set. 
     *              For example, "UTF-8" or "ISO-8859-1".
     */
    public void setEncoding(String value) {
        defaultAppender.setEncoding(value);
    }

    /**
     * Retrieves the character encoding used by the default appender.
     *
     * @return the encoding as a {@link String}, or {@code null} if no encoding is set.
     */
    public String getEncoding() {
        return defaultAppender.getEncoding();
    }

    /**
     * Sets the threshold level for logging. Log events with a level lower than 
     * the specified threshold will not be logged by this appender.
     *
     * @param threshold the logging threshold to set. This determines the minimum 
     *                  priority level of log events that will be processed.
     */
    public void setThreshold(Priority threshold) {
        defaultAppender.setThreshold(threshold);
    }

    /**
     * Retrieves the logging threshold level for the appender.
     * The threshold determines the minimum priority level of log messages
     * that will be logged by this appender.
     *
     * @return the current logging threshold as a {@link Priority} object.
     */
    public Priority getThreshold() {
        return defaultAppender.getThreshold();
    }

}
