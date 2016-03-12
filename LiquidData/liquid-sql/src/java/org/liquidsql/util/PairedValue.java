package org.liquidsql.util;

import java.io.Serializable;

public class PairedValue<A, B> implements Serializable, Cloneable
{
    private final A firstValue;
    private final B secondValue;

    public PairedValue(A firstValue, B secondValue)
    {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public A getFirstValue()
    {
        return firstValue;
    }

    public B getSecondValue()
    {
        return secondValue;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
