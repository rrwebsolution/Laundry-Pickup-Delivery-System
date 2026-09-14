package view;

import dao.LaundryOrderDAO;
import event.DataChangeEvent;
import event.DataChangeListener;
import event.DataChangeManager;
import model.LaundryOrder;
import model.Payment;
import model.PaymentMethod;
import service.PaymentService;
import service.ValidationException;
import view.components.Async;
import view.components.Card;
import view.components.Dialogs;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentPanel extends JPanel implements DataChangeListener, Refreshable {

    private final PaymentService paymentService = new PaymentService();
    private final LaundryOrderDAO orderDAO = new LaundryOrderDAO();

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final JLabel countLabel = UITheme.helperText("");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Payment ID", "Order ID", "Customer", "Total Amount", "Amount Paid", "Change",
                    "Method", "Payment Date", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton editButton = UITheme.secondaryButton(AppIcon.Name.EDIT, "Edit");
    private final JButton deleteButton = UITheme.dangerButton(AppIcon.Name.DELETE, "Delete");

    public PaymentPanel() {
        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadPayments();
        DataChangeManager.addListener(this);
    }

    @Override
    public void onDataChanged(DataChangeEvent event) {
        if (event == DataChangeEvent.PAYMENT_CHANGED) {
            loadPayments();
        }
    }

    @Override
    public void refreshNow() {
        loadPayments();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Payments");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Record and track customer payments.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JButton addButton = UITheme.primaryButton(AppIcon.Name.ADD, "Record Payment");
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
        table.getColumnModel().getColumn(0).setMaxWidth(90);
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
        editButton.addActionListener(e -> openForm(selectedPaymentId()));
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

    private Integer selectedPaymentId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (int) tableModel.getValueAt(row, 0);
    }

    private void loadPayments() {
        Async.run(paymentService::getAll, this::populateTable,
                ex -> Toast.error(this, "Failed to load payments: " + ex.getMessage()));
    }

    private void populateTable(List<Payment> payments) {
        tableModel.setRowCount(0);
        for (Payment p : payments) {
            tableModel.addRow(new Object[]{
                    p.getPaymentId(),
                    p.getOrder().getOrderId(),
                    p.getOrder().getCustomer().getFullName(),
                    "PHP " + p.getTotalAmount(),
                    "PHP " + p.getAmountPaid(),
                    "PHP " + p.getChangeAmount(),
                    p.getPaymentMethod().getLabel(),
                    p.getPaymentDate() == null ? "" : p.getPaymentDate().format(DATETIME_FMT),
                    p.getPaymentStatus().getLabel()
            });
        }
        countLabel.setText(payments.size() + (payments.size() == 1 ? " payment" : " payments"));
        updateActionState();
    }

    private void openForm(Integer paymentId) {
        boolean editing = paymentId != null;
        Payment existing = null;
        if (editing) {
            try {
                existing = new dao.PaymentDAO().findById(paymentId);
            } catch (SQLException ex) {
                Toast.error(this, "Failed to load payment: " + ex.getMessage());
                return;
            }
        }
        final Payment existingPayment = existing;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                editing ? "Edit Payment" : "Record Payment", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel titleLabel = UITheme.sectionTitle(editing ? "Edit Payment" : "Record Payment");
        titleLabel.setFont(UITheme.FONT_SECTION_TITLE.deriveFont(19f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitleLabel = UITheme.subtitle("Select an order to see its billing summary.");
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(2, 0, 18, 0));

        JComboBox<LaundryOrder> orderBox = UITheme.comboBox();
        orderBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        try {
            for (LaundryOrder o : orderDAO.findAll()) {
                orderBox.addItem(o);
            }
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load orders: " + ex.getMessage());
        }

        JLabel orderLabel = new JLabel("Order");
        orderLabel.setFont(UITheme.FONT_LABEL_BOLD);
        orderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderLabel.setBorder(new EmptyBorder(0, 0, 4, 0));

        Card summaryCard = new Card().withBackground(new Color(0xF8, 0xFA, 0xFC)).withoutShadow();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        summaryCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel laundryCostRow = summaryRow(summaryCard, "Laundry Cost");
        JLabel pickupFeeRow = summaryRow(summaryCard, "Pickup Fee");
        JLabel deliveryFeeRow = summaryRow(summaryCard, "Delivery Fee");
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBorder(new EmptyBorder(8, 0, 8, 0));
        summaryCard.add(sep);
        JLabel totalRow = summaryRow(summaryCard, "Total");
        totalRow.setFont(UITheme.FONT_SECTION_TITLE);

        JLabel amountPaidLabel = new JLabel("Amount Paid");
        amountPaidLabel.setFont(UITheme.FONT_LABEL_BOLD);
        amountPaidLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountPaidLabel.setBorder(new EmptyBorder(16, 0, 4, 0));
        JTextField amountPaidField = UITheme.textField();
        amountPaidField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountPaidField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel methodLabel = new JLabel("Payment Method");
        methodLabel.setFont(UITheme.FONT_LABEL_BOLD);
        methodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodLabel.setBorder(new EmptyBorder(12, 0, 4, 0));
        JComboBox<PaymentMethod> methodBox = UITheme.comboBox();
        for (PaymentMethod method : PaymentMethod.values()) {
            methodBox.addItem(method);
        }
        methodBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel changeCaption = new JLabel("Change");
        changeCaption.setFont(UITheme.FONT_LABEL);
        changeCaption.setForeground(UITheme.TEXT_SECONDARY);
        JLabel changeValue = new JLabel("PHP 0.00");
        changeValue.setFont(UITheme.FONT_SECTION_TITLE);
        changeValue.setForeground(UITheme.SUCCESS);
        JPanel changeRow = new JPanel(new BorderLayout());
        changeRow.setOpaque(false);
        changeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        changeRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        changeRow.add(changeCaption, BorderLayout.WEST);
        changeRow.add(changeValue, BorderLayout.EAST);

        Runnable recalc = () -> {
            LaundryOrder order = (LaundryOrder) orderBox.getSelectedItem();
            if (order == null) {
                return;
            }
            BigDecimal laundryCost = order.getPrice().multiply(order.getWeightQuantity());
            laundryCostRow.setText("PHP " + laundryCost);
            pickupFeeRow.setText("PHP " + order.getPickupFee());
            deliveryFeeRow.setText("PHP " + order.getDeliveryFee());
            totalRow.setText("PHP " + order.getTotalAmount());
            try {
                BigDecimal paid = amountPaidField.getText().trim().isEmpty()
                        ? BigDecimal.ZERO : new BigDecimal(amountPaidField.getText().trim());
                BigDecimal change = Payment.calculateChange(paid, order.getTotalAmount());
                changeValue.setText("PHP " + change);
            } catch (NumberFormatException ex) {
                changeValue.setText("PHP 0.00");
            }
        };
        orderBox.addActionListener(e -> recalc.run());
        amountPaidField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                recalc.run();
            }
        });

        if (editing) {
            for (int i = 0; i < orderBox.getItemCount(); i++) {
                if (orderBox.getItemAt(i).getOrderId() == existingPayment.getOrder().getOrderId()) {
                    orderBox.setSelectedIndex(i);
                    break;
                }
            }
            amountPaidField.setText(existingPayment.getAmountPaid().toPlainString());
            methodBox.setSelectedItem(existingPayment.getPaymentMethod());
        }
        recalc.run();

        JButton cancelButton = UITheme.secondaryButton(AppIcon.Name.CANCEL, "Cancel");
        JButton saveButton = UITheme.primaryButton(AppIcon.Name.SAVE, editing ? "Save Changes" : "Record Payment");
        cancelButton.addActionListener(e -> dialog.dispose());
        boolean[] saved = {false};
        String originalSaveLabel = saveButton.getText();
        saveButton.addActionListener(e -> {
            LaundryOrder order = (LaundryOrder) orderBox.getSelectedItem();
            if (order == null) {
                Toast.warning(this, "Select an order.");
                return;
            }
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setTotalAmount(order.getTotalAmount());
            try {
                payment.setAmountPaid(new BigDecimal(amountPaidField.getText().trim()));
            } catch (NumberFormatException ex) {
                Toast.warning(this, "Enter a valid numeric amount paid.");
                return;
            }
            payment.setPaymentMethod((PaymentMethod) methodBox.getSelectedItem());
            saveButton.setEnabled(false);
            saveButton.setText("Saving...");
            try {
                if (editing) {
                    payment.setPaymentId(existingPayment.getPaymentId());
                    paymentService.updatePayment(payment);
                } else {
                    paymentService.recordPayment(payment);
                }
                saved[0] = true;
                dialog.dispose();
            } catch (ValidationException ex) {
                Toast.warning(this, ex.getMessage());
            } catch (SQLException ex) {
                Toast.error(this, "Database error: " + ex.getMessage());
            } finally {
                if (!saved[0]) {
                    saveButton.setEnabled(true);
                    saveButton.setText(originalSaveLabel);
                }
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));
        footer.add(cancelButton);
        footer.add(saveButton);

        content.add(titleLabel);
        content.add(subtitleLabel);
        content.add(orderLabel);
        content.add(orderBox);
        content.add(Box.createVerticalStrut(12));
        content.add(summaryCard);
        content.add(amountPaidLabel);
        content.add(amountPaidField);
        content.add(methodLabel);
        content.add(methodBox);
        content.add(changeRow);
        content.add(footer);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setSize(460, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        if (saved[0]) {
            Toast.success(this, editing ? "Payment updated successfully." : "Payment recorded successfully.");
        }
    }

    private JLabel summaryRow(JPanel container, String label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel keyLabel = new JLabel(label);
        keyLabel.setFont(UITheme.FONT_LABEL);
        keyLabel.setForeground(UITheme.TEXT_SECONDARY);
        JLabel valueLabel = new JLabel("PHP 0.00");
        valueLabel.setFont(UITheme.FONT_LABEL_BOLD);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);
        row.add(keyLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        container.add(row);
        return valueLabel;
    }

    private void deleteSelected() {
        Integer paymentId = selectedPaymentId();
        if (paymentId == null) {
            return;
        }
        boolean confirmed = Dialogs.confirmDelete(this, "Delete Payment",
                "Are you sure you want to delete this payment record?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            paymentService.deletePayment(paymentId);
            Toast.success(this, "Payment deleted.");
        } catch (SQLException ex) {
            Toast.error(this, "Failed to delete payment: " + ex.getMessage());
        }
    }
}
