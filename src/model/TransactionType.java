package model;

public enum TransactionType {
    PICKUP("Pickup"),
    DELIVERY("Delivery");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TransactionType fromLabel(String label) {
        for (TransactionType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
