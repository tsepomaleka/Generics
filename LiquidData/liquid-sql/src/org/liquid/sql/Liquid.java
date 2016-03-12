package org.liquid.sql;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.liquid.model.annotation.Column;
import org.liquid.model.annotation.Id;
import org.liquid.model.annotation.JoinColumn;
import org.liquid.model.annotation.JoinTable;
import org.liquid.model.annotation.MappedSuperClass;
import org.liquid.model.annotation.OneToMany;
import org.liquid.model.annotation.OneToOne;
import org.liquid.model.annotation.Persistent;
import org.liquid.model.annotation.Table;
import org.liquid.model.annotation.Temporal;
import org.liquid.model.condition.Condition;
import org.liquid.model.domain.ColumnInformation;
import org.liquid.model.enumeration.JoinType;
import org.liquid.model.enumeration.TemporalType;
import org.liquid.util.BucketMap;
import org.liquid.util.scanner.ColumnScannerException;

public class Liquid implements Serializable
{
    private static final Logger LOG = Logger.getLogger(Liquid.class);
    
    // stores the class type mapped to its unique alias name
    private final Map<String, Class> aliasMap;
    // stores the column information scanned from an entity mapped
    // to an alias
    private final BucketMap<String, ColumnInformation> columnsBucketMap;
    
    // the root entity class
    private Class rootEntityClass;
    
    // the SELECT FROM parts
    private final Collection<String> selectParts;
    // the JOIN parts
    private final Collection<String> joinParts;
    // the WHERE parts
    private String whereParts;
    
    // the JDBC connection
    private Connection connection;
    // the JDBC prepared statement
    private PreparedStatement preparedStatement;
    // the JDBC result set
    private ResultSet resultSet;
    // the Liquid SQL configuration
    private final LiquidConfig liquidConfig;
    
    /**
     * Private constructor
     * @param liquidConfig
     */
    private Liquid(LiquidConfig liquidConfig)
    {
        this.liquidConfig = liquidConfig;
        
        this.aliasMap = new HashMap<>();
        this.columnsBucketMap = new BucketMap<>();
        
        this.selectParts = new ArrayList<>();
        this.joinParts = new ArrayList<>();
        
        this.whereParts = null;
    }
    
    /**
     * Gets an instance of the Liquid rootObject.
     * @return Liquid
     */
    public static Liquid getInstance()
    {
        Liquid liquid = new Liquid(LiquidConfig.getCurrentInstance());
        return liquid;
    }
    
    /**
     * Maps the class entity for SELECT query.
     *
     * @param entityClass
     * @param alias
     * @return Liquid
     *
     * @throws LiquidException
     */
    public Liquid createAlias(Class entityClass, String alias) throws LiquidException
    {
        try
        {
            // set this entity class as the root entity class (of entry)
            this.rootEntityClass = entityClass;
            
            // validate the entity class
            checkRootEntityClass(rootEntityClass);
            
            // put the entity class in the alias map
            this.aliasMap.put(alias, entityClass);
            
            // scan for all columns in the entity into the columns bucket map
            scanRootEntityClassForColumns(alias, rootEntityClass, columnsBucketMap);
            
            // start building the query
            StringBuilder buffer = new StringBuilder();
            appendColumnsToBuffer(buffer, columnsBucketMap.get(alias));
            
            selectParts.add(buffer.toString());
        }
        catch (Exception e)
        {
            LOG.error("Could not create an alias", e);
            throw new LiquidException(e);
        }
        return this;
    }
    
    /**
     * Adds a joining relationship to the root entity class.
     *
     * @param propertyPath
     * @param joinType
     * @return
     */
    public Liquid addJoin(String propertyPath, JoinType joinType) throws LiquidException
    {
        try
        {
            // get the alias name from the property path
            String alias = stripAliasNameFromPropertyPath(propertyPath);
            // get the field name from the property path
            String fieldName = stripFieldNameFromPropertyPath(propertyPath);
            // find the mapped class entity from the alias map using this alias
            Class entityClass = aliasMap.get(alias);
            
            boolean fieldFound = false;
            
            // get a complete field collection of this entity class
            Map<String, Field> fieldMap = getCompleteFieldMap(entityClass);
            Field field = fieldMap.get(fieldName);
            
            if (field != null &&
                    field.isAnnotationPresent(JoinColumn.class) &&
                    (field.isAnnotationPresent(OneToOne.class) ||
                    field.isAnnotationPresent(OneToMany.class)))
            {
                fieldFound = true;
                
                // get the JoinColumn annotation
                JoinColumn column = (JoinColumn) field.getAnnotation(JoinColumn.class);
                Class targetClass = column.targetClass();
                
                // add the target rootObject into the alias map using the property path
                // as its alias name
                aliasMap.put(propertyPath, targetClass);
                
                // scan for all columns in the target entity
                scanRootEntityClassForColumns(propertyPath, targetClass, columnsBucketMap);
                
                // build the query
                StringBuilder selectPartBuffer = new StringBuilder();
                appendColumnsToBuffer(selectPartBuffer, columnsBucketMap.get(propertyPath));
                
                selectParts.add(selectPartBuffer.toString());
                
                // append the columns to the select query
                String joinTableName = extractTableName(targetClass);
                
                // append the addJoin column information to the
                // selectPartBuffer
                StringBuilder joinPartBuffer = new StringBuilder();
                joinPartBuffer.append(joinType.getKeyword())
                        .append(" ").append(joinTableName)
                        .append(" ON (")
                        .append(extractTableName(entityClass)).append(".").append(column.columnName())
                        .append(" = ")
                        .append(joinTableName).append(".").append(column.referencedColumnName())
                        .append(")\n");
                
                joinParts.add(joinPartBuffer.toString());
                
                
            }
            
            if (fieldFound == false)
            {
                throw new ColumnScannerException("The field name was not found as specified in the property path.");
            }
        }
        catch (Exception e)
        {
            LOG.error("Could not add join column information.", e); 
            throw new LiquidException(e);
        }
        
        return this;
    }
    
    /**
     * Adds an intermediate relationship for a sub-selection.
     *
     * @param propertyPath
     * @param joinType
     * @return
     */
    public Liquid addIntermediateJoin(String propertyPath, JoinType joinType) throws LiquidException
    {
        try
        {
            // find the class type from the alias name
            String alias = stripAliasNameFromPropertyPath(propertyPath);
            //  find the field name
            String fieldName = stripFieldNameFromPropertyPath(propertyPath);
            // find the mapped class entity from the alias map using this alias
            Class entityClass = aliasMap.get(alias);
            // find the field in the entity class
            Field field = entityClass.getDeclaredField(fieldName);
            
            // check if the field has the required annotation
            if (field.isAnnotationPresent(JoinTable.class))
            {
                // get the addJoin table
                JoinTable table = (JoinTable) field.getAnnotation(JoinTable.class);
                Class targetClass = table.rightTargetClass();
                
                // add the target class to the alias mapping
                aliasMap.put(propertyPath, targetClass);
                
                // scan for all columns in the target entity
                scanRootEntityClassForColumns(propertyPath, targetClass, columnsBucketMap);
                
                // build the query
                StringBuilder selectPartBuffer = new StringBuilder();
                appendColumnsToBuffer(selectPartBuffer, columnsBucketMap.get(propertyPath));
                selectParts.add(selectPartBuffer.toString());
                
                // add the left addJoin info
                joinParts.add(getJoinTableExpression(table, joinType) + "\n");
                
                // add the right addJoin info
                String joinTableName = extractTableName(table.rightTargetClass());
                StringBuilder rightJoinInfoBuffer = new StringBuilder();
                
                rightJoinInfoBuffer.append(joinType.getKeyword())
                        .append(" ").append(joinTableName)
                        .append(" ON (")
                        .append(table.intermediateTableName()).append(".").append(table.rightReferenceColumnName())
                        .append(" = ")
                        .append(joinTableName).append(".").append(table.rightReferenceColumnName())
                        .append(")\n");
                
                joinParts.add(rightJoinInfoBuffer.toString());
            }
        }
        catch (Exception e)
        {
            LOG.error("Could not add the intermediate table join.", e);
            throw new LiquidException(e);
        }
        return this;
    }
    
    /**
     * Adds a condition
     *
     * @param condition
     * @return
     */
    public Liquid addCondition(Condition condition) throws LiquidException
    {
        try
        {
            // capture the condition expression
            String expression = condition.getExpression();
            
            // iterate the list of mapped columns as to their alias
            for (String alias : columnsBucketMap.keySet())
            {
                for (ColumnInformation columnInformation : columnsBucketMap.get(alias))
                {
                    // build the property path
                    String propertyPath = alias + "." + columnInformation.getFieldName();
                    // check if the property path exists in the expression
                    if (expression.contains(propertyPath))
                    {
                        // find the full column name
                        String fullColumnName = columnInformation.getFullColumnName();
                        // replace the occurence in the expression
                        expression = expression.replace(propertyPath, fullColumnName);
                    }
                }
            }
            
            this.whereParts = expression;
        }
        catch (Exception e)
        {
            LOG.error("Could not add condition.",e);
            throw new LiquidException(e);
        }
        
        return this;
    }
    
    /**
     * Executes the query and fetches the results.
     *
     * @return Collection
     */
    public Collection executeAndFetch() throws LiquidException
    {
        try
        {
            // consolidate the SQL statement
            String query = consolidateSQLParts();
            LOG.info("Generated query:\n" + query + "\n");
            
            // create a connection
            connection = liquidConfig.getConnection();
            // prepare the statement
            preparedStatement = connection.prepareStatement(query);
            // execute query
            resultSet = preparedStatement.executeQuery();
            
            // the object mapping maps
            Map fetchMap = new HashMap();
            Map collectionsMap = new HashMap();
            
            while (resultSet.next())
            {
                adaptDataFromResultSet(resultSet, fetchMap, collectionsMap);
            }
            
            return fetchMap.values();
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
        finally
        {
            safelyCloseConnection(connection);
        }
    }
    
    public boolean updateObject(Object object) throws ColumnScannerException
    {
        try
        {
            // check the entity first
            checkRootEntityClass(object.getClass());
            // get a complete field map for the entity
            Map<String, Field> fieldMap = getCompleteFieldMap(object.getClass());
            // build an update SQL
            StringBuilder buffer = new StringBuilder();
            buffer.append("UPDATE ").append(extractTableName(object.getClass())).append(" ")
                  .append("SET ");
            
            for (Field field : fieldMap.values())
            {
                // check if field has a column field
                if (field.isAnnotationPresent(Column.class))
                {
                    // get the column name
                    Column column = (Column) field.getAnnotation(Column.class);
                    String columnName = column.name();
                    
                    // get the value
                    Method accessorMethod = object.getClass()
                            .getDeclaredMethod(getAccessorMethodName(field), new Class[] {});
                    Object value = accessorMethod.invoke(object, new Object[] {});
                    
                    // get the field type
                    Class fieldType = field.getType();
                    
                    // append
                    buffer.append(columnName).append(" = ?");
                    
                    if (fieldType == String.class)
                        buffer.append("'").append(value.toString()).append("'\n");
                    else if (fieldType == Date.class)
                        buffer.append("'").append()
                            
                }
            }
            
        }
        catch (Exception e)
        {
            safelyRollback(connection);
            LOG.error("Could not update object.", e);
        }
        finally
        {
            safelyCloseConnection(connection);
        }
    }
    
    /**
     * Adapts data from the result set into entities
     *
     * @param resultSet
     * @param fetchMap
     * @param collectionsMap
     * 
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws SQLException
     * @throws ColumnScannerException
     */
    private void adaptDataFromResultSet(ResultSet resultSet, Map fetchMap, Map collectionsMap) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, ColumnScannerException, InstantiationException, NoSuchFieldException
    {
        
        Object rootObject = null;
        Map<String, Object> objectMap = new HashMap<>();
        
        Object objectId = 0L;
        boolean isRootAlias = false;
        
        for (String alias : aliasMap.keySet())
        {
            // get the entity class and the column information list
            Class entityClass = aliasMap.get(alias);
            
            // create an instance of the entity class
            Constructor constructor = entityClass.getConstructor(new Class[] {});
            Object entityObject = constructor.newInstance(new Object[] {});
            
            // if the alias is not a property path then
            // set it to the main root object
            if (!alias.contains("."))
            {
                rootObject = entityObject;
                // add the root object to the object map
                // mapped to the current alias
                objectMap.put(alias, rootObject);
                
                isRootAlias = true;
            }
            // else if the alias is a property path
            else
            {
                // get root alias
                String rootAlias = stripAliasNameFromPropertyPath(alias);
                Object parentObject = objectMap.get(rootAlias);
                // get the field name
                String fieldName = stripFieldNameFromPropertyPath(alias);
                
                // find the field from the entity class
                Field field = parentObject.getClass().getDeclaredField(fieldName);
                // find its associated setter method
                Method setterMethod = parentObject.getClass().getMethod(
                        getModifierMethodName(field), new Class[] { field.getType() } );
                // invoke the setter method
                if (Collection.class.isAssignableFrom(field.getType()))
                {
                    Collection collection = null;
                    
                    // try to find the collection from the
                    // object alias map
                    collection = (Collection) collectionsMap.get(alias);
                    
                    // if the collection is null
                    if (collection == null)
                    {
                        collection = new ArrayList();
                        collection.add(entityObject);
                        
                        setterMethod.invoke(parentObject, new Object[] { collection } );
                    }
                    // if not null, add the entity object to the collection
                    else
                    {
                        collection.add(entityObject);
                        setterMethod.invoke(parentObject, new Object[] { collection } );
                    }
                    
                    collectionsMap.put(alias, collection);
                }
                else
                {
                    setterMethod.invoke(parentObject, new Object[] { entityObject } );
                }
                
                //map the object to the alias
                objectMap.put(alias, entityObject);
                isRootAlias = false;
            }
            
            // get the columns under this entity
            Collection<ColumnInformation> columnInfoList = columnsBucketMap.get(alias);
            // get a complete field collection
            Map<String, Field> fieldMap = getCompleteFieldMap(entityClass);
            
            for (ColumnInformation columnInfo : columnInfoList)
            {
                // get the field
                Field field = fieldMap.get(columnInfo.getFieldName());
                // get the value from the result set
                Object value = getValueFromResultSet(columnInfo.getColumnNameAs(), field, resultSet);
                // build a setter method
                Method setterMethod = entityClass.getMethod(getModifierMethodName(field),
                        new Class[] { field.getType() } );
                // invoke the setter method
                setterMethod.invoke(entityObject, new Object[] { value } );
                
                if (isRootAlias && field.isAnnotationPresent(Id.class))
                {
                    objectId = getObjectId(field, entityObject);
                }
            }
        }
        
        if (rootObject != null)
        {
            fetchMap.put(objectId, rootObject);
        }
    }
    
    /**
     * Safely closes the connection
     * @param connection
     */
    private void safelyCloseConnection(Connection connection)
    {
        try
        {
            if (connection != null && connection.isClosed() == false)
                connection.close();
        }
        catch (Exception e)
        {
            LOG.error("Could not safely close connection.", e);
        }
    }
    
    /**
     * Gets the object's @Id field value.
     *
     * @param field
     * @param entityObject
     * @return Object
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private Object getObjectId(Field field, Object entityObject) throws NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if (field.isAnnotationPresent(Id.class))
        {
            String methodName = getAccessorMethodName(field);
            
            Method method = entityObject.getClass().getMethod(methodName , new Class[] {});
            return method.invoke(entityObject, new Object[] {});
        }
        
        return null;
    }
    
    /**
     * Consolidates the different SQL parts into one
     *
     * @return
     * @throws ColumnScannerException
     */
    private String consolidateSQLParts() throws ColumnScannerException
    {
        // build the SQL query from different
        // query parts.
        StringBuilder buffer = new StringBuilder();
        // add the SELECT part
        buffer.append("SELECT \n");
        
        for (String part : selectParts)
        {
            if (((ArrayList)selectParts).indexOf(part) == 0)
                buffer.append(part);
            else
                buffer.append(",\n").append(part);
        }
        
        buffer.append("\nFROM ").append(extractTableName(rootEntityClass))
                .append("\n");
        
        // add the JOIN parts
        for (String part : joinParts)
            buffer.append(part);
        buffer.append("\n");
        
        // add the WHERE parts
        buffer.append("WHERE\n")
                .append(whereParts).append("\n");
        
        return buffer.toString();
    }
    
    /**
     * Inject the values into the SQL statement template.
     *
     * @param statementTemplate
     * @param buffer
     * @param tableName
     * @param table
     * @param joinType
     * @return
     */
    private String getJoinTableExpression(JoinTable table, JoinType joinType) throws ColumnScannerException
    {
        // the sub-select template string
        String expressionTemplate ="{1} {2} ON ({3} = {4})";
        
        String leftTableName = extractTableName(table.leftTargetClass());
        
        expressionTemplate = expressionTemplate
                .replace("{1}", joinType.getKeyword())
                .replace("{2}", table.intermediateTableName())
                .replace("{3}", leftTableName + "." + table.leftReferenceColumnName())
                .replace("{4}", table.intermediateTableName() + "." + table.leftIntermediateColumnName());
        
        return expressionTemplate;
    }
    
    /**
     * Gets a complete field collection of the entity class
     * (including those that are in the mapped super classes).
     *
     * @param entityClass
     * @return
     */
    @Deprecated
    private Collection<Field> getCompleteFieldCollection(Class entityClass)
    {
        // create a new collection
        Collection<Field> fieldCollection = new ArrayList<>();
        
        // scan all the fields from the super mapped class
        Class superClass = entityClass;
        while (superClass != null)
        {
            superClass = superClass.getSuperclass();
            
            if (superClass != null)
            {
                if (superClass.isAnnotationPresent(MappedSuperClass.class))
                {
                    fieldCollection.addAll(Arrays.asList(superClass.getDeclaredFields()));
                }
            }
        }
        
        // then scan the current entity class for fields
        fieldCollection.addAll(Arrays.asList(entityClass.getDeclaredFields()));
        
        return fieldCollection;
    }
    
    /**
     * Gets a complete field collection of the entity class
     * (including those that are in the mapped super classes).
     *
     * @param entityClass
     * @return
     */
    private Map<String, Field> getCompleteFieldMap(Class entityClass)
    {
        // create a new collection
        Map<String, Field> fieldMap = new HashMap<>();
        
        // scan all the fields from the super mapped class
        Class superClass = entityClass;
        while (superClass != null)
        {
            superClass = superClass.getSuperclass();
            
            if (superClass != null)
            {
                if (superClass.isAnnotationPresent(MappedSuperClass.class))
                {
                    for (Field field : superClass.getDeclaredFields())
                    {
                        fieldMap.put(field.getName(), field);
                    }
                }
            }
        }
        
        // then scan the current entity class for fields
        for (Field field : entityClass.getDeclaredFields())
        {
            fieldMap.put(field.getName(), field);
        }
        
        return fieldMap;
    }
    
    /**
     * Strips the alias name from the property path.
     * @param propertyPath
     * @return String
     */
    private String stripAliasNameFromPropertyPath(String propertyPath)
    {
        if (!propertyPath.contains("."))
            return propertyPath;
        else
        {
            String[] parts = propertyPath.split("\\.");
            int length = parts.length;
            return propertyPath.substring(0, propertyPath.indexOf(parts[length - 1]) - 1);
        }
    }
    
    /**
     * Strips the field name from the property path.
     * @param propertyPath
     * @return
     * @throws ColumnScannerException
     */
    private String stripFieldNameFromPropertyPath(String propertyPath) throws ColumnScannerException
    {
        if (!propertyPath.contains("."))
            throw new ColumnScannerException("The property path does not contain any field"
                    + " names as contained in the entity class.");
        else
        {
            String[] parts = propertyPath.split("\\.");
            int length = parts.length;
            return (parts[length - 1]);
        }
    }
    
    /**
     * Appends columns to the selectPartBuffer
     *
     * @param buffer
     * @param tableName
     * @param columns
     */
    private void appendColumnsToBuffer(StringBuilder buffer, Collection<ColumnInformation> columns)
    {
        Iterator<ColumnInformation> iterator = columns.iterator();
        while (iterator.hasNext())
        {
            ColumnInformation columnInformation = iterator.next();
            
            if (iterator.hasNext())
            {
                buffer.append(columnInformation.getFullColumnName()).append(" AS ").append(columnInformation.getColumnNameAs())
                        .append(",\n");
            }
            else
            {
                buffer.append(columnInformation.getFullColumnName()).append(" AS ").append(columnInformation.getColumnNameAs());
            }
        }
    }
    
    /**
     * Checks for validations in the root entity class.
     *
     * @param rootEntityClass
     * @throws ColumnScannerException
     */
    private void checkRootEntityClass(Class rootEntityClass) throws ColumnScannerException
    {
        // check if the root entity class has the persistent
        // annotation
        if (!rootEntityClass.isAnnotationPresent(Persistent.class))
        {
            throw new ColumnScannerException("The entity is not annotated as being a persistent object.");
        }
        
        // check if the root entity class has the table annotation
        if (!rootEntityClass.isAnnotationPresent(Table.class) && !rootEntityClass.isAnnotationPresent(MappedSuperClass.class))
        {
            throw new ColumnScannerException("The entity is does not have the Table annotation present.");
        }
    }
    
    /**
     * Gets the master table name.
     *
     * @param rootEntityClass
     * @return
     * @throws ColumnScannerException
     */
    private String extractTableName(Class rootEntityClass) throws ColumnScannerException
    {
        Table table = (Table) rootEntityClass.getAnnotation(Table.class);
        if (table == null) return null;
        else return table.name();
    }
    
    /**
     * Scans for columns in the entity class
     *
     * @param rootEntityClass
     * @param columnsBucket
     */
    private void scanRootEntityClassForColumns(String aliasName, Class rootEntityClass, BucketMap<String, ColumnInformation> columnsBucket) throws ColumnScannerException
    {
        checkRootEntityClass(rootEntityClass);
        
        // get a complete list of all fields
        Collection<Field> fieldCollection = getCompleteFieldCollection(rootEntityClass);
        
        // scan the fields in the class
        for (Field field : fieldCollection)
        {
            // if the Column or Join Column annotation is present
            if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class))
            {
                ColumnInformation columnInformation = createColumnInformation(rootEntityClass, field);
                
                // add the column information to the
                // bucket map
                columnsBucket.put(aliasName, columnInformation);
            }
        }
    }
    
    /**
     * Creates an instance of the column information.
     *
     * @param rootEntityClass
     * @param field
     * @return
     */
    private ColumnInformation createColumnInformation(Class rootEntityClass, Field field) throws ColumnScannerException
    {
        String tableName = extractTableName(rootEntityClass);
        
        ColumnInformation columnInformation = new ColumnInformation(tableName);
        
        columnInformation.setFieldName(field.getName());
        columnInformation.setEntityClass(rootEntityClass);
        columnInformation.setColumnType(field.getType());
        
        // check for the Id annotation
        if (field.isAnnotationPresent(Id.class))
        {
            columnInformation.setPrimaryKey(true);
        }
        
        // check for the Column annotation
        if (field.isAnnotationPresent(Column.class))
        {
            Column column = (Column) field.getAnnotation(Column.class);
            columnInformation.setColumnName(column.name());
        }
        
        return columnInformation;
    }
    
    /**
     * Gets the setter method name
     *
     * @param field
     * @return
     */
    private String getModifierMethodName(Field field)
    {
        String fieldName = field.getName();
        return "set" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
    }
    
    /**
     * Gets the getter method name
     * @param field
     * @return
     */
    private String getAccessorMethodName(Field field)
    {
        String fieldName = field.getName();
        Class type = field.getType();
        
        String partialMethodName = fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
        
        if (type == Boolean.class || type == boolean.class)
        {
            return "is" + partialMethodName;
        }
        else
        {
            return "get" + partialMethodName;
        }
    }
    
    /**
     * Gets the value from the result set.
     *
     * @param fullColumnName
     * @param type
     * @param rs
     * @return
     * @throws SQLException
     */
    private Object getValueFromResultSet(String fullColumnName, Field field, ResultSet rs) throws SQLException, ColumnScannerException
    {
        Class type = field.getType();
        
        if (type == Boolean.class || type == boolean.class)
            return rs.getBoolean(fullColumnName);
        
        else if (type == Integer.class || type == int.class)
            return rs.getInt(fullColumnName);
        
        else if (type == Long.class || type == long.class)
            return rs.getLong(fullColumnName);
        
        else if (type == Short.class || type == short.class)
            return rs.getShort(fullColumnName);
        
        else if (type == Double.class || type == double.class)
            return rs.getDouble(fullColumnName);
        
        else if (type == String.class)
            return rs.getString(fullColumnName);
        
        else if (type == BigDecimal.class)
            return rs.getBigDecimal(fullColumnName);
        
        else if (type == Date.class)
        {
            TemporalType temporalType = null;
            
            if (field.isAnnotationPresent(Temporal.class))
            {
                Temporal temporal = (Temporal) field.getAnnotation(Temporal.class);
                temporalType = temporal.temporalType();
                
                if (temporalType == TemporalType.DATE)
                    return rs.getDate(fullColumnName);
                else if (temporalType == TemporalType.TIME)
                    return rs.getTime(fullColumnName);
                else
                    return rs.getTimestamp(fullColumnName);
            }
            else
                throw new ColumnScannerException("Date type fields must be annotated "
                        + "with @Temporal");
        }
        
        return null;
    }

    private void safelyRollback(Connection connection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
