package org.liquidsql.util.querybuilder.impl;

import org.liquidsql.model.aggregate.AggregateColumnInformation;
import org.liquidsql.model.alias.NodeAlias;
import org.liquidsql.model.annotation.Column;
import org.liquidsql.util.LiquidException;
import org.liquidsql.util.querybuilder.AggregateFetchQueryBuilder;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class AggregateFetchQueryBuilderImpl extends FetchQueryBuilderImpl implements AggregateFetchQueryBuilder
{

    // aggregated column information bucket map
    private Map<Integer, AggregateColumnInformation> aggregateColumnInformationMap;

    public AggregateFetchQueryBuilderImpl() {}

    @Override
    public String generateQuery()
    {
        StringBuilder buffer = new StringBuilder();

        // append the SELECT columns
        applyColumnsToQueryStatement(buffer);

        // append the FROM table
        String masterTableName = defaultEntityScanner.getTableName(rootEntityClass);
        buffer.append("FROM ").append(masterTableName).append("\n");

        // append the JOIN tables
        applyJoinTablesToQueryStatement(buffer, joinInformationCollection);

        // add the condition (WHERE block)
        applyConditionToQueryStatement(buffer, condition);

        // add the group by information
        applyGroupByToQueryStatement(buffer, groupByCollection);

        // add the order by information
        applyOrderByToQueryStatement(buffer, orderByCollection);

        // add the offset & limit
        if (offset >= 0)
            buffer.append("OFFSET ").append(offset).append(" ");

        if (limit > 0)
            buffer.append("LIMIT ").append(limit).append(" ");

        String formattedStatement = replaceAllPropertyPathsWithColumns(buffer.toString().trim(), nodeAliasMap);

        // display the SQL to the console
        System.out.println("Generated SQL statement: \n" + formattedStatement + "\n");

        return formattedStatement;
    }

    /**
     * Applies query columns to the buffer
     * @param buffer
     */
    protected void applyColumnsToQueryStatement(StringBuilder buffer)
    {
        if (buffer != null)
        {
            buffer.append("SELECT \n");

            Iterator iterator = aggregateColumnInformationMap.values().iterator();

            while (iterator.hasNext())
            {
                AggregateColumnInformation information = (AggregateColumnInformation) iterator.next();

                if (information.isDistinct())
                    buffer.append("DISTINCT ");

                buffer.append(information.getFullColumnName()).append(" AS ")
                        .append(information.getColumnNameAs());

                if (iterator.hasNext()) buffer.append(",");

                buffer.append("\n");
            }
        }
    }

    /**
     * Replaces all property path occurrences with the full column names in
     * the query statement.
     *
     * @param queryStatement
     * @return
     */
    protected String replaceAllPropertyPathsWithColumns(String queryStatement, Map<Integer, NodeAlias> nodeAliasMap)
    {
        try
        {
            // iterate all the node aliases
            for (Integer key : nodeAliasMap.keySet())
            {
                // get the mapped node alias
                NodeAlias nodeAlias = nodeAliasMap.get(key);
                // get complete field collection
                Collection<Field> fieldCollection = defaultEntityScanner.getCompleteFieldCollection(nodeAlias.getEntityClass());

                // iterate the column information collection
                for (Field field : fieldCollection)
                {
                    // build the property path
                    String propertyPath = "${" + nodeAlias.getAliasName() + "." + field.getName() + "}";

                    // check if property path exists in the query statement
                    if (queryStatement.contains(propertyPath))
                    {
                        // get the full column name
                        Column column = (Column) field.getAnnotation(Column.class);
                        String tableName = defaultEntityScanner.getTableName(nodeAlias.getEntityClass());

                        String fullColumnName = tableName + "." + column.name();
                        // replace property path occurrences with this full
                        // column name
                        queryStatement = queryStatement.replace(propertyPath, fullColumnName);
                    }
                }
            }

            return queryStatement;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    @Override
    public void setAggregateColumnsMap(Map<Integer, AggregateColumnInformation> aggregateColumnsMap)
    {
        this.aggregateColumnInformationMap = aggregateColumnsMap;
    }
}
