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

/**
 * The {@code EnvConfigLocation} class represents a configuration location
 * for environment-specific settings. It provides a default path and allows
 * customization of the path as needed.
 *
 * <p>This class includes a singleton instance {@link #INSTANCE} for global
 * access and a default path set to {@code "classpath"}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     EnvConfigLocation configLocation = EnvConfigLocation.INSTANCE;
 *     String currentPath = configLocation.getPath();
 *     configLocation.setPath("new/path");
 * </pre>
 *
 * <p>Thread Safety: This class is not thread-safe as the {@code setPath} method
 * modifies the internal state.</p>
 *
 * @author Akshay
 * @version 1.0
 */

public class EnvConfigLocation {
    /**
     * Constant representing the classpath location prefix.
     * This is typically used to specify resources that are located
     * within the application's classpath.
     */
    public static final String CLASSPATH = "classpath";


    /**
     * Singleton instance of the {@code EnvConfigLocation} class.
     * This instance provides a centralized access point for environment
     * configuration location settings.
     */
    public static final EnvConfigLocation INSTANCE = new EnvConfigLocation();

    private String path = CLASSPATH;

    /**
     * Retrieves the path associated with this configuration.
     *
     * @return the path as a {@code String}.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path for the configuration.
     *
     * @param path the path to be set
     */
    public void setPath(String path) {
        this.path = path;
    }
}
