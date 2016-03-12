package org.liquid.model.enumeration;

public enum JoinType 
{
    INNER_JOIN("JOIN"), 
    LEFT_OUTER_JOIN("LEFT OUTER JOIN"), 
    FULL_JOIN("FULL JOIN"), 
    RIGHT_OUTER_JOIN("RIGHT JOIN")
    ;
    
    private final String keyword;
    
    private JoinType(String keyword)
    {
        this.keyword = keyword;
    }

    public String getKeyword() 
    {
        return keyword;
    }
}
