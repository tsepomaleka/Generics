package org.liquidsql.util.querybuilder;

import org.liquidsql.model.alias.NodeAlias;
import org.liquidsql.model.condition.Condition;
import org.liquidsql.model.order.Order;

import java.util.Collection;
import java.util.Map;

public interface FetchQueryBuilder extends QueryBuilder
{
    public void setNodeAliasMap(Map<Integer, NodeAlias> nodeAliasMap);
    public void setJoinInformationCollection(Collection joinInformationCollection);
    public void setCondition(Condition condition);
    public void setGroupByCollection(Collection<String> groupByCollection);
    public void setOrderByCollection(Collection<Order> orderByCollection);
    public void setLimit(int limit);
    public void setOffset(int offset);
    public void setRootEntityClass(Class rootEntityClass);
}
