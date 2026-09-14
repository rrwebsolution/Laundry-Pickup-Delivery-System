package view;

import model.Customer;
import service.CustomerService;
import service.ValidationException;
import view.components.Card;
import view.components.Dialogs;
import view.components.EntityFormDialog;
import view.components.FormLayout;
import view.components.AppIcon;
import view.components.Toast;
import view.components.ZebraRowRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerService customerService = new CustomerService();
    private final JTextField searchField = UITheme.searchField("Search by name or contact number...");
    private final JLabel countLabel = UITheme.helperText("");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Full Name", "Contact Number", "Email", "Address", "Date Registered"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton viewButton = UITheme.secondaryButton(AppIcon.Name.VIEW, "View");
    private final JButton editButton = UITheme.secondaryButton(AppIcon.Name.EDIT, "Edit");
    private final JButton deleteButton = UITheme.dangerButton(AppIcon.Name.DELETE, "Delete");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public CustomerPanel() {
        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadCustomers();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Customers");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Manage customer information and addresses.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new BorderLayout(UITheme.SPACE_MD, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(UITheme.SPACE_LG, 0, 0, 0));

        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.addActionListener(e -> search());
        JButton searchButton = UITheme.secondaryButton(AppIcon.Name.SEARCH, "Search");
        searchButton.addActionListener(e -> search());
        JButton showAllButton = UITheme.secondaryButton("Show All");
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            loadCustomers();
        });

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchGroup.setOpaque(false);
        searchGroup.add(searchField);
        searchGroup.add(searchButton);
        searchGroup.add(showAllButton);

        JButton addButton = UITheme.primaryButton(AppIcon.Name.ADD, "Add Customer");
        addButton.addActionListener(e -> openForm(null));

        toolbar.add(searchGroup, BorderLayout.WEST);
        toolbar.add(addButton, BorderLayout.EAST);

        JPanel headerWrap = new JPanel();
        headerWrap.setOpaque(false);
        headerWrap.setLayout(new BoxLayout(headerWrap, BoxLayout.Y_AXIS));
        header.add(toolbar, BorderLayout.SOUTH);
        return header;
    }

    private Card buildTableCard() {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, UITheme.SPACE_MD));

        UITheme.styleTable(table);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new ZebraRowRenderer());
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionState();
            }
        });
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);

        JPanel actionsRow = new JPanel(new BorderLayout());
        actionsRow.setOpaque(false);
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonGroup.setOpaque(false);
        viewButton.addActionListener(e -> viewSelected());
        editButton.addActionListener(e -> openForm(selectedCustomer()));
        deleteButton.addActionListener(e -> deleteSelected());
        buttonGroup.add(viewButton);
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
        viewButton.setEnabled(hasSelection);
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private void loadCustomers() {
        try {
            populateTable(customerService.getAllCustomers());
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load customers: " + ex.getMessage());
        }
    }

    private void search() {
        try {
            populateTable(customerService.search(searchField.getText()));
        } catch (SQLException ex) {
            Toast.error(this, "Search failed: " + ex.getMessage());
        }
    }

    private void populateTable(List<Customer> customers) {
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getCustomerId(), c.getFullName(), c.getContactNumber(),
                    c.getEmail() == null ? "" : c.getEmail(), c.getAddress(),
                    c.getDateRegistered() == null ? "" : c.getDateRegistered().format(DATE_FMT)
            });
        }
        countLabel.setText(customers.size() + (customers.size() == 1 ? " record" : " records"));
        updateActionState();
    }

    private Customer selectedCustomer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        Customer c = new Customer();
        c.setCustomerId((int) tableModel.getValueAt(row, 0));
        c.setFullName(String.valueOf(tableModel.getValueAt(row, 1)));
        c.setContactNumber(String.valueOf(tableModel.getValueAt(row, 2)));
        c.setEmail(String.valueOf(tableModel.getValueAt(row, 3)));
        c.setAddress(String.valueOf(tableModel.getValueAt(row, 4)));
        return c;
    }

    private void viewSelected() {
        Customer c = selectedCustomer();
        if (c == null) {
            return;
        }
        String message = "<html><body style='width:260px'>"
                + "<b>Full Name:</b> " + c.getFullName() + "<br><br>"
                + "<b>Contact Number:</b> " + c.getContactNumber() + "<br><br>"
                + "<b>Email:</b> " + (c.getEmail() == null || c.getEmail().isEmpty() ? "-" : c.getEmail()) + "<br><br>"
                + "<b>Address:</b> " + c.getAddress()
                + "</body></html>";
        JOptionPane.showMessageDialog(this, message, "Customer Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void openForm(Customer existing) {
        boolean editing = existing != null;
        EntityFormDialog dialog = new EntityFormDialog(SwingUtilities.getWindowAncestor(this),
                editing ? "Edit Customer" : "Add Customer",
                editing ? "Update this customer's information." : "Enter the new customer's information.",
                editing ? "Save Changes" : "Add Customer");

        JTextField nameField = UITheme.textField();
        JTextField contactField = UITheme.textField();
        JTextField emailField = UITheme.textField();
        JTextField addressField = UITheme.textField();
        addressField.putClientProperty("JTextField.leadingIcon", AppIcon.of(AppIcon.Name.LOCATION, 16, UITheme.ICON_COLOR));
        if (editing) {
            nameField.setText(existing.getFullName());
            contactField.setText(existing.getContactNumber());
            emailField.setText(existing.getEmail());
            addressField.setText(existing.getAddress());
        }

        JPanel form = dialog.getForm();
        int row = FormLayout.sectionTitle(form, 0, "Customer Information");
        row = FormLayout.fullRow(form, row, "Full Name", nameField);
        row = FormLayout.splitRow(form, row, "Contact Number", contactField, "Email", emailField);
        FormLayout.fullRow(form, row, "Address", addressField);

        dialog.onSave(() -> {
            try {
                Customer customer = new Customer();
                customer.setFullName(nameField.getText().trim());
                customer.setContactNumber(contactField.getText().trim());
                customer.setEmail(emailField.getText().trim());
                customer.setAddress(addressField.getText().trim());
                if (editing) {
                    customer.setCustomerId(existing.getCustomerId());
                    customerService.updateCustomer(customer);
                } else {
                    customerService.addCustomer(customer);
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

        dialog.showCentered(480);
        if (dialog.isSaved()) {
            Toast.success(this, editing ? "Customer updated successfully." : "Customer added successfully.");
            loadCustomers();
        }
    }

    private void deleteSelected() {
        Customer c = selectedCustomer();
        if (c == null) {
            return;
        }
        boolean confirmed = Dialogs.confirmDelete(this, "Delete Customer",
                "Are you sure you want to delete " + c.getFullName() + "?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            customerService.deleteCustomer(c.getCustomerId());
            Toast.success(this, "Customer deleted.");
            loadCustomers();
        } catch (SQLException ex) {
            Toast.error(this, "Cannot delete: this customer has existing orders, or a database error occurred.");
        }
    }
}
