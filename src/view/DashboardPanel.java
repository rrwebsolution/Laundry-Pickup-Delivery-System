package view;

import service.CustomerService;
import service.OrderService;
import service.PaymentService;
import view.components.AppIcon;
import view.components.Card;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Summary cards shown right after login. */
public class DashboardPanel extends JPanel {

    private final CustomerService customerService = new CustomerService();
    private final OrderService orderService = new OrderService();
    private final PaymentService paymentService = new PaymentService();

    private final JPanel cardsPanel = new JPanel(new GridLayout(0, 4, UITheme.SPACE_MD, UITheme.SPACE_MD));
    private final JLabel statusLabel = UITheme.helperText(" ");
    private final JLabel welcomeLabel = UITheme.pageTitle("Welcome back");

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

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        cardsPanel.removeAll();
        try {
            int totalCustomers = customerService.countCustomers();
            int totalOrders = orderService.countAll();
            int pendingPickups = orderService.countByStatuses("Pending", "Scheduled for Pickup");
            int inProcess = orderService.countByStatuses("Picked Up", "Washing", "Drying", "Folding");
            int readyForDelivery = orderService.countByStatuses("Ready for Delivery", "Out for Delivery");
            int completed = orderService.countByStatuses("Delivered");
            BigDecimal todaysRevenue = paymentService.todaysRevenue();

            cardsPanel.add(buildCard(AppIcon.Name.CUSTOMERS, String.valueOf(totalCustomers), "Total Customers",
                    "Registered accounts", UITheme.PRIMARY));
            cardsPanel.add(buildCard(AppIcon.Name.ORDERS, String.valueOf(totalOrders), "Total Orders",
                    "All orders placed", UITheme.SECONDARY));
            cardsPanel.add(buildCard(AppIcon.Name.CLOCK, String.valueOf(pendingPickups), "Pending Pickups",
                    "Awaiting rider pickup", UITheme.WARNING));
            cardsPanel.add(buildCard(AppIcon.Name.SERVICES, String.valueOf(inProcess), "Laundry in Process",
                    "Washing, drying, folding", new Color(0x8B, 0x5C, 0xF6)));
            cardsPanel.add(buildCard(AppIcon.Name.PICKUP_DELIVERY, String.valueOf(readyForDelivery), "Ready for Delivery",
                    "Ready or out for delivery", new Color(0x06, 0x94, 0x8D)));
            cardsPanel.add(buildCard(AppIcon.Name.CHECK_CIRCLE, String.valueOf(completed), "Completed Orders",
                    "Successfully delivered", UITheme.SUCCESS));
            cardsPanel.add(buildCard(AppIcon.Name.MONEY, "PHP " + todaysRevenue, "Today's Revenue",
                    "Cash collected today", UITheme.DANGER));

            statusLabel.setText(" ");
        } catch (SQLException ex) {
            statusLabel.setText("Failed to load dashboard data: " + ex.getMessage());
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private Card buildCard(AppIcon.Name icon, String value, String label, String trend, Color accent) {
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

        JLabel valueLabel = new JLabel(value);
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
        return card;
    }

    private Color tint(Color color, float amount) {
        int r = (int) (255 * (1 - amount) + color.getRed() * amount);
        int g = (int) (255 * (1 - amount) + color.getGreen() * amount);
        int b = (int) (255 * (1 - amount) + color.getBlue() * amount);
        return new Color(r, g, b);
    }
}
