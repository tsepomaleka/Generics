package org.liquidsql.sql.impl;

import org.liquidsql.model.annotation.JoinTable;
import org.liquidsql.sql.LiquidUpdate;
import org.liquidsql.util.LiquidException;
import org.liquidsql.util.parameter.ParameterUtil;
import org.liquidsql.util.querybuilder.UpdateQueryBuilder;
import org.liquidsql.util.querybuilder.impl.UpdateQueryBuilderImpl;
import org.liquidsql.util.reflections.ReflectionsUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

public class LiquidUpdateImpl extends CommonLiquidTasks implements LiquidUpdate
{
    /**
     * Default constructor
     * @param connection
     */
    public LiquidUpdateImpl(Connection connection)
    {
        super(connection);
    }

    @Override
    public boolean updateObject(Object object) throws LiquidException
    {
        try
        {
            // check the entity first
            checkRootEntityClass(object.getClass());
            
            // prepare the SQL builder
            UpdateQueryBuilder builder = new UpdateQueryBuilderImpl();
            builder.setEntityObject(object); 
            
            // build the SQL query
            String query = builder.generateQuery();
            
            // set the prepared statement
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            
            // insert the parameters
            for (Integer cursor : builder.getParameterValueTreeMap().keySet())
            {
                Object parameter = builder.getParameterValueTreeMap().get(cursor);
                ParameterUtil.addParameter(preparedStatement, cursor, parameter);
            }
            
            // execute the update
            int affectedRecords = preparedStatement.executeUpdate();

            return (affectedRecords > 0);
        }
        catch (Exception e)
        {

            throw new LiquidException(e);
        }
    }

    @Override
    public boolean updateIntermediateRelationship(Object object, String propertyName) throws LiquidException 
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
            
            Object value = accessorMethod.invoke(object, new Object[] {});
            
            // build the DELETE query
            StringBuilder deleteBuffer = new StringBuilder();
            deleteBuffer.append("DELETE FROM ")
                    .append(table.tableName())
                    .append(" WHERE ")
                    .append(table.leftJoinColumn().referencedColumnName())
                    .append(" = ?");
            
            // prepare statement for DELETE
            System.out.println(deleteBuffer.toString() + "\n");

            PreparedStatement preparedStatement = connection.prepareStatement(deleteBuffer.toString());
            ParameterUtil.addParameter(preparedStatement, 1, value);
            
            // execute the DELETE 
            preparedStatement.execute();
            
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
                        ParameterUtil.addParameter(preparedStatement0, 1, value);
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
