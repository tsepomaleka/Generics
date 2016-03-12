package org.liquidsql.sql.impl;

import org.liquidsql.model.alias.NodeAlias;
import org.liquidsql.model.alias.NodeAliasImpl;
import org.liquidsql.model.annotation.JoinColumn;
import org.liquidsql.model.annotation.JoinRelation;
import org.liquidsql.model.annotation.JoinTable;
import org.liquidsql.model.column.ColumnInformation;
import org.liquidsql.model.condition.Condition;
import org.liquidsql.model.join.JoinColumnInformation;
import org.liquidsql.model.join.JoinTableInformation;
import org.liquidsql.model.order.Order;
import org.liquidsql.sql.LiquidFetch;
import org.liquidsql.util.LiquidException;
import org.liquidsql.util.PairedValue;
import org.liquidsql.util.parameter.ParameterUtil;
import org.liquidsql.util.querybuilder.FetchQueryBuilder;
import org.liquidsql.util.querybuilder.QueryBuilderException;
import org.liquidsql.util.querybuilder.impl.FetchQueryBuilderImpl;
import org.liquidsql.util.reflections.ReflectionsUtil;
import org.liquidsql.util.scanner.DefaultEntityScanner;
import org.liquidsql.util.scanner.EntityScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class LiquidFetchImpl extends CommonLiquidTasks implements LiquidFetch
{
    // the default entity scanner
    private final EntityScanner defaultEntityScanner = DefaultEntityScanner.getInstance();
    // a map of all the node aliases
    private final Map<Integer, NodeAlias> nodeAliasMap;
    // table + column join information
    private Collection joinInformationCollection;
    // the condition
    private Condition condition;
    // the group by collection
    private Collection<String> groupByCollection;
    // the order by collection
    private Collection<Order> orderByCollection;
    // the limit & offset
    private int limit = -1;
    private int offset = -1;
    // the root entity class
    private Class rootEntityClass;

    /**
     * Default constructor
     * @param connection JDBC connection
     */
    public LiquidFetchImpl(Connection connection)
    {
        super(connection);
        this.nodeAliasMap = new TreeMap<>();
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
     * Creates a new alias from the entity class
     * with the specified alias name.
     *
     * @param entityClass The persistent entity class
     * @param alias       The unique alias name
     * @return LiquidSimpleFetch
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidFetch createAlias(Class entityClass, String alias) throws LiquidException
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

            // scan for column + field information
            Collection<ColumnInformation> columnInformationCollection =
                    defaultEntityScanner.getColumnInformation(entityClass);
            nodeAlias.setColumnInformationCollection(columnInformationCollection);

            // move the ID column to the beginning
            // moveIdColumnToBeginning(columnInformationCollection);

            // add the node alias to the map
            addNodeAliasToMap(nodeAlias);

            // set this entity class as root
            rootEntityClass = entityClass;

            return this;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Sets the specified property field as distinct.
     *
     * @param propertyPath
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidFetch distinct(String propertyPath) throws LiquidException
    {
        try
        {
            // strip the property path into an alias and a field name
            String aliasName = stripAliasNameFromPropertyPath(propertyPath);
            String propertyName = stripFieldNameFromPropertyPath(propertyPath);

            // find the node alias
            NodeAlias nodeAlias = getNodeAliasFromMap(aliasName);

            // iterate the column information collection to find
            // the associated field
            ColumnInformation distinctColumnInfo = null;
            int indexOfColumnInfo = -1;

            if (nodeAlias.getColumnInformationCollection() instanceof ArrayList)
            {
                boolean found = false;

                for (ColumnInformation columnInformation : nodeAlias.getColumnInformationCollection())
                {
                    if (columnInformation.getField().getName().equals(propertyName))
                    {
                        columnInformation.setDistinct(true);
                        indexOfColumnInfo = ((ArrayList) nodeAlias.getColumnInformationCollection())
                                .indexOf(columnInformation);
                        found = true;

                        break;
                    }
                }

                if (found)
                {
                    // move this distinct column information to the beginning
                    // of the collection
                    distinctColumnInfo = (ColumnInformation)((ArrayList) nodeAlias
                            .getColumnInformationCollection()).remove(indexOfColumnInfo);
                    ((ArrayList) nodeAlias.getColumnInformationCollection()).add(0, distinctColumnInfo);
                }
            }

            return this;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * @param propertyPath
     * @param alias
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidFetch addJoin(String propertyPath, String alias) throws LiquidException
    {
        try {
            // strip the property path into an alias and a field name
            String parentAliasName = stripAliasNameFromPropertyPath(propertyPath);
            String propertyName = stripFieldNameFromPropertyPath(propertyPath);

            // find the parent node alias
            NodeAlias parentNodeAlias = getNodeAliasFromMap(parentAliasName);

            // find the parent class & the field
            Class parentEntityClass = parentNodeAlias.getEntityClass();
            PairedValue<Class, Field> pairedValue = ReflectionsUtil.getDeclaredField(parentEntityClass, propertyName);

            if (pairedValue != null)
            {
                Field field = pairedValue.getSecondValue();

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

                    // scan for column information
                    Collection<ColumnInformation> columnInformationCollection =
                            defaultEntityScanner.getColumnInformation(targetClass);
                    nodeAlias.setColumnInformationCollection(columnInformationCollection);

                    // move the ID column to the beginning
                    // moveIdColumnToBeginning(columnInformationCollection);

                    // add this node to the map
                    addNodeAliasToMap(nodeAlias);

                    // extract the column join information
                    JoinColumnInformation joinColumnInformation =
                            defaultEntityScanner.getTableJoinInfoForJoinColumn(field,
                                    parentEntityClass);

                    // add the column join information to the list
                    if (joinInformationCollection == null)
                        joinInformationCollection = new ArrayList();

                    joinInformationCollection.add(joinColumnInformation);
                }

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
    public LiquidFetch addIntermediateJoin(String propertyPath, String alias) throws LiquidException
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

            // find the class-field paired value
            PairedValue<Class, Field> pairedValue = ReflectionsUtil.getDeclaredField(parentEntityClass, propertyName);
            Field field = pairedValue.getSecondValue();

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

                // scan for column information
                Collection<ColumnInformation> columnInformationCollection =
                        defaultEntityScanner.getColumnInformation(targetClass);
                nodeAlias.setColumnInformationCollection(columnInformationCollection);

                // move the ID column to the beginning
                // moveIdColumnToBeginning(columnInformationCollection);

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
    public LiquidFetch addCondition(Condition condition)
    {
        if (condition != null)
                this.condition = condition;

        return this;
    }

    /**
     * Adds a property path to the group by section
     *
     * @param propertyPath
     * @return
     * @throws org.liquidsql.util.LiquidException
     */
    @Override
    public LiquidFetch addGroupBy(String propertyPath)
    {
        if (this.groupByCollection == null)
            this.groupByCollection = new ArrayList<>();

        String formattedPropertyPath = "${" + propertyPath + "}";
        if (!groupByCollection.contains(formattedPropertyPath))
        {
            groupByCollection.add(formattedPropertyPath);
        }

        return this;

    }

    /**
     * Adds an order to the result set
     *
     * @param order
     * @return
     */
    @Override
    public LiquidFetch addOrder(Order order)
    {
        if (order != null)
        {
            if (this.orderByCollection == null)
                this.orderByCollection = new ArrayList<>();

            orderByCollection.add(order);
        }

        return this;
    }

    /**
     * Sets the offset of the result set
     *
     * @param offset
     * @return
     */
    @Override
    public LiquidFetch setOffset(int offset)
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
    public LiquidFetch setLimit(int limit)
    {
        if (limit > 0)
            this.limit = limit;

        return this;
    }

    /**
     * Executes a data fetch
     *
     * @return
     * @throws LiquidException
     */
    @Override
    public Collection executeAndFetch() throws LiquidException
    {
        Map fetchedObjectsMap = new TreeMap();
        Map collectionsMap = new TreeMap();

        try
        {
            // generate the SQL statement
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
                adaptFromResultSet(resultSet, fetchedObjectsMap, collectionsMap);
            }

            return fetchedObjectsMap.values();
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }

    /**
     * Adapts the result set data into an entity object instance
     * 
     * @param resultSet
     * @param fetchMap
     * @param collectionsMap 
     */
    private void adaptFromResultSet(ResultSet resultSet, Map fetchMap, Map collectionsMap) throws LiquidException
    {
        try
        {
            Object rootObject = null;
            Map objectMap = new HashMap<>();

            Object objectId = 0L;

            for (Integer aliasId : nodeAliasMap.keySet())
            {
                // get the node alias
                NodeAlias nodeAlias = nodeAliasMap.get(aliasId);

                // get the target class
                Class entityClass = nodeAlias.getEntityClass();
                // create an instance of this object from class
                Constructor defaultConstructor = entityClass.getConstructor(new Class[] {});
                Object entityObject = defaultConstructor.newInstance(new Object[] {});

                // if this node alias is a root alias
                if (nodeAlias.isRootNodeAlias())
                {
                    // set the created entity object as root object
                    rootObject = entityObject;

                    // add the root object to the object map
                    objectMap.put(nodeAlias.getAliasName(), entityObject);

                }
                // if the node alias is not a root alias
                else
                {
                    // obtain the parent node alias
                    NodeAlias parentNodeAlias = nodeAlias.getParentNodeAlias();
                    // get the parent object instance
                    Object parentObject = objectMap.get(parentNodeAlias.getAliasName());

                    // strip the property field from the node property path
                    String fieldName = stripFieldNameFromPropertyPath(nodeAlias.getPropertyPath());

                    // obtain the field from the parent class
                    PairedValue<Class, Field> pairedValue =
                            ReflectionsUtil.getDeclaredField(parentObject.getClass(), fieldName);

                    Field field = pairedValue.getSecondValue();

                    // obtain this field's associated modifier method
                    Method modifierMethod = pairedValue.getFirstValue().getMethod(
                            ReflectionsUtil.getModifierMethodName(field),
                            new Class[]{field.getType()});

                    // check if the field type is of a collection
                    if (Collection.class.isAssignableFrom(field.getType()))
                    {
                        // try to obtain the collection from the
                        // collections map
                        Collection collection = (Collection) collectionsMap.get(nodeAlias.getAliasName());

                        // if no collection exists, create a new one
                        // and
                        if (collection == null)
                        {
                            collection = new ArrayList();
                        }

                        //add the entity object instance created to the collection
                        collection.add(entityObject);
                        modifierMethod.invoke(parentObject, new Object[] { collection });

                        // add the collection to the collections map
                        collectionsMap.put(nodeAlias.getAliasName(), collection);
                    }
                    // else if this field type is not a collection
                    else
                    {
                        modifierMethod.invoke(parentObject, new Object[] { entityObject });
                    }

                    //map the object to the alias
                    objectMap.put(nodeAlias.getAliasName(), entityObject);
                }

                // get all the column information collection from this node
                Collection<ColumnInformation> columnInformationCollection = 
                        nodeAlias.getColumnInformationCollection();

                // iterate the column information collection
                for (ColumnInformation columnInformation : columnInformationCollection)
                {
                    // get the mapped field
                    Field field = columnInformation.getField();
                    // get the value from the result set
                    Object value = getValueFromResultSet(columnInformation.getColumnNameAs(),
                            field, resultSet);

                    // get the modifier method
                    Method method = columnInformation.getModifierMethod();
                    // invoke the modifier method
                    method.invoke(entityObject, new Object[] { value });

                    // if the node was a root alias and the column is a primary key
                    if (nodeAlias.isRootNodeAlias() && columnInformation.isPrimaryKey())
                    {
                        objectId = value;
                    }
                }

            }

            if (rootObject != null && objectId != null)
            {
                fetchMap.put(objectId, rootObject);
            }
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
    }


    /**
     * Generates the query statement
     *
     * @return
     */
    private String generateQueryStatement() throws QueryBuilderException
    {
        FetchQueryBuilder queryBuilder = new FetchQueryBuilderImpl();

        queryBuilder.setNodeAliasMap(nodeAliasMap);
        queryBuilder.setJoinInformationCollection(joinInformationCollection);
        queryBuilder.setCondition(condition);
        queryBuilder.setGroupByCollection(groupByCollection);
        queryBuilder.setOrderByCollection(orderByCollection);
        queryBuilder.setLimit(limit);
        queryBuilder.setOffset(offset);
        queryBuilder.setRootEntityClass(rootEntityClass);

        return queryBuilder.generateQuery();
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
     * Adds the node alias to the map
     * @param nodeAlias
     */
    private void addNodeAliasToMap(NodeAlias nodeAlias)
    {
        nodeAliasMap.put(nextAliasId(), nodeAlias);
    }


}
