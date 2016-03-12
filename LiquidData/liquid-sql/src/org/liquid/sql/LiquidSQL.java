package org.liquid.sql;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

public class LiquidSQL implements Serializable
{
    private static final Logger LOG = Logger.getLogger(LiquidSQL.class);
    
    // stores the class type mapped to its unique alias name
    private final Map<String, Class> aliasMap;
    // stores the column information scanned from an entity mapped
    // to an alias
    private final BucketMap<String, ColumnInformation> columnsBucketMap;
    // the collection of alias staged for sub-select
    private final Map<String, String> stagedForSubSelect;
    
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
    private LiquidSQL(LiquidConfig liquidConfig)
    {
        this.liquidConfig = liquidConfig;
        
        this.aliasMap = new HashMap<>();
        this.columnsBucketMap = new BucketMap<>();
        
        this.stagedForSubSelect = new HashMap<>();
        
        this.selectParts = new ArrayList<>();
        this.joinParts = new ArrayList<>();
        
        this.whereParts = null;
    }
    
    /**
     * Gets an instance of the LiquidSQL object.
     * @return LiquidSQL
     */
    public static LiquidSQL getInstance()
    {
        LiquidSQL liquidSQL = new LiquidSQL(LiquidConfig.getCurrentInstance());
        return liquidSQL;
    }
    
    /**
     * Maps the class entity for SELECT query.
     * 
     * @param entityClass
     * @param alias
     * @return LiquidSQL
     * 
     * @throws LiquidException 
     */
    public LiquidSQL mapForSelect(Class entityClass, String alias) throws LiquidException
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
            String tableName = extractTableName(rootEntityClass);
            appendColumnsToStatementBuffer(buffer, tableName, columnsBucketMap.get(alias));
            
            selectParts.add(buffer.toString());
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
        finally 
        {
            
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
    public LiquidSQL join(String propertyPath, JoinType joinType)
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
            Collection<Field> fieldCollection = getCompleteFieldCollection(entityClass);
            // scan all fields
            for (Field field : fieldCollection) 
            {
                if (field.getName().equals(fieldName) && 
                        field.isAnnotationPresent(JoinColumn.class) && 
                        (field.isAnnotationPresent(OneToOne.class) || 
                        field.isAnnotationPresent(OneToMany.class)))
                {
                    fieldFound = true;
                    
                    // get the JoinColumn annotation
                    JoinColumn column = (JoinColumn) field.getAnnotation(JoinColumn.class);
                    Class targetClass = column.targetClass();
                    
                    // add the target object into the alias map using the property path
                    // as its alias name
                    aliasMap.put(propertyPath, targetClass);
                    
                    // scan for all columns in the target entity
                    scanRootEntityClassForColumns(propertyPath, targetClass, columnsBucketMap);
                    
                    // build the query
                    StringBuilder buffer = new StringBuilder();
                    String tableName = extractTableName(targetClass);
                    appendColumnsToStatementBuffer(buffer, tableName, columnsBucketMap.get(alias));

                    selectParts.add(buffer.toString());
                    
                    // append the columns to the select query
                    String joinTableName = extractTableName(targetClass);
               
                    // append the join column information to the
                    // buffer
                    StringBuilder buffer0 = new StringBuilder();
                    buffer0.append(joinType.getKeyword())
                            .append(" ON (")
                                .append(extractTableName(entityClass)).append(".").append(column.columnName())
                                .append(" = ")
                                .append(joinTableName).append(".").append(column.referencedColumnName())
                            .append(")\n");
                    
                    joinParts.add(buffer0.toString());
                    
                    break;
                }
            }
            
            if (fieldFound == false)
            {
                throw new ColumnScannerException("The field name was not found as specified in the property path.");
            }
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
        finally 
        {
            
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
    public LiquidSQL subSelect(String propertyPath, JoinType joinType)
    {
        try
        {
            
            StringBuilder buffer = null;
            
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
                // get the join table
                JoinTable table = (JoinTable) field.getAnnotation(JoinTable.class);
                Class targetClass = table.targetClass();
                
                // add the target class to the alias mapping
                aliasMap.put(propertyPath, targetClass);
                
                // collect template requirements & build query
                // 0 - collect all columns
                String tableName = extractTableName(targetClass);
                        
                Collection<Field> fieldCollection = getCompleteFieldCollection(targetClass);
                Collection<ColumnInformation> columnInfoCollection = new ArrayList<>();
                
                for (Field f : fieldCollection)
                {
                    ColumnInformation columnInformation = 
                            createColumnInformation(targetClass, f);
                    columnInfoCollection.add(columnInformation);
                }
                
                buffer = new StringBuilder();
                Iterator<ColumnInformation> iterator = columnInfoCollection.iterator();
                
                while (iterator.hasNext())
                {
                    ColumnInformation c = iterator.next();
                    buffer.append(tableName).append(".").append(c.getColumnName());
                    
                    if (iterator.hasNext()) buffer.append(", \n");
                }
                
                // map the SQL to the property path
                stagedForSubSelect.put(propertyPath, injectValuesIntoStatementTemplate(buffer.toString(),
                        table, joinType));
            }
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
        finally 
        {
            
        }
        
        return this;
    }
    
    /**
     * Adds a condition 
     * 
     * @param condition
     * @return 
     */
    public LiquidSQL addCondition(Condition condition)
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
                        String tableName = extractTableName(columnInformation.getEntityClass());
                        String fullColumnName = tableName + "." + columnInformation.getColumnName();
                        
                        // replace the occurence in the expression
                        expression = expression.replace(propertyPath, fullColumnName);
                    }
                }
            }
            
            this.whereParts = expression;
        }
        catch (Exception e)
        {
            throw new LiquidException(e);
        }
        finally 
        {
            
        }
        
        return this;
    }
    
    /**
     * Executes the query and fetches the results.
     * 
     * @return Collection
     * @throws ColumnScannerException 
     */
    public Collection executeFetch() throws ColumnScannerException
    {
        Map fetchMap = new HashMap();
        
        // build the SQL query from different 
        // query parts.
        StringBuilder buffer = new StringBuilder();
        // add the SELECT part
        buffer.append("SELECT \n");
        
        for (String part : selectParts)
            buffer.append(part);
        buffer.append("\nFROM ").append(extractTableName(rootEntityClass))
             .append("\n");
        
        // add the JOIN parts
        for (String part : joinParts)
           buffer.append(part);
        buffer.append("\n");
        
        // add the WHERE parts
        buffer.append("WHERE\n")
                .append(whereParts).append("\n");
        
        System.out.println(buffer.toString());
        
        return fetchMap.values();
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
    private String injectValuesIntoStatementTemplate(String delimitedColumns, JoinTable table, JoinType joinType) throws ColumnScannerException
    {
        // the sub-select template string
        String statementTemplate = 
                    "SELECT \n{0} \nFROM {1} \n" +
                    "{2} {3} ON ({4} = {5}) \n" +
                    "WHERE {6} = ?";
        
        String inverseTableName = extractTableName(table.inverseTargetClass());
       
        statementTemplate = statementTemplate
                .replace("{0}", delimitedColumns)
                .replace("{1}", inverseTableName)
                .replace("{2}", joinType.getKeyword())
                .replace("{3}", table.intermediateTableName())
                .replace("{4}", inverseTableName + "." + table.inverseRefColumnName())
                .replace("{5}", table.intermediateTableName() + "." + table.inverseRefColumnName())
                .replace("{6}", table.intermediateTableName() + "." + table.referenceColumnName());
        
        return statementTemplate;
    }
    
    /**
     * Gets a complete field collection of the entity class
     * (including those that are in the mapped super classes).
     * 
     * @param entityClass
     * @return 
     */
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
     * Appends columns to the statement buffer
     * 
     * @param buffer
     * @param tableName
     * @param columns 
     */
    private void appendColumnsToStatementBuffer(StringBuilder buffer, String tableName, Collection<ColumnInformation> columns)
    {
        Iterator<ColumnInformation> iterator = columns.iterator();
        while (iterator.hasNext())
        {
            ColumnInformation columnInformation = iterator.next();
            String fullColumnName = tableName + "." + columnInformation.getColumnName();
            
            if (iterator.hasNext())
            {
                buffer.append(fullColumnName).append(", \n");
            }
            else
            {
                buffer.append(fullColumnName);
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
    private ColumnInformation createColumnInformation(Class rootEntityClass, Field field)
    {
        ColumnInformation columnInformation = new ColumnInformation();
                            
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
     * Gets the setter method
     * 
     * @param field
     * @return 
     */
    private String getSetterMethod(Field field) 
    {
        String fieldName = field.getName();
        return "set" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
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
        Class type = field.getClass();
        
        if (type == Boolean.class)
            return rs.getBoolean(fullColumnName);
        
        else if (type == Integer.class)
            return rs.getInt(fullColumnName);
        
        else if (type == Long.class)
            return rs.getLong(fullColumnName);
        
        else if (type == Double.class)
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
}
