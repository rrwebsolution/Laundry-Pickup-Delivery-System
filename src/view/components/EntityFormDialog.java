package view.components;

import view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BooleanSupplier;

/**
 * Reusable modal "Add/Edit" dialog shell: a titled card containing a form
 * panel (populated by the caller via {@link #getForm()}) and a Cancel/Save
 * footer. Keeps every CRUD dialog in the app visually identical.
 */
public class EntityFormDialog extends JDialog {

    private final JPanel form = FormLayout.newForm();
    private final JButton saveButtonRef;
    private boolean saved = false;

    public EntityFormDialog(Window owner, String title, String subtitle, String saveLabel) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel titleLabel = UITheme.pageTitle(title);
        titleLabel.setFont(UITheme.FONT_SECTION_TITLE.deriveFont(19f));
        JLabel subtitleLabel = UITheme.subtitle(subtitle);
        subtitleLabel.setBorder(new EmptyBorder(2, 0, 20, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(subtitleLabel);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        content.add(header, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        JButton cancelButton = UITheme.secondaryButton(AppIcon.Name.CANCEL, "Cancel");
        JButton saveButton = UITheme.primaryButton(AppIcon.Name.SAVE, saveLabel);
        cancelButton.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(18, 0, 0, 0));
        footer.add(cancelButton);
        footer.add(saveButton);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
        this.saveButtonRef = saveButton;
    }

    public JPanel getForm() {
        return form;
    }

    /** Wires the Save button: if the validator returns true, the dialog closes and isSaved() becomes true. */
    public void onSave(BooleanSupplier validatorAndSaver) {
        for (var l : saveButtonRef.getActionListeners()) {
            saveButtonRef.removeActionListener(l);
        }
        saveButtonRef.addActionListener(e -> {
            if (validatorAndSaver.getAsBoolean()) {
                saved = true;
                dispose();
            }
        });
    }

    public boolean isSaved() {
        return saved;
    }

    public void showCentered(int width) {
        pack();
        setSize(Math.max(width, getWidth()), getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
}
