package model;

public enum PricingType {
    PER_KILOGRAM("Per Kilogram"),
    PER_PIECE("Per Piece");

    private final String label;

    PricingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PricingType fromLabel(String label) {
        for (PricingType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown pricing type: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
