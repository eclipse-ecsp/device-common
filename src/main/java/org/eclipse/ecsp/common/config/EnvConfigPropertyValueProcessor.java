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
