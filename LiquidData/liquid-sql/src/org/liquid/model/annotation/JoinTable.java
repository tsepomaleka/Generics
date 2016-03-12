package org.liquid.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinTable 
{
    
    public String intermediateTableName();
    
    public Class leftTargetClass();
    public String leftReferenceColumnName();
    public String leftIntermediateColumnName();
    
    public Class rightTargetClass();
    public String rightReferenceColumnName();
    public String rightIntermediateReferenceColumnName();
    
}
