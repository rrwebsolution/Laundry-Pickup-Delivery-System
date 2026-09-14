package model;

/** The three account roles supported by the system. */
public enum UserRole {
    ADMINISTRATOR("Administrator"),
    STAFF("Staff"),
    RIDER("Rider");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static UserRole fromLabel(String label) {
        for (UserRole role : values()) {
            if (role.label.equalsIgnoreCase(label)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
