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
