package org.liquidsql.util.querybuilder;


public interface QueryBuilder
{
    /**
     * Generate the SQL query
     * @return
     * @throws QueryBuilderException
     */
    public String generateQuery() throws QueryBuilderException;
}
