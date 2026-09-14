package view;

import model.LaundryService;
import model.PricingType;
import model.User;
import model.UserRole;
import service.ServiceCatalogService;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ServicePanel extends JPanel {

    private final ServiceCatalogService serviceCatalogService = new ServiceCatalogService();
    private final boolean canEdit;

    private final JLabel countLabel = UITheme.helperText("");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Service Name", "Description", "Price", "Pricing Type"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton editButton = UITheme.secondaryButton(AppIcon.Name.EDIT, "Edit");
    private final JButton deleteButton = UITheme.dangerButton(AppIcon.Name.DELETE, "Delete");

    public ServicePanel(User currentUser) {
        this.canEdit = currentUser.getRole() == UserRole.ADMINISTRATOR;

        setLayout(new BorderLayout(0, UITheme.SPACE_MD));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG, UITheme.SPACE_LG));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadServices();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UITheme.pageTitle("Laundry Services");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UITheme.subtitle(canEdit ? "Manage the services your shop offers and their pricing."
                : "View the services your shop offers and their pricing.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        if (canEdit) {
            JButton addButton = UITheme.primaryButton(AppIcon.Name.ADD, "Add Service");
            addButton.addActionListener(e -> openForm(null));
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            toolbar.setOpaque(false);
            toolbar.setBorder(new EmptyBorder(UITheme.SPACE_LG, 0, 0, 0));
            toolbar.add(addButton);
            header.add(toolbar, BorderLayout.SOUTH);
        }
        return header;
    }

    private Card buildTableCard() {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, UITheme.SPACE_MD));

        UITheme.styleTable(table);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new ZebraRowRenderer());
        }
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionState();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(null);

        JPanel actionsRow = new JPanel(new BorderLayout());
        actionsRow.setOpaque(false);
        if (canEdit) {
            JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            buttonGroup.setOpaque(false);
            editButton.addActionListener(e -> openForm(selectedService()));
            deleteButton.addActionListener(e -> deleteSelected());
            buttonGroup.add(editButton);
            buttonGroup.add(deleteButton);
            actionsRow.add(buttonGroup, BorderLayout.WEST);
        } else {
            JLabel notice = UITheme.helperText("Only Administrators can modify services.");
            actionsRow.add(notice, BorderLayout.WEST);
        }
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

    private void loadServices() {
        try {
            populateTable(serviceCatalogService.getAllServices());
        } catch (SQLException ex) {
            Toast.error(this, "Failed to load services: " + ex.getMessage());
        }
    }

    private void populateTable(List<LaundryService> services) {
        tableModel.setRowCount(0);
        for (LaundryService s : services) {
            tableModel.addRow(new Object[]{
                    s.getServiceId(), s.getServiceName(), s.getDescription(), "PHP " + s.getPrice(),
                    s.getPricingType().getLabel()
            });
        }
        countLabel.setText(services.size() + (services.size() == 1 ? " service" : " services"));
        updateActionState();
    }

    private LaundryService selectedService() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        LaundryService s = new LaundryService();
        s.setServiceId((int) tableModel.getValueAt(row, 0));
        s.setServiceName(String.valueOf(tableModel.getValueAt(row, 1)));
        s.setDescription(String.valueOf(tableModel.getValueAt(row, 2)));
        s.setPrice(new BigDecimal(String.valueOf(tableModel.getValueAt(row, 3)).replace("PHP ", "")));
        s.setPricingType(PricingType.fromLabel(String.valueOf(tableModel.getValueAt(row, 4))));
        return s;
    }

    private void openForm(LaundryService existing) {
        boolean editing = existing != null;
        EntityFormDialog dialog = new EntityFormDialog(SwingUtilities.getWindowAncestor(this),
                editing ? "Edit Service" : "Add Service",
                editing ? "Update this service's details." : "Define a new laundry service.",
                editing ? "Save Changes" : "Add Service");

        JTextField nameField = UITheme.textField();
        JTextField descriptionField = UITheme.textField();
        JTextField priceField = UITheme.textField();
        JComboBox<PricingType> pricingTypeBox = UITheme.comboBox();
        for (PricingType type : PricingType.values()) {
            pricingTypeBox.addItem(type);
        }
        if (editing) {
            nameField.setText(existing.getServiceName());
            descriptionField.setText(existing.getDescription());
            priceField.setText(existing.getPrice().toPlainString());
            pricingTypeBox.setSelectedItem(existing.getPricingType());
        }

        JPanel form = dialog.getForm();
        int row = FormLayout.sectionTitle(form, 0, "Service Details");
        row = FormLayout.fullRow(form, row, "Service Name", nameField);
        row = FormLayout.fullRow(form, row, "Description", descriptionField);
        FormLayout.splitRow(form, row, "Price (PHP)", priceField, "Pricing Type", pricingTypeBox);

        dialog.onSave(() -> {
            try {
                LaundryService service = new LaundryService();
                service.setServiceName(nameField.getText().trim());
                service.setDescription(descriptionField.getText().trim());
                try {
                    service.setPrice(new BigDecimal(priceField.getText().trim()));
                } catch (NumberFormatException ex) {
                    Toast.warning(this, "Enter a valid numeric price.");
                    return false;
                }
                service.setPricingType((PricingType) pricingTypeBox.getSelectedItem());
                if (editing) {
                    service.setServiceId(existing.getServiceId());
                    serviceCatalogService.updateService(service);
                } else {
                    serviceCatalogService.addService(service);
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

        dialog.showCentered(460);
        if (dialog.isSaved()) {
            Toast.success(this, editing ? "Service updated successfully." : "Service added successfully.");
            loadServices();
        }
    }

    private void deleteSelected() {
        LaundryService s = selectedService();
        if (s == null) {
            return;
        }
        boolean confirmed = Dialogs.confirmDelete(this, "Delete Service",
                "Are you sure you want to delete \"" + s.getServiceName() + "\"?\nThis action cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            serviceCatalogService.deleteService(s.getServiceId());
            Toast.success(this, "Service deleted.");
            loadServices();
        } catch (SQLException ex) {
            Toast.error(this, "Cannot delete: this service is used by existing orders, or a database error occurred.");
        }
    }
}
