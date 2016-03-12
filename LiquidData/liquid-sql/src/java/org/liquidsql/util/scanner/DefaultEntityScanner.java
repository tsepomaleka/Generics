package org.liquidsql.util.scanner;

import org.liquidsql.model.annotation.*;
import org.liquidsql.model.column.ColumnInformation;
import org.liquidsql.model.column.ColumnInformationImpl;
import org.liquidsql.model.join.JoinColumnInformation;
import org.liquidsql.model.join.JoinColumnInformationImpl;
import org.liquidsql.model.join.JoinTableInformation;
import org.liquidsql.model.join.JoinTableInformationImpl;
import org.liquidsql.util.reflections.ReflectionsUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class DefaultEntityScanner implements EntityScanner
{
    private static EntityScanner INSTANCE  = null;
    
    private DefaultEntityScanner() {}
    
    /**
     * Gets the instance of the entity scanner utility
     *
     * @return EntityScanner
     */
    public static EntityScanner getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new DefaultEntityScanner();
        
        return INSTANCE;
    }
    
    @Override
    public final Collection<ColumnInformation> getColumnInformation(Class entityClass) throws EntityScannerException
    {
        // validate the entity class
        checkEntityClass(entityClass);
        
        Collection<ColumnInformation> columnInfoCollection = new ArrayList<>();
        
        // get a complete field map of all fields
        Collection<Field> fieldCollection = getCompleteFieldCollection(entityClass);
        // if the field map is not empty
        if (fieldCollection != null && !fieldCollection.isEmpty())
        {
            // scan each field for column information
            for (Field field : fieldCollection)
            {
                // if the Column or Join Column annotation is present
                if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class))
                {
                    ColumnInformation columnInformation = createColumnInformation(entityClass, field);
                    columnInfoCollection.add(columnInformation);
                }
            }
        }
        
        return columnInfoCollection;
    }
    
    /**
     * Creates an instance of the column information.
     *
     * @param rootEntityClass
     * @param field
     * @return ColumnInformation
     */
    private ColumnInformation createColumnInformation(Class rootEntityClass, Field field) throws EntityScannerException
    {
        try
        {
            String tableName = getTableName(rootEntityClass);

            ColumnInformation columnInformation = new ColumnInformationImpl(tableName);

            // set the common column information
            columnInformation.setColumnType(field.getType());
            columnInformation.setDistinct(false);

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

            // set the property information
            columnInformation.setField(field);
            columnInformation.setEntityClass(rootEntityClass);

            String methodName =  ReflectionsUtil.getAccessorMethodName(field);
            columnInformation.setAccessorMethod(rootEntityClass.getMethod(methodName, new Class[] {}));

            methodName = ReflectionsUtil.getModifierMethodName(field);
            columnInformation.setModifierMethod(rootEntityClass.getMethod(methodName, new Class[] { field.getType() }));

            for (Annotation annotation : field.getDeclaredAnnotations())
                columnInformation.addAnnotation(annotation);

            return columnInformation;
        }
        catch (Exception e)
        {
            throw new EntityScannerException(e);
        }
    }
    
    /**
     * Checks for validations in the root entity class.
     *
     * @param entityClass
     * @throws EntityScannerException
     */
    private void checkEntityClass(Class entityClass) throws EntityScannerException
    {
        // check if the root entity class has the persistent
        // annotation
        if (!entityClass.isAnnotationPresent(Entity.class))
        {
            throw new EntityScannerException("The entity is not annotated as being a persistent object.");
        }
        
        // check if the root entity class has the table annotation
        if (!entityClass.isAnnotationPresent(Table.class) && !entityClass.isAnnotationPresent(MappedSuperClass.class))
        {
            throw new EntityScannerException("The entity is does not have the Table annotation present.");
        }
    }
    
    @Override
    public final Map<String, Field> getCompleteFieldMap(Class entityClass) throws EntityScannerException
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
    
    @Override
    public final Collection<JoinColumnInformation> getJoinColumnInformation(Class entityClass) throws EntityScannerException
    {
        // validate the entity
        checkEntityClass(entityClass);
        
        Collection<JoinColumnInformation> collection = new ArrayList<>();
        
        // get a complete field map
        Map<String, Field> fieldMap = getCompleteFieldMap(entityClass);
        
        if (fieldMap != null && !fieldMap.isEmpty())
        {
            for (Field field : fieldMap.values())
            {
                // check if the field has a JoinColumn annotation
                if (field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(JoinRelation.class))
                {
                    JoinColumnInformation information = (JoinColumnInformation) getTableJoinInfoForJoinColumn(field, entityClass);
                    if (information != null)
                        collection.add(information);
                }
            }
        }
        
        return collection;
    }
    
    @Override
    public final String getTableName(Class entityClass)
    {
        if (entityClass.isAnnotationPresent(Table.class))
        {
            Table table = (Table) entityClass.getAnnotation(Table.class);
            return table.name();
        }
        return null;
    }
    
    @Override
    public JoinColumnInformation getTableJoinInfoForJoinColumn(Field field, Class entityClass) throws EntityScannerException
    {
        // check if the field has a JoinColumn annotation
        if (field.isAnnotationPresent(JoinColumn.class) && field.isAnnotationPresent(JoinRelation.class))
        {
            JoinColumn column = (JoinColumn) field.getAnnotation(JoinColumn.class);
            JoinRelation relation = (JoinRelation) field.getAnnotation(JoinRelation.class);
            
            JoinColumnInformation information = new JoinColumnInformationImpl();
            
            // resolve the left table information (host table)
            information.setLeftColumnName(column.columnName());
            information.setLeftTableName(getTableName(entityClass));
            
            // resolve the right table information (referenced table)
            information.setRightColumnName(column.referencedColumnName());
            information.setRightTableName(getTableName(column.targetClass()));
            
            // resolve the relation type
            information.setJoinType(relation.value());
            
            return information;
        }
        
        return null;
    }
    
    @Override
    public JoinTableInformation getTableJoinInfoForJoinTable(Field field) throws EntityScannerException
    {
        if (field.isAnnotationPresent(JoinTable.class) && field.isAnnotationPresent(JoinRelation.class))
        {
            JoinTable table = (JoinTable) field.getAnnotation(JoinTable.class);
            JoinRelation relation = (JoinRelation) field.getAnnotation(JoinRelation.class);
            
            JoinTableInformation information = new JoinTableInformationImpl();
            
            // resolve the intermediate table info
            information.setIntermediateTableName(table.tableName());
            
            // resolve the left table information (host table)
            JoinColumnInformation leftJoinColumnInformation = new JoinColumnInformationImpl();
            leftJoinColumnInformation.setJoinType(relation.value());
            
            leftJoinColumnInformation.setLeftTableName(getTableName(table.leftJoinColumn().targetClass()));
            leftJoinColumnInformation.setLeftColumnName(table.leftJoinColumn().columnName());
            
            leftJoinColumnInformation.setRightTableName(table.tableName());
            leftJoinColumnInformation.setRightColumnName(table.leftJoinColumn().referencedColumnName());
            
            information.setLeftJoinColumnInformation(leftJoinColumnInformation);
            
            // resolve the right table information (inverse table)
            JoinColumnInformation rightJoinColumnInformation = new JoinColumnInformationImpl();
            rightJoinColumnInformation.setJoinType(relation.value());
            
            rightJoinColumnInformation.setLeftTableName(table.tableName());
            rightJoinColumnInformation.setLeftColumnName(table.rightJoinColumn().referencedColumnName()); 
            
            rightJoinColumnInformation.setRightTableName(getTableName(table.rightJoinColumn().targetClass()));
            rightJoinColumnInformation.setRightColumnName(table.rightJoinColumn().columnName());
            
            information.setRightJoinColumnInformation(rightJoinColumnInformation);

            return information;
        }
        return null;
    }

    @Override
    public Collection<Field> getCompleteFieldCollection(Class entityClass) throws EntityScannerException 
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
    
}
