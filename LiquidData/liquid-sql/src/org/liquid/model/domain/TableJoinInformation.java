package org.liquid.model.domain;

import org.liquid.model.enumeration.JoinType;
import org.liquid.model.enumeration.RelationType;

public class TableJoinInformation 
{
    private String leftTableName;
    private String rightTableName;
    private JoinType joinType;
    private RelationType relationType;
    
    private String leftColumnName;
    private String rightColumnName;
    
    private boolean ignoreOnEmpty;
    private boolean eagerlyFetch;
    
    public TableJoinInformation() 
    {
        this.ignoreOnEmpty = false;
        this.eagerlyFetch = true;
    }

    public String getLeftTableName() 
    {
        return leftTableName;
    }

    public void setLeftTableName(String leftTableName) 
    {
        this.leftTableName = leftTableName;
    }

    public String getRightTableName() 
    {
        return rightTableName;
    }

    public void setRightTableName(String rightTableName) 
    {
        this.rightTableName = rightTableName;
    }

    public JoinType getJoinType() 
    {
        return joinType;
    }

    public void setJoinType(JoinType joinType) 
    {
        this.joinType = joinType;
    }

    public String getLeftColumnName() {
        return leftColumnName;
    }

    public void setLeftColumnName(String leftColumnName) {
        this.leftColumnName = leftColumnName;
    }

    public String getRightColumnName() 
    {
        return rightColumnName;
    }

    public void setRightColumnName(String rightColumnName) 
    {
        this.rightColumnName = rightColumnName;
    }

    public boolean isIgnoreOnEmpty() 
    {
        return ignoreOnEmpty;
    }

    public void setIgnoreOnEmpty(boolean ignoreOnEmpty) 
    {
        this.ignoreOnEmpty = ignoreOnEmpty;
    }

    public boolean isEagerlyFetch() 
    {
        return eagerlyFetch;
    }

    public void setEagerlyFetch(boolean eagerlyFetch) 
    {
        this.eagerlyFetch = eagerlyFetch;
    }

    public RelationType getRelationType() 
    {
        return relationType;
    }

    public void setRelationType(RelationType relationType) 
    {
        this.relationType = relationType;
    }
    
    
}
