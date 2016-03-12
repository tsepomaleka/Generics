package org.liquidsql.model.join;

public class JoinTableInformationImpl implements JoinTableInformation
{
    private String intermediateTableName;
    private JoinColumnInformation leftJoinColumnInformation;
    private JoinColumnInformation rightJoinColumnInformation;

    public JoinTableInformationImpl() 
    {
    }
    
    @Override
    public String getIntermediateTableName() 
    {
        return intermediateTableName;
    }

    @Override
    public void setIntermediateTableName(String intermediateTableName) 
    {
        this.intermediateTableName = intermediateTableName;
    }

    @Override
    public JoinColumnInformation getLeftJoinColumnInformation() 
    {
        return leftJoinColumnInformation;
    }

    @Override
    public void setLeftJoinColumnInformation(JoinColumnInformation leftJoinColumnInformation) 
    {
        this.leftJoinColumnInformation = leftJoinColumnInformation;
    }

    @Override
    public JoinColumnInformation getRightJoinColumnInformation() 
    {
        return rightJoinColumnInformation;
    }

    @Override
    public void setRightJoinColumnInformation(JoinColumnInformation rightJoinColumnInformation) 
    {
        this.rightJoinColumnInformation = rightJoinColumnInformation;
    }

}
