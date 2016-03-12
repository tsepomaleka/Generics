package org.liquidsql.model.annotation;

import org.liquidsql.model.enumeration.TemporalType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Temporal 
{
    public TemporalType value() default TemporalType.TIMESTAMP;
}
