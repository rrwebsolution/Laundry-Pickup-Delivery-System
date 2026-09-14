package model;

public enum PickupDeliveryStatus {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    PickupDeliveryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PickupDeliveryStatus fromLabel(String label) {
        for (PickupDeliveryStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown pickup/delivery status: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
