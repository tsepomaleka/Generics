package org.liquidsql.model.aggregate;

public class AggregateColumnInformationImpl implements AggregateColumnInformation
{
    private int columnIndex;
    private String columnName;
    private Class columnType;
    private boolean distinct;

    private final String tableName;
    private final ColumnAggregate columnAggregate;

    /**
     * Default constructor
     *
     * @param columnAggregate
     */
    public AggregateColumnInformationImpl(ColumnAggregate columnAggregate, String tableName)
    {
        this.columnAggregate = columnAggregate;
        this.tableName = tableName;
    }

    @Override
    public int getColumnIndex()
    {
        return columnIndex;
    }

    @Override
    public void setColumnIndex(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    @Override
    public String getColumnName()
    {
        return columnName;
    }

    @Override
    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    /**
     * Gets the name of the column alias
     * to be set as during a SELECT query.
     *
     * @return String
     */
    @Override
    public String getColumnNameAs()
    {
        return "this_" + columnAggregate.getColumnAggregateType().getFunctionName().toLowerCase() +
                "__" + tableName + "__" + columnName + "_";
    }

    /**
     * Gets a full column name by prepending the table name
     * to the column name.
     *
     * @return String
     */
    @Override
    public String getFullColumnName()
    {
        return columnAggregate.getColumnAggregateType().getFunctionName() +
                "(" + tableName + "." + columnName + ")";
    }

    @Override
    public Class getColumnType()
    {
        return columnType;
    }

    @Override
    public void setColumnType(Class columnType)
    {
        this.columnType = columnType;
    }

    public String getTableName()
    {
        return tableName;
    }

    @Override
    public ColumnAggregate getColumnAggregate()
    {
        return columnAggregate;
    }

    @Override
    public boolean isDistinct()
    {
        return distinct;
    }

    @Override
    public void setDistinct(boolean distinct)
    {
        this.distinct = distinct;
    }
}

