package model;

public enum PaymentMethod {
    CASH("Cash"),
    GCASH("GCash"),
    BANK_TRANSFER("Bank Transfer");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PaymentMethod fromLabel(String label) {
        for (PaymentMethod method : values()) {
            if (method.label.equalsIgnoreCase(label)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
