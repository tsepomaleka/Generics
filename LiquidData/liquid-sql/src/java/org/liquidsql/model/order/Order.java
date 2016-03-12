package org.liquidsql.model.order;

import org.liquidsql.model.enumeration.OrderByType;

public class Order
{
    private final String propertyPath;
    private final OrderByType orderByType;

    /**
     * Private constructor
     * @param propertyPath
     */
    private Order(String propertyPath, OrderByType orderByType)
    {
        this.propertyPath = propertyPath;
        this.orderByType = orderByType;
    }

    /**
     * Get the property path
     * @return
     */
    public String getPropertyPath()
    {
        return propertyPath;
    }

    /**
     * Get the order by type
     * @return
     */
    public OrderByType getOrderByType()
    {
        return orderByType;
    }

    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("${").append(propertyPath)
                .append("} ").append(orderByType.getExpression());

        return buffer.toString();
    }

    /**
     * Orders the result set with respect to the
     * specified property field in ASCENDING order.
     *
     * @param propertyPath
     * @return Order
     */
    public static Order asc(String propertyPath)
    {
        Order order = new Order(propertyPath, OrderByType.ASCENDING);
        return order;
    }

    /**
     * Orders the result set with respect to the
     * specified property field in DESCENDING order.
     *
     * @param propertyPath
     * @return Order
     */
    public static Order desc(String propertyPath)
    {
        Order order = new Order(propertyPath, OrderByType.DESCENDING);
        return order;
    }
}
