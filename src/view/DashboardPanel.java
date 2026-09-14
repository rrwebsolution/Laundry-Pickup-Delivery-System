package view;

import event.DataChangeEvent;
import event.DataChangeListener;
import event.DataChangeManager;
import service.CustomerService;
import service.OrderService;
import service.PaymentService;
import view.components.Async;
import view.components.AppIcon;
import view.components.Card;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/** Summary cards shown right after login. */
public class DashboardPanel extends JPanel implements DataChangeListener, Refreshable {

    private final CustomerService customerService = new CustomerService();
    private final OrderService orderService = new OrderService();
    private final PaymentService paymentService = new PaymentService();

    private final JPanel cardsPanel = new JPanel(new GridLayout(0, 4, UITheme.SPACE_MD, UITheme.SPACE_MD));
    private final JLabel statusLabel = UITheme.helperText(" ");
    private final JLabel welcomeLabel = UITheme.pageTitle("Welcome back");

    // Built once and updated in place on every refresh, instead of being torn down and
    // recreated, so a background poll never causes the dashboard to flicker.
    private final JLabel totalCustomersValue;
    private final JLabel totalOrdersValue;
    private final JLabel pendingPickupsValue;
    private final JLabel inProcessValue;
    private final JLabel readyForDeliveryValue;
    private final JLabel completedValue;
    private final JLabel todaysRevenueValue;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, UITheme.SPACE_LG, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Manage your laundry operations efficiently.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(welcomeLabel);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JButton refreshButton = UITheme.secondaryButton(AppIcon.Name.REFRESH, "Refresh");
        refreshButton.addActionListener(e -> refresh());
        header.add(refreshButton, BorderLayout.EAST);

        cardsPanel.setOpaque(false);

        totalCustomersValue = addStatCard(AppIcon.Name.CUSTOMERS, "Total Customers", "Registered accounts",
                UITheme.PRIMARY);
        totalOrdersValue = addStatCard(AppIcon.Name.ORDERS, "Total Orders", "All orders placed", UITheme.SECONDARY);
        pendingPickupsValue = addStatCard(AppIcon.Name.CLOCK, "Pending Pickups", "Awaiting rider pickup",
                UITheme.WARNING);
        inProcessValue = addStatCard(AppIcon.Name.SERVICES, "Laundry in Process", "Washing, drying, folding",
                new Color(0x8B, 0x5C, 0xF6));
        readyForDeliveryValue = addStatCard(AppIcon.Name.PICKUP_DELIVERY, "Ready for Delivery",
                "Ready or out for delivery", new Color(0x06, 0x94, 0x8D));
        completedValue = addStatCard(AppIcon.Name.CHECK_CIRCLE, "Completed Orders", "Successfully delivered",
                UITheme.SUCCESS);
        todaysRevenueValue = addStatCard(AppIcon.Name.MONEY, "Today's Revenue", "Cash collected today",
                UITheme.DANGER);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        refresh();
        DataChangeManager.addListener(this);
    }

    @Override
    public void onDataChanged(DataChangeEvent event) {
        if (event == DataChangeEvent.CUSTOMER_CHANGED || event == DataChangeEvent.ORDER_CHANGED
                || event == DataChangeEvent.PAYMENT_CHANGED) {
            refresh();
        }
    }

    @Override
    public void refreshNow() {
        refresh();
    }

    /** Snapshot of every figure shown on the dashboard, computed off the EDT in one background pass. */
    private static final class Stats {
        int totalCustomers;
        int totalOrders;
        int pendingPickups;
        int inProcess;
        int readyForDelivery;
        int completed;
        BigDecimal todaysRevenue;
    }

    public void refresh() {
        Async.run(this::loadStats, this::applyStats,
                ex -> statusLabel.setText("Failed to load dashboard data: " + ex.getMessage()));
    }

    private Stats loadStats() throws Exception {
        Stats stats = new Stats();
        stats.totalCustomers = customerService.countCustomers();
        stats.totalOrders = orderService.countAll();
        stats.pendingPickups = orderService.countByStatuses("Pending", "Scheduled for Pickup");
        stats.inProcess = orderService.countByStatuses("Picked Up", "Washing", "Drying", "Folding");
        stats.readyForDelivery = orderService.countByStatuses("Ready for Delivery", "Out for Delivery");
        stats.completed = orderService.countByStatuses("Delivered");
        stats.todaysRevenue = paymentService.todaysRevenue();
        return stats;
    }

    /** Updates the existing cards' labels in place - no components are destroyed or rebuilt. */
    private void applyStats(Stats stats) {
        totalCustomersValue.setText(String.valueOf(stats.totalCustomers));
        totalOrdersValue.setText(String.valueOf(stats.totalOrders));
        pendingPickupsValue.setText(String.valueOf(stats.pendingPickups));
        inProcessValue.setText(String.valueOf(stats.inProcess));
        readyForDeliveryValue.setText(String.valueOf(stats.readyForDelivery));
        completedValue.setText(String.valueOf(stats.completed));
        todaysRevenueValue.setText("PHP " + stats.todaysRevenue);
        statusLabel.setText(" ");
    }

    /** Builds one stat card, adds it to the grid, and returns its (mutable) value label. */
    private JLabel addStatCard(AppIcon.Name icon, String label, String trend, Color accent) {
        Card card = new Card();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(220, 150));

        JLabel iconLabel = new JLabel(AppIcon.of(icon, 26, accent));
        iconLabel.setOpaque(true);
        iconLabel.setBackground(tint(accent, 0.14f));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setMaximumSize(new Dimension(40, 40));

        JPanel iconRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconRow.setOpaque(false);
        iconRow.add(iconLabel);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setBorder(new EmptyBorder(14, 0, 0, 0));

        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(UITheme.FONT_CARD_VALUE);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(UITheme.FONT_LABEL_BOLD);
        textLabel.setForeground(UITheme.TEXT_PRIMARY);
        textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textLabel.setBorder(new EmptyBorder(2, 0, 4, 0));

        JLabel trendLabel = new JLabel(trend);
        trendLabel.setFont(UITheme.FONT_HELPER);
        trendLabel.setForeground(UITheme.TEXT_SECONDARY);
        trendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(valueLabel);
        textCol.add(textLabel);
        textCol.add(trendLabel);

        card.add(iconRow, BorderLayout.NORTH);
        card.add(textCol, BorderLayout.CENTER);
        cardsPanel.add(card);
        return valueLabel;
    }

    private Color tint(Color color, float amount) {
        int r = (int) (255 * (1 - amount) + color.getRed() * amount);
        int g = (int) (255 * (1 - amount) + color.getGreen() * amount);
        int b = (int) (255 * (1 - amount) + color.getBlue() * amount);
        return new Color(r, g, b);
    }
}
