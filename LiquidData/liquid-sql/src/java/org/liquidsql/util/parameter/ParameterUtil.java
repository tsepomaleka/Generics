package org.liquidsql.util.parameter;

import org.liquidsql.model.annotation.Column;
import org.liquidsql.model.annotation.Temporal;
import org.liquidsql.model.enumeration.TemporalType;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;

public final class ParameterUtil
{
    /**
     * Adds a parameter to the prepared statement.
     *
     * @param preparedStatement
     * @param index
     * @param parameter
     * 
     * @throws SQLParameterException
     */
    public static void addParameter(PreparedStatement preparedStatement, int index, Object parameter) throws SQLParameterException
    {
        try
        {
            if (parameter == null)
            {
                preparedStatement.setNull(index, Types.NULL);
            }
            else
            {
                if (parameter instanceof Boolean)
                    preparedStatement.setBoolean(index, (Boolean) parameter);

                else if (parameter instanceof Short)
                    preparedStatement.setShort(index, (Short) parameter);

                else if (parameter instanceof Integer)
                    preparedStatement.setInt(index, (Integer) parameter);

                else if (parameter instanceof Long)
                    preparedStatement.setLong(index, (Long) parameter);

                else if (parameter instanceof Double)
                    preparedStatement.setDouble(index, (Double) parameter);

                else if (parameter instanceof BigDecimal)
                    preparedStatement.setBigDecimal(index, (BigDecimal) parameter);

                else if (parameter instanceof String)
                    preparedStatement.setString(index, (String) parameter);

                else if (parameter instanceof Timestamp)
                    preparedStatement.setTimestamp(index, (Timestamp) parameter);

                else if (parameter instanceof Time)
                    preparedStatement.setTime(index, (Time) parameter);

                else if (parameter instanceof Date)
                    preparedStatement.setDate(index, (Date) parameter);
            }
            
        }
        catch (Exception e)
        {
            throw new SQLParameterException(e);
        }
    }

    /**
     * Check if values is temporal type
     *
     * @param field
     * @param value
     * @return Object
     */
    public static Object checkIfValueIsTemporal(Field field, Object value)
    {
        if (value instanceof java.util.Date &&
                (field.isAnnotationPresent(Column.class) &&
                        field.isAnnotationPresent(Temporal.class)))
        {
            Temporal temporal = (Temporal) field.getAnnotation(Temporal.class);
            TemporalType type = temporal.value();

            if (type == TemporalType.DATE)
            {
                java.sql.Date instance = new java.sql.Date(((java.util.Date)value).getTime());
                return instance;
            }
            else if (type == TemporalType.TIME)
            {
                Time instance = new Time(((java.util.Date)value).getTime());
                return instance;
            }
            else
            {
                Timestamp instance = new Timestamp(((java.util.Date)value).getTime());
                return instance;
            }
        }

        return value;
    }
}
