package org.liquidsql.model.aggregate;

import org.liquidsql.model.enumeration.ColumnAggregateType;

public class ColumnAggregate
{
    private final ColumnAggregateType columnAggregateType;
    private final String propertyPath;

    /**
     * Private constructor
     *
     * @param propertyPath
     * @param columnAggregateType
     */
    private ColumnAggregate(String propertyPath, ColumnAggregateType columnAggregateType)
    {
        this.propertyPath = propertyPath;
        this.columnAggregateType = columnAggregateType;
    }

    /**
     * Gets the column aggregate type
     * @return
     */
    public ColumnAggregateType getColumnAggregateType()
    {
        return this.columnAggregateType;
    }

    /**
     * Gets property path
     * @return
     */
    public String getPropertyPath()
    {
        return this.propertyPath;
    }

    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getColumnAggregateType().getFunctionName())
                .append("(${").append(propertyPath).append("})");

        return buffer.toString();
    }

    /**
     * Creates an instance of the AVERAGE column aggregate
     * @param propertyPath The property path
     *
     * @return ColumnAggregate
     */
    public static ColumnAggregate average(String propertyPath)
    {
        ColumnAggregate columnAggregate = new ColumnAggregate(propertyPath, ColumnAggregateType.AVERAGE);
        return columnAggregate;
    }

    /**
     * Creates an instance of the COUNT column aggregate
     * @param propertyPath The property path
     *
     * @return ColumnAggregate
     */
    public static ColumnAggregate count(String propertyPath)
    {
        ColumnAggregate columnAggregate = new ColumnAggregate(propertyPath, ColumnAggregateType.COUNT);
        return columnAggregate;
    }

    /**
     * Creates an instance of the FIRST column aggregate
     * @param propertyPath The property path
     *
     * @return ColumnAggregate
     */
    public static ColumnAggregate first(String propertyPath)
    {
        ColumnAggregate columnAggregate = new ColumnAggregate(propertyPath, ColumnAggregateType.FIRST);
        return columnAggregate;
    }

    /**
     * Creates an instance of the LAST column aggregate
     * @param propertyPath The property path
     *
     * @return ColumnAggregate
     */
    public static ColumnAggregate last(String propertyPath)
    {
        ColumnAggregate columnAggregate = new ColumnAggregate(propertyPath, ColumnAggregateType.LAST);
        return columnAggregate;
    }

    /**
     * Creates an instance of the MIN column aggregate
     * @param propertyPath The property path
     *
     * @return ColumnAggregate
     */
    public static ColumnAggregate minimum(String propertyPath)
    {
        ColumnAggregate columnAggregate = new ColumnAggregate(propertyPath, ColumnAggregateType.MINIMUM);
        return columnAggregate;
    }

    /**
     * Creates an instance of the MAX column aggregate
     * @param propertyPath The property path
     *
     * @return ColumnAggregate
     */
    public static ColumnAggregate maximum(String propertyPath)
    {
        ColumnAggregate columnAggregate = new ColumnAggregate(propertyPath, ColumnAggregateType.MAXIMUM);
        return columnAggregate;
    }

    /**
     * Creates an instance of the SUM column aggregate
     * @param propertyPath The property path
     *
     * @return ColumnAggregate
     */
    public static ColumnAggregate sum(String propertyPath)
    {
        ColumnAggregate columnAggregate = new ColumnAggregate(propertyPath, ColumnAggregateType.SUM);
        return columnAggregate;
    }
}
