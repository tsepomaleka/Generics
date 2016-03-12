package org.liquidsql.model.column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface PropertyInformation 
{
    /**
     * Gets the entity class
     * @return
     */
    public Class getEntityClass();
    /**
     * Sets the entity class
     * @param entityClass
     */
    public void setEntityClass(Class entityClass);
    /**
     * Gets the reflective field
     * @return Field
     */
    public Field getField();
    
    /**
     * Sets the reflective field
     * @param field 
     */
    public void setField(Field field);
    
    /**
     * Gets the modifier method
     * @return Method
     */
    public Method getModifierMethod();
    
    /**
     * Sets the modifier method.
     * @param method 
     */
    public void setModifierMethod(Method method);

    /**
     * Gets the accessor method
     * @return
     */
    public Method getAccessorMethod();

    /**
     * Sets the modifier method
     * @param method
     */
    public void setAccessorMethod(Method method);
    
    /**
     * Determines if the property has the annotation
     * @param annotationClass
     * @return 
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass);
    
    /**
     * Adds an annotation 
     * @param annotation 
     */
    public void addAnnotation(Annotation annotation);
    
    /**
     * Gets an annotation
     * @param annotationClass
     * @return Annotation
     */
    public Annotation getAnnotation(Class annotationClass);
    
    /**
     * Determines if this property is assignable
     * from a collection
     * @return boolean
     */
    public boolean isPropertyCollection();
}
