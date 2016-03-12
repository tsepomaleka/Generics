package org.liquid.model.domain;

import java.io.Serializable;

/**
 * Table column information model.
 * 
 */
public class ColumnInformation implements Serializable
{
    private String tableName;
    private Class entityClass;
    
    private String columnName;
    private String fieldName;
    private Class columnType;
    
    private boolean primaryKey;
    
    private ColumnInformation() 
    {
        this.primaryKey = false;
    }
    
    public ColumnInformation(String tableName) 
    {
        this();
        this.tableName = tableName;
    }

    public String getColumnName() 
    {
        return columnName;
    }

    public void setColumnName(String columnName) 
    {
        this.columnName = columnName;
    }
    
    public String getColumnNameAs()
    {
        return "this__" + tableName.toLowerCase() + "_" + columnName.toLowerCase() + "_";
    }
    
    public String getFullColumnName()
    {
        return this.tableName + "." + this.columnName;
    }

    public Class getColumnType() 
    {
        return columnType;
    }

    public void setColumnType(Class columnType) 
    {
        this.columnType = columnType;
    }

    public boolean isPrimaryKey() 
    {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) 
    {
        this.primaryKey = primaryKey;
    }

    public String getFieldName() 
    {
        return fieldName;
    }

    public void setFieldName(String fieldName) 
    {
        this.fieldName = fieldName;
    }

    public Class getEntityClass() 
    {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) 
    {
        this.entityClass = entityClass;
    }

    public String getTableName() {
        return tableName;
    }

}
