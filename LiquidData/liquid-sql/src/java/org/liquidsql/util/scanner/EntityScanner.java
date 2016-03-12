package org.liquidsql.util.scanner;

import org.liquidsql.model.column.ColumnInformation;
import org.liquidsql.model.join.JoinColumnInformation;
import org.liquidsql.model.join.JoinTableInformation;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public interface EntityScanner 
{
    /**
     * Gets a complete collection of column information
     * for all fields that were annotated with @Column and @Id
     * 
     * @param entityClass
     * @return Collection
     * @throws EntityScannerException 
     */
    public Collection<ColumnInformation> getColumnInformation(Class entityClass) throws EntityScannerException;
    
    /**
     * Gets a complete map of all the fields in the entity 
     * class that were annotated with @Column and @Id
     * 
     * @param entityClass
     * @return Map
     * @throws EntityScannerException 
     */
    public Map<String, Field> getCompleteFieldMap(Class entityClass) throws EntityScannerException;
    
    /**
     *  Gets a complete collection of all the fields in the entity 
     *  class that were annotated with @Column and @Id
     * 
     * @param entityClass
     * @return
     * @throws EntityScannerException 
     */
    public Collection<Field> getCompleteFieldCollection(Class entityClass) throws EntityScannerException;
    
    /**
     * Gets a complete collection of all the join column information
     * in the entity class.
     * 
     * Deprecated: Please use the getTableJoinInfoForJoinColumn(Field field, Class entityClass)
     * instead.
     * 
     * @param entityClass
     * @return
     * @throws EntityScannerException 
     */
    @Deprecated
    public Collection<JoinColumnInformation> getJoinColumnInformation(Class entityClass) throws EntityScannerException;
    
    /**
     * Extracts table join information for a @JoinColumn annotated field.
     * @param field
     * @param entityClass
     * @return
     * @throws EntityScannerException 
     */
    public JoinColumnInformation getTableJoinInfoForJoinColumn(Field field, Class entityClass) throws EntityScannerException;
    
    /**
     * Extracts table join information for a @JoinTable annotated field.
     * 
     * @param field
     * @return
     * @throws EntityScannerException 
     */
    public JoinTableInformation getTableJoinInfoForJoinTable(Field field) throws EntityScannerException;
    
    /**
     * Gets the table name associated with this entity class.
     * 
     * @param entityClass
     * @return String
     */
    public String getTableName(Class entityClass);
    
    
}
