package org.liquidsql.model.alias;


import org.liquidsql.model.column.ColumnInformation;

import java.util.ArrayList;
import java.util.Collection;

public class NodeAliasImpl implements NodeAlias
{
    private final String aliasName;
    private String propertyPath;
    private Class entityClass;
    private Collection<ColumnInformation> columnInformationCollection;

    private NodeAlias parentNodeAlias;
    private Collection<NodeAlias> childNodeAliases;

    public NodeAliasImpl(String aliasName)
    {
        this.aliasName = aliasName;
    }

    @Override
    public String getAliasName()
    {
        return aliasName;
    }

    @Override
    public String getPropertyPath()
    {
        return propertyPath;
    }

    @Override
    public void setPropertyPath(String propertyPath)
    {
        this.propertyPath = propertyPath;
    }

    /**
     * Determines if this node alias is a root alias
     *
     * @return boolean
     */
    @Override
    public boolean isRootNodeAlias()
    {
        return (parentNodeAlias == null);
    }

    @Override
    public Class getEntityClass()
    {
        return entityClass;
    }

    @Override
    public void setEntityClass(Class entityClass)
    {
        this.entityClass = entityClass;
    }

    @Override
    public Collection<ColumnInformation> getColumnInformationCollection()
    {
        return columnInformationCollection;
    }

    @Override
    public void setColumnInformationCollection(Collection<ColumnInformation> columnInformationCollection)
    {
        this.columnInformationCollection = columnInformationCollection;
    }

    @Override
    public NodeAlias getParentNodeAlias()
    {
        return parentNodeAlias;
    }

    @Override
    public void setParentNodeAlias(NodeAlias parentNodeAlias)
    {
        this.parentNodeAlias = parentNodeAlias;
    }

    @Override
    public Collection<NodeAlias> getChildNodeAliases()
    {
        if (this.childNodeAliases == null)
            this.childNodeAliases = new ArrayList<>();

        return childNodeAliases;
    }

    @Override
    public void addChildNodeAlias(NodeAlias nodeAlias)
    {
        if (this.childNodeAliases == null)
            this.childNodeAliases = new ArrayList<>();

        if (this.childNodeAliases.contains(nodeAlias) == false)
            this.childNodeAliases.add(nodeAlias);
    }
}
