package org.liquidsql.util.querybuilder;

import org.liquidsql.model.aggregate.AggregateColumnInformation;

import java.util.Map;

public interface AggregateFetchQueryBuilder extends FetchQueryBuilder
{
    public void setAggregateColumnsMap(Map<Integer, AggregateColumnInformation> aggregateColumnsMap);
}
