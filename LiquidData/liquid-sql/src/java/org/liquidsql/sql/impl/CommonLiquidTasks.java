package org.liquidsql.sql.impl;

import org.liquidsql.model.annotation.*;
import org.liquidsql.model.column.ColumnInformation;
import org.liquidsql.model.condition.Condition;
import org.liquidsql.model.enumeration.TemporalType;
import org.liquidsql.util.LiquidException;
import org.liquidsql.util.PairedValue;
import org.liquidsql.util.reflections.ReflectionsUtil;
import org.liquidsql.util.scanner.DefaultEntityScanner;
import org.liquidsql.util.scanner.EntityScanner;
import org.liquidsql.util.scanner.EntityScannerException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public abstract class CommonLiquidTasks
{
    // the JDBC connection
    protected final Connection connection;

    /**
     * Protected constructor
     * @param connection
     */
    protected CommonLiquidTasks(Connection connection)
    {
        checkConnection(connection);
        this.connection = connection;

        Condition.resetParameterValueMap();
    }

    /**
     * Checks JDBC connection for nullity
     * @param connection
     */
    protected final void checkConnection(Connection connection)
    {
        if (connection == null)
            throw new LiquidException("The connection is not provided or not available.");
    }

    /**
     * Checks for validations in the root entity class.
     *
     * @param rootEntityClass
     * @throws EntityScannerException
     */
    protected final void checkRootEntityClass(Class rootEntityClass) throws EntityScannerException
    {
        // check if the root entity class has the persistent
        // annotation
        if (!rootEntityClass.isAnnotationPresent(Entity.class))
        {
            throw new EntityScannerException("The entity is not annotated as being a persistent object.");
        }

        // check if the root entity class has the table annotation
        if (!rootEntityClass.isAnnotationPresent(Table.class) && !rootEntityClass.isAnnotationPresent(MappedSuperClass.class))
        {
            throw new EntityScannerException("The entity is does not have the Table annotation present.");
        }
    }

    /**
     * Strips the alias name from the property path.
     * @param propertyPath
     * @return String
     */
    protected final String stripAliasNameFromPropertyPath(String propertyPath)
    {
        if (!propertyPath.contains("."))
            return propertyPath;
        else
        {
            String[] parts = propertyPath.split("\\.");
            int length = parts.length;
            return propertyPath.substring(0, propertyPath.indexOf(parts[length - 1]) - 1);
        }
    }

    /**
     * Strips the field name from the property path.
     * @param propertyPath
     * @return
     * @throws EntityScannerException
     */
    protected final String stripFieldNameFromPropertyPath(String propertyPath) throws EntityScannerException
    {
        if (!propertyPath.contains("."))
            throw new EntityScannerException("The property path does not contain any field"
                    + " names as contained in the entity class.");
        else
        {
            String[] parts = propertyPath.split("\\.");
            int length = parts.length;
            return (parts[length - 1]);
        }
    }


    /**
     * Moves the Id column to the beginning of the collection
     * @param collection
     */
    protected final void moveIdColumnToBeginning(Collection<ColumnInformation> collection)
    {
        boolean found = false;
        int indexOfColumnInfo = -1;

        for (ColumnInformation columnInformation : collection)
        {
            if (columnInformation.isPrimaryKey() &&
                    (columnInformation.hasAnnotation(Id.class)))
            {
                indexOfColumnInfo = ((ArrayList) collection)
                        .indexOf(columnInformation);
                found = true;

                break;
            }
        }

        if (found)
        {
            // move this primary key column information to the beginning
            // of the collection
            ColumnInformation primaryKeyColumnInfo = (ColumnInformation)((ArrayList) collection)
                    .remove(indexOfColumnInfo);
            ((ArrayList) collection).add(0, primaryKeyColumnInfo);
        }
    }

    /**
     * Gets the value from the result set.
     *
     * @param fullColumnName
     * @param field
     * @param rs
     * @return
     * @throws java.sql.SQLException
     */
    protected final Object getValueFromResultSet(String fullColumnName, Field field, ResultSet rs) throws SQLException, EntityScannerException
    {
        Class type = field.getType();

        if (type == Date.class)
        {
            TemporalType temporalType = null;

            if (field.isAnnotationPresent(Temporal.class))
            {
                Temporal temporal = (Temporal) field.getAnnotation(Temporal.class);
                temporalType = temporal.value();

                if (temporalType == TemporalType.DATE)
                    return rs.getDate(fullColumnName);
                else if (temporalType == TemporalType.TIME)
                    return rs.getTime(fullColumnName);
                else
                    return rs.getTimestamp(fullColumnName);
            }
            else
                throw new EntityScannerException("Date type fields must be annotated "
                        + "with @Temporal");
        }
        else
             return getValueFromResultSet(fullColumnName, type, rs);
    }

    /**
     * Gets the value from the result set
     * (Excludes date types)
     *
     * @param fullColumnName
     * @param type
     * @param rs
     * @return
     * @throws SQLException
     */
    protected final Object getValueFromResultSet(String fullColumnName, Class type, ResultSet rs) throws SQLException
    {

        if (type == Boolean.class || type == boolean.class)
            return rs.getBoolean(fullColumnName);

        else if (type == Integer.class || type == int.class)
            return rs.getInt(fullColumnName);

        else if (type == Long.class || type == long.class)
            return rs.getLong(fullColumnName);

        else if (type == Short.class || type == short.class)
            return rs.getShort(fullColumnName);

        else if (type == Double.class || type == double.class)
            return rs.getDouble(fullColumnName);

        else if (type == String.class)
            return rs.getString(fullColumnName);

        else if (type == BigDecimal.class)
            return rs.getBigDecimal(fullColumnName);

        return null;
    }

    /**
     * Updates the generated ID / keys after an insert.
     * @param object
     * @param generatedValue
     *
     * @throws EntityScannerException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    protected void updateGeneratedIdentities(final Object object, final Object generatedValue) throws EntityScannerException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        PairedValue pairedValue = ReflectionsUtil.getFieldAnnotatedWithId(object.getClass());
        Class declaringClass = (Class) pairedValue.getFirstValue();
        Field targetField = (Field) pairedValue.getSecondValue();
        Method method = declaringClass.getMethod(ReflectionsUtil.getModifierMethodName(targetField),
                new Class[] { targetField.getType() });
        method.invoke(object, new Object[] { generatedValue });
    }
}
