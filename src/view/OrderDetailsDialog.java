package view;

import dao.PaymentDAO;
import dao.PickupDeliveryDAO;
import model.LaundryOrder;
import model.OrderStatus;
import model.Payment;
import model.PickupDelivery;
import model.TransactionType;
import view.components.Card;
import view.components.AppIcon;
import view.components.StatusColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Read-only, richly detailed view of a single order: info sections + a visual progress tracker. */
public class OrderDetailsDialog extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private static final OrderStatus[] TRACK_STEPS = {
            OrderStatus.PENDING, OrderStatus.SCHEDULED_FOR_PICKUP, OrderStatus.PICKED_UP, OrderStatus.WASHING,
            OrderStatus.DRYING, OrderStatus.FOLDING, OrderStatus.READY_FOR_DELIVERY, OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED
    };
    private static final String[] TRACK_LABELS = {
            "Order Created", "Scheduled for Pickup", "Picked Up", "Washing", "Drying", "Folding",
            "Ready for Delivery", "Out for Delivery", "Delivered"
    };

    public OrderDetailsDialog(Window owner, LaundryOrder order) {
        super(owner, "Order #" + order.getOrderId(), ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(UITheme.BACKGROUND);
        root.setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        root.add(buildHeader(order));
        root.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        root.add(buildProgressCard(order));
        root.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        root.add(buildTwoColumn(buildCustomerCard(order), buildLaundryCard(order)));
        root.add(Box.createVerticalStrut(UITheme.SPACE_MD));

        List<PickupDelivery> pickupDeliveries;
        List<Payment> payments;
        try {
            pickupDeliveries = new PickupDeliveryDAO().findByOrderId(order.getOrderId());
            payments = new PaymentDAO().findByOrderId(order.getOrderId());
        } catch (SQLException ex) {
            pickupDeliveries = List.of();
            payments = List.of();
        }

        root.add(buildTwoColumn(buildTransactionCard("Pickup Information", TransactionType.PICKUP, pickupDeliveries),
                buildTransactionCard("Delivery Information", TransactionType.DELIVERY, pickupDeliveries)));
        root.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        root.add(buildPaymentCard(order, payments));
        if (order.getNotes() != null && !order.getNotes().trim().isEmpty()) {
            root.add(Box.createVerticalStrut(UITheme.SPACE_MD));
            root.add(buildNotesCard(order));
        }

        JScrollPane scrollPane = new JScrollPane(root);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(12, 20, 12, 20)));
        JButton closeButton = UITheme.secondaryButton("Close");
        closeButton.addActionListener(e -> dispose());
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        setSize(760, 780);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader(LaundryOrder order) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Order #" + order.getOrderId());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UITheme.subtitle(order.getCustomer().getFullName() + " • "
                + (order.getOrderDate() == null ? "" : order.getOrderDate().format(DATETIME_FMT)));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(2, 0, 0, 0));
        left.add(title);
        left.add(sub);

        header.add(left, BorderLayout.WEST);
        header.add(StatusColors.badge(order.getStatus().getLabel()), BorderLayout.EAST);
        return header;
    }

    private Card buildProgressCard(LaundryOrder order) {
        Card card = new Card();
        card.setLayout(new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, order.getStatus() == OrderStatus.CANCELLED ? 90 : 320));

        JLabel title = UITheme.sectionTitle("Order Progress");
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        card.add(title, BorderLayout.NORTH);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            JLabel cancelled = new JLabel("This order has been cancelled.",
                    AppIcon.of(AppIcon.Name.ERROR, 18, UITheme.DANGER), SwingConstants.LEFT);
            cancelled.setIconTextGap(8);
            cancelled.setFont(UITheme.FONT_LABEL_BOLD);
            cancelled.setForeground(UITheme.DANGER);
            card.add(cancelled, BorderLayout.CENTER);
            return card;
        }

        int currentIndex = order.getStatus().ordinal();
        JPanel steps = new JPanel();
        steps.setOpaque(false);
        steps.setLayout(new BoxLayout(steps, BoxLayout.Y_AXIS));

        for (int i = 0; i < TRACK_STEPS.length; i++) {
            boolean done = i < currentIndex;
            boolean current = i == currentIndex;
            steps.add(buildStepRow(TRACK_LABELS[i], done, current));
            if (i < TRACK_STEPS.length - 1) {
                JPanel connector = new JPanel();
                connector.setPreferredSize(new Dimension(2, 16));
                connector.setMaximumSize(new Dimension(2, 16));
                connector.setBackground(done ? UITheme.SUCCESS : UITheme.BORDER);
                JPanel connectorWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
                connectorWrap.setOpaque(false);
                connectorWrap.add(connector);
                steps.add(connectorWrap);
            }
        }
        card.add(steps, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStepRow(String label, boolean done, boolean current) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        Color bulletBg;
        Color bulletFg;
        AppIcon.Name bulletIcon;
        if (done) {
            bulletBg = UITheme.SUCCESS;
            bulletFg = Color.WHITE;
            bulletIcon = AppIcon.Name.CHECK;
        } else if (current) {
            bulletBg = UITheme.PRIMARY;
            bulletFg = Color.WHITE;
            bulletIcon = AppIcon.Name.CIRCLE;
        } else {
            bulletBg = new Color(0xF1, 0xF5, 0xF9);
            bulletFg = UITheme.TEXT_SECONDARY;
            bulletIcon = AppIcon.Name.CIRCLE_OUTLINE;
        }
        JLabel bullet = new CircleBadge(bulletBg);
        bullet.setIcon(AppIcon.of(bulletIcon, 13, bulletFg));
        bullet.setPreferredSize(new Dimension(24, 24));

        JLabel text = new JLabel(label);
        text.setFont(current ? UITheme.FONT_LABEL_BOLD : UITheme.FONT_LABEL);
        text.setForeground(done || current ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY);

        row.add(bullet);
        row.add(text);
        return row;
    }

    private JPanel buildTwoColumn(JComponent left, JComponent right) {
        JPanel panel = new JPanel(new GridLayout(1, 2, UITheme.SPACE_MD, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        panel.add(left);
        panel.add(right);
        return panel;
    }

    private Card buildCustomerCard(LaundryOrder order) {
        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        addCardTitle(card, "Customer Information");
        addKeyValue(card, "Name", order.getCustomer().getFullName());
        addKeyValue(card, "Contact", order.getCustomer().getContactNumber());
        addKeyValue(card, "Email", emptyDash(order.getCustomer().getEmail()));
        addKeyValue(card, "Address", order.getCustomer().getAddress());
        return card;
    }

    private Card buildLaundryCard(LaundryOrder order) {
        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        addCardTitle(card, "Laundry Details");
        addKeyValue(card, "Service", order.getService().getServiceName());
        addKeyValue(card, "Weight / Quantity", order.getWeightQuantity() + " (" + order.getService().getPricingType().getLabel() + ")");
        addKeyValue(card, "Order Date", order.getOrderDate() == null ? "-" : order.getOrderDate().format(DATETIME_FMT));
        addKeyValue(card, "Expected Completion",
                order.getExpectedCompletionDate() == null ? "-" : order.getExpectedCompletionDate().format(DATE_FMT));
        return card;
    }

    private Card buildTransactionCard(String title, TransactionType type, List<PickupDelivery> records) {
        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        addCardTitle(card, title);

        PickupDelivery match = records.stream().filter(r -> r.getType() == type).findFirst().orElse(null);
        if (match == null) {
            JLabel none = UITheme.helperText("Not yet scheduled.");
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(none);
            return card;
        }
        addKeyValue(card, "Address", match.getAddress());
        addKeyValue(card, "Rider", match.getAssignedRider() == null ? "Unassigned" : match.getAssignedRider().getFullName());
        addKeyValue(card, "Scheduled", match.getScheduledDate().format(DATE_FMT) + " at " + match.getScheduledTime().format(TIME_FMT));
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusRow.add(StatusColors.badge(match.getStatus().getLabel()));
        card.add(statusRow);
        return card;
    }

    private Card buildPaymentCard(LaundryOrder order, List<Payment> payments) {
        Card card = new Card();
        card.setLayout(new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        addCardTitle(card, "Payment Summary");

        java.math.BigDecimal laundryCost = order.getPrice().multiply(order.getWeightQuantity());

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        addAmountRow(body, "Laundry Cost", laundryCost, false);
        addAmountRow(body, "Pickup Fee", order.getPickupFee(), false);
        addAmountRow(body, "Delivery Fee", order.getDeliveryFee(), false);
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBorder(new EmptyBorder(8, 0, 8, 0));
        body.add(sep);
        addAmountRow(body, "Total Amount", order.getTotalAmount(), true);

        if (!payments.isEmpty()) {
            Payment latest = payments.get(payments.size() - 1);
            JSeparator sep2 = new JSeparator();
            sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
            sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep2.setBorder(new EmptyBorder(10, 0, 10, 0));
            body.add(sep2);
            addAmountRow(body, "Amount Paid (" + latest.getPaymentMethod().getLabel() + ")", latest.getAmountPaid(), false);
            addAmountRow(body, "Change", latest.getChangeAmount(), false);
            JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            statusRow.setOpaque(false);
            statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusRow.add(StatusColors.badge(latest.getPaymentStatus().getLabel()));
            body.add(statusRow);
        } else {
            JLabel none = UITheme.helperText("No payment recorded yet.");
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            none.setBorder(new EmptyBorder(8, 0, 0, 0));
            body.add(none);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private Card buildNotesCard(LaundryOrder order) {
        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        addCardTitle(card, "Notes");
        JLabel notes = new JLabel("<html><body style='width:600px'>" + order.getNotes() + "</body></html>");
        notes.setFont(UITheme.FONT_LABEL);
        notes.setForeground(UITheme.TEXT_PRIMARY);
        notes.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(notes);
        return card;
    }

    private void addCardTitle(Card card, String text) {
        JLabel label = UITheme.sectionTitle(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(label);
    }

    private void addKeyValue(Card card, String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(UITheme.FONT_HELPER);
        keyLabel.setForeground(UITheme.TEXT_SECONDARY);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UITheme.FONT_LABEL_BOLD);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);
        row.add(keyLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(row);
    }

    private void addAmountRow(JPanel body, String label, java.math.BigDecimal amount, boolean emphasize) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, emphasize ? 30 : 22));
        JLabel keyLabel = new JLabel(label);
        keyLabel.setFont(emphasize ? UITheme.FONT_SECTION_TITLE : UITheme.FONT_LABEL);
        keyLabel.setForeground(emphasize ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY);
        JLabel valueLabel = new JLabel("PHP " + amount);
        valueLabel.setFont(emphasize ? UITheme.FONT_SECTION_TITLE : UITheme.FONT_LABEL_BOLD);
        valueLabel.setForeground(emphasize ? UITheme.PRIMARY : UITheme.TEXT_PRIMARY);
        row.add(keyLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        row.setBorder(new EmptyBorder(0, 0, 6, 0));
        body.add(row);
    }

    private String emptyDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    /** A small solid-filled circle used as the progress tracker's step bullet, with an icon centered on top. */
    private static class CircleBadge extends JLabel {
        private final Color fill;

        CircleBadge(Color fill) {
            super();
            this.fill = fill;
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
