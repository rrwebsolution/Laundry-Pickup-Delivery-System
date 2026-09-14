package view;

import dao.CustomerDAO;
import dao.LaundryOrderDAO;
import dao.LaundryServiceDAO;
import model.Customer;
import model.LaundryOrder;
import model.LaundryService;
import model.OrderStatus;
import service.OrderService;
import service.ValidationException;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderPanel extends JPanel {

    private final OrderService orderService = new OrderService();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final LaundryServiceDAO laundryServiceDAO = new LaundryServiceDAO();
    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();

    private final JTextField searchField = UITheme.searchField("Search Order ID or Customer...");
    private final JComboBox<String> statusFilterBox = UITheme.comboBox();
    private final JLabel countLabel = UITheme.helperText("");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Order ID", "Customer", "Service", "Weight/Qty", "Total Amount", "Order Date",
                    "Expected Completion", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton viewButton = UITheme.secondaryButton(AppIcon.Name.VIEW, "View Details");
    private final JButton editButton = UITheme.secondaryButton(AppIcon.Name.EDIT, "Edit");
    private final JButton deleteButton = UITheme.dangerButton(AppIcon.Name.DELETE, "Delete");

    public OrderPanel() {
        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        statusFilterBox.addItem("All Statuses");
        for (OrderStatus status : OrderStatus.values()) {
            statusFilterBox.addItem(status.getLabel());
        }

        loadOrders();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Orders");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Track and manage every laundry order from pickup to delivery.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new BorderLayout(UITheme.SPACE_MD, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(UITheme.SPACE_LG, 0, 0, 0));

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterGroup.setOpaque(false);
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.addActionListener(e -> search());
        statusFilterBox.setPreferredSize(new Dimension(170, 36));
        statusFilterBox.addActionListener(e -> search());
        JButton searchButton = UITheme.secondaryButton(AppIcon.Name.SEARCH, "Search");
        searchButton.addActionListener(e -> search());
        JButton showAllButton = UITheme.secondaryButton("Show All");
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterBox.setSelectedIndex(0);
            loadOrders();
        });
        filterGroup.add(searchField);
        filterGroup.add(statusFilterBox);
        filterGroup.add(searchButton);
        filterGroup.add(showAllButton);

        JButton addButton = UITheme.primaryButton(AppIcon.Name.ADD, "Add New Order");
        addButton.addActionListener(e -> openForm(null));

        toolbar.add(filterGroup, BorderLayout.WEST);
        toolbar.add(addButton, BorderLayout.EAST);
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
        table.getColumnModel().getColumn(0).setMaxWidth(80);
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
        viewButton.addActionListener(e -> viewSelected());
        editButton.addActionListener(e -> openForm(selectedOrderId()));
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

    private Integer selectedOrderId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (int) tableModel.getValueAt(row, 0);
    }

    private void loadOrders() {
        try {
            populateTable(orderService.getAllOrders());
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load orders: " + ex.getMessage());
        }
    }

    private void search() {
        try {
            String status = (String) statusFilterBox.getSelectedItem();
            populateTable(orderService.search(searchField.getText(), "All Statuses".equals(status) ? "All" : status));
        } catch (SQLException ex) {
            Toast.error(this, "Search failed: " + ex.getMessage());
        }
    }

    private void populateTable(List<LaundryOrder> orders) {
        tableModel.setRowCount(0);
        for (LaundryOrder o : orders) {
            tableModel.addRow(new Object[]{
                    o.getOrderId(),
                    o.getCustomer().getFullName(),
                    o.getService().getServiceName(),
                    o.getWeightQuantity(),
                    "PHP " + o.getTotalAmount(),
                    o.getOrderDate() == null ? "" : o.getOrderDate().format(DATETIME_FMT),
                    o.getExpectedCompletionDate() == null ? "" : o.getExpectedCompletionDate().format(DATE_FMT),
                    o.getStatus().getLabel()
            });
        }
        countLabel.setText(orders.size() + (orders.size() == 1 ? " order" : " orders"));
        updateActionState();
    }

    private void viewSelected() {
        Integer orderId = selectedOrderId();
        if (orderId == null) {
            return;
        }
        try {
            LaundryOrder order = orderDAO.findById(orderId);
            if (order == null) {
                Toast.error(this, "Order not found.");
                return;
            }
            new OrderDetailsDialog(SwingUtilities.getWindowAncestor(this), order).setVisible(true);
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load order: " + ex.getMessage());
        }
    }

    private void openForm(Integer orderId) {
        boolean editing = orderId != null;
        LaundryOrder existing = null;
        if (editing) {
            try {
                existing = orderDAO.findById(orderId);
            } catch (SQLException ex) {
                Toast.error(this, "Failed to load order: " + ex.getMessage());
                return;
            }
        }
        final LaundryOrder existingOrder = existing;

        EntityFormDialog dialog = new EntityFormDialog(SwingUtilities.getWindowAncestor(this),
                editing ? "Edit Order" : "Add New Order",
                editing ? "Update this order's details." : "Create a new laundry order for a customer.",
                editing ? "Save Changes" : "Create Order");

        JComboBox<Customer> customerBox = UITheme.comboBox();
        JComboBox<LaundryService> serviceBox = UITheme.comboBox();
        JTextField weightField = UITheme.textField();
        JTextField pickupFeeField = UITheme.textField();
        JTextField deliveryFeeField = UITheme.textField();
        JTextField totalAmountField = UITheme.textField();
        totalAmountField.setEditable(false);
        totalAmountField.setBackground(new Color(0xF1, 0xF5, 0xF9));
        JTextField expectedDateField = UITheme.textField();
        UITheme.placeholder(expectedDateField, "yyyy-MM-dd");
        expectedDateField.putClientProperty("JTextField.leadingIcon", AppIcon.of(AppIcon.Name.CALENDAR, 16, UITheme.ICON_COLOR));
        JComboBox<OrderStatus> statusBox = UITheme.comboBox();
        for (OrderStatus status : OrderStatus.values()) {
            statusBox.addItem(status);
        }
        JTextField notesField = UITheme.textField();

        try {
            for (Customer c : customerDAO.findAll()) {
                customerBox.addItem(c);
            }
            for (LaundryService s : laundryServiceDAO.findAll()) {
                serviceBox.addItem(s);
            }
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load customers/services: " + ex.getMessage());
        }

        Runnable recalc = () -> {
            try {
                LaundryService service = (LaundryService) serviceBox.getSelectedItem();
                if (service == null) {
                    return;
                }
                BigDecimal weight = parseOrZero(weightField.getText());
                BigDecimal pickupFee = parseOrZero(pickupFeeField.getText());
                BigDecimal deliveryFee = parseOrZero(deliveryFeeField.getText());
                BigDecimal laundryCost = orderService.computeLaundryCost(weight, service);
                totalAmountField.setText(orderService.computeTotal(laundryCost, pickupFee, deliveryFee).toPlainString());
            } catch (NumberFormatException ignored) {
                totalAmountField.setText("");
            }
        };
        serviceBox.addActionListener(e -> recalc.run());
        FocusAdapter recalcOnBlur = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                recalc.run();
            }
        };
        weightField.addFocusListener(recalcOnBlur);
        pickupFeeField.addFocusListener(recalcOnBlur);
        deliveryFeeField.addFocusListener(recalcOnBlur);

        if (editing) {
            selectByOrderCustomer(customerBox, existingOrder.getCustomer().getCustomerId());
            selectByOrderService(serviceBox, existingOrder.getService().getServiceId());
            weightField.setText(existingOrder.getWeightQuantity().toPlainString());
            pickupFeeField.setText(existingOrder.getPickupFee().toPlainString());
            deliveryFeeField.setText(existingOrder.getDeliveryFee().toPlainString());
            totalAmountField.setText(existingOrder.getTotalAmount().toPlainString());
            expectedDateField.setText(existingOrder.getExpectedCompletionDate() == null ? ""
                    : existingOrder.getExpectedCompletionDate().format(DATE_FMT));
            statusBox.setSelectedItem(existingOrder.getStatus());
            notesField.setText(existingOrder.getNotes() == null ? "" : existingOrder.getNotes());
        } else {
            pickupFeeField.setText("0");
            deliveryFeeField.setText("0");
        }

        JPanel form = dialog.getForm();
        int row = FormLayout.sectionTitle(form, 0, "Order Details");
        row = FormLayout.splitRow(form, row, "Customer", customerBox, "Service", serviceBox);
        row = FormLayout.splitRow(form, row, "Weight / Quantity", weightField, "Expected Completion (yyyy-MM-dd)",
                expectedDateField);
        row = FormLayout.sectionTitle(form, row, "Fees & Total");
        row = FormLayout.splitRow(form, row, "Pickup Fee", pickupFeeField, "Delivery Fee", deliveryFeeField);
        row = FormLayout.fullRow(form, row, "Total Amount (auto-calculated)", totalAmountField);
        row = FormLayout.sectionTitle(form, row, "Status & Notes");
        row = FormLayout.fullRow(form, row, "Status", statusBox);
        FormLayout.fullRow(form, row, "Notes", notesField);

        dialog.onSave(() -> {
            Customer customer = (Customer) customerBox.getSelectedItem();
            LaundryService service = (LaundryService) serviceBox.getSelectedItem();
            if (customer == null || service == null) {
                Toast.warning(this, "Select a customer and a service.");
                return false;
            }
            LaundryOrder order = new LaundryOrder();
            order.setCustomer(customer);
            order.setService(service);
            try {
                order.setWeightQuantity(parseOrZero(weightField.getText()));
                order.setPickupFee(parseOrZero(pickupFeeField.getText()));
                order.setDeliveryFee(parseOrZero(deliveryFeeField.getText()));
            } catch (NumberFormatException ex) {
                Toast.warning(this, "Weight/Quantity and fees must be valid numbers.");
                return false;
            }
            if (!expectedDateField.getText().trim().isEmpty()) {
                try {
                    order.setExpectedCompletionDate(LocalDate.parse(expectedDateField.getText().trim(), DATE_FMT));
                } catch (Exception ex) {
                    Toast.warning(this, "Expected completion date must be in yyyy-MM-dd format.");
                    return false;
                }
            }
            order.setStatus((OrderStatus) statusBox.getSelectedItem());
            order.setNotes(notesField.getText().trim());
            try {
                if (editing) {
                    order.setOrderId(existingOrder.getOrderId());
                    orderService.updateOrder(order);
                } else {
                    orderService.addOrder(order);
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

        dialog.showCentered(560);
        if (dialog.isSaved()) {
            Toast.success(this, editing ? "Order updated successfully." : "Order created successfully.");
            loadOrders();
        }
    }

    private BigDecimal parseOrZero(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text.trim());
    }

    private void selectByOrderCustomer(JComboBox<Customer> box, int id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).getCustomerId() == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectByOrderService(JComboBox<LaundryService> box, int id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).getServiceId() == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void deleteSelected() {
        Integer orderId = selectedOrderId();
        if (orderId == null) {
            return;
        }
        boolean confirmed = Dialogs.confirmDelete(this, "Delete Order",
                "Are you sure you want to delete Order #" + orderId + "?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            orderService.deleteOrder(orderId);
            Toast.success(this, "Order deleted (any related pickup/delivery and payment records were removed too).");
            loadOrders();
        } catch (SQLException ex) {
            Toast.error(this, "Failed to delete order: " + ex.getMessage());
        }
    }
}
