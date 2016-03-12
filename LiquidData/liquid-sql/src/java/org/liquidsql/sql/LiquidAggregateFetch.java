package org.liquidsql.sql;

import org.liquidsql.model.aggregate.ColumnAggregate;
import org.liquidsql.model.condition.Condition;
import org.liquidsql.model.order.Order;
import org.liquidsql.util.LiquidException;

import java.util.Collection;
import java.util.Map;

public interface LiquidAggregateFetch
{
    /**
     * Creates a new alias from the entity class
     * with the specified alias name.
     *
     * @param entityClass The persistent entity class
     * @param alias       The unique alias name
     *
     * @return LiquidSimpleFetch
     * @throws org.liquidsql.util.LiquidException
     */
    public LiquidAggregateFetch createAggregateAlias(Class entityClass, String alias) throws LiquidException;

    /**
     * Adds a column aggregate
     *
     * @param columnAggregate
     * @return
     * @throws LiquidException
     */
    public LiquidAggregateFetch addAggregate(ColumnAggregate columnAggregate,  boolean distinct) throws LiquidException;

    /**
     * Creates a join from the parent alias
     *
     * @param propertyPath The property path
     * @param alias The unique alias name
     *
     * @return LiquidFetch
     * @throws LiquidException
     */
    public LiquidAggregateFetch addJoin(String propertyPath, String alias) throws LiquidException;

    /**
     * Creates an intermediate join from the parent alias
     *
     * @param propertyPath The property path
     * @param alias The unique alias name (to identify a single instance in a collection)
     *
     * @return LiquidFetch
     * @throws LiquidException
     */
    public LiquidAggregateFetch addIntermediateJoin(String propertyPath, String alias) throws LiquidException;

    /**
     * Sets the condition
     *
     * @param condition
     * @return
     * @throws LiquidException
     */
    public LiquidAggregateFetch addCondition(Condition condition) throws LiquidException;

    /**
     * Sets the offset of the result set
     * @param offset
     * @return
     */
    public LiquidAggregateFetch setOffset(int offset);

    /**
     * Sets the limit count of the result set
     * @param limit
     * @return
     */
    public LiquidAggregateFetch setLimit(int limit);

    /**
     * Executes a data fetch
     *
     * @return
     * @throws LiquidException
     */
    public Collection executeAggregateFetch() throws LiquidException;
}
