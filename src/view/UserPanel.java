package view;

import event.DataChangeEvent;
import event.DataChangeListener;
import event.DataChangeManager;
import model.User;
import model.UserRole;
import model.UserStatus;
import service.UserManagementService;
import service.ValidationException;
import view.components.Async;
import view.components.Card;
import view.components.Dialogs;
import view.components.EntityFormDialog;
import view.components.FormLayout;
import view.components.AppIcon;
import view.components.StatusCellRenderer;
import view.components.Toast;
import view.components.ZebraRowRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserPanel extends JPanel implements DataChangeListener, Refreshable {

    private final UserManagementService userManagementService = new UserManagementService();

    private final JLabel countLabel = UITheme.helperText("");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"User ID", "Full Name", "Username", "Password", "Role", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton editButton = UITheme.secondaryButton(AppIcon.Name.EDIT, "Edit");
    private final JButton deleteButton = UITheme.dangerButton(AppIcon.Name.DELETE, "Delete");

    public UserPanel() {
        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadUsers();
        DataChangeManager.addListener(this);
    }

    @Override
    public void onDataChanged(DataChangeEvent event) {
        if (event == DataChangeEvent.USER_CHANGED) {
            loadUsers();
        }
    }

    @Override
    public void refreshNow() {
        loadUsers();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Users");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Manage system accounts for Administrators, Staff, and Riders.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JButton addButton = UITheme.primaryButton(AppIcon.Name.ADD, "Add User");
        addButton.addActionListener(e -> openForm(null));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(UITheme.SPACE_LG, 0, 0, 0));
        toolbar.add(addButton);
        header.add(toolbar, BorderLayout.SOUTH);
        return header;
    }

    private Card buildTableCard() {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, UITheme.SPACE_MD));

        UITheme.styleTable(table);
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new ZebraRowRenderer());
        }
        table.getColumnModel().getColumn(table.getColumnCount() - 1).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionState();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);

        JPanel actionsRow = new JPanel(new BorderLayout());
        actionsRow.setOpaque(false);
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonGroup.setOpaque(false);
        editButton.addActionListener(e -> openForm(selectedUser()));
        deleteButton.addActionListener(e -> deleteSelected());
        buttonGroup.add(editButton);
        buttonGroup.add(deleteButton);
        actionsRow.add(buttonGroup, BorderLayout.WEST);
        actionsRow.add(countLabel, BorderLayout.EAST);

        card.add(actionsRow, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);
        updateActionState();
        return card;
    }

    private void updateActionState() {
        boolean hasSelection = table.getSelectedRow() >= 0;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private User selectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int userId = (int) tableModel.getValueAt(row, 0);
        try {
            return new dao.UserDAO().findById(userId);
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load user: " + ex.getMessage());
            return null;
        }
    }

    private void loadUsers() {
        Async.run(userManagementService::getAllUsers, this::populateTable,
                ex -> Toast.error(this, "Failed to load users: " + ex.getMessage()));
    }

    private void populateTable(List<User> users) {
        tableModel.setRowCount(0);
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUserId(), u.getFullName(), u.getUsername(), "••••••••",
                    u.getRole().getLabel(), u.getStatus().getLabel()
            });
        }
        countLabel.setText(users.size() + (users.size() == 1 ? " user" : " users"));
        updateActionState();
    }

    private void openForm(User existing) {
        boolean editing = existing != null;
        EntityFormDialog dialog = new EntityFormDialog(SwingUtilities.getWindowAncestor(this),
                editing ? "Edit User" : "Add User",
                editing ? "Update this account's details." : "Create a new system account.",
                editing ? "Save Changes" : "Add User");

        JTextField fullNameField = UITheme.textField();
        JTextField usernameField = UITheme.textField();
        JPasswordField passwordField = UITheme.passwordField();
        JComboBox<UserRole> roleBox = UITheme.comboBox();
        for (UserRole role : UserRole.values()) {
            roleBox.addItem(role);
        }
        JComboBox<UserStatus> statusBox = UITheme.comboBox();
        for (UserStatus status : UserStatus.values()) {
            statusBox.addItem(status);
        }

        if (editing) {
            fullNameField.setText(existing.getFullName());
            usernameField.setText(existing.getUsername());
            roleBox.setSelectedItem(existing.getRole());
            statusBox.setSelectedItem(existing.getStatus());
        }

        JPanel form = dialog.getForm();
        int row = FormLayout.sectionTitle(form, 0, "Account Information");
        row = FormLayout.fullRow(form, row, "Full Name", fullNameField);
        row = FormLayout.splitRow(form, row, "Username", usernameField,
                editing ? "New Password (leave blank to keep current)" : "Password", passwordField);
        FormLayout.splitRow(form, row, "Role", roleBox, "Status", statusBox);

        dialog.onSave(() -> {
            try {
                if (editing) {
                    userManagementService.updateUser(existing, fullNameField.getText(), usernameField.getText(),
                            new String(passwordField.getPassword()), (UserRole) roleBox.getSelectedItem(),
                            (UserStatus) statusBox.getSelectedItem());
                } else {
                    userManagementService.addUser(fullNameField.getText(), usernameField.getText(),
                            new String(passwordField.getPassword()), (UserRole) roleBox.getSelectedItem(),
                            (UserStatus) statusBox.getSelectedItem());
                }
                return true;
            } catch (ValidationException ex) {
                Toast.warning(this, ex.getMessage());
                return false;
            } catch (SQLException ex) {
                Toast.error(this, "Database error: " + ex.getMessage());
                return false;
            }
        });

        dialog.showCentered(500);
        if (dialog.isSaved()) {
            Toast.success(this, editing ? "User updated successfully." : "User added successfully.");
        }
    }

    private void deleteSelected() {
        User user = selectedUser();
        if (user == null) {
            return;
        }
        boolean confirmed = Dialogs.confirmDelete(this, "Delete User",
                "Are you sure you want to delete " + user.getFullName() + "?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            userManagementService.deleteUser(user.getUserId());
            Toast.success(this, "User deleted.");
        } catch (SQLException ex) {
            Toast.error(this, "Failed to delete user: " + ex.getMessage());
        }
    }
}
