package org.liquid.util.scanner;

import java.sql.ResultSet;
import java.util.Collection;

public interface IColumnScanner 
{
    /**
     * Generates the SQL statement
     * 
     * @return StringBuilder
     * @throws ColumnScannerException 
     */
    public StringBuilder generateQueryStatement() throws ColumnScannerException;
    
    public Collection createEntityCollectionFromResultSet(final ResultSet resultSet, Class entityClass) throws ColumnScannerException;
}
