package org.liquidsql.util.querybuilder;

import java.util.TreeMap;

public interface SaveQueryBuilder extends QueryBuilder
{
    /**
     * Sets the entity object to generate the
     * SQL from.
     *
     * @param object
     */
    public void setEntityObject(Object object);

    /**
     * Gets the value parameter value map
     * @return
     */
    public TreeMap<Integer, Object> getParameterValueTreeMap();
}
