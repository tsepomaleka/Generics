package org.liquidsql.model.column;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class ColumnInformationImpl implements ColumnInformation, Serializable
{
    private String tableName;
    
    private String columnName;
    private Class columnType;
    
    private boolean primaryKey;
    private int columnIndex;
    private boolean distinct;

    private Field field;
    private Method modifierMethod;
    private Method accessorMethod;
    private Class entityClass;
    private Collection<Annotation> annotationCollection;
    
    private ColumnInformationImpl() 
    {
        this.primaryKey = false;
        this.distinct = false;
        this.columnIndex = -1;
    }
    
    public ColumnInformationImpl(String tableName) 
    {
        this();
        this.tableName = tableName;
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
    
    @Override
    public String getColumnNameAs()
    {
        return "this__" + tableName.toLowerCase() + "_" + columnName.toLowerCase() + "_";
    }
    
    @Override
    public String getFullColumnName()
    {
        return this.tableName + "." + this.columnName;
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

    @Override
    public boolean isPrimaryKey() 
    {
        return primaryKey;
    }

    @Override
    public void setPrimaryKey(boolean primaryKey) 
    {
        this.primaryKey = primaryKey;
    }


    @Override
    public String getTableName()
    {
        return tableName;
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
    public boolean isDistinct()
    {
        return distinct;
    }

    @Override
    public void setDistinct(boolean distinct)
    {
        this.distinct = distinct;
    }

    @Override
    public Field getField()
    {
        return field;
    }

    @Override
    public void setField(Field field)
    {
        this.field = field;
    }

    @Override
    public Method getModifierMethod()
    {
        return modifierMethod;
    }

    @Override
    public void setModifierMethod(Method modifierMethod)
    {
        this.modifierMethod = modifierMethod;
    }

    @Override
    public Method getAccessorMethod()
    {
        return accessorMethod;
    }

    @Override
    public void setAccessorMethod(Method accessorMethod)
    {
        this.accessorMethod = accessorMethod;
    }

    /**
     * Determines if the property has the annotation
     *
     * @param annotationClass
     * @return
     */
    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass)
    {
        if (annotationCollection == null || annotationCollection.isEmpty())
            return false;
        else
        {
            for (Annotation annotation : annotationCollection)
            {
                if (annotation.getClass() == annotationClass)
                    return true;
            }
        }

        return false;
    }

    /**
     * Adds an annotation
     *
     * @param annotation
     */
    @Override
    public void addAnnotation(Annotation annotation)
    {
        if (annotationCollection == null)
            annotationCollection = new ArrayList<>();

        if (annotationCollection.contains(annotation) == false)
            annotationCollection.add(annotation);
    }

    /**
     * Gets an annotation
     *
     * @param annotationClass
     * @return Annotation
     */
    @Override
    public Annotation getAnnotation(Class annotationClass)
    {
        if (annotationCollection == null || annotationCollection.isEmpty())
            return null;

        for (Annotation annotation : annotationCollection)
        {
            if (annotation.getClass() == annotationClass)
                return annotation;
        }

        return null;
    }

    /**
     * Determines if this property is assignable
     * from a collection
     *
     * @return boolean
     */
    @Override
    public boolean isPropertyCollection()
    {
        return (Collection.class.isAssignableFrom(field.getType()));
    }

    public Class getEntityClass()
    {
        return entityClass;
    }

    public void setEntityClass(Class entityClass)
    {
        this.entityClass = entityClass;
    }
}
