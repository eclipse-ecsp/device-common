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
 * Represents the types of environment configuration properties.
 * This enum is used to categorize configuration properties based on their
 * accessibility and security requirements.
 * 
 * <ul>
 *   <li><b>PUBLIC</b>: Indicates that the configuration property is publicly accessible.</li>
 *   <li><b>SECURED</b>: Indicates that the configuration property is secured and requires restricted access.</li>
 * </ul>
 */
public enum EnvConfigPropertyType {
    PUBLIC, SECURED
}
