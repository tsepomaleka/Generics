package org.liquidsql.sql;

import org.liquidsql.model.condition.Condition;
import org.liquidsql.model.order.Order;
import org.liquidsql.util.LiquidException;

import java.util.Collection;

public interface LiquidFetch
{
    /**
     * Creates a new alias from the entity class
     * with the specified alias name.
     *
     * @param entityClass The persistent entity class
     * @param alias       The unique alias name
     *
     * @return LiquidSimpleFetch
     * @throws LiquidException
     */
    public LiquidFetch createAlias(Class entityClass, String alias) throws LiquidException;

    /**
     * Sets the specified property field as distinct.
     *
     * @param propertyPath
     * @return
     * @throws LiquidException
     */
    public LiquidFetch distinct(String propertyPath) throws LiquidException;

    /**
     * Creates a join from the parent alias
     *
     * @param propertyPath The property path
     * @param alias The unique alias name
     *
     * @return LiquidFetch
     * @throws LiquidException
     */
    public LiquidFetch addJoin(String propertyPath, String alias) throws LiquidException;

    /**
     * Creates an intermediate join from the parent alias
     *
     * @param propertyPath The property path
     * @param alias The unique alias name (to identify a single instance in a collection)
     *
     * @return LiquidFetch
     * @throws LiquidException
     */
    public LiquidFetch addIntermediateJoin(String propertyPath, String alias) throws LiquidException;

    /**
     * Sets the condition
     *
     * @param condition
     * @return
     * @throws LiquidException
     */
    public LiquidFetch addCondition(Condition condition) throws LiquidException;

    /**
     * Adds a property path to the group by section
     * @param propertyPath
     * @return
     * @throws LiquidException
     */
    public LiquidFetch addGroupBy(String propertyPath) throws LiquidException;

    /**
     * Adds an order to the result set
     * @param order
     * @return
     */
    public LiquidFetch addOrder(Order order);

    /**
     * Sets the offset of the result set
     * @param offset
     * @return
     */
    public LiquidFetch setOffset(int offset);

    /**
     * Sets the limit count of the result set
     * @param limit
     * @return
     */
    public LiquidFetch setLimit(int limit);

    /**
     * Executes a data fetch
     *
     * @return
     * @throws LiquidException
     */
    public Collection executeAndFetch() throws LiquidException;
}
