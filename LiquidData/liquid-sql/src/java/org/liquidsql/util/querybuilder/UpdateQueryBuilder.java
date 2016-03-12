package org.liquidsql.util.querybuilder;

import java.util.TreeMap;

public interface UpdateQueryBuilder extends QueryBuilder
{
    /**
     * Sets the entity object to build an 
     * update SQL for.
     * @param object 
     */
    public void setEntityObject(Object object);
    
    /**
     * Gets the list of the parameter value list.
     * @return List
     */
    public TreeMap<Integer, Object> getParameterValueTreeMap();
}
