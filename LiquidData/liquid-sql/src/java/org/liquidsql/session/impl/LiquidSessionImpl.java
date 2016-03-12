package org.liquidsql.session.impl;


import org.liquidsql.session.LiquidSession;
import org.liquidsql.session.Transaction;
import org.liquidsql.sql.LiquidAggregateFetch;
import org.liquidsql.sql.LiquidFetch;
import org.liquidsql.sql.LiquidSave;
import org.liquidsql.sql.LiquidUpdate;
import org.liquidsql.sql.impl.LiquidAggregateFetchImpl;
import org.liquidsql.sql.impl.LiquidFetchImpl;
import org.liquidsql.sql.impl.LiquidSaveImpl;
import org.liquidsql.sql.impl.LiquidUpdateImpl;
import org.liquidsql.util.LiquidException;

import java.sql.Connection;

public class LiquidSessionImpl implements LiquidSession
{
    private final Connection connection;
    private Transaction transaction;

    /**
     * Creates an instance of the liquid session
     * implementation
     *
     * @param connection
     */
    public LiquidSessionImpl(Connection connection)
    {
        this.connection = connection;
        this.transaction = null;
    }

    /**
     * Begins a JDBC connection transaction
     * @return Transaction
     */
    @Override
    public Transaction beginTransaction()
    {
        transaction = new TransactionImpl(this.connection);
        return transaction;
    }

    /**
     * Creates a new alias from the entity class
     * with the specified alias name.
     *
     * @param entityClass The persistent entity class
     * @param alias       The unique alias name
     * @return LiquidSimpleFetch
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidFetch createAlias(Class entityClass, String alias) throws LiquidException
    {
        LiquidFetch fetch = new LiquidFetchImpl(this.connection);
        return fetch.createAlias(entityClass, alias);
    }

    /**
     * Creates a new alias from the entity class
     * with the specified alias name for an
     * aggregated fetch.
     *
     * @param entityClass The persistent entity class
     * @param alias       The unique alias name
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidAggregateFetch createAggregateAlias(Class entityClass, String alias) throws LiquidException
    {
        LiquidAggregateFetch aggregateFetch = new LiquidAggregateFetchImpl(this.connection);
        return aggregateFetch.createAggregateAlias(entityClass, alias);
    }

    /**
     * Updates the object
     *
     * @param object
     * @return LiquidUpdate
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public boolean updateObject(Object object) throws LiquidException
    {
        LiquidUpdate update = new LiquidUpdateImpl(this.connection);
        return update.updateObject(object);
    }

    /**
     * Updates an intermediate relationship
     * within the specified object containing
     * the specified property name.
     *
     * @param object
     * @param propertyName
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public boolean updateIntermediateRelationship(Object object, String propertyName) throws LiquidException
    {
        LiquidUpdate update = new LiquidUpdateImpl(this.connection);
        return update.updateIntermediateRelationship(object, propertyName);
    }

    /**
     * Saves the object
     *
     * @param object
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public boolean saveObject(Object object) throws LiquidException
    {
        LiquidSave save = new LiquidSaveImpl(this.connection);
        return save.saveObject(object);
    }

    /**
     * Saves the intermediate relationship
     * within the specified object containing
     * the specified property name.
     *
     * @param object
     * @param propertyName
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public boolean saveIntermediateRelationship(Object object, String propertyName) throws LiquidException
    {
        LiquidSave save = new LiquidSaveImpl(this.connection);
        return save.saveIntermediateRelationship(object, propertyName);
    }

    /**
     * Closes the current session of the JDBC connection
     */
    @Override
    public void close()
    {
         try
         {
             if (this.connection != null &&
                     this.connection.isClosed() == false)
                 connection.close();
         }
         catch (Exception e)
         {
             e.printStackTrace(System.out);
         }
    }
}
