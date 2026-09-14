package view;

import model.LaundryOrder;
import model.Payment;
import model.PickupDelivery;
import service.ReportService;
import view.components.Card;
import view.components.AppIcon;
import view.components.StatusCellRenderer;
import view.components.ZebraRowRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();

    private static final String[] REPORT_TYPES = {
            "Daily Orders", "Completed Orders", "Pending Orders", "Pickup/Delivery Records",
            "Payment Records", "Daily Revenue", "Monthly Revenue"
    };

    private final JComboBox<String> reportTypeBox = UITheme.comboBox();
    private final JTextField dateField = UITheme.textField();
    private final JTextField yearField = UITheme.textField();
    private final JTextField monthField = UITheme.textField();
    private final JLabel revenueValue = new JLabel(" ");
    private final Card revenueCard = new Card();

    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public ReportsPanel() {
        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Reports");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle("Generate operational and revenue reports on demand.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(subtitle);

        add(titleBlock, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, UITheme.SPACE_MD));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(UITheme.SPACE_LG, 0, 0, 0));
        center.add(buildControls(), BorderLayout.NORTH);

        revenueCard.setLayout(new BorderLayout());
        revenueCard.setVisible(false);
        revenueCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        JLabel revenueCaption = new JLabel("Total Revenue", AppIcon.of(AppIcon.Name.MONEY, 16, UITheme.SUCCESS),
                SwingConstants.LEFT);
        revenueCaption.setIconTextGap(8);
        revenueCaption.setFont(UITheme.FONT_LABEL_BOLD);
        revenueCaption.setForeground(UITheme.TEXT_SECONDARY);
        revenueValue.setFont(UITheme.FONT_CARD_VALUE.deriveFont(30f));
        revenueValue.setForeground(UITheme.SUCCESS);
        JPanel revenueInner = new JPanel();
        revenueInner.setOpaque(false);
        revenueInner.setLayout(new BoxLayout(revenueInner, BoxLayout.Y_AXIS));
        revenueInner.add(revenueCaption);
        revenueInner.add(revenueValue);
        revenueCard.add(revenueInner, BorderLayout.WEST);

        Card tableCard = new Card();
        tableCard.setLayout(new BorderLayout());
        UITheme.styleTable(table);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        JPanel resultsWrapper = new JPanel();
        resultsWrapper.setOpaque(false);
        resultsWrapper.setLayout(new BoxLayout(resultsWrapper, BoxLayout.Y_AXIS));
        resultsWrapper.add(revenueCard);
        resultsWrapper.add(Box.createVerticalStrut(UITheme.SPACE_MD));
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(tableCard, BorderLayout.CENTER);
        resultsWrapper.add(tableWrap);

        center.add(resultsWrapper, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        for (String type : REPORT_TYPES) {
            reportTypeBox.addItem(type);
        }
        UITheme.placeholder(dateField, "yyyy-MM-dd");
        dateField.setText(LocalDate.now().format(DATE_FMT));
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        monthField.setText(String.valueOf(LocalDate.now().getMonthValue()));

        generateReport();
    }

    private Card buildControls() {
        Card card = new Card();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        reportTypeBox.setPreferredSize(new Dimension(190, 34));
        dateField.setPreferredSize(new Dimension(120, 34));
        yearField.setPreferredSize(new Dimension(70, 34));
        monthField.setPreferredSize(new Dimension(50, 34));

        JButton generateButton = UITheme.primaryButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());

        card.add(labeled("Report Type", reportTypeBox));
        card.add(labeled("Date", dateField));
        card.add(labeled("Year", yearField));
        card.add(labeled("Month", monthField));
        JPanel buttonWrap = new JPanel();
        buttonWrap.setOpaque(false);
        buttonWrap.setBorder(new EmptyBorder(18, 0, 0, 0));
        buttonWrap.add(generateButton);
        card.add(buttonWrap);
        return card;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = UITheme.helperText(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(field);
        return panel;
    }

    private void generateReport() {
        String type = (String) reportTypeBox.getSelectedItem();
        revenueCard.setVisible(false);
        try {
            switch (type) {
                case "Daily Orders":
                    showOrders(reportService.dailyOrders(parseDate()));
                    break;
                case "Completed Orders":
                    showOrders(reportService.completedOrders());
                    break;
                case "Pending Orders":
                    showOrders(reportService.pendingOrders());
                    break;
                case "Pickup/Delivery Records":
                    showPickupDeliveries(reportService.pickupDeliveryRecords());
                    break;
                case "Payment Records":
                    showPayments(reportService.paymentRecords());
                    break;
                case "Daily Revenue":
                    tableModel.setDataVector(new Object[0][0], new Object[0]);
                    revenueValue.setText("PHP " + reportService.dailyRevenue(parseDate()) + "  (" + parseDate() + ")");
                    revenueCard.setVisible(true);
                    break;
                case "Monthly Revenue":
                    tableModel.setDataVector(new Object[0][0], new Object[0]);
                    int year = Integer.parseInt(yearField.getText().trim());
                    int month = Integer.parseInt(monthField.getText().trim());
                    revenueValue.setText("PHP " + reportService.monthlyRevenue(year, month) + "  ("
                            + year + "-" + String.format("%02d", month) + ")");
                    revenueCard.setVisible(true);
                    break;
                default:
                    break;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        revalidate();
        repaint();
    }

    private LocalDate parseDate() {
        return LocalDate.parse(dateField.getText().trim(), DATE_FMT);
    }

    private void applyRenderers(int statusColumnIndex) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i)
                    .setCellRenderer(i == statusColumnIndex ? new StatusCellRenderer() : new ZebraRowRenderer());
        }
    }

    private void showOrders(List<LaundryOrder> orders) {
        tableModel.setDataVector(new Object[0][0], new Object[]{"Order ID", "Customer", "Service", "Weight/Qty",
                "Total Amount", "Order Date", "Status"});
        for (LaundryOrder o : orders) {
            tableModel.addRow(new Object[]{
                    o.getOrderId(), o.getCustomer().getFullName(), o.getService().getServiceName(),
                    o.getWeightQuantity(), "PHP " + o.getTotalAmount(),
                    o.getOrderDate() == null ? "" : o.getOrderDate().format(DATETIME_FMT),
                    o.getStatus().getLabel()
            });
        }
        applyRenderers(6);
    }

    private void showPickupDeliveries(List<PickupDelivery> records) {
        tableModel.setDataVector(new Object[0][0], new Object[]{"Txn ID", "Order ID", "Customer", "Type", "Address",
                "Rider", "Scheduled Date", "Scheduled Time", "Status"});
        for (PickupDelivery pd : records) {
            tableModel.addRow(new Object[]{
                    pd.getTransactionId(), pd.getOrder().getOrderId(), pd.getOrder().getCustomer().getFullName(),
                    pd.getType().getLabel(), pd.getAddress(),
                    pd.getAssignedRider() == null ? "Unassigned" : pd.getAssignedRider().getFullName(),
                    pd.getScheduledDate(), pd.getScheduledTime(), pd.getStatus().getLabel()
            });
        }
        applyRenderers(8);
    }

    private void showPayments(List<Payment> payments) {
        tableModel.setDataVector(new Object[0][0], new Object[]{"Payment ID", "Order ID", "Customer",
                "Total Amount", "Amount Paid", "Change", "Method", "Payment Date", "Status"});
        for (Payment p : payments) {
            tableModel.addRow(new Object[]{
                    p.getPaymentId(), p.getOrder().getOrderId(), p.getOrder().getCustomer().getFullName(),
                    "PHP " + p.getTotalAmount(), "PHP " + p.getAmountPaid(), "PHP " + p.getChangeAmount(),
                    p.getPaymentMethod().getLabel(),
                    p.getPaymentDate() == null ? "" : p.getPaymentDate().format(DATETIME_FMT),
                    p.getPaymentStatus().getLabel()
            });
        }
        applyRenderers(8);
    }
}
