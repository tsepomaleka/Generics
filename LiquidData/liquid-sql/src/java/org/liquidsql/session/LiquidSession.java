package org.liquidsql.session;

import org.liquidsql.sql.LiquidAggregateFetch;
import org.liquidsql.sql.LiquidFetch;
import org.liquidsql.util.LiquidException;

public interface LiquidSession extends ConnectionManaged
{
    /**
     * Begins a JDBC connection transaction
     * @return
     */
    public Transaction beginTransaction();

    /**
     * Creates a new alias from the entity class
     * with the specified alias name.
     *
     * @param entityClass The persistent entity class
     * @param alias The unique alias name
     *
     * @return LiquidSimpleFetch
     * @throws LiquidException
     */
    public LiquidFetch createAlias(Class entityClass, String alias) throws LiquidException;

    /**
     * Creates a new alias from the entity class
     * with the specified alias name for an
     * aggregated fetch.
     *
     * @param entityClass The persistent entity class
     * @param alias The unique alias name
     * @return
     * @throws LiquidException
     */
    public LiquidAggregateFetch createAggregateAlias(Class entityClass, String alias) throws LiquidException;

    /**
     * Updates the object
     *
     * @param object
     * @return LiquidUpdate
     * @throws LiquidException
     */
    public boolean updateObject(Object object) throws LiquidException;

    /**
     * Updates an intermediate relationship
     * within the specified object containing
     * the specified property name.
     *
     * @param object
     * @param propertyName
     * @return
     * @throws LiquidException
     */
    public boolean updateIntermediateRelationship(Object object, String propertyName) throws LiquidException;

    /**
     * Saves the object
     *
     * @param object
     * @return
     * @throws LiquidException
     */
    public boolean saveObject(Object object) throws LiquidException;

    /**
     * Saves the intermediate relationship
     * within the specified object containing
     * the specified property name.
     *
     * @param object
     * @param propertyName
     * @return
     * @throws LiquidException
     */
    public boolean saveIntermediateRelationship(Object object, String propertyName) throws LiquidException;
}
