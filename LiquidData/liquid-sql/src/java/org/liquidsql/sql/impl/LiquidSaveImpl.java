package org.liquidsql.sql.impl;

import org.liquidsql.model.annotation.JoinTable;
import org.liquidsql.util.LiquidException;
import org.liquidsql.util.parameter.ParameterUtil;
import org.liquidsql.util.querybuilder.SaveQueryBuilder;
import org.liquidsql.util.querybuilder.impl.SaveQueryBuilderImpl;
import org.liquidsql.util.reflections.ReflectionsUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

public class LiquidSaveImpl extends CommonLiquidTasks implements org.liquidsql.sql.LiquidSave
{
    /**
     * Protected constructor
     *
     * @param connection
     */
    public LiquidSaveImpl(Connection connection)
    {
        super(connection);
    }

    /**
     * Saves the specified object
     *
     * @param object
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public boolean saveObject(Object object) throws LiquidException
    {
        try
        {
            // check the entity first
            checkRootEntityClass(object.getClass());

            // prepare the SQL statement from builder
            SaveQueryBuilder builder = new SaveQueryBuilderImpl();
            builder.setEntityObject(object);

            // build the query
            String query = builder.generateQuery();

            // set the prepared statement
            PreparedStatement preparedStatement = connection.prepareStatement(query,
                    PreparedStatement.RETURN_GENERATED_KEYS);

            // insert the parameters
            for (Integer cursor : builder.getParameterValueTreeMap().keySet())
            {
                Object parameter = builder.getParameterValueTreeMap().get(cursor);
                ParameterUtil.addParameter(preparedStatement, cursor, parameter);
            }

            // execute the query
            int noOfAffectedRows = preparedStatement.executeUpdate();
            if (noOfAffectedRows > 0)
            {
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next())
                {
                    long generatedKey = resultSet.getLong(1);
                    updateGeneratedIdentities(object, generatedKey);
                }
            }

            // return the result
            return (noOfAffectedRows != 0);
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Saves an intermediate relationship
     *
     * @param object
     * @param propertyName
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public boolean saveIntermediateRelationship(Object object, String propertyName) throws LiquidException
    {
        try
        {
            // check the entity first
            checkRootEntityClass(object.getClass());

            // get the intermediate field from the property name
            Field intermediateField = object.getClass().getDeclaredField(propertyName);
            JoinTable table = (JoinTable) intermediateField.getAnnotation(JoinTable.class);

            // get the intermediate field / value of the left column name - referenced in the intermediate table
            Field field = ReflectionsUtil
                    .getAssociatedFieldFromColumnName(object.getClass(), table.leftJoinColumn().columnName());
            Method accessorMethod = object.getClass().getMethod(ReflectionsUtil.getAccessorMethodName(field),
                    new Class[] {});

            // get the LEFT value
            Object leftValue = accessorMethod.invoke(object, new Object[] {});

            // build the INSERT queries
            if (Collection.class.isAssignableFrom(intermediateField.getType()))
            {
                // get the collection through an accessor method
                accessorMethod = object.getClass().getMethod(ReflectionsUtil.getAccessorMethodName(intermediateField), new Class[] {});
                Collection collection = (Collection) accessorMethod.invoke(object, new Object[] {});

                if (collection != null && !collection.isEmpty())
                {
                    for (Object entry : collection)
                    {
                        Object referenceValue  = null;
                        Field referenceField = ReflectionsUtil.getAssociatedFieldFromColumnName(table.rightJoinColumn().targetClass(),
                                table.rightJoinColumn().columnName());

                        accessorMethod = entry.getClass().getMethod(ReflectionsUtil.getAccessorMethodName(referenceField), new Class[] {});
                        referenceValue = accessorMethod.invoke(entry, new Object[] {});

                        // build the INSERT statement
                        StringBuilder insertBuffer = new StringBuilder();
                        insertBuffer.append("INSERT INTO ").append(table.tableName())
                                .append("(")
                                .append(table.leftJoinColumn().referencedColumnName()).append(", ")
                                .append(table.rightJoinColumn().referencedColumnName()).append(") \n")
                                .append("VALUES(?, ?)");

                        System.out.println(insertBuffer.toString() + "\n");

                        // prepare the statement
                        PreparedStatement preparedStatement0 = connection.prepareStatement(insertBuffer.toString());

                        // set parameters
                        ParameterUtil.addParameter(preparedStatement0, 1, leftValue);
                        ParameterUtil.addParameter(preparedStatement0, 2, referenceValue);

                        // execute insert
                        preparedStatement0.execute();
                    }
                }
            }
            else
            {
                throw new LiquidException("A collection is required for an intermediate relationship.");
            }

            return true;

        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }
}
