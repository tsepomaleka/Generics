package org.liquidsql.config;

import org.liquidsql.session.LiquidSession;
import org.liquidsql.session.impl.LiquidSessionImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class LiquidSessionBuilder implements LiquidConfigConstants
{
    private static LiquidConfiguration configuration = null;

    static
    {
        /*
            load configuration from the XML file in the
            classpath
        */
        ConfigXmlReader configXmlReader = new ConfigXmlReader();
        configuration = configXmlReader.readConfigurationFromXmlFile();

        // find the JDBC class driver class and offload
        offloadJdbcDriverClass();

    }

    /**
     * Offloads the JDBC driver class from the configuration
     */
    private static void offloadJdbcDriverClass()
    {
        try
        {
            Class.forName(configuration.getConfigurationProperty(DRIVER_CLASS_NAME));
        }
        catch (ClassNotFoundException ex)
        {
            ex.printStackTrace(System.out);
        }
    }

    /**
     * Builds a new Liquid session
     *
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static LiquidSession buildSession() throws ClassNotFoundException, SQLException
    {
        Connection connection = getConnection();
        connection.setAutoCommit(true);

        LiquidSession session = new LiquidSessionImpl(connection);
        return session;
    }

    /**
     * Gets the JDBC connection
     * @return Connection
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static Connection getConnection() throws ClassNotFoundException, SQLException
    {
        Connection connection = DriverManager.getConnection(
                configuration.getConfigurationProperty(CONNECTION_URL),
                configuration.getConfigurationProperty(CONNECTION_USERNAME),
                configuration.getConfigurationProperty(CONNECTION_PASSWORD));

        return connection;
    }
}
