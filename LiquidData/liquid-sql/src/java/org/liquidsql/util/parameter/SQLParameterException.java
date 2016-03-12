package org.liquidsql.util.parameter;

public class SQLParameterException extends Exception 
{
    public SQLParameterException(Throwable t)
    {
        super(t);
    }
    
    public SQLParameterException(String message)
    {
        super(message);
    }
}
