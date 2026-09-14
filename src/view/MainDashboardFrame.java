package view;

import model.User;
import model.UserRole;
import view.components.AppIcon;
import view.components.Dialogs;
import view.components.IconButton;
import view.components.SidebarButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main application window: fixed sidebar + top header + CardLayout content area. */
public class MainDashboardFrame extends JFrame {

    private final User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, SidebarButton> navButtons = new LinkedHashMap<>();
    private final JLabel headerTitle = new JLabel();
    private DashboardPanel dashboardPanel;

    private static final Object[][] NAV_ITEMS = {
            {"Dashboard", AppIcon.Name.DASHBOARD},
            {"Customers", AppIcon.Name.CUSTOMERS},
            {"Laundry Services", AppIcon.Name.SERVICES},
            {"Orders", AppIcon.Name.ORDERS},
            {"Pickup & Delivery", AppIcon.Name.PICKUP_DELIVERY},
            {"Payments", AppIcon.Name.PAYMENTS},
            {"Reports", AppIcon.Name.REPORTS},
            {"Users", AppIcon.Name.USERS},
            {"Settings", AppIcon.Name.SETTINGS}
    };

    public MainDashboardFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Laundry Pickup and Delivery Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 840);
        setMinimumSize(new Dimension(1080, 660));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        add(buildSidebar(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(UITheme.BACKGROUND);
        right.add(buildHeader(), BorderLayout.NORTH);
        right.add(buildContent(), BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        showCard("Dashboard");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(248, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.X_AXIS));
        brand.setBorder(new EmptyBorder(26, 22, 26, 22));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel logo = new JLabel(AppIcon.of(AppIcon.Name.SERVICES, 26, Color.WHITE));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setBorder(new EmptyBorder(0, 10, 0, 0));
        JLabel brandName = new JLabel("LaundryPro");
        brandName.setFont(new Font(UITheme.FONT_SECTION_TITLE.getFamily(), Font.BOLD, 17));
        brandName.setForeground(Color.WHITE);
        JLabel brandTag = new JLabel("Management Suite");
        brandTag.setFont(UITheme.FONT_HELPER);
        brandTag.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        brandText.add(brandName);
        brandText.add(brandTag);

        brand.add(logo);
        brand.add(brandText);
        sidebar.add(brand);

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.SIDEBAR_HOVER);
        sep.setBackground(UITheme.SIDEBAR_HOVER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(12));

        for (Object[] item : NAV_ITEMS) {
            String name = (String) item[0];
            AppIcon.Name icon = (AppIcon.Name) item[1];
            boolean visible = !name.equals("Users") || currentUser.getRole() == UserRole.ADMINISTRATOR;
            SidebarButton button = new SidebarButton(icon, name);
            button.setVisible(visible);
            button.addActionListener(e -> showCard(name));
            navButtons.put(name, button);
            sidebar.add(button);
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildProfileSection());
        return sidebar;
    }

    private JPanel buildProfileSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(UITheme.SIDEBAR_HOVER);
        panel.setBorder(new EmptyBorder(14, 18, 14, 14));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JLabel avatar = new JLabel(initials(currentUser.getFullName()), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(UITheme.PRIMARY);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(UITheme.FONT_LABEL_BOLD);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(currentUser.getFullName());
        nameLabel.setFont(UITheme.FONT_LABEL_BOLD);
        nameLabel.setForeground(Color.WHITE);
        JLabel roleLabel = new JLabel(currentUser.getRole().getLabel());
        roleLabel.setFont(UITheme.FONT_HELPER);
        roleLabel.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        textCol.add(nameLabel);
        textCol.add(roleLabel);

        IconButton logoutButton = new IconButton(AppIcon.Name.LOGOUT, 18, "Logout");
        logoutButton.setIconColor(UITheme.SIDEBAR_TEXT_MUTED);
        logoutButton.addActionListener(e -> logout());

        panel.add(avatar, BorderLayout.WEST);
        panel.add(textCol, BorderLayout.CENTER);
        panel.add(logoutButton, BorderLayout.EAST);
        return panel;
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return sb.length() > 0 ? sb.toString() : "U";
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(18, 28, 18, 28)));

        headerTitle.setFont(UITheme.FONT_PAGE_TITLE);
        headerTitle.setForeground(UITheme.TEXT_PRIMARY);
        header.add(headerTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        IconButton bellButton = new IconButton(AppIcon.Name.NOTIFICATION, 18, "Notifications");
        bellButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "You're all caught up. No new notifications.",
                "Notifications", JOptionPane.PLAIN_MESSAGE));

        JButton userMenu = new JButton(currentUser.getFullName());
        userMenu.setIcon(AppIcon.of(AppIcon.Name.CHEVRON_DOWN, 14, UITheme.ICON_COLOR));
        userMenu.setHorizontalTextPosition(SwingConstants.LEADING);
        userMenu.setIconTextGap(8);
        userMenu.setFont(UITheme.FONT_LABEL_BOLD);
        userMenu.setFocusPainted(false);
        userMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userMenu.putClientProperty("JButton.buttonType", "roundRect");
        userMenu.setBackground(UITheme.BACKGROUND);
        userMenu.setForeground(UITheme.TEXT_PRIMARY);
        userMenu.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPopupMenu menu = new JPopupMenu();
        JMenuItem settingsItem = new JMenuItem("Settings", AppIcon.of(AppIcon.Name.SETTINGS, 16, UITheme.ICON_COLOR));
        settingsItem.addActionListener(e -> showCard("Settings"));
        JMenuItem logoutItem = new JMenuItem("Logout", AppIcon.of(AppIcon.Name.LOGOUT, 16, UITheme.ICON_COLOR));
        logoutItem.addActionListener(e -> logout());
        menu.add(settingsItem);
        menu.addSeparator();
        menu.add(logoutItem);
        userMenu.addActionListener(e -> menu.show(userMenu, 0, userMenu.getHeight()));

        right.add(bellButton);
        right.add(userMenu);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        dashboardPanel = new DashboardPanel();
        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(new CustomerPanel(), "Customers");
        contentPanel.add(new ServicePanel(currentUser), "Laundry Services");
        contentPanel.add(new OrderPanel(), "Orders");
        contentPanel.add(new PickupDeliveryPanel(), "Pickup & Delivery");
        contentPanel.add(new PaymentPanel(), "Payments");
        contentPanel.add(new ReportsPanel(), "Reports");
        if (currentUser.getRole() == UserRole.ADMINISTRATOR) {
            contentPanel.add(new UserPanel(), "Users");
        }
        contentPanel.add(new SettingsPanel(currentUser), "Settings");
        contentPanel.setBackground(UITheme.BACKGROUND);
        return contentPanel;
    }

    private void showCard(String name) {
        for (Map.Entry<String, SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(name));
        }
        headerTitle.setText(name);
        if (name.equals("Dashboard") && dashboardPanel != null) {
            dashboardPanel.refresh();
        }
        cardLayout.show(contentPanel, name);
    }

    private void logout() {
        boolean confirmed = Dialogs.confirmAction(this, "Logout",
                "Are you sure you want to logout of your session?", "Logout");
        if (confirmed) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
