package org.eclipse.ecsp.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;


/**
 * The {@code EnvConfig} class provides a generic configuration management utility
 * for enumerated properties. It allows retrieval of property values from a 
 * {@link Properties} object, with support for default values, type conversion, 
 * and obfuscation of secured properties for display purposes.
 *
 * <p>
 * This class is designed to work with enums that implement the {@link EnvConfigProperty} 
 * interface, which defines methods for retrieving property metadata such as the 
 * property name in the configuration file and its default value.
 * </p>
 *
 * <p>
 * Key features of this class include:
 * <ul>
 *   <li>Retrieving property values as {@code String}, {@code Boolean}, {@code Long}, 
 *       or {@code Integer} types.</li>
 *   <li>Obfuscating secured property values for display purposes.</li>
 *   <li>Generating a map of property names and their corresponding display values.</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of the enum representing the configuration properties. 
 *            The enum must extend {@link Enum} and implement {@link EnvConfigProperty}.
 */
public class EnvConfig<T extends Enum<T>> {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(EnvConfig.class);

    private Class<T> enumClass;
    private Properties properties;
    private static final int TWO = 2;

    public EnvConfig(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    void setProperties(Properties globalProps) {
        this.properties = globalProps;
    }

    public T[] getPropertyEnums() {
        return enumClass.getEnumConstants();
    }

    /**
     * Retrieves the string value associated with the given property. If the
     * property is not found in the configuration file, the default value specified
     * in the property is returned.
     *
     * @param prop the property whose value is to be retrieved. It must be an
     *             instance of {@link EnvConfigProperty}.
     * @return the string value of the property, or its default value if not found.
     * @throws ClassCastException if the provided property is not of type
     *                            {@link EnvConfigProperty}.
     */
    public String getStringValue(T prop) {
        EnvConfigProperty property = (EnvConfigProperty) prop;
        String key = property.getNameInFile();
        String value = properties.getProperty(key);
        if (value == null) {
            value = property.getDefaultValue();
        }
        return value;
    }

    /**
     * Retrieves the boolean value of the specified property.
     *
     * <p>
     * This method first fetches the string representation of the property value
     * using {@code getStringValue(property)}. If the string value is {@code null},
     * this method returns {@code null}. Otherwise, it converts the string value to
     * a {@code Boolean} using {@code Boolean.valueOf(value)}.
     * </p>
     *
     * @param property the property whose boolean value is to be retrieved
     * @return the boolean value of the property, or {@code null} if the string
     *         value of the property is {@code null}
     */
    public Boolean getBooleanValue(T property) {
        String value = getStringValue(property);
        return (value == null) ? null : Boolean.valueOf(value);
    }

    /**
     * Retrieves the value of the specified property as a Long.
     *
     * <p>
     * This method first fetches the property value as a String and then attempts to
     * convert it to a Long. If the property value is null, this method returns
     * null.
     * </p>
     *
     * @param property the property whose value is to be retrieved
     * @return the Long representation of the property value, or null if the value
     *         is null
     * @throws NumberFormatException if the property value cannot be converted to a
     *                               Long
     */
    public Long getLongValue(T property) {
        String value = getStringValue(property);
        return (value == null) ? null : Long.valueOf(value);
    }

    /**
     * Retrieves the integer value of the specified property.
     *
     * <p>
     * This method first fetches the string representation of the property value
     * using {@code getStringValue(property)}. If the string value is {@code null},
     * the method returns {@code null}. Otherwise, it converts the string value to
     * an {@code Integer} and returns it.
     * </p>
     *
     * @param property the property whose integer value is to be retrieved
     * @return the integer value of the property, or {@code null} if the property
     *         value is not set or cannot be converted to an integer
     * @throws NumberFormatException if the string value of the property cannot be
     *                               parsed as an integer
     */
    public Integer getIntegerValue(T property) {
        String value = getStringValue(property);
        return (value == null) ? null : Integer.valueOf(value);
    }

    /**
     * Retrieves the displayable value for the given property. If the property type
     * is secured and the value is not null or empty, the method obfuscates the
     * value by replacing every alternate character with a '#' symbol. Otherwise, it
     * returns the original value.
     *
     * @param prop the property whose value is to be retrieved and potentially
     *             obfuscated
     * @return the obfuscated value for secured properties, or the original value
     *         for others
     * @throws ClassCastException if the provided property is not of type
     *                            {@link EnvConfigProperty}
     */
    public String getValueForDisplay(T prop) {
        String value = getStringValue(prop);
        EnvConfigProperty property = (EnvConfigProperty) prop;

        if (EnvConfigPropertyType.SECURED != property.getType() || value == null || value.isEmpty()) {
            return value;
        }

        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i = i + TWO) {
            chars[i] = '#';
        }
        return String.valueOf(chars);
    }

    /**
     * Retrieves a map of property names and their corresponding values for display
     * purposes. The method iterates through all property enums, determines their
     * display values, and includes them in the resulting map if they are defined or
     * present in the properties.
     *
     * @return A {@link Map} containing property names (as defined in the file) as
     *         keys and their corresponding display values as values. The map is
     *         sorted in natural order of the property names.
     */
    public Map<String, String> getValuesForDisplay() {
        TreeMap<String, String> effectiveProperties = new TreeMap<String, String>();
        T[] propertyEnums = getPropertyEnums();
        for (T propertyEnum : propertyEnums) {
            String valueForDisplay = getValueForDisplay(propertyEnum);
            String nameInFile = ((EnvConfigProperty) propertyEnum).getNameInFile();
            if (valueForDisplay == null && properties.containsKey(nameInFile) == false) {
                continue;
            }
            effectiveProperties.put(nameInFile, valueForDisplay);
        }
        return effectiveProperties;

    }

}
