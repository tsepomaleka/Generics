package org.liquidsql.util.scanner;

public class EntityScannerException extends Exception
{
    public EntityScannerException(Throwable throwable)
    {
        super(throwable);
    }
    
    public EntityScannerException(String message)
    {
        super(message);
    }
}
