package org.liquid.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LiquidConfig implements LiquidConfigConstants
{
    private static LiquidConfig INSTANCE = null;
    
    private final Map<String, String> configMap;
    
    private LiquidConfig()
    {
        this.configMap = new HashMap<>(); 
        
        /* 
        addConfigurationProperty(DRIVER_CLASS_NAME, "org.postgresql.Driver");
        addConfigurationProperty(CONNECTION_URL, "jdbc:postgresql://localhost:5432/xplain2me");
        addConfigurationProperty(CONNECTION_USERNAME, "postgres");
        addConfigurationProperty(CONNECTION_PASSWORD, "8to0much@JB");
        */
        
        addConfigurationProperty(DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
        addConfigurationProperty(CONNECTION_URL, "jdbc:derby://localhost:1527/test_db");
        addConfigurationProperty(CONNECTION_USERNAME, "test_db");
        addConfigurationProperty(CONNECTION_PASSWORD, "test_db");
        
        try 
        {
            Class.forName(getConfigurationProperty(DRIVER_CLASS_NAME));
        } 
        catch (ClassNotFoundException ex) 
        {
            ex.printStackTrace(System.out);
        }
                
    }
    
    public static LiquidConfig getCurrentInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new LiquidConfig();
        }
        
        return INSTANCE;
    }
    
    private void addConfigurationProperty(String name, String value)
    {
        configMap.put(name, value);
    }
    
    private String getConfigurationProperty(String name)
    {
        return configMap.get(name);
    }
    
    public Connection getConnection() throws ClassNotFoundException, SQLException
    {
        Connection connection = DriverManager.getConnection(
                getConfigurationProperty(CONNECTION_URL), 
                getConfigurationProperty(CONNECTION_USERNAME), 
                getConfigurationProperty(CONNECTION_PASSWORD));
        
        connection.setAutoCommit(false);
        return connection;
    }
}
