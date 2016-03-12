package org.liquidsql.model.aggregate;

public interface AggregateColumnInformation
{
    /**
     * Gets the table name
     * @return
     */
    public String getTableName();
    /**
     * Gets the column index
     * @return Integer
     */
    public int getColumnIndex();

    /**
     * Sets the column index
     * @param columnIndex The column index
     */
    public void setColumnIndex(int columnIndex);

    /**
     * Gets the column name
     * @return String
     */
    public String getColumnName();
    /**
     * Sets the column name
     * @param columnName The name of the column as in the database table column.
     */
    public void setColumnName(String columnName);

    /**
     * Gets the name of the column alias
     * to be set as during a SELECT query.
     * @return String
     */
    public String getColumnNameAs();
    /**
     * Gets a full column name by prepending the table name
     * to the column name.
     * @return String
     */
    public String getFullColumnName();

    /**
     * Gets the data (or class) type of the column
     * @return Class
     */
    public Class getColumnType();
    /**
     * Sets the data (or class) type of the column
     * @param columnType The data (or class) type of the column
     */
    public void setColumnType(Class columnType);

    /**
     * Gets the column aggregate
     * @return ColumnAggregate
     */
    public ColumnAggregate getColumnAggregate();

    /**
     * Determines if this column is marked as distinct
     * @return
     */
    public boolean isDistinct();

    /**
     * Sets the column to be distinct during selection
     * @param distinct
     */
    public void setDistinct(boolean distinct);


}
