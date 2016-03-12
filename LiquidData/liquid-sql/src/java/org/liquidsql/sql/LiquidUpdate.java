package org.liquidsql.sql;

import org.liquidsql.util.LiquidException;

public interface LiquidUpdate
{
    /**
     * Updates the specified object - if it already exists in the
     * database.
     * 
     * @param object
     * @return boolean 
     */
    public boolean updateObject(Object object) throws LiquidException;
    
    /**
     * Update entities that share an intermediate relationship
     * @param object
     * @param propertyName
     * @return
     * @throws LiquidException 
     */
    public boolean updateIntermediateRelationship(Object object, String propertyName) throws LiquidException;
    
}
