package org.liquidsql.sql.impl;

import org.liquidsql.model.aggregate.AggregateColumnInformation;
import org.liquidsql.model.aggregate.AggregateColumnInformationImpl;
import org.liquidsql.model.aggregate.ColumnAggregate;
import org.liquidsql.model.alias.NodeAlias;
import org.liquidsql.model.alias.NodeAliasImpl;
import org.liquidsql.model.annotation.Column;
import org.liquidsql.model.annotation.JoinColumn;
import org.liquidsql.model.annotation.JoinRelation;
import org.liquidsql.model.annotation.JoinTable;
import org.liquidsql.model.condition.Condition;
import org.liquidsql.model.enumeration.ColumnAggregateType;
import org.liquidsql.model.join.JoinColumnInformation;
import org.liquidsql.model.join.JoinTableInformation;
import org.liquidsql.sql.LiquidAggregateFetch;
import org.liquidsql.util.LiquidException;
import org.liquidsql.util.parameter.ParameterUtil;
import org.liquidsql.util.querybuilder.AggregateFetchQueryBuilder;
import org.liquidsql.util.querybuilder.QueryBuilderException;
import org.liquidsql.util.querybuilder.impl.AggregateFetchQueryBuilderImpl;
import org.liquidsql.util.reflections.ReflectionsUtil;
import org.liquidsql.util.scanner.DefaultEntityScanner;
import org.liquidsql.util.scanner.EntityScanner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class LiquidAggregateFetchImpl extends CommonLiquidTasks implements LiquidAggregateFetch
{
    // the default entity scanner
    private final EntityScanner defaultEntityScanner = DefaultEntityScanner.getInstance();
    // a map of all the node aliases
    private final Map<Integer, NodeAlias> nodeAliasMap;
    // aggregated column information bucket map
    private final Map<Integer, AggregateColumnInformation> aggregateColumnInformationMap;
    // table + column join information
    private Collection joinInformationCollection;
    // the condition
    private Condition condition;
    // the limit & offset
    private int limit = -1;
    private int offset = -1;
    // the root entity class
    private Class rootEntityClass;

    /**
     * Default constructor
     * @param connection
     */
    public LiquidAggregateFetchImpl(Connection connection)
    {
        super(connection);

        this.nodeAliasMap = new TreeMap<>();
        this.aggregateColumnInformationMap = new TreeMap<>();

        Condition.resetParameterValueMap();
    }

    /**
     * Gets the next alias ID
     * @return
     */
    private int nextAliasId()
    {
        return (this.nodeAliasMap.size() + 1);
    }

    /**
     * Adds the node alias to the map
     * @param nodeAlias
     */
    private void addNodeAliasToMap(NodeAlias nodeAlias)
    {
        nodeAliasMap.put(nextAliasId(), nodeAlias);
    }

    /**
     * Gets a node alias from the map
     * @param aliasName
     * @return NodeAlias
     */
    private NodeAlias getNodeAliasFromMap(String aliasName)
    {
        for (Integer nodeId : nodeAliasMap.keySet())
        {
            NodeAlias nodeAlias = nodeAliasMap.get(nodeId);
            if (nodeAlias.getAliasName().equals(aliasName))
            {
                return nodeAlias;
            }
        }

        return null;
    }

    /**
     * Creates a new alias from the entity class
     * with the specified alias name.
     *
     * @param entityClass The persistent entity class
     * @param alias       The unique alias name
     * @return LiquidSimpleFetch
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidAggregateFetch createAggregateAlias(Class entityClass, String alias) throws LiquidException
    {
        try
        {
            // check the entity class
            checkRootEntityClass(entityClass);

            // create a new node alias
            NodeAlias nodeAlias = new NodeAliasImpl(alias);
            nodeAlias.setEntityClass(entityClass);
            nodeAlias.setParentNodeAlias(null);
            nodeAlias.setPropertyPath(null);

            // add the node alias to the map
            addNodeAliasToMap(nodeAlias);
            
            // add the root entity class
            this.rootEntityClass = entityClass;

            return this;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     *
     * @param distinct Adds a column aggregate
     *
     * @param columnAggregate
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidAggregateFetch addAggregate(ColumnAggregate columnAggregate, boolean distinct) throws LiquidException
    {
        try
        {
            // get the property path
            String propertyPath = columnAggregate.getPropertyPath();

            // get the alias name from the property path
            String aliasName = stripAliasNameFromPropertyPath(propertyPath);
            // get the field name from the property path
            String fieldName = stripFieldNameFromPropertyPath(propertyPath);

            // find the node alias
            NodeAlias nodeAlias = getNodeAliasFromMap(aliasName);
            // get the field from the entity class
            Field field = ReflectionsUtil.getDeclaredField(nodeAlias.getEntityClass(),
                    fieldName).getSecondValue(); //nodeAlias.getEntityClass().getDeclaredField(fieldName);

            // get the table name
            String tableName = defaultEntityScanner.getTableName(nodeAlias.getEntityClass());

            // check if field has column annotation
            if (field.isAnnotationPresent(Column.class))
            {
                Column column = (Column) field.getAnnotation(Column.class);

                // next alias ID
                int columnIndex = nextAggregateColumnsMapCount();

                // create new column aggregate
                AggregateColumnInformation information = new AggregateColumnInformationImpl(columnAggregate, tableName);
                information.setDistinct(distinct);
                information.setColumnIndex(columnIndex);
                information.setColumnName(column.name());

                // determine the type of the column
                ColumnAggregateType type = columnAggregate.getColumnAggregateType();

                switch (type)
                {
                    case COUNT:
                        information.setColumnType(Long.class);
                        break;

                    default:
                        information.setColumnType(field.getType());
                }

                // add the aggregated column to the bucket
                aggregateColumnInformationMap.put(columnIndex, information);
            }

            return this;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }


    /**
     * Creates a join from the parent alias
     *
     * @param propertyPath The property path
     * @param alias        The unique alias name
     * @return LiquidFetch
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidAggregateFetch addJoin(String propertyPath, String alias) throws LiquidException
    {
        try
        {
            // strip the property path into an alias and a field name
            String parentAliasName = stripAliasNameFromPropertyPath(propertyPath);
            String propertyName = stripFieldNameFromPropertyPath(propertyPath);

            // find the parent node alias
            NodeAlias parentNodeAlias = getNodeAliasFromMap(parentAliasName);

            // find the parent class & the field
            Class parentEntityClass = parentNodeAlias.getEntityClass();
            Field field = ReflectionsUtil.getDeclaredField(parentEntityClass,
                    propertyName).getSecondValue(); //parentEntityClass.getDeclaredField(propertyName);

            if (field.isAnnotationPresent(JoinColumn.class) &&
                    field.isAnnotationPresent(JoinRelation.class))
            {
                // extract join column annotation
                JoinColumn column = (JoinColumn) field.getAnnotation(JoinColumn.class);
                Class targetClass = column.targetClass();

                // create a node alias
                NodeAlias nodeAlias = new NodeAliasImpl(alias);
                nodeAlias.setEntityClass(targetClass);
                nodeAlias.setParentNodeAlias(parentNodeAlias);
                nodeAlias.setPropertyPath(propertyPath);

                // add this node as a child to the parent
                parentNodeAlias.addChildNodeAlias(nodeAlias);

                // add this node to the map
                addNodeAliasToMap(nodeAlias);

                // extract the column join information
                JoinColumnInformation joinColumnInformation =
                        defaultEntityScanner.getTableJoinInfoForJoinColumn(field, parentEntityClass);

                // add the column join information to the list
                if (joinInformationCollection == null)
                    joinInformationCollection = new ArrayList();

                joinInformationCollection.add(joinColumnInformation);
            }

            return this;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Creates an intermediate join from the parent alias
     *
     * @param propertyPath The property path
     * @param alias        The unique alias name (to identify a single instance in a collection)
     * @return LiquidFetch
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidAggregateFetch addIntermediateJoin(String propertyPath, String alias) throws LiquidException
    {
        try
        {
            // strip the property path into an alias and a field name
            String parentAliasName = stripAliasNameFromPropertyPath(propertyPath);
            String propertyName = stripFieldNameFromPropertyPath(propertyPath);

            // find the parent node alias
            NodeAlias parentNodeAlias = getNodeAliasFromMap(parentAliasName);

            // find the parent class & the field
            Class parentEntityClass = parentNodeAlias.getEntityClass();
            Field field = ReflectionsUtil.getDeclaredField(parentEntityClass,
                    propertyName).getSecondValue(); //parentEntityClass.getDeclaredField(propertyName);

            // check if the field has the required annotations
            if (field.isAnnotationPresent(JoinTable.class) &&
                    field.isAnnotationPresent(JoinRelation.class))
            {
                // get the addJoin table
                JoinTable table = (JoinTable) field.getAnnotation(JoinTable.class);
                Class targetClass = table.rightJoinColumn().targetClass();

                // create a node alias
                NodeAlias nodeAlias = new NodeAliasImpl(alias);
                nodeAlias.setEntityClass(targetClass);
                nodeAlias.setParentNodeAlias(parentNodeAlias);
                nodeAlias.setPropertyPath(propertyPath);

                // add this node as a child to the parent
                parentNodeAlias.addChildNodeAlias(nodeAlias);

                // add this node to the map
                addNodeAliasToMap(nodeAlias);

                // collection join table information
                JoinTableInformation tableJoinInformation = (JoinTableInformation)
                        defaultEntityScanner.getTableJoinInfoForJoinTable(field);

                if (joinInformationCollection == null)
                    joinInformationCollection = new ArrayList();

                joinInformationCollection.add(tableJoinInformation);
            }

            return this;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Sets the condition
     *
     * @param condition
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidAggregateFetch addCondition(Condition condition) throws LiquidException
    {
        if (condition != null)
            this.condition = condition;

        return this;
    }

    /**
     * Sets the offset of the result set
     *
     * @param offset
     * @return
     */
    @Override
    public LiquidAggregateFetch setOffset(int offset)
    {
        if (offset >= 0)
            this.offset = offset;

        return this;
    }

    /**
     * Sets the limit count of the result set
     *
     * @param limit
     * @return
     */
    @Override
    public LiquidAggregateFetch setLimit(int limit)
    {
        if (limit > 0)
            this.limit = limit;

        return this;
    }

    /**
     * Executes a data fetch
     *
     * @return Map
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public Collection executeAggregateFetch() throws LiquidException
    {
        Collection resultsCollection = new ArrayList();

        try
        {
            // build the query statement
            String query = generateQueryStatement();
            // create a prepared statement
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            // set the parameters
            for (Integer index : Condition.getParameterValueMap().keySet())
            {
                Object mappedValue = Condition.getParameterValueMap().get(index);
                ParameterUtil.addParameter(preparedStatement, index, mappedValue);
            }

            // execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();
                
            // fetch the rows from the result set
            while (resultSet.next())
            {
                Collection collection = new ArrayList();
                
                for (Integer key : aggregateColumnInformationMap.keySet())
                {
                    // get the aggregate column
                    AggregateColumnInformation information = aggregateColumnInformationMap.get(key);

                    // obtain the column name & column type
                    String columnName = information.getColumnNameAs();
                    Class type = information.getColumnType();

                    // get the value
                    Object result = getValueFromResultSet(columnName, type, resultSet);

                    // add the value to the collection
                    if (result != null)
                        resultsCollection.add(result);
                }
            }

            return resultsCollection;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Gets the next aggregate information column information ID
     * @return
     */
    private int nextAggregateColumnsMapCount()
    {
        return (this.aggregateColumnInformationMap.size() + 1);
    }

    /**
     * Generates the query statement
     *
     * @return
     * @throws org.liquidsql.util.querybuilder.QueryBuilderException
     */
    protected String generateQueryStatement() throws QueryBuilderException
    {
        AggregateFetchQueryBuilder queryBuilder = new AggregateFetchQueryBuilderImpl();

        queryBuilder.setAggregateColumnsMap(aggregateColumnInformationMap);
        queryBuilder.setNodeAliasMap(nodeAliasMap);
        queryBuilder.setJoinInformationCollection(joinInformationCollection);
        queryBuilder.setCondition(condition);
        queryBuilder.setLimit(limit);
        queryBuilder.setOffset(offset);
        queryBuilder.setRootEntityClass(rootEntityClass);

        return queryBuilder.generateQuery();
    }

}
