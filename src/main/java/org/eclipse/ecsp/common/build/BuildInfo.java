package org.eclipse.ecsp.common.build;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The {@code BuildInfo} class is a component that provides build-related information
 * such as the source version, build version, and build timestamp. This information
 * is loaded from a properties file named {@code buildInfo.properties} located in the
 * classpath.
 *
 * <p>The class is designed as a singleton with a private constructor to prevent
 * instantiation. It uses a {@link Properties} object to store the loaded properties
 * and provides methods to retrieve specific build-related details.
 *
 * <p>If the {@code buildInfo.properties} file is not found or an error occurs while
 * loading it, appropriate warnings are logged, and default values are returned for
 * the requested properties.
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * BuildInfo buildInfo = new BuildInfo();
 * String sourcesVersion = buildInfo.getSourcesVersion();
 * String buildVersion = buildInfo.getBuildVersion();
 * String buildTimestamp = buildInfo.getBuildTimestamp();
 * }
 * </pre>
 *
 * <p>Note: This class requires the SLF4J logging framework for logging warnings.
 *
 * @author Akshay
 * @since 1.0
 */
@Component
public class BuildInfo {
    private static Logger log = LoggerFactory.getLogger(BuildInfo.class);
    private static final String NOT_DEFINED = "NOT DEFINED";

    private Properties properties = new Properties();

    /**
     * Private constructor for the BuildInfo class.
     * This constructor attempts to load the "buildInfo.properties" file from the classpath.
     * If the file is not found, a warning is logged.
     * If an error occurs while loading the file, a warning with the exception details is logged.
     *
     * <p>
     * Note: This constructor is private to prevent instantiation of the BuildInfo class.
     * </p>
     */
    private BuildInfo() {
        String filename = "buildInfo.properties";
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            log.warn("Build info property file cannot be found in CLASSPATH. Expected file name: " + filename);
            return;
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            log.warn("Errors while loading build info property file: " + filename, e);
        }
    }

    /**
     * Retrieves the version of the sources from the properties.
     *
     * @return The version of the sources as a {@code String}, or a default value
     *         if the "sources.version" property is not defined.
     */
    public String getSourcesVersion() {
        return properties.getProperty("sources.version", NOT_DEFINED);
    }

    /**
     * Retrieves the build version from the properties.
     *
     * @return The build version as a {@code String}. If the "build.version" property
     *         is not defined, it returns a default value indicating it is not defined.
     */
    public String getBuildVersion() {
        return properties.getProperty("build.version", NOT_DEFINED);
    }

    /**
     * Retrieves the build timestamp from the properties.
     *
     * @return The build timestamp as a {@code String} if defined, 
     *         or a default value indicating it is not defined.
     */
    public String getBuildTimestamp() {
        return properties.getProperty("build.timestamp", NOT_DEFINED);
    }
}
