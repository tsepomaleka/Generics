package org.liquidsql.util.querybuilder.impl;

import org.liquidsql.model.alias.NodeAlias;
import org.liquidsql.model.column.ColumnInformation;
import org.liquidsql.model.condition.Condition;
import org.liquidsql.model.join.JoinColumnInformation;
import org.liquidsql.model.join.JoinTableInformation;
import org.liquidsql.model.order.Order;
import org.liquidsql.util.querybuilder.FetchQueryBuilder;
import org.liquidsql.util.scanner.DefaultEntityScanner;
import org.liquidsql.util.scanner.EntityScanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation class for the SELECT SQL builder interface.
 */
public class FetchQueryBuilderImpl implements FetchQueryBuilder
{
    // the query buffer
    protected final StringBuilder buffer = new StringBuilder();
    // the default entity scanner
    protected final EntityScanner defaultEntityScanner = DefaultEntityScanner.getInstance();

    // a map of all the node aliases
    protected Map<Integer, NodeAlias> nodeAliasMap;
    // table + column join information
    protected Collection joinInformationCollection;
    // the condition
    protected Condition condition;
    // the group by collection
    protected Collection<String> groupByCollection;
    // the order by collection
    protected Collection<Order> orderByCollection;
    // the limit & offset
    protected int limit = -1;
    protected int offset = -1;
    // the root entity class
    protected Class rootEntityClass;

    public FetchQueryBuilderImpl()
    {
    }

    public void setNodeAliasMap(Map<Integer, NodeAlias> nodeAliasMap)
    {
        this.nodeAliasMap = nodeAliasMap;
    }

    public void setJoinInformationCollection(Collection joinInformationCollection)
    {
        this.joinInformationCollection = joinInformationCollection;
    }

    public void setCondition(Condition condition)
    {
        this.condition = condition;
    }

    public void setGroupByCollection(Collection<String> groupByCollection)
    {
        this.groupByCollection = groupByCollection;
    }

    public void setOrderByCollection(Collection<Order> orderByCollection)
    {
        this.orderByCollection = orderByCollection;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public void setRootEntityClass(Class rootEntityClass)
    {
        this.rootEntityClass = rootEntityClass;
    }

    /**
     * Aplies the order by to the query statement
     * @param orderByCollection
     */
    protected final void applyOrderByToQueryStatement(StringBuilder buffer, Collection<Order> orderByCollection)
    {
        if (buffer != null)
        {
            if (orderByCollection != null && orderByCollection.isEmpty() == false)
            {
                buffer.append("ORDER BY ");

                Iterator iterator = orderByCollection.iterator();

                while (iterator.hasNext())
                {
                    Order order = (Order) iterator.next();
                    buffer.append(order.toString());

                    if (iterator.hasNext())
                        buffer.append(", ");
                }

                buffer.append("\n");
            }
        }
    }

    /**
     * Applies the group by to the query statement
     */
    protected final void applyGroupByToQueryStatement(StringBuilder buffer, Collection<String> groupByCollection)
    {
        if (buffer != null)
        {
            if (groupByCollection != null && groupByCollection.isEmpty() == false)
            {
                buffer.append("GROUP BY ");

                Iterator iterator = groupByCollection.iterator();
                while (iterator.hasNext())
                {
                    String groupBy = (String) iterator.next();
                    buffer.append(groupBy);

                    if (iterator.hasNext())
                        buffer.append(", ");
                }

                buffer.append("\n");
            }
        }
    }

    /**
     * Applies the condition to the query statement
     * @param buffer
     */
    protected final void applyConditionToQueryStatement(StringBuilder buffer, Condition condition)
    {
        if (buffer != null && condition != null && !condition.toString().isEmpty())
        {
            buffer.append("WHERE \n")
                    .append(condition.toString())
                    .append("\n");
        }
    }

    /**
     * Applies the join columns to the query statement
     * @param joinInformationCollection
     */
    protected final void applyJoinTablesToQueryStatement(StringBuilder buffer, Collection joinInformationCollection)
    {
        if (buffer != null && joinInformationCollection != null)
        {
            Iterator iterator = joinInformationCollection.iterator();

            while (iterator.hasNext())
            {
                Object information = iterator.next();

                if (information instanceof JoinColumnInformation)
                {
                    JoinColumnInformation c = (JoinColumnInformation) information;
                    applyJoinInformationToBuffer(buffer, c);
                }
                else if (information instanceof JoinTableInformation)
                {
                    JoinTableInformation t = (JoinTableInformation) information;

                    applyJoinInformationToBuffer(buffer, t.getLeftJoinColumnInformation());
                    applyJoinInformationToBuffer(buffer, t.getRightJoinColumnInformation());
                }
            }

            buffer.append("\n");
        }
    }


    /**
     * Applies the join information to the buffer
     * @param buffer
     * @param information
     */
    protected final void applyJoinInformationToBuffer(StringBuilder buffer, JoinColumnInformation information)
    {
        if (buffer != null && information != null)
        {
            buffer.append(information.getJoinType().getKeyword())
                    .append(" ").append(information.getRightTableName())
                    .append(" ON ").append("(")
                    .append(information.getLeftTableName()).append(".").append(information.getLeftColumnName())
                    .append(" = ")
                    .append(information.getRightTableName()).append(".").append(information.getRightColumnName())
                    .append(")\n");
        }
    }

    /**
     * Replaces all property path occurrences with the full column names in
     * the query statement.
     *
     * @param queryStatement
     * @param nodeAliasMap
     * @return
     */
    protected String replaceAllPropertyPathsWithColumns(String queryStatement, Map<Integer, NodeAlias> nodeAliasMap)
    {
        // iterate all the node aliases
        for (Integer key : nodeAliasMap.keySet())
        {
            // get the mapped node alias
            NodeAlias nodeAlias = nodeAliasMap.get(key);
            // iterate the column information collection
            for (ColumnInformation columnInformation : nodeAlias.getColumnInformationCollection())
            {
                // build the property path
                String propertyPath = "${" + nodeAlias.getAliasName() + "." +
                        columnInformation.getField().getName() + "}";

                // check if property path exists in the query statement
                if (queryStatement.contains(propertyPath))
                {
                    // get the full column name
                    String fullColumnName = columnInformation.getFullColumnName();
                    // replace property path occurrences with this full
                    // column name
                    queryStatement = queryStatement.replace(propertyPath, fullColumnName);
                }
            }
        }

        return queryStatement;
    }

    /**
     * Applies the offset and limit to the query statement
     * @param buffer
     * @param offset
     * @param limit
     */
    protected void applyOffsetAndLimitToQueryStatement(StringBuilder buffer, int offset, int limit)
    {
        if (buffer != null)
        {
            // add the offset & limit
            if (offset >= 0)
                buffer.append("OFFSET ").append(offset).append(" ");

            if (limit > 0)
                buffer.append("LIMIT ").append(limit).append(" ");
        }
    }

    /**
     * Applies query columns to the buffer
     * @param buffer
     */
    private void applyColumnsToQueryStatement(StringBuilder buffer)
    {
        if (buffer != null)
        {
            buffer.append("SELECT \n");

            Collection allColumnInformationCollection = new ArrayList();
            for (Integer key : nodeAliasMap.keySet())
            {
                NodeAlias nodeAlias = nodeAliasMap.get(key);
                allColumnInformationCollection.addAll(nodeAlias.getColumnInformationCollection());
            }

            Iterator iterator = allColumnInformationCollection.iterator();

            while (iterator.hasNext())
            {
                ColumnInformation information = (ColumnInformation) iterator.next();

                if (information.isDistinct())
                    buffer.append("DISTINCT ");

                buffer.append(information.getFullColumnName()).append(" AS ")
                        .append(information.getColumnNameAs());

                if (iterator.hasNext()) buffer.append(",");

                buffer.append("\n");
            }
        }
    }

    @Override
    public String generateQuery()
    {

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

        // apply the limit + offset
        applyOffsetAndLimitToQueryStatement(buffer, offset, limit);

        // replace all property fields with full column names
        String formattedStatement = replaceAllPropertyPathsWithColumns(
                buffer.toString().trim(), nodeAliasMap);

        // display the SQL to the console
        System.out.println("Generated SQL statement: \n" + formattedStatement + "\n");

        return formattedStatement;
    }


}
