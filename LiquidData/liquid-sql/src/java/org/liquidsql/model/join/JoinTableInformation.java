package org.liquidsql.model.join;

public interface JoinTableInformation 
{
    /**
     * Gets the intermediate (or bridging) table name.
     * @return String
     */
    public String getIntermediateTableName();
    
    /**
     * Sets the intermediate (or bridging) table name.
     * @param intermediateTableName The name of the intermediate (or bridging) table name
     */
    public void setIntermediateTableName(String intermediateTableName);
    
    /**
     * Gets the left join column information
     * @return JoinColumnInformation
     */
    public JoinColumnInformation getLeftJoinColumnInformation();
    
    /**
     * Sets the left join column information
     * @param information 
     */
    public void setLeftJoinColumnInformation(JoinColumnInformation information);
    
    /**
     * Gets the right join column information
     * @return JoinColumnInformation
     */
    public JoinColumnInformation getRightJoinColumnInformation();
    
    /**
     * Sets the right column information
     * @param information 
     */
    public void setRightJoinColumnInformation(JoinColumnInformation information);
}
