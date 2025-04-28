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
    public static final String CLASSPATH = "classpath";
    public static final EnvConfigLocation INSTANCE = new EnvConfigLocation();

    private String path = CLASSPATH;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
