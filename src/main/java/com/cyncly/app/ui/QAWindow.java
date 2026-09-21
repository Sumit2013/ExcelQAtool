package com.cyncly.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

import com.cyncly.app.model.Checkpoint;
import com.cyncly.app.model.QAProduct;
import com.cyncly.app.service.QAservice;

public class QAWindow {

    private final QAservice service;
    private QAProduct currentProduct;

    private JFrame frame;
    private JLabel skuLabel;
    private JLabel typeLabel;
    private JLabel subtypeLabel;
    private JLabel descriptionLabel;
    private JLabel rowInfoLabel;
    private JLabel statusLabel;

    private JPanel checkpointPanel;
    private JTextField bugIdField;
    private JButton doneButton;
    private JButton nextButton;
    private JButton backButton;
    private JButton saveAndExitButton;
    private JPanel buttonPanel;

    private final List<CheckpointUI> checkpointUIList = new ArrayList<>();

    // ── simple DTO ───────────────────────────────────────────────────────────
    private static class CheckpointUI {
        Checkpoint checkpoint;
        JComboBox<String> dropdown;
        JTextField commentField;

        CheckpointUI(Checkpoint c, JComboBox<String> d, JTextField t) {
            this.checkpoint = c;
            this.dropdown = d;
            this.commentField = t;
        }
    }

    // ── constructors ─────────────────────────────────────────────────────────
    public QAWindow(QAProduct product) {
        this(new QAservice(), product);
    }

    public QAWindow(QAservice service, QAProduct initialProduct) {
        this.service = service;
        this.currentProduct = initialProduct;
    }

    // ── public entry point ───────────────────────────────────────────────────
    public void show() {
        // Use cross-platform L&F so our custom colours are not overridden
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Scope dark overrides to combo/list/textfield only.
        // Do NOT override Panel.background globally — it bleeds into JOptionPane.
        UIManager.put("ComboBox.background", Theme.FIELD_BG);
        UIManager.put("ComboBox.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", Theme.ACCENT_DIM);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.buttonBackground", Theme.FIELD_BG);
        UIManager.put("ComboBox.disabledBackground", Theme.FIELD_BG);
        UIManager.put("ComboBox.disabledForeground", Theme.TEXT_MUTED);
        UIManager.put("List.background", Theme.FIELD_BG);
        UIManager.put("List.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", Theme.ACCENT_DIM);
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("TextField.background", Theme.FIELD_BG);
        UIManager.put("TextField.foreground", Theme.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", Theme.ACCENT);
        UIManager.put("TextField.selectionBackground", Theme.ACCENT_DIM);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.inactiveForeground", Theme.TEXT_MUTED);

        // Keep JOptionPane / system dialogs light and fully readable
        Color dialogBg = new Color(245, 246, 250);
        Color dialogFg = new Color(25, 25, 35);
        UIManager.put("OptionPane.background", dialogBg);
        UIManager.put("OptionPane.messageForeground", dialogFg);
        UIManager.put("OptionPane.messageFont", Theme.plain(13));
        UIManager.put("OptionPane.buttonFont", Theme.bold(12));
        UIManager.put("Panel.background", dialogBg); // keeps dialog panels light
        UIManager.put("Label.foreground", dialogFg); // dialog message text

        frame = new JFrame("Excel QA Tool – Product Validation");
        frame.setSize(980, 720);
        frame.setMinimumSize(new Dimension(780, 540));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });

        // Root background
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ── Title bar strip ───────────────────────────────────────────────
        root.add(createTitleBar(), BorderLayout.NORTH);

        // ── Center: header card + checkpoints ────────────────────────────
        JPanel centerCol = new JPanel(new BorderLayout(0, 10));
        centerCol.setOpaque(false);
        centerCol.add(createHeaderCard(), BorderLayout.NORTH);
        centerCol.add(createCheckpointsArea(), BorderLayout.CENTER);
        root.add(centerCol, BorderLayout.CENTER);

        // ── South: bug field + buttons ────────────────────────────────────
        root.add(createBottomPanel(), BorderLayout.SOUTH);

        frame.setContentPane(root);

        if (currentProduct != null) {
            displayProduct(currentProduct);
        }
        frame.setVisible(true);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────
    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Excel QA Tool");
        title.setFont(Theme.bold(20));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Product Validation");
        subtitle.setFont(Theme.plain(13));
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);
        bar.add(left, BorderLayout.WEST);

        // Thin accent separator at the bottom
        bar.add(createSeparator(), BorderLayout.SOUTH);
        return bar;
    }

    // ── Product header card ───────────────────────────────────────────────────
    private JPanel createHeaderCard() {
        JPanel card = new JPanel(new GridLayout(2, 2, 14, 6));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundLineBorder(Theme.BORDER, 1, 10),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        skuLabel = styledLabel("SKU: —", Theme.TEXT_PRIMARY, 14, true);
        rowInfoLabel = styledLabel("Excel Row: —", Theme.TEXT_MUTED, 12, false);
        typeLabel = styledLabel("Type: —", Theme.TEXT_PRIMARY, 12, false);
        subtypeLabel = styledLabel("Subtype: —", Theme.TEXT_PRIMARY, 12, false);
        descriptionLabel = styledLabel("Description: —", Theme.TEXT_MUTED, 12, false);

        // Row 1: SKU (left) | Excel row (right)
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        row1.add(skuLabel, BorderLayout.WEST);
        row1.add(rowInfoLabel, BorderLayout.EAST);

        // Row 2: type | subtype
        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);
        row2.add(typeLabel);
        row2.add(subtypeLabel);

        card.add(row1);
        card.add(new JLabel()); // spacer
        card.add(row2);
        card.add(descriptionLabel);

        return card;
    }

    // ── Checkpoints scroll area ───────────────────────────────────────────────
    private JScrollPane createCheckpointsArea() {
        checkpointPanel = new JPanel();
        checkpointPanel.setLayout(new BoxLayout(checkpointPanel, BoxLayout.Y_AXIS));
        checkpointPanel.setBackground(Theme.BG_DARK);
        checkpointPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        JScrollPane scroll = new JScrollPane(checkpointPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                new RoundLineBorder(Theme.BORDER, 1, 10),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        scroll.setBackground(Theme.BG_DARK);
        scroll.getViewport().setBackground(Theme.BG_DARK);

        // Style the scrollbar
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.getVerticalScrollBar().setBackground(Theme.BG_DARK);
        scroll.getHorizontalScrollBar().setUI(new DarkScrollBarUI());

        // Section label above
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);

        JLabel sectionLabel = styledLabel("Quality Checkpoints", Theme.ACCENT, 13, true);
        wrapper.add(sectionLabel, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);

        // We need to return the scroll, but we want the wrapper in CENTER.
        // So we put it in a wrapper panel that is returned
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setOpaque(false);
        outer.add(sectionLabel, BorderLayout.NORTH);
        outer.add(scroll, BorderLayout.CENTER);

        // Save scroll as the component we return
        checkpointPanel.putClientProperty("scroll", scroll);
        return scroll;
    }

    // ── Bottom panel: bug field + buttons ────────────────────────────────────
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(createSeparator());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createBugRow());
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionRow());

        return panel;
    }

    private JPanel createBugRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel label = styledLabel("Bug ID & Description:", Theme.TEXT_PRIMARY, 12, true);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        bugIdField = styledTextField();

        row.add(label, BorderLayout.WEST);
        row.add(bugIdField, BorderLayout.CENTER);
        return row;
    }

    private JPanel createActionRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        statusLabel = styledLabel("Ready", Theme.TEXT_MUTED, 12, false);
        row.add(statusLabel, BorderLayout.WEST);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        saveAndExitButton = ghostButton("Save & Exit", Theme.BTN_GHOST, Theme.BTN_GHOST_HV);
        saveAndExitButton.addActionListener(e -> onSaveAndExitClicked());

        backButton = ghostButton("<< Back", Theme.BTN_GHOST, Theme.BTN_GHOST_HV);
        backButton.addActionListener(e -> onBackClicked());

        doneButton = accentButton("Done", Theme.ACCENT, Theme.ACCENT_HOVER);
        doneButton.addActionListener(e -> onDoneClicked());

        nextButton = accentButton("Next >>", Theme.ACCENT_DIM, Theme.ACCENT);
        nextButton.setVisible(false);
        nextButton.addActionListener(e -> onNextClicked());

        buttonPanel.add(saveAndExitButton);
        buttonPanel.add(backButton);
        buttonPanel.add(doneButton);
        buttonPanel.add(nextButton);

        row.add(buttonPanel, BorderLayout.EAST);
        return row;
    }

    // ── displayProduct ────────────────────────────────────────────────────────
    public void displayProduct(QAProduct product) {
        this.currentProduct = product;
        checkpointUIList.clear();

        skuLabel.setText("SKU: " + nvl(product.getSku(), "N/A"));
        typeLabel.setText("Type: " + nvl(product.getType(), "—"));
        subtypeLabel.setText("Subtype: " + nvl(product.getSubtype(), "—"));
        descriptionLabel.setText("Description: " + nvl(product.getDescription(), "—"));
        rowInfoLabel.setText("Row " + (product.getRowIndex() + 1));

        checkpointPanel.removeAll();

        // ── Column header row ─────────────────────────────────────────────
        JPanel headerRow = new JPanel(new GridLayout(1, 3, 10, 0));
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        headerRow.setBackground(Theme.CARD_BG);
        headerRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 6, 8)));

        headerRow.add(colHeader("Checkpoint"));
        headerRow.add(colHeader("Value / Status"));
        headerRow.add(colHeader("Comment / Remarks"));
        checkpointPanel.add(headerRow);
        checkpointPanel.add(Box.createVerticalStrut(4));

        // ── Checkpoint rows ───────────────────────────────────────────────
        if (product.getCheckpoints() != null) {
            int idx = 0;
            for (Checkpoint cp : product.getCheckpoints()) {
                JPanel row = buildCheckpointRow(cp, idx % 2 == 0);
                checkpointPanel.add(row);
                checkpointPanel.add(Box.createVerticalStrut(2));
                idx++;
            }
        }

        if (bugIdField != null) {
            bugIdField.setText(nvl(product.getBugIdAndDescription(), ""));
        }

        nextButton.setVisible(false);
        if (backButton != null)
            backButton.setEnabled(service.hasPreviousProduct());
        setStatus("Viewing SKU: " + product.getSku(), Theme.TEXT_MUTED);

        checkpointPanel.revalidate();
        checkpointPanel.repaint();
        if (buttonPanel != null) {
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }
    }

    private JPanel buildCheckpointRow(Checkpoint cp, boolean isEven) {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setBackground(isEven ? Theme.CARD_BG : Theme.ROW_ALT);
        row.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel nameLabel = new JLabel(cp.getName());
        nameLabel.setFont(Theme.plain(12));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setToolTipText(cp.getName());

        String[] options = cp.getValues() != null
                ? cp.getValues().toArray(new String[0])
                : new String[] { "Ok", "Need to confirm", "NA" };

        JComboBox<String> dropdown = styledCombo(options);
        if (cp.getSelectedValue() != null && !cp.getSelectedValue().isEmpty()) {
            String val = cp.getSelectedValue().trim();
            boolean found = false;
            for (int i = 0; i < dropdown.getItemCount(); i++) {
                if (dropdown.getItemAt(i).equalsIgnoreCase(val)) {
                    dropdown.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                dropdown.addItem(val);
                dropdown.setSelectedItem(val);
            }
        }

        JTextField commentField = styledTextField();
        if (cp.getComment() != null)
            commentField.setText(cp.getComment());

        row.add(nameLabel);
        row.add(dropdown);
        row.add(commentField);

        checkpointUIList.add(new CheckpointUI(cp, dropdown, commentField));
        return row;
    }

    // ── Button actions ────────────────────────────────────────────────────────
    private void onDoneClicked() {
        if (currentProduct == null) {
            JOptionPane.showMessageDialog(frame, "No product is currently loaded.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (CheckpointUI item : checkpointUIList) {
            item.checkpoint.setSelectedValue((String) item.dropdown.getSelectedItem());
            item.checkpoint.setComment(item.commentField.getText());
        }
        if (bugIdField != null)
            currentProduct.setBugIdAndDescription(bugIdField.getText().trim());

        try {
            service.saveCurrentProduct(currentProduct);
            setStatus("✓ Saved – SKU: " + currentProduct.getSku()
                    + "  (Row " + (currentProduct.getRowIndex() + 1) + ")", Theme.TEXT_SUCCESS);
            JOptionPane.showMessageDialog(frame,
                    "Data for SKU '" + currentProduct.getSku() + "' saved to Excel successfully!",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            nextButton.setVisible(true);
            buttonPanel.revalidate();
            buttonPanel.repaint();
        } catch (IOException ex) {
            ex.printStackTrace();
            setStatus("Save failed: " + ex.getMessage(), Theme.TEXT_ERROR);
            JOptionPane.showMessageDialog(frame,
                    "Error saving to Excel file:\n" + ex.getMessage()
                            + "\n\nIf the file is open in Excel, please close it and try again.",
                    "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onNextClicked() {
        setStatus("Loading next SKU…", Theme.TEXT_MUTED);
        QAProduct next = service.loadNextProduct();
        if (next != null) {
            displayProduct(next);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No more SKU rows found. You have reached the end!",
                    "End of Sheet", JOptionPane.INFORMATION_MESSAGE);
            setStatus("All SKU rows completed.", Theme.TEXT_SUCCESS);
            nextButton.setVisible(false);
            if (buttonPanel != null) {
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }
        }
    }

    private void onBackClicked() {
        setStatus("Loading previous SKU…", Theme.TEXT_MUTED);
        QAProduct prev = service.loadPreviousProduct();
        if (prev != null) {
            displayProduct(prev);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "You are already on the first SKU row.", "First SKU",
                    JOptionPane.INFORMATION_MESSAGE);
            setStatus("First SKU reached.", Theme.TEXT_MUTED);
        }
    }

    private void handleWindowClosing() {
        String[] opts = { "Save & Exit", "Exit without Saving", "Cancel" };
        int choice = JOptionPane.showOptionDialog(frame,
                "You have an active QA review session.\nWould you like to save before exiting?",
                "Exit Confirmation", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        if (choice == 0)
            onSaveAndExitClicked();
        else if (choice == 1) {
            service.close();
            frame.dispose();
            System.exit(0);
        }
    }

    private void onSaveAndExitClicked() {
        if (currentProduct != null) {
            for (CheckpointUI item : checkpointUIList) {
                item.checkpoint.setSelectedValue((String) item.dropdown.getSelectedItem());
                item.checkpoint.setComment(item.commentField.getText());
            }
            if (bugIdField != null)
                currentProduct.setBugIdAndDescription(bugIdField.getText().trim());
            try {
                service.saveAndClose(currentProduct);
                JOptionPane.showMessageDialog(frame,
                        "Progress saved for SKU: '" + currentProduct.getSku()
                                + "' (Row " + (currentProduct.getRowIndex() + 1) + ").\nGoodbye!",
                        "Progress Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                int proceed = JOptionPane.showConfirmDialog(frame,
                        "Error saving:\n" + ex.getMessage()
                                + "\n\nClose Excel and try again, or exit without saving?",
                        "Save Failed", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (proceed != JOptionPane.YES_OPTION)
                    return;
            }
        } else {
            service.close();
        }
        frame.dispose();
        System.exit(0);
    }

    // ── Utility / factory helpers ─────────────────────────────────────────────

    private static JLabel styledLabel(String text, Color fg, float size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(bold ? Theme.bold(size) : Theme.plain(size));
        l.setForeground(fg);
        return l;
    }

    private static JLabel colHeader(String text) {
        JLabel l = styledLabel(text, Theme.TEXT_MUTED, 11, true);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return l;
    }

    private static JTextField styledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(Theme.plain(12));
        tf.setBackground(Theme.FIELD_BG);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setCaretColor(Theme.ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundLineBorder(Theme.BORDER, 1, 6),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundLineBorder(Theme.BORDER_FOCUS, 1, 6),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundLineBorder(Theme.BORDER, 1, 6),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            }
        });
        return tf;
    }

    private static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.plain(12));
        cb.setBackground(Theme.FIELD_BG);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setOpaque(true);
        cb.setMaximumRowCount(14);

        // Custom UI: controls arrow button AND the selected-item paint area so
        // the background never reverts to white when focus is lost.
        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                // Draw a proper down-triangle via Graphics2D (glyph characters render as □ on
                // some JVMs)
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        // Fill background
                        g.setColor(Theme.FIELD_BG);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        // Draw solid downward triangle
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Theme.TEXT_MUTED);
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        int[] xs = { cx - 5, cx + 5, cx };
                        int[] ys = { cy - 3, cy - 3, cy + 3 };
                        g2.fillPolygon(xs, ys, 3);
                        g2.dispose();
                    }
                };
                btn.setBackground(Theme.FIELD_BG);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setOpaque(true);
                btn.setPreferredSize(new Dimension(22, 0));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds,
                    boolean hasFocus) {
                // Always fill with FIELD_BG — never let Metal paint a white background
                g.setColor(Theme.FIELD_BG);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Paint the selected item ourselves to guarantee FIELD_BG background
                Object selected = comboBox.getSelectedItem();
                JLabel lbl = new JLabel(selected != null ? selected.toString() : "");
                lbl.setFont(Theme.plain(12));
                lbl.setForeground(Theme.TEXT_PRIMARY);
                lbl.setBackground(Theme.FIELD_BG);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
                currentValuePane.paintComponent(g, lbl, comboBox,
                        bounds.x, bounds.y, bounds.width, bounds.height, false);
            }

        });

        cb.setBorder(new RoundLineBorder(Theme.BORDER, 1, 6));

        // Style the drop-down popup list
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object val,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
                setFont(Theme.plain(12));
                setForeground(isSelected ? Color.WHITE : Theme.TEXT_PRIMARY);
                setBackground(isSelected ? Theme.ACCENT_DIM : Theme.FIELD_BG);
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        return cb;
    }

    /** Primary accent button. */
    private static JButton accentButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.bold(12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 34));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    /** Secondary ghost button. */
    private static JButton ghostButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.bold(12));
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 34));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private static Component createSeparator() {
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(1, 1));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBackground(Theme.BORDER);
        return sep;
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private static String nvl(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    // ── Custom dark scroll-bar UI ─────────────────────────────────────────────
    private static class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(60, 72, 110);
            trackColor = Theme.BG_DARK;
            thumbDarkShadowColor = Theme.BG_DARK;
            thumbHighlightColor = Theme.BG_DARK;
            thumbLightShadowColor = Theme.BG_DARK;
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return invisibleBtn();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return invisibleBtn();
        }

        private static JButton invisibleBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
    }

    // ── Custom rounded border ─────────────────────────────────────────────────
    static class RoundLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int arc;

        RoundLineBorder(Color color, int thickness, int arc) {
            this.color = color;
            this.thickness = thickness;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
            return insets;
        }
    }
}