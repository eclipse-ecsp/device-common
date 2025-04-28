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

    @Override
    public void setLayout(Layout layout) {
        defaultAppender.setLayout(layout);
    }

    @Override
    public Layout getLayout() {
        return defaultAppender.getLayout();
    }

    @Override
    public void setName(String name) {
        defaultAppender.setName(name);
    }

    @Override
    public String getName() {
        return defaultAppender.getName();
    }

    @Override
    public boolean requiresLayout() {
        return defaultAppender.requiresLayout();
    }

    // ####################################################
    // DailyRollingFileAppender
    // ####################################################

    public void setDatePattern(String pattern) {
        defaultAppender.setDatePattern(pattern);
    }

    public String getDatePattern() {
        return defaultAppender.getDatePattern();
    }

    // ####################################################
    // FileAppender
    // ####################################################
    public void setFile(String file) {
        defaultAppender.setFile(file);
    }

    public String getFile() {
        return defaultAppender.getFile();
    }

    public void setAppend(boolean flag) {
        defaultAppender.setAppend(flag);
    }

    public boolean getAppend() {
        return defaultAppender.getAppend();
    }

    public void setBufferedIo(boolean bufferedIo) {
        defaultAppender.setBufferedIO(bufferedIo);
    }

    public boolean getBufferedIo() {
        return defaultAppender.getBufferedIO();
    }

    public void setBufferSize(int bufferSize) {
        defaultAppender.setBufferSize(bufferSize);
    }

    public int getBufferSize() {
        return defaultAppender.getBufferSize();
    }

    // ####################################################
    // WriterAppender
    // ####################################################

    public void setImmediateFlush(boolean value) {
        defaultAppender.setImmediateFlush(value);
    }

    public boolean getImmediateFlush() {
        return defaultAppender.getImmediateFlush();
    }

    public void setEncoding(String value) {
        defaultAppender.setEncoding(value);
    }

    public String getEncoding() {
        return defaultAppender.getEncoding();
    }

    // ####################################################
    // Appender Skeleton
    // ####################################################

    public void setThreshold(Priority threshold) {
        defaultAppender.setThreshold(threshold);
    }

    public Priority getThreshold() {
        return defaultAppender.getThreshold();
    }

}
