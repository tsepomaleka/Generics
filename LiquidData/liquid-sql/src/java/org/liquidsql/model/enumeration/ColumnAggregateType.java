package org.liquidsql.model.enumeration;

import org.liquidsql.model.aggregate.ISQLFunctionType;

public enum ColumnAggregateType
{
    /*   Aggregate Functions   */

    AVERAGE("AVG", ISQLFunctionType.AGGREGATE),
    COUNT("COUNT", ISQLFunctionType.AGGREGATE),
    FIRST("FIRST", ISQLFunctionType.AGGREGATE),
    LAST("LAST", ISQLFunctionType.AGGREGATE),
    MAXIMUM("MAX", ISQLFunctionType.AGGREGATE),
    MINIMUM("MIN", ISQLFunctionType.AGGREGATE),
    SUM("SUM", ISQLFunctionType.AGGREGATE);

    private final String functionName;
    private final short functionType;

    private ColumnAggregateType(String functionName, short functionType)
    {
        this.functionName = functionName;
        this.functionType = functionType;
    }

    /**
     * Gets the SQL function name
     * @return
     */
    public String getFunctionName()
    {
        return this.functionName;
    }

    /**
     * Gets the SQL function type (aggregate or scalar)
     * @return
     */
    public short getFunctionType()
    {
        return this.functionType;
    }
}
