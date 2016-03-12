package org.liquidsql.util.querybuilder.impl;

import org.liquidsql.model.annotation.Column;
import org.liquidsql.model.annotation.Id;
import org.liquidsql.model.annotation.JoinColumn;
import org.liquidsql.util.LiquidException;
import org.liquidsql.util.parameter.ParameterUtil;
import org.liquidsql.util.querybuilder.UpdateQueryBuilder;
import org.liquidsql.util.reflections.ReflectionsUtil;
import org.liquidsql.util.scanner.DefaultEntityScanner;
import org.liquidsql.util.scanner.EntityScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class UpdateQueryBuilderImpl implements UpdateQueryBuilder
{
    private Object entityObject;
    private final TreeMap<Integer, Object> parameterValueTreeMap;
    
    private final EntityScanner entityScanner;
    
    /**
     * Update SQL Builder implementation constructor
     */
    public UpdateQueryBuilderImpl()
    {
        this.entityObject = null;
        this.parameterValueTreeMap = new TreeMap<>();
        this.entityScanner = DefaultEntityScanner.getInstance();
    }

    @Override
    public void setEntityObject(Object entityObject) 
    {
        this.entityObject = entityObject;
    }

    @Override
    public String generateQuery()
    {
        try 
        {
            StringBuilder buffer = new StringBuilder();
            
            // get a complete field map for the entity
            Map<String, Field> fieldMap = entityScanner.getCompleteFieldMap(entityObject.getClass());
            
            // build an update SQL
            buffer.append("UPDATE ").append(
                    entityScanner.getTableName(entityObject.getClass())).append(" ")
                    .append("SET ");
            
            Iterator iterator = fieldMap.values().iterator();
            int currentCursor = 0;
            
             while (iterator.hasNext())
            {
                Field field = (Field) iterator.next();
                
                // check if field has a column field
                if (field.isAnnotationPresent(Column.class)
                        && !field.isAnnotationPresent(Id.class))
                {
                    
                    // get the column name
                    Column column = (Column) field.getAnnotation(Column.class);
                    String columnName = column.name();
                    
                    // get the value
                    Method accessorMethod = entityObject.getClass().getMethod(getAccessorMethodName(field), new Class[] {});
                    Object value = accessorMethod.invoke(entityObject, new Object[] {});
                    
                    // check if value if a temporal type
                    value = checkIfValueIsTemporal(field, value);
                    
                    // add the value to the parameter map
                    currentCursor = nextCursor();
                    parameterValueTreeMap.put(currentCursor, value);
                    
                    // append
                    if (iterator.hasNext())
                        buffer.append(columnName).append(" = ?,\n");
                    else
                        buffer.append(columnName).append(" = ?\n");       
                }
                
                // append the join column
                else if (field.isAnnotationPresent(JoinColumn.class))
                {
                    
                    // get the join column
                    JoinColumn column = (JoinColumn) field.getAnnotation(JoinColumn.class);
                    String columnName = column.columnName();
                    
                    // get the referenced target object
                    Method accessorMethod = entityObject.getClass().getMethod(getAccessorMethodName(field), new Class[] {});
                    Object referencedObject = accessorMethod.invoke(entityObject, new Object[] {});
                    
                    // get the field that is annotated with this column name
                    String referenceColumnName = column.referencedColumnName();
                    Field referencedField = ReflectionsUtil.getAssociatedFieldFromColumnName(referencedObject.getClass(), referenceColumnName);
                    // get the value from this referenced field
                    accessorMethod = referencedObject.getClass().getMethod(getAccessorMethodName(referencedField), new Class[] {});
                    Object value = accessorMethod.invoke(referencedObject, new Object[] {});
                    
                    // add the value to the parameter map
                    currentCursor = nextCursor();
                    parameterValueTreeMap.put(currentCursor, value);
                    
                    // append
                    if (iterator.hasNext())
                        buffer.append(columnName).append(" = ?,\n");
                    else
                        buffer.append(columnName).append(" = ?\n");   
                }
            }
            
            //obtain the unique identifier
            for (Field field : fieldMap.values())
            {
                if (field.isAnnotationPresent(Id.class) && 
                        field.isAnnotationPresent(Column.class))
                {
                    
                    // get the column name
                    Column column = (Column) field.getAnnotation(Column.class);
                    String columnName = column.name();
                    
                    // get the value
                    Method accessorMethod = entityObject.getClass().getMethod(getAccessorMethodName(field), new Class[] {});
                    Object value = accessorMethod.invoke(entityObject, new Object[] {});
                    
                    // add the value to the parameter map
                    currentCursor = nextCursor();
                    parameterValueTreeMap.put(currentCursor, value);
                    
                    // append
                    buffer.append("\nWHERE ").append(columnName).append(" = ?");
                    break;
                }
            }
            
            System.out.println(buffer.toString());
            
            return buffer.toString();
        } 
        catch (Exception ex) 
        {
            throw new LiquidException(ex);
        }
    }

    @Override
    public TreeMap<Integer, Object> getParameterValueTreeMap() 
    {
        return this.parameterValueTreeMap;
    }

    /**
     * Gets the name of the accessor method
     * for the specified field.
     * 
     * @param field
     * @return 
     */
    private String getAccessorMethodName(Field field) 
    {
        return ReflectionsUtil.getAccessorMethodName(field);
    }
    
    /**
     * Gets the next cursor value 
     * @return Integer
     */
    private int nextCursor()
    {
        int currentCapacity = this.parameterValueTreeMap.size();
        return (currentCapacity + 1);
    }
    
    /**
     * Check if values is temporal type
     * @param field
     * @param value
     * @return Object
     */
    private Object checkIfValueIsTemporal(Field field, Object value) 
    {
        return ParameterUtil.checkIfValueIsTemporal(field, value);
    }
}
