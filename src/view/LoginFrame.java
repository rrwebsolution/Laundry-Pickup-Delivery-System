package view;

import model.User;
import service.AuthService;
import service.ValidationException;
import view.components.AppIcon;
import view.components.StyledPasswordField;
import view.components.StyledTextField;
import view.components.Toast;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

/** First screen shown to the user: a premium split-screen login for all three roles. */
public class LoginFrame extends JFrame {

    private final StyledTextField usernameField = new StyledTextField("Enter your username");
    private final StyledPasswordField passwordField = new StyledPasswordField("Enter your password");
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Laundry Pickup and Delivery Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 600);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        usernameField.setLeadingIcon(AppIcon.Name.USERNAME);
        passwordField.setLeadingIcon(AppIcon.Name.PASSWORD);

        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, UITheme.SIDEBAR, getWidth(), getHeight(), new Color(0x1E, 0x3A, 0x8A)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(64, 48, 48, 48));

        JLabel badge = new JLabel(AppIcon.of(AppIcon.Name.SERVICES, 48, Color.WHITE));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("<html>Laundry Pickup &amp;<br>Delivery Management</html>");
        title.setFont(new Font(UITheme.FONT_PAGE_TITLE.getFamily(), Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(22, 0, 12, 0));

        JLabel slogan = new JLabel("Fresh. Fast. Easy.");
        slogan.setFont(new Font(UITheme.FONT_LABEL.getFamily(), Font.BOLD, 15));
        slogan.setForeground(UITheme.SECONDARY);
        slogan.setAlignmentX(Component.LEFT_ALIGNMENT);
        slogan.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel subtitle = new JLabel("<html>Manage customers, orders, pickups, deliveries,<br>and payments in one clean dashboard.</html>");
        subtitle.setFont(UITheme.FONT_LABEL);
        subtitle.setForeground(UITheme.SIDEBAR_TEXT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(badge);
        panel.add(title);
        panel.add(slogan);
        panel.add(subtitle);
        panel.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("(c) Laundry Management System");
        footer.setFont(UITheme.FONT_HELPER);
        footer.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(footer);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(40, 44, 36, 44)));
        card.setPreferredSize(new Dimension(380, 360));

        JLabel header = UITheme.pageTitle("Welcome Back");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = UITheme.subtitle("Sign in to continue");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 24, 0));

        JLabel userLabel = UITheme.fieldLabel("Username");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel passLabel = UITheme.fieldLabel("Password");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passLabel.setBorder(new EmptyBorder(16, 0, 4, 0));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton loginButton = UITheme.primaryButton("Sign In");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.addActionListener(e -> attemptLogin());

        JButton exitButton = UITheme.secondaryButton("Exit");
        exitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        exitButton.addActionListener(e -> System.exit(0));

        JLabel hint = new JLabel("Default admin: admin / admin123");
        hint.setFont(UITheme.FONT_HELPER);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(18, 0, 0, 0));

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                usernameField.setError(false);
                passwordField.setError(false);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        card.add(header);
        card.add(sub);
        card.add(userLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(passLabel);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(8));
        card.add(exitButton);
        card.add(hint);

        outer.add(card);
        return outer;
    }

    private void attemptLogin() {
        try {
            User user = authService.login(usernameField.getText(), new String(passwordField.getPassword()));
            usernameField.setError(false);
            passwordField.setError(false);
            dispose();
            SwingUtilities.invokeLater(() -> new MainDashboardFrame(user).setVisible(true));
        } catch (ValidationException ex) {
            usernameField.setError(true);
            passwordField.setError(true);
            Toast.error(usernameField, ex.getMessage());
            passwordField.setText("");
        } catch (SQLException ex) {
            Toast.error(usernameField, "Could not connect to the database. " + ex.getMessage());
        }
    }
}
