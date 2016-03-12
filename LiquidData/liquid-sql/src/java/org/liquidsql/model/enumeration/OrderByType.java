package org.liquidsql.model.enumeration;

public enum OrderByType
{
    ASCENDING("ASC"),
    DESCENDING("DESC")
    ;

    // the SQL expression
    private final String expression;

    private OrderByType(String expression)
    {
        this.expression = expression;
    }

    /**
     * Gets the SQL expression
     * @return
     */
    public String getExpression()
    {
        return expression;
    }
}
