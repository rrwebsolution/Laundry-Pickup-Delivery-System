package view;

import model.User;
import service.UserManagementService;
import service.ValidationException;
import view.components.AppIcon;
import view.components.Card;
import view.components.FormLayout;
import view.components.StyledPasswordField;
import view.components.Toast;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Lets the signed-in user review their account details and change their own password. */
public class SettingsPanel extends JPanel {

    private final UserManagementService userManagementService = new UserManagementService();
    private final User currentUser;

    public SettingsPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Settings");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Manage your account information and password.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, UITheme.SPACE_LG, 0));
        header.add(title);
        header.add(subtitle);

        Card card = new Card();
        card.setMaximumSize(new Dimension(560, 420));
        card.setLayout(new BorderLayout());

        JPanel form = FormLayout.newForm();
        JTextField fullNameField = UITheme.textField();
        fullNameField.setText(currentUser.getFullName());
        fullNameField.setEditable(false);
        JTextField usernameField = UITheme.textField();
        usernameField.setText(currentUser.getUsername());
        usernameField.setEditable(false);
        JTextField roleField = UITheme.textField();
        roleField.setText(currentUser.getRole().getLabel());
        roleField.setEditable(false);
        StyledPasswordField newPasswordField = UITheme.passwordField();
        StyledPasswordField confirmPasswordField = UITheme.passwordField();
        newPasswordField.setLeadingIcon(AppIcon.Name.PASSWORD);
        confirmPasswordField.setLeadingIcon(AppIcon.Name.PASSWORD);

        int row = FormLayout.sectionTitle(form, 0, "Account Information");
        row = FormLayout.fullRow(form, row, "Full Name", fullNameField);
        row = FormLayout.splitRow(form, row, "Username", usernameField, "Role", roleField);
        row = FormLayout.sectionTitle(form, row, "Change Password");
        row = FormLayout.fullRow(form, row, "New Password", newPasswordField);
        row = FormLayout.fullRow(form, row, "Confirm New Password", confirmPasswordField);

        JButton saveButton = UITheme.primaryButton(AppIcon.Name.SAVE, "Update Password");
        saveButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());
            if (newPassword.isEmpty()) {
                Toast.warning(this, "Enter a new password first.");
                return;
            }
            if (!newPassword.equals(confirm)) {
                Toast.error(this, "New password and confirmation do not match.");
                return;
            }
            try {
                userManagementService.updateUser(currentUser, currentUser.getFullName(), currentUser.getUsername(),
                        newPassword, currentUser.getRole(), currentUser.getStatus());
                Toast.success(this, "Password updated successfully.");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
            } catch (ValidationException ex) {
                Toast.error(this, ex.getMessage());
            } catch (SQLException ex) {
                Toast.error(this, "Database error: " + ex.getMessage());
            }
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.setBorder(new EmptyBorder(UITheme.SPACE_LG, 0, 0, 0));
        buttonRow.add(saveButton);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.add(form, BorderLayout.NORTH);
        formWrapper.add(buttonRow, BorderLayout.SOUTH);

        card.add(formWrapper, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }
}
