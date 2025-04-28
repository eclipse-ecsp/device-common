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
 * The {@code EnvConfigPropertyValueProcessor} interface defines a contract for processing
 * configuration property values. Implementations of this interface are responsible for
 * transforming or modifying the value of a configuration property based on its key and
 * original value.
 *
 * <p>This interface can be used in scenarios where environment-specific or dynamic
 * configuration adjustments are required.
 *
 * <p>Example use cases include:
 * <ul>
 *   <li>Decrypting encrypted configuration values.</li>
 *   <li>Resolving placeholders in configuration values.</li>
 *   <li>Applying custom transformations to configuration values.</li>
 * </ul>
 *
 * @see java.util.Properties
 */
public interface EnvConfigPropertyValueProcessor {
    String processValue(String key, String value);
}
