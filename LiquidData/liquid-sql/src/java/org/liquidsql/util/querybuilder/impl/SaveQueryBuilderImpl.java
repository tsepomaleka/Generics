package org.liquidsql.util.querybuilder.impl;

import org.liquidsql.model.annotation.Column;
import org.liquidsql.model.annotation.Id;
import org.liquidsql.model.annotation.JoinColumn;
import org.liquidsql.util.parameter.ParameterUtil;
import org.liquidsql.util.querybuilder.QueryBuilderException;
import org.liquidsql.util.querybuilder.SaveQueryBuilder;
import org.liquidsql.util.reflections.ReflectionsUtil;
import org.liquidsql.util.scanner.DefaultEntityScanner;
import org.liquidsql.util.scanner.EntityScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class SaveQueryBuilderImpl implements SaveQueryBuilder
{
    private Object entityObject;
    private final EntityScanner entityScanner;
    private final TreeMap<Integer, Object> parameterValueTreeMap;

    /**
     * Default constructor
     */
    public SaveQueryBuilderImpl()
    {
        this.entityObject = null;
        this.parameterValueTreeMap = new TreeMap<>();
        this.entityScanner = DefaultEntityScanner.getInstance();
    }

    /**
     * Sets the entity object to generate the
     * SQL from.
     *
     * @param entityObject
     */
    @Override
    public void setEntityObject(Object entityObject)
    {
        this.entityObject = entityObject;
    }

    @Override
    public TreeMap<Integer, Object> getParameterValueTreeMap()
    {
        return this.parameterValueTreeMap;
    }

    /**
     * Generate the SQL query
     *
     * @return
     * @throws org.liquidsql.util.querybuilder.QueryBuilderException
     */
    @Override
    public String generateQuery() throws QueryBuilderException
    {
        StringBuilder buffer = new StringBuilder();

        try
        {
            // get a complete field map for the entity
            Map<String, Field> fieldMap = entityScanner.getCompleteFieldMap(entityObject.getClass());

            // start building the insert query
            buffer.append("INSERT INTO ")
                    .append(entityScanner.getTableName(entityObject.getClass()))
                    .append("(");

            // iterate through the columns
            Iterator<Field> iterator = fieldMap.values().iterator();
            int currentCursor = 0;

            while (iterator.hasNext())
            {
                Field field = iterator.next();

                // if the field has the Column annotation present (exclude the Id)
                if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Id.class))
                {
                    // get the column name from annotation
                    Column column = (Column) field.getAnnotation(Column.class);
                    String columnName = column.name();

                    // get the value stored in the field
                    Method accessorMethod = entityObject.getClass().getMethod(getAccessorMethodName(field), new Class[] {});
                    Object value = accessorMethod.invoke(entityObject, new Object[] {});

                    // check if value if a temporal type
                    value = ParameterUtil.checkIfValueIsTemporal(field, value);

                    // add the value to the parameter map
                    currentCursor = nextCursor();
                    parameterValueTreeMap.put(currentCursor, value);

                    // append to buffer
                    if (iterator.hasNext())
                        buffer.append(columnName).append(", ");
                    else
                        buffer.append(columnName);
                }

                // if the field has a join column annotation
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

                    // append to buffer
                    if (iterator.hasNext())
                        buffer.append(columnName).append(", ");
                    else
                        buffer.append(columnName);
                }
            }

            // append the values to buffer
            buffer.append(")\n")
                    .append("VALUES(");

            for (int i = 0; i < parameterValueTreeMap.size(); i++)
            {
                if (i == (parameterValueTreeMap.size() - 1))
                    buffer.append("?");
                else
                    buffer.append("?, ");
            }

            buffer.append(")");

        }
        catch (Exception e)
        {
            throw new QueryBuilderException(e);
        }

        return buffer.toString();
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
}
