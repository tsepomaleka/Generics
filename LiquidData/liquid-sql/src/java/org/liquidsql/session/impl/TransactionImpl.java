package org.liquidsql.session.impl;

import org.liquidsql.session.Transaction;
import org.liquidsql.util.LiquidException;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionImpl implements Transaction
{
    private final Connection connection;
    private boolean begun = false;

    /**
     * Creates an instance of the session transaction
     * @param connection
     */
    public TransactionImpl(Connection connection)
    {
        this.connection = connection;
        checkConnection();

        this.begin();
    }

    /**
     * Pre checks the JDBC connection
     * @throws LiquidException
     */
    private void checkConnection() throws LiquidException
    {
        if (connection == null)
            throw new LiquidException("No connection was established or provided.");
    }

    /**
     * Begins the transaction
     */
    @Override
    public final void begin()
    {
        try
        {
            if (!begun)
            {
                connection.setAutoCommit(false);
                begun = true;
            }
        }
        catch (SQLException e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Commits the transaction
     */
    @Override
    public final void commit() throws LiquidException
    {
        try
        {
            connection.commit();
        }
        catch (SQLException e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Rolls back the transaction
     */
    @Override
    public final void rollback()
    {
        try
        {
            connection.rollback();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
    }
}
