package org.eclipse.ecsp.common.config;

/**
 * Represents a configuration property that can be defined in an environment configuration file.
 * This interface provides methods to retrieve the name of the property as it appears in the file,
 * its default value, and its type.
 */
public interface EnvConfigProperty {
    String getNameInFile();

    String getDefaultValue();

    EnvConfigPropertyType getType();
}
