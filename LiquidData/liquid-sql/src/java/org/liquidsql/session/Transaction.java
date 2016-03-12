package org.liquidsql.session;

import org.liquidsql.util.LiquidException;

public interface Transaction
{
    /**
     * Begins the transaction
     */
    public void begin() throws LiquidException;

    /**
     * Commits the transaction
     */
    public void commit();

    /**
     * Rolls back the transaction
     */
    public void rollback();
}
