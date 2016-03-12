package org.liquid.model.condition;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class Condition 
{
    private String propertyPath;
    private Object value;
    private String operator;
    
    private String expression;
    
    private Condition(String expression) 
    {
        this.expression = expression;
    }
    
    /**
     * Private constructor
     * 
     * @param propertyPath
     * @param value 
     */
    private Condition(String propertyPath, Object value, String operator)
    {
        this.propertyPath = propertyPath;
        this.value = value;
        this.operator = operator;
    }
    
    public String getPropertyPath()
    {
        return this.propertyPath;
    }
    
    public Object getValue()
    {
        return this.value;
    }
    
    /**
     * Sets the expression
     * @return 
     */
    public String getExpression()
    {
        if (this.expression == null)
        {
            this.expression = "(" + propertyPath + " " + operator;
            
            if ((value instanceof String) || (value instanceof Date))
                this.expression += " '" + value.toString() + "')";
            else
                this.expression += " " + value.toString() + ")";
        }
        
        return expression;
    }
            
    public static Condition equals(String propertyPath, Object value)
    {
        return new Condition(propertyPath, value, "=");
    }
    
    public static Condition notEqual(String propertyPath, Object value)
    {
        return new Condition(propertyPath, value, " != ");
    }
    
    public static Condition greaterThan(String propertyPath, Object value)
    {
        return new Condition(propertyPath, value, ">"); 
    }
    
    public static Condition greaterOrEquals(String propertyPath, Object value)
    {
        return new Condition(propertyPath, value, ">="); 
    }
    
    public static Condition lessThan(String propertyPath, Object value)
    {
        return new Condition(propertyPath, value, "<");
    }
    
    public static Condition lessThanOrEquals(String propertyPath, Object value)
    {
        return new Condition(propertyPath, value, "<=");
    }
    
    public static Condition and(Condition a, Condition b)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(")
              .append(a.getExpression()).append("\nAND ")
              .append(b.getExpression())
              .append(")\n");
        
        return new Condition(buffer.toString());
    }
    
    public static Condition or(Condition a, Condition b)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(")
              .append(a.getExpression()).append("\nOR ")
              .append(b.getExpression())
              .append(")\n");
        
        return new Condition(buffer.toString());
    }
    
    public static Condition allEqual(Collection<Condition> conditions)
    {
        StringBuilder buffer = new StringBuilder();
        Iterator<Condition> iterator = conditions.iterator();
        
        buffer.append("(");
        while (iterator.hasNext())
        {
            Condition condition = iterator.next();
            buffer.append(" ")
                    .append(condition.getExpression());
            
            if (iterator.hasNext())
                  buffer.append("\nAND ");
            else
                buffer.append(")\n");
        }
                
        
        return new Condition(buffer.toString());
    }
}
