package org.liquidsql.model.condition;

import org.liquidsql.model.enumeration.MatchMode;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

public class Condition
{
    private String propertyPath;
    private String operator;
    private String expression;

    // carries a indexed map of parameter values
    private static final TreeMap<Integer, Object> parameterValueMap = new TreeMap<>();

    /**
     * Private constructor
     * @param expression
     */
    private Condition(String expression)
    {
        this.expression = expression;
        this.propertyPath = null;
        this.operator = null;
    }
    
    /**
     * Private constructor
     * 
     * @param propertyPath The property path
     * @param operator The operator
     */
    private Condition(String propertyPath, String operator)
    {
        this.propertyPath = propertyPath;
        this.operator = operator;
        this.expression = null;
    }

    /**
     * Gets the property path
     * @return String
     */
    public String getPropertyPath()
    {
        return this.propertyPath;
    }
    

    /**
     * Sets the expression
     * @return 
     */
    @Override
    public String toString()
    {
        if (this.expression == null)
        {

            StringBuilder buffer = new StringBuilder();
            buffer.append("(${").append(propertyPath).append("}")
                    .append(" ").append(operator).append(" ?)");

            this.expression = buffer.toString();
        }
        
        return expression;
    }

    /**
     * Adds the parameter value to the next count.
     * @param value
     */
    private static void addNextParameterValue(Object value)
    {
        int currentCapacity = parameterValueMap.size();
        int nextCount = currentCapacity + 1;

        parameterValueMap.put(nextCount, value);
    }

    /**
     * Clears all current contents of the parameter value map.
     */
    public static void resetParameterValueMap()
    {
        parameterValueMap.clear();
    }

    /**
     * Gets the parameter value map
     * @return TreeMap
     */
    public static TreeMap<Integer, Object> getParameterValueMap()
    {
        return parameterValueMap;
    }

    /**
     * Creates an condition for EQUALS
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value for condition evaluation
     * @return Condition
     */
    public static Condition equals(String propertyPath, Object value)
    {
        Condition condition = new Condition(propertyPath, "=");
        addNextParameterValue(value);
        return condition;
    }

    /**
     * Creates an condition for NOT EQUAL TO
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value for condition evaluation
     * @return Condition
     */
    public static Condition notEquals(String propertyPath, Object value)
    {
        Condition condition = new Condition(propertyPath, "!=");
        addNextParameterValue(value);
        return condition;
    }

    /**
     * Creates an condition for GREATER THAN
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value for condition evaluation
     * @return Condition
     */
    public static Condition greaterThan(String propertyPath, Object value)
    {
        Condition condition = new Condition(propertyPath, ">");
        addNextParameterValue(value);
        return condition;
    }

    /**
     * Creates an condition for GREATER THAN OR EQUAL TO
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value for condition evaluation
     * @return Condition
     */
    public static Condition greaterOrEquals(String propertyPath, Object value)
    {
        Condition condition = new Condition(propertyPath, ">=");
        addNextParameterValue(value);
        return condition;
    }

    /**
     * Creates an condition for LESS THAN
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value for condition evaluation
     * @return Condition
     */
    public static Condition lessThan(String propertyPath, Object value)
    {
        Condition condition = new Condition(propertyPath, "<");
        addNextParameterValue(value);
        return condition;
    }

    /**
     * Creates an condition for LESS THAN OR EQUAL TO
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value for condition evaluation
     * @return Condition
     */
    public static Condition lessThanOrEquals(String propertyPath, Object value)
    {
        Condition condition = new Condition(propertyPath, "<=");
        addNextParameterValue(value);
        return condition;
    }

    /**
     * Creates an condition for BETWEEN
     *
     * @param propertyPath The property path for condition evaluation
     * @param minimum The minimum inclusive value in the range
     * @param maximum The maximum inclusive value in the range
     * @return Condition
     */
    public static Condition between(String propertyPath, Object minimum, Object maximum)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(${").append(propertyPath).append("} ")
                .append("BETWEEN ")
                .append(minimum.toString()).append(" AND ")
                .append(maximum.toString()).append(")");

        Condition condition = new Condition(buffer.toString());
        addNextParameterValue(minimum);
        addNextParameterValue(maximum);

        return condition;
    }

    /**
     * Creates an condition for IN
     *
     * @param propertyPath The property path for condition evaluation
     * @param values A collection of values in evaluation
     * @return Condition
     */
    public static Condition in(String propertyPath, Collection values)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(${").append(propertyPath).append("}")
                .append(" IN (");

        Iterator iterator = values.iterator();
        while (iterator.hasNext())
        {
            buffer.append("?");
            addNextParameterValue(iterator.next());

            if (iterator.hasNext())
                buffer.append(", ");
        }

        buffer.append("))");

        Condition condition = new Condition(buffer.toString());
        return condition;
    }

    /**
     * Creates a condition for NOT IN
     *
     * @param propertyPath The property path for condition evaluation
     * @param values A collection of values in evaluation
     * @return Condition
     */
    public static Condition notIn(String propertyPath, Collection values)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(${").append(propertyPath).append("}")
                .append(" NOT IN (");

        Iterator iterator = values.iterator();
        while (iterator.hasNext())
        {
            buffer.append("?");
            addNextParameterValue(iterator.next());

            if (iterator.hasNext())
                buffer.append(", ");
        }

        buffer.append("))");

        Condition condition = new Condition(buffer.toString());
        return condition;
    }

    /**
     * Creates a condition for LIKE
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value in evaluation
     * @param matchMode The match mode
     * @return Condition
     */
    public static Condition like(String propertyPath, String value, MatchMode matchMode)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(${").append(propertyPath).append("}")
                .append(" LIKE ?)");

        if (matchMode == MatchMode.STARTS_WITH)
            addNextParameterValue(value + "%");

        else if (matchMode == MatchMode.ENDS_WITH)
            addNextParameterValue("%" + value);

        else if (matchMode == MatchMode.ANYWHERE)
            addNextParameterValue("%" + value + "%");

        Condition condition = new Condition(buffer.toString());
        return condition;
    }

    /**
     * Creates a condition for LIKE
     *
     * @param propertyPath The property path for condition evaluation
     * @param value The value in evaluation
     * @param matchMode The match mode
     * @return Condition
     */
    public static Condition notLike(String propertyPath, String value, MatchMode matchMode)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(${").append(propertyPath).append("}")
                .append(" NOT LIKE ?)");

        if (matchMode == MatchMode.STARTS_WITH)
            addNextParameterValue(value + "%");

        else if (matchMode == MatchMode.ENDS_WITH)
            addNextParameterValue("%" + value);

        else if (matchMode == MatchMode.ANYWHERE)
            addNextParameterValue("%" + value + "%");

        Condition condition = new Condition(buffer.toString());
        return condition;
    }


    /**
     * Creates a compound condition in which two conditions
     * must hold true for an AND logic evaluation.
     *
     * @param a The first condition in the compound
     * @param b The second condition in the compound
     *
     * @return Condition
     */
    public static Condition and(Condition a, Condition b)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(")
              .append(a.toString()).append("\nAND ")
              .append(b.toString())
              .append(")\n");
        
        return new Condition(buffer.toString());
    }

    /**
     * Creates a compound condition in which either one of the conditions
     * must hold true for an OR logic evaluation.
     *
     * @param a The first condition in the compound
     * @param b The second condition in the compound
     *
     * @return Condition
     */
    public static Condition or(Condition a, Condition b)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(")
              .append(a.toString()).append("\nOR ")
              .append(b.toString())
              .append(")\n");
        
        return new Condition(buffer.toString());
    }

    /**
     * Creates a compound condition in which one or more conditions
     * must hold true for an AND logic evaluation.
     *
     * @param conditions The collection of conditions
     *
     * @return Condition
     */
    public static Condition and(Collection<Condition> conditions)
    {
        StringBuilder buffer = new StringBuilder();
        Iterator<Condition> iterator = conditions.iterator();
        
        buffer.append("(");
        while (iterator.hasNext())
        {
            Condition condition = iterator.next();
            buffer.append(" ")
                    .append(condition.toString());
            
            if (iterator.hasNext())
                  buffer.append("\nAND ");
            else
                buffer.append(")\n");
        }
        
        return new Condition(buffer.toString());
    }

    /**
     * Creates a compound condition in which at least one of the conditions
     * must hold true for an OR logic evaluation.
     *
     * @param conditions The collection of conditions
     *
     * @return Condition
     */
    public static Condition or(Collection<Condition> conditions)
    {
        StringBuilder buffer = new StringBuilder();
        Iterator<Condition> iterator = conditions.iterator();

        buffer.append("(");
        while (iterator.hasNext())
        {
            Condition condition = iterator.next();
            buffer.append(" ")
                    .append(condition.toString());

            if (iterator.hasNext())
                buffer.append("\nOR ");
            else
                buffer.append(")\n");
        }

        return new Condition(buffer.toString());
    }
}
