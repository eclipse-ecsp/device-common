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
 * Represents a configuration property that can be defined in an environment configuration file.
 * This interface provides methods to retrieve the name of the property as it appears in the file,
 * its default value, and its type.
 */
public interface EnvConfigProperty {
    /**
     * Retrieves the name of the property as it appears in the configuration file.
     *
     * @return the name of the property in the configuration file.
     */
    String getNameInFile();


    /**
     * Retrieves the default value associated with this configuration property.
     *
     * @return the default value as a {@code String}, or {@code null} if no default value is set.
     */
    String getDefaultValue();

    /**
     * Retrieves the type of the environment configuration property.
     *
     * @return the {@link EnvConfigPropertyType} representing the type of the property.
     */
    EnvConfigPropertyType getType();
}
