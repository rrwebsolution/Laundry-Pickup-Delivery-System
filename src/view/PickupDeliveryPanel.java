package view;

import dao.LaundryOrderDAO;
import dao.UserDAO;
import event.DataChangeEvent;
import event.DataChangeListener;
import event.DataChangeManager;
import model.LaundryOrder;
import model.PickupDelivery;
import model.PickupDeliveryStatus;
import model.Rider;
import model.TransactionType;
import model.User;
import model.UserRole;
import service.PickupDeliveryService;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PickupDeliveryPanel extends JPanel implements DataChangeListener, Refreshable {

    private final PickupDeliveryService pickupDeliveryService = new PickupDeliveryService();
    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();
    private final UserDAO userDAO = new UserDAO();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter TIME_DISPLAY = DateTimeFormatter.ofPattern("h:mm a");

    private final JLabel countLabel = UITheme.helperText("");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Txn ID", "Order ID", "Customer", "Type", "Address", "Rider", "Scheduled Date",
                    "Scheduled Time", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton editButton = UITheme.secondaryButton(AppIcon.Name.EDIT, "Edit");
    private final JButton deleteButton = UITheme.dangerButton(AppIcon.Name.DELETE, "Delete");

    public PickupDeliveryPanel() {
        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadRecords();
        DataChangeManager.addListener(this);
    }

    @Override
    public void onDataChanged(DataChangeEvent event) {
        if (event == DataChangeEvent.PICKUP_DELIVERY_CHANGED) {
            loadRecords();
        }
    }

    @Override
    public void refreshNow() {
        loadRecords();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Pickup & Delivery");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Schedule pickups/deliveries and assign riders.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JButton addButton = UITheme.primaryButton(AppIcon.Name.ADD, "Schedule Pickup/Delivery");
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
        table.getColumnModel().getColumn(3).setCellRenderer(new TypeCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new view.components.IconTextCellRenderer(AppIcon.Name.LOCATION));
        table.getColumnModel().getColumn(5).setCellRenderer(new view.components.IconTextCellRenderer(AppIcon.Name.RIDER));
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
        editButton.addActionListener(e -> openForm(selectedTransactionId()));
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

    private Integer selectedTransactionId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (int) tableModel.getValueAt(row, 0);
    }

    private void loadRecords() {
        Async.run(pickupDeliveryService::getAll, this::populateTable,
                ex -> Toast.error(this, "Failed to load pickup/delivery records: " + ex.getMessage()));
    }

    private void populateTable(List<PickupDelivery> records) {
        tableModel.setRowCount(0);
        for (PickupDelivery pd : records) {
            tableModel.addRow(new Object[]{
                    pd.getTransactionId(),
                    pd.getOrder().getOrderId(),
                    pd.getOrder().getCustomer().getFullName(),
                    pd.getType().getLabel(),
                    pd.getAddress(),
                    pd.getAssignedRider() == null ? "Unassigned" : pd.getAssignedRider().getFullName(),
                    pd.getScheduledDate() == null ? "" : pd.getScheduledDate().format(DATE_DISPLAY),
                    pd.getScheduledTime() == null ? "" : pd.getScheduledTime().format(TIME_DISPLAY),
                    pd.getStatus().getLabel()
            });
        }
        countLabel.setText(records.size() + (records.size() == 1 ? " record" : " records"));
        updateActionState();
    }

    private void openForm(Integer transactionId) {
        boolean editing = transactionId != null;
        PickupDelivery existing = null;
        if (editing) {
            try {
                existing = new dao.PickupDeliveryDAO().findById(transactionId);
            } catch (SQLException ex) {
                Toast.error(this, "Failed to load record: " + ex.getMessage());
                return;
            }
        }
        final PickupDelivery existingRecord = existing;

        EntityFormDialog dialog = new EntityFormDialog(SwingUtilities.getWindowAncestor(this),
                editing ? "Edit Pickup/Delivery" : "Schedule Pickup/Delivery",
                editing ? "Update this schedule." : "Schedule a pickup or delivery for an order.",
                editing ? "Save Changes" : "Schedule");

        JComboBox<LaundryOrder> orderBox = UITheme.comboBox();
        JComboBox<TransactionType> typeBox = UITheme.comboBox();
        for (TransactionType type : TransactionType.values()) {
            typeBox.addItem(type);
        }
        JTextField addressField = UITheme.textField();
        addressField.putClientProperty("JTextField.leadingIcon", AppIcon.of(AppIcon.Name.LOCATION, 16, UITheme.ICON_COLOR));
        JComboBox<Rider> riderBox = UITheme.comboBox();
        JTextField scheduledDateField = UITheme.textField();
        UITheme.placeholder(scheduledDateField, "yyyy-MM-dd");
        scheduledDateField.putClientProperty("JTextField.leadingIcon", AppIcon.of(AppIcon.Name.CALENDAR, 16, UITheme.ICON_COLOR));
        JTextField scheduledTimeField = UITheme.textField();
        UITheme.placeholder(scheduledTimeField, "HH:mm (24-hour)");
        scheduledTimeField.putClientProperty("JTextField.leadingIcon", AppIcon.of(AppIcon.Name.CLOCK, 16, UITheme.ICON_COLOR));
        JComboBox<PickupDeliveryStatus> statusBox = UITheme.comboBox();
        for (PickupDeliveryStatus status : PickupDeliveryStatus.values()) {
            statusBox.addItem(status);
        }
        JTextField notesField = UITheme.textField();

        try {
            for (LaundryOrder o : orderDAO.findAll()) {
                orderBox.addItem(o);
            }
            riderBox.addItem(null);
            for (User u : userDAO.findByRole(UserRole.RIDER)) {
                riderBox.addItem((Rider) u);
            }
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load orders/riders: " + ex.getMessage());
        }

        orderBox.addActionListener(e -> {
            LaundryOrder order = (LaundryOrder) orderBox.getSelectedItem();
            if (order != null && addressField.getText().trim().isEmpty()) {
                addressField.setText(order.getCustomer().getAddress());
            }
        });

        if (editing) {
            selectOrderItem(orderBox, existingRecord.getOrder().getOrderId());
            typeBox.setSelectedItem(existingRecord.getType());
            addressField.setText(existingRecord.getAddress());
            selectRiderItem(riderBox, existingRecord.getAssignedRider());
            scheduledDateField.setText(existingRecord.getScheduledDate() == null ? "" : existingRecord.getScheduledDate().format(DATE_FMT));
            scheduledTimeField.setText(existingRecord.getScheduledTime() == null ? "" : existingRecord.getScheduledTime().format(TIME_FMT));
            statusBox.setSelectedItem(existingRecord.getStatus());
            notesField.setText(existingRecord.getNotes() == null ? "" : existingRecord.getNotes());
        }

        JPanel form = dialog.getForm();
        int row = FormLayout.sectionTitle(form, 0, "Schedule Details");
        row = FormLayout.splitRow(form, row, "Order", orderBox, "Type", typeBox);
        row = FormLayout.fullRow(form, row, "Address", addressField);
        row = FormLayout.fullRow(form, row, "Assigned Rider", riderBox);
        row = FormLayout.splitRow(form, row, "Scheduled Date", scheduledDateField, "Scheduled Time", scheduledTimeField);
        row = FormLayout.fullRow(form, row, "Status", statusBox);
        FormLayout.fullRow(form, row, "Notes", notesField);

        dialog.onSave(() -> {
            LaundryOrder order = (LaundryOrder) orderBox.getSelectedItem();
            if (order == null) {
                Toast.warning(this, "Select an order.");
                return false;
            }
            PickupDelivery pd = new PickupDelivery();
            pd.setOrder(order);
            pd.setType((TransactionType) typeBox.getSelectedItem());
            pd.setAddress(addressField.getText().trim());
            pd.setAssignedRider((Rider) riderBox.getSelectedItem());
            if (scheduledDateField.getText().trim().isEmpty() || scheduledTimeField.getText().trim().isEmpty()) {
                Toast.warning(this, "Scheduled date and time are required.");
                return false;
            }
            try {
                pd.setScheduledDate(LocalDate.parse(scheduledDateField.getText().trim(), DATE_FMT));
            } catch (Exception ex) {
                Toast.warning(this, "Scheduled date must be in yyyy-MM-dd format.");
                return false;
            }
            try {
                pd.setScheduledTime(LocalTime.parse(scheduledTimeField.getText().trim(), TIME_FMT));
            } catch (Exception ex) {
                Toast.warning(this, "Scheduled time must be in HH:mm (24-hour) format.");
                return false;
            }
            pd.setStatus((PickupDeliveryStatus) statusBox.getSelectedItem());
            pd.setNotes(notesField.getText().trim());
            try {
                if (editing) {
                    pd.setTransactionId(existingRecord.getTransactionId());
                    pickupDeliveryService.update(pd);
                } else {
                    pickupDeliveryService.schedule(pd);
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

        dialog.showCentered(520);
        if (dialog.isSaved()) {
            Toast.success(this, editing ? "Record updated successfully." : "Pickup/Delivery scheduled successfully.");
        }
    }

    private void selectOrderItem(JComboBox<LaundryOrder> box, int orderId) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).getOrderId() == orderId) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectRiderItem(JComboBox<Rider> box, Rider rider) {
        if (rider == null) {
            box.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < box.getItemCount(); i++) {
            Rider r = box.getItemAt(i);
            if (r != null && r.getUserId() == rider.getUserId()) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void deleteSelected() {
        Integer transactionId = selectedTransactionId();
        if (transactionId == null) {
            return;
        }
        boolean confirmed = Dialogs.confirmDelete(this, "Delete Record",
                "Are you sure you want to delete this pickup/delivery record?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            pickupDeliveryService.delete(transactionId);
            Toast.success(this, "Record deleted.");
        } catch (SQLException ex) {
            Toast.error(this, "Failed to delete record: " + ex.getMessage());
        }
    }

    /** Renders the Type column with a small pickup/delivery glyph so it reads at a glance. */
    private static class TypeCellRenderer extends ZebraRowRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = String.valueOf(value);
            AppIcon.Name iconName = text.equals("Pickup") ? AppIcon.Name.PICKUP : AppIcon.Name.DELIVERY;
            label.setIcon(AppIcon.of(iconName, 15, UITheme.ICON_COLOR));
            label.setIconTextGap(6);
            label.setText(text);
            return label;
        }
    }
}
