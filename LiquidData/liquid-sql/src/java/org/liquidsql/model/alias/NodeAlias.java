package org.liquidsql.model.alias;

import org.liquidsql.model.column.ColumnInformation;

import java.util.Collection;

public interface NodeAlias 
{
    /**
     * Gets the alias name
     * @return String
     */
    public String getAliasName();
    
    /**
     * Gets the property path
     * @return String
     */
    public String getPropertyPath();
    
    /**
     * Sets the property path
     * @param propertyPath 
     */
    public void setPropertyPath(String propertyPath);
    
    /**
     * Determines if this node alias is a root alias
     * @return boolean
     */
    public boolean isRootNodeAlias();
    
    /**
     * Gets the node alias that is parent to the current
     * node alias.
     * @return NodeAlias
     */
    public NodeAlias getParentNodeAlias();
    
    /**
     * Sets the parent node alias
     * @param parentNodeAlias 
     */
    public void setParentNodeAlias(NodeAlias parentNodeAlias);
    
    /**
     * Gets a collection of all alias that are
     * immediate children to the current node.
     * @return Collection
     */
    public Collection<NodeAlias> getChildNodeAliases();
    
    /**
     * Adds a child node alias.
     * @param nodeAlias 
     */
    public void addChildNodeAlias(NodeAlias nodeAlias);
    
    /**
     * Gets the entity class.
     * @return Class
     */
    public Class getEntityClass();
    
    /**
     * Sets the entity class.
     * @param entityClass 
     */
    public void setEntityClass(Class entityClass);

    /**
     * Sets the column information collection
     * @param columnInformationCollection
     */
    public void setColumnInformationCollection(Collection<ColumnInformation> columnInformationCollection);

    /**
     * Gets the column information collection
     * @return
     */
    public Collection<ColumnInformation> getColumnInformationCollection();
}
