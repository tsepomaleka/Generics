package org.liquidsql.model.join;

import org.liquidsql.model.enumeration.JoinType;


public interface JoinColumnInformation
{
    /**
     * Gets the join type
     * @return JoinType
     */
    public JoinType getJoinType();
    
    /**
     * Sets the join type
     * @param joinType 
     */
    public void setJoinType(JoinType joinType);
    
    /**
     * Gets the left table name
     * @return String
     */
    public String getLeftTableName();
    
    /**
     * Sets the left table name
     * @param leftTableName 
     */
    public void setLeftTableName(String leftTableName);
    
    /**
     * Gets the right table name
     * @return String
     */
    public String getRightTableName();
    
    /**
     * Sets the right table name
     * @param rightTableName 
     */
    public void setRightTableName(String rightTableName);
    
    /**
     * Gets the left column name
     * @return String
     */
    public String getLeftColumnName();
    
    /**
     * Sets the left column name
     * @param leftColumnName 
     */
    public void setLeftColumnName(String leftColumnName);
    
    /**
     * Gets the right column name
     * @return String
     */
    public String getRightColumnName();
    
    /**
     * Sets the right column name
     * @param rightColumnName 
     */
    public void setRightColumnName(String rightColumnName);
}
