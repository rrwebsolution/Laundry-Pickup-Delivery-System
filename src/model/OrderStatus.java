package model;

public enum OrderStatus {
    PENDING("Pending"),
    SCHEDULED_FOR_PICKUP("Scheduled for Pickup"),
    PICKED_UP("Picked Up"),
    WASHING("Washing"),
    DRYING("Drying"),
    FOLDING("Folding"),
    READY_FOR_DELIVERY("Ready for Delivery"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static OrderStatus fromLabel(String label) {
        for (OrderStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
