package view.components;

import view.UITheme;

import java.awt.*;

/** Maps every status label used across Orders, Pickup/Delivery, Payments, and Users to a badge color. */
public final class StatusColors {

    private StatusColors() {
    }

    public static Color fillFor(String statusLabel) {
        switch (statusLabel) {
            case "Pending":
            case "Unpaid":
            case "Scheduled":
            case "Scheduled for Pickup":
                return new Color(0xFE, 0xF3, 0xC7);
            case "Picked Up":
            case "Washing":
            case "Drying":
            case "Folding":
            case "In Progress":
            case "Partially Paid":
                return new Color(0xDB, 0xEA, 0xFE);
            case "Ready for Delivery":
            case "Out for Delivery":
                return new Color(0xE0, 0xE7, 0xFF);
            case "Delivered":
            case "Completed":
            case "Paid":
            case "Active":
                return new Color(0xDC, 0xFC, 0xE7);
            case "Cancelled":
            case "Inactive":
                return new Color(0xFE, 0xE2, 0xE2);
            default:
                return new Color(0xF1, 0xF5, 0xF9);
        }
    }

    public static Color textFor(String statusLabel) {
        switch (statusLabel) {
            case "Pending":
            case "Unpaid":
            case "Scheduled":
            case "Scheduled for Pickup":
                return new Color(0x92, 0x6D, 0x03);
            case "Picked Up":
            case "Washing":
            case "Drying":
            case "Folding":
            case "In Progress":
            case "Partially Paid":
                return new Color(0x1D, 0x4E, 0xD8);
            case "Ready for Delivery":
            case "Out for Delivery":
                return new Color(0x43, 0x38, 0xCA);
            case "Delivered":
            case "Completed":
            case "Paid":
            case "Active":
                return new Color(0x15, 0x80, 0x3D);
            case "Cancelled":
            case "Inactive":
                return UITheme.DANGER;
            default:
                return UITheme.TEXT_SECONDARY;
        }
    }

    public static StatusBadge badge(String statusLabel) {
        return new StatusBadge(statusLabel, fillFor(statusLabel), textFor(statusLabel));
    }
}
