package org.liquidsql.sql;

import org.liquidsql.util.LiquidException;

public interface LiquidSave
{
    /**
     * Saves the specified object
     *
     * @param object
     * @return
     * @throws LiquidException
     */
    public boolean saveObject(Object object) throws LiquidException;

    /**
     * Saves an intermediate relationship
     *
     * @param object
     * @param propertyName
     * @return
     * @throws LiquidException
     */
    public boolean saveIntermediateRelationship(Object object, String propertyName) throws LiquidException;
}
