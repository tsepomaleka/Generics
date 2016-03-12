package org.liquidsql.session;

public interface ConnectionManaged
{
    /**
     * Closes the current session of the JDBC connection
     */
    public void close();
}
