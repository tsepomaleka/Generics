package org.liquidsql.util;

public class LiquidException extends RuntimeException
{
    public LiquidException(Throwable t)
    {
        super(t);
    }
    
    public LiquidException(String message)
    {
        super(message);
    }
}
