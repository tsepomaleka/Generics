package org.liquidsql.model.join;

import org.liquidsql.model.enumeration.JoinType;

public class JoinColumnInformationImpl implements JoinColumnInformation
{
    protected JoinType joinType;
    
    protected String leftTableName;
    protected String rightTableName;
    
    protected String leftColumnName;
    protected String rightColumnName;
    
    public JoinColumnInformationImpl() {}
     
    @Override
    public JoinType getJoinType() 
    {
        return joinType;
    }

    @Override
    public void setJoinType(JoinType joinType) 
    {
        this.joinType = joinType;
    }
    
    @Override
    public String getLeftTableName() 
    {
        return leftTableName;
    }

    @Override
    public void setLeftTableName(String leftTableName) 
    {
        this.leftTableName = leftTableName;
    }

    @Override
    public String getRightTableName() 
    {
        return rightTableName;
    }

    @Override
    public void setRightTableName(String rightTableName) 
    {
        this.rightTableName = rightTableName;
    }


    @Override
    public String getLeftColumnName() 
    {
        return leftColumnName;
    }

    @Override
    public void setLeftColumnName(String leftColumnName) 
    {
        this.leftColumnName = leftColumnName;
    }

    @Override
    public String getRightColumnName() 
    {
        return rightColumnName;
    }

    @Override
    public void setRightColumnName(String rightColumnName) 
    {
        this.rightColumnName = rightColumnName;
    }
    
}
