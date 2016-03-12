package org.liquid.util.scanner;

public class ColumnScannerException extends Exception
{
    public ColumnScannerException(Throwable throwable)
    {
        super(throwable);
    }
    
    public ColumnScannerException(String message)
    {
        super(message);
    }
}
