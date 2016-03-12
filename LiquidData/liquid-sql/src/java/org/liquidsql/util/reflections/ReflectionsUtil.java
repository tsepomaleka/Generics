package org.liquidsql.util.reflections;

import org.liquidsql.model.annotation.Column;
import org.liquidsql.model.annotation.Id;
import org.liquidsql.model.annotation.MappedSuperClass;
import org.liquidsql.util.PairedValue;
import org.liquidsql.util.scanner.DefaultEntityScanner;
import org.liquidsql.util.scanner.EntityScanner;
import org.liquidsql.util.scanner.EntityScannerException;

import java.lang.reflect.Field;
import java.util.Collection;

public final class ReflectionsUtil 
{
    /**
     * Gets the setter method name
     *
     * @param field
     * @return
     */
    public static String getModifierMethodName(Field field)
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
    public static String getAccessorMethodName(Field field)
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
     * Gets the field that contains the associated column name from
     * the entity class.
     * 
     * @param parentClass
     * @param columnName
     * @return
     * @throws EntityScannerException 
     */
    public static Field getAssociatedFieldFromColumnName(Class parentClass, String columnName) throws EntityScannerException
    {
        EntityScanner scanner = DefaultEntityScanner.getInstance();
        //get a complete list of all the fields in the parent class
        Collection<Field> fieldCollection = scanner.getCompleteFieldCollection(parentClass);
        // scan the fields
        for (Field field : fieldCollection)
        {
            if (field.isAnnotationPresent(Column.class))
            {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column.name().equals(columnName))
                {
                    return field;
                }
            }
        }
        
        return null;
    }

    /**
     * Gets the paired value of a Class and Field
     * that contain the declared field with the @Id annotation
     * @param entityClass
     * @return
     * @throws EntityScannerException
     */
    public static PairedValue<? extends Class, Field> getFieldAnnotatedWithId(Class entityClass) throws EntityScannerException
    {
        EntityScanner scanner = DefaultEntityScanner.getInstance();
        //get a complete list of all the fields in the parent class
        Collection<Field> fieldCollection = scanner.getCompleteFieldCollection(entityClass);
        // scan the fields
        for (Field field : fieldCollection)
        {
            if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class))
            {
                PairedValue<? extends Class, Field> pairedValue = new PairedValue<>(field.getDeclaringClass(), field);
                return pairedValue;
            }
        }

        return null;
    }

    /**
     * Gets a declared field, scanning also from the super mapped class
     *
     * @param entityClass
     * @param fieldName
     * @return
     * @throws EntityScannerException
     */
    public static PairedValue<Class, Field> getDeclaredField(Class entityClass, String fieldName) throws EntityScannerException
    {
        PairedValue<Class, Field> pairedValue = null;

        // scan all the fields from the super mapped class
        // to find the field
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
                        if (field.getName().equals(fieldName))
                        {
                            pairedValue = new PairedValue<>(superClass, field);
                            return pairedValue;
                        }
                    }
                }
            }
        }

        // if the field was not found in the super classes,
        // scan the current entity class
        for (Field field : entityClass.getDeclaredFields())
        {
            if (field.getName().equals(fieldName))
            {
                pairedValue = new PairedValue<>(entityClass, field);
                return pairedValue;
            }
        }

        return null;
    }
}
