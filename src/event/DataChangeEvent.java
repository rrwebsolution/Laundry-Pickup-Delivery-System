package event;

/** The kinds of data changes that panels can subscribe to via {@link DataChangeManager}. */
public enum DataChangeEvent {
    CUSTOMER_CHANGED,
    ORDER_CHANGED,
    SERVICE_CHANGED,
    PICKUP_DELIVERY_CHANGED,
    PAYMENT_CHANGED,
    USER_CHANGED
}
