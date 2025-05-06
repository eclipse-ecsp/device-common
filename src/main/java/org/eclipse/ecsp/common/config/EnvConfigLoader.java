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

package org.eclipse.ecsp.common.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * The {@code EnvConfigLoader} class is responsible for loading and managing
 * configuration properties for an application. It supports loading properties
 * from multiple sources, including configuration files and environment
 * variables, and provides mechanisms for normalizing and processing these
 * properties.
 *
 * <p>
 * This class is designed to work with an enumeration of properties that
 * implement the {@code EnvConfigProperty} interface. It ensures that only
 * registered properties are loaded and processed, and it provides flexibility
 * for handling property values through a custom
 * {@code EnvConfigPropertyValueProcessor}.
 * </p>
 *
 * <p>Key features of this class include:
 * <ul>
 * <li>Loading properties from global, application, and test configuration
 * files.</li>
 * <li>Overriding properties with environment variables.</li>
 * <li>Normalizing and validating properties against a predefined set of
 * property names.</li>
 * <li>Providing a mechanism to reload configuration properties
 * dynamically.</li>
 * </ul>
 *
 * <p>
 * Typical usage involves creating an instance of {@code EnvConfigLoader} with a
 * specific property enumeration and file name prefix, and then accessing the
 * loaded configuration through the {@code getServerConfig()} method.
 * </p>
 *
 * <p>Example:
 * 
 * <pre>{@code
 * EnvConfigLoader<MyPropertyEnum> loader = new EnvConfigLoader<>(MyPropertyEnum.class, "my-app");
 * EnvConfig<MyPropertyEnum> config = loader.getServerConfig();
 * }</pre>
 *
 * @param <T> The type of the property enumeration that extends {@code Enum<T>}
 *            and implements {@code EnvConfigProperty}.
 */
public class EnvConfigLoader<T extends Enum<T>> {

    private static Logger log = LoggerFactory.getLogger(EnvConfigLoader.class);
    private static final String GLOBAL_CONFIG_FILE_NAME = ".properties";
    private static final String APP_CONFIG_FILE_NAME = "-app.properties";
    private static final String TEST_CONFIG_FILE_NAME = "-test.properties";
    private static String fileSeparator = System.getProperty("file.separator");

    private final EnvConfig<T> config;

    private final Class<T> propertyClass;
    private final String fileNamePrefix;
    private final EnvConfigPropertyValueProcessor valueProcessor;

    /**
     * Constructs an instance of {@code EnvConfigLoader} with the specified property
     * class and file name prefix. This constructor delegates to another constructor
     * with an additional parameter for environment overrides, which is set to
     * {@code null}.
     *
     * @param propertyClass  the class type of the properties to be loaded
     * @param fileNamePrefix the prefix of the file name to be used for loading
     *                       configuration files
     */
    public EnvConfigLoader(Class<T> propertyClass, String fileNamePrefix) {
        this(propertyClass, fileNamePrefix, null);
    }

    /**
     * Constructs an instance of {@code EnvConfigLoader} with the specified property
     * class, file name prefix, and value processor.
     *
     * @param propertyClass  The class of the property enum that implements
     *                       {@code EnvConfigProperty}. This class is used to define
     *                       the configuration properties.
     * @param fileNamePrefix The prefix of the configuration file name. Must not be
     *                       null or empty.
     * @param valueProcessor The processor used to handle property values during
     *                       configuration loading.
     * 
     * @throws RuntimeException If the provided {@code propertyClass} does not
     *                          implement the {@code EnvConfigProperty} interface.
     * @throws RuntimeException If the provided {@code fileNamePrefix} is null or
     *                          empty.
     */
    public EnvConfigLoader(Class<T> propertyClass, String fileNamePrefix,
            EnvConfigPropertyValueProcessor valueProcessor) {

        if (!EnvConfigProperty.class.isAssignableFrom(propertyClass)) {
            throw new RuntimeException("Property Enum " + propertyClass.getName() + " must implement interface "
                    + EnvConfigProperty.class.getName());
        }
        this.valueProcessor = valueProcessor;
        this.fileNamePrefix = StringUtils.trimToNull(fileNamePrefix);
        if (this.fileNamePrefix == null) {
            throw new RuntimeException("File name prefix must not be empty. Current value '" + fileNamePrefix + "'.");
        }
        this.propertyClass = propertyClass;
        this.config = new EnvConfig<>(propertyClass);
        reload();

    }

    /**
     * Retrieves the server configuration.
     *
     * @return the server configuration of type {@code EnvConfig<T>}.
     */
    public EnvConfig<T> getServerConfig() {
        return config;
    }

    /**
     * Reloads the configuration properties by performing the following steps:
     * <ul>
     * <li>Loads all properties from the specified configuration file path and file
     * name prefix.</li>
     * <li>Overrides the loaded properties with environment variables, if
     * applicable.</li>
     * <li>Updates the configuration with the final set of properties.</li>
     * </ul>
     * This method ensures that the configuration is refreshed with the latest
     * values from both the configuration files and the environment variables.
     */
    public void reload() {
        Properties properties = loadAllProperties(EnvConfigLocation.INSTANCE.getPath(), this.fileNamePrefix);
        overrideWithEnvironmentVariables(properties);
        config.setProperties(properties);
    }

    /**
     * Overrides the given {@link Properties} object with values from the system's
     * environment variables. If a key in the environment variables matches a key in
     * the provided {@link Properties} object, the value from the environment
     * variables will replace the value in the {@link Properties} object.
     *
     * @param properties the {@link Properties} object to be updated with
     *                   environment variable values.
     */
    private void overrideWithEnvironmentVariables(Properties properties) {
        Map<String, String> environmentMap = System.getenv();
        log.info("System properties: {}", environmentMap);

        for (String key : environmentMap.keySet()) {
            if (properties.containsKey(key)) {
                log.info("Overriding with system property {} : {}", key, environmentMap.get(key));
                properties.put(key, environmentMap.get(key));
            }
        }
    }

    /**
     * Loads and merges properties from multiple configuration files located in the
     * specified path. The method combines properties from global, application, and
     * test configuration files, normalizes them, and returns the resulting set of
     * properties.
     *
     * @param path   The directory path where the configuration files are located.
     * @param prefix The prefix to be added to the configuration file names. This
     *               prefix is used to construct the file names for global,
     *               application, and test configuration files.
     * @return A {@link Properties} object containing the merged and normalized
     *         properties from the global, application, and test configuration
     *         files.
     */
    private Properties loadAllProperties(String path, String prefix) {
        String globalConfigFileName = prefix + GLOBAL_CONFIG_FILE_NAME;
        String appConfigFileName = prefix + APP_CONFIG_FILE_NAME;
        String testConfigFileName = prefix + TEST_CONFIG_FILE_NAME;

        Properties globalProps = loadFileWithProperties(path, globalConfigFileName, true);

        Properties appProps = loadFileWithProperties(path, appConfigFileName, false);
        globalProps.putAll(appProps);

        Properties testProps = loadFileWithProperties(path, testConfigFileName, false);
        globalProps.putAll(testProps);

        return normalize(globalProps);

    }

    /**
     * Normalizes the given global properties by filtering and processing them based
     * on the predefined set of property names and an optional value processor.
     *
     * 
     * <p>
     * This method performs the following steps:
     * </p>
     * <ul>
     * <li>Retrieves the set of property names defined in the enumeration of the
     * property class.</li>
     * <li>Iterates through the entries in the provided global properties.</li>
     * <li>Ignores properties that are not registered in the predefined set of
     * property names.</li>
     * <li>Trims and optionally processes the property values using the provided
     * value processor.</li>
     * <li>Adds the valid and processed properties to the resulting
     * {@link Properties} object.</li>
     * </ul>
     *
     * @param globalProps The global properties to be normalized. This is a
     *                    {@link Properties} object containing key-value pairs of
     *                    property names and their corresponding values.
     * @return A {@link Properties} object containing the normalized properties that
     *         are valid and processed. Properties not registered in the predefined
     *         set are ignored.
     * @throws NullPointerException If {@code globalProps} is null.
     */
    private Properties normalize(Properties globalProps) {

        Properties result = new Properties();
        HashSet<String> propsNameInFile = new HashSet<>();

        T[] propertyEnums = propertyClass.getEnumConstants();
        for (T propertyEnum : propertyEnums) {
            propsNameInFile.add(((EnvConfigProperty) propertyEnum).getNameInFile());
        }

        Set<Entry<Object, Object>> entrySet = globalProps.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (!propsNameInFile.contains(key)) {
                log.error("Property with the name {} is not registered as AhaProperty. It will be ignored.", key);
                continue;
            }

            value = StringUtils.trimToEmpty(value);
            if (valueProcessor != null) {
                value = valueProcessor.processValue(key, value);
            }
            if (log.isDebugEnabled()) {
                log.debug(" adding property {} = {}", key, value);
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * Loads a properties file from the specified path and returns a
     * {@link Properties} object.
     *
     * @param path      The path where the properties file is located. If the value
     *                  is "classpath", the file will be loaded from the classpath.
     * @param fileName  The name of the properties file to load.
     * @param mustExist A flag indicating whether the file must exist. If true and
     *                  the file is not found, a {@link FileNotFoundException} will
     *                  be thrown.
     * @return A {@link Properties} object containing the properties loaded from the
     *         file. If the file does not exist and {@code mustExist} is false, an
     *         empty {@link Properties} object is returned.
     * @throws RuntimeException      If an error occurs while loading the properties
     *                               file. This includes cases where the file is not
     *                               found and {@code mustExist} is true.
     * @throws FileNotFoundException If the file is not found and {@code mustExist}
     *                               is true.
     */
    private Properties loadFileWithProperties(String path, String fileName, boolean mustExist) {
        Properties props = new Properties();

        InputStream istream = null;
        try {
            log.warn("Looking for path:{} and file:{}", path, fileName);
            if (log.isDebugEnabled()) {
                log.debug(
                        "YOU CAN IGNORE THIS EXCEPTION: the purpose of this exception is to trace "
                        + "invocation points of AhaConfigFactory",
                        new RuntimeException("AhaConfigFactory invocation stack trace."));
            }
            String propertiesPath = null;

            if ("classpath".equalsIgnoreCase(path)) {
                propertiesPath = fileName;
                istream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
                if (istream == null) {
                    if (mustExist) {
                        log.error("Cannot find file with configuration properties in the classpath:{}", fileName);
                        throw new FileNotFoundException(fileName);
                    }
                    return props;
                }
            } else {
                File file = new File(path + fileSeparator + fileName);
                propertiesPath = file.toString();
                if (!file.exists()) {
                    if (mustExist) {
                        log.error("Cannot find file with configuration properties:{}", propertiesPath);
                        throw new FileNotFoundException(file.toString());
                    }
                    return props;
                }
                istream = new FileInputStream(file);
            }
            log.warn("FILE FOUND loading: {}", propertiesPath);
            props.load(istream);
            return props;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (Exception ignore) {
                    //do nothing
                }
            }
        }
    }
}
