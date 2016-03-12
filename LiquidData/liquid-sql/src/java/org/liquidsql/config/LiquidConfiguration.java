package org.liquidsql.config;

import java.util.HashMap;
import java.util.Map;

public class LiquidConfiguration implements LiquidConfigConstants
{
    private Map<String, String> configMap;

    /**
     * Protected constructor
     */
    protected LiquidConfiguration()
    {
        this.configMap = new HashMap<>();
    }

    /**
     * Adds a configuration
     * @param name The configuration name
     * @param value The configuration value
     */
    protected void addConfigurationProperty(String name, String value)
    {
        if (!configMap.containsKey(name))
            configMap.put(name, value);
    }

    /**
     * Gets the value from the mapped configuration name
     * @param name The configuration name
     * @return String
     */
    protected String getConfigurationProperty(String name)
    {
        if (name == null || name.trim().isEmpty())
            return null;

        return configMap.get(name);
    }
}
