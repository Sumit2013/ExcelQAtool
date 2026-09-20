package com.cyncly.app.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

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

    private static class CheckpointUI {
        Checkpoint checkpoint;
        JComboBox<String> dropdown;
        JTextField commentField;

        CheckpointUI(Checkpoint checkpoint, JComboBox<String> dropdown, JTextField commentField) {
            this.checkpoint = checkpoint;
            this.dropdown = dropdown;
            this.commentField = commentField;
        }
    }

    public QAWindow(QAProduct product) {
        this(new QAservice(), product);
    }

    public QAWindow(QAservice service, QAProduct initialProduct) {
        this.service = service;
        this.currentProduct = initialProduct;
    }

    public void show() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        frame = new JFrame("Excel QA Tool - Product Validation");
        frame.setSize(900, 680);
        frame.setMinimumSize(new Dimension(750, 500));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing();
            }
        });

        // Main layout
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // North: Product Information Header Card
        JPanel headerCard = createHeaderPanel();
        mainContainer.add(headerCard, BorderLayout.NORTH);

        // Center: Scrollable Checkpoints Panel
        checkpointPanel = new JPanel();
        checkpointPanel.setLayout(new BoxLayout(checkpointPanel, BoxLayout.Y_AXIS));
        checkpointPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(checkpointPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Quality Checkpoints"));
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        // South: Control & Action Buttons Panel
        JPanel southPanel = createBottomControlPanel();
        mainContainer.add(southPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainContainer);

        // Load the initial product data
        if (currentProduct != null) {
            displayProduct(currentProduct);
        }

        frame.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Product Details"),
                BorderFactory.createEmptyBorder(6, 10, 8, 10)
        ));

        skuLabel = new JLabel("SKU: -");
        skuLabel.setFont(skuLabel.getFont().deriveFont(Font.BOLD, 14f));

        typeLabel = new JLabel("Type: -");
        subtypeLabel = new JLabel("Subtype: -");
        descriptionLabel = new JLabel("Description: -");
        rowInfoLabel = new JLabel("Excel Row: -");
        rowInfoLabel.setForeground(new Color(90, 90, 90));

        JPanel row1 = new JPanel(new BorderLayout());
        row1.add(skuLabel, BorderLayout.WEST);
        row1.add(rowInfoLabel, BorderLayout.EAST);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 5));
        row2.add(typeLabel);
        row2.add(subtypeLabel);

        card.add(row1);
        card.add(Box.createVerticalStrut(4));
        card.add(row2);
        card.add(Box.createVerticalStrut(4));
        card.add(descriptionLabel);

        return card;
    }

    private JPanel createBottomControlPanel() {
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Bug Tracking Panel
        JPanel bugPanel = new JPanel(new BorderLayout(10, 0));
        bugPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Defect Tracking"),
                BorderFactory.createEmptyBorder(4, 8, 6, 8)
        ));
        JLabel bugLabel = new JLabel("Bug ID & Description:");
        bugLabel.setFont(bugLabel.getFont().deriveFont(Font.BOLD, 12f));
        bugIdField = new JTextField();
        bugIdField.setPreferredSize(new Dimension(300, 28));
        bugPanel.add(bugLabel, BorderLayout.WEST);
        bugPanel.add(bugIdField, BorderLayout.CENTER);

        // Action Buttons & Status Row
        JPanel actionRow = new JPanel(new BorderLayout(10, 0));
        actionRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(new Color(60, 60, 60));

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        doneButton = new JButton("Done");
        doneButton.setFont(doneButton.getFont().deriveFont(Font.BOLD, 13f));
        doneButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        doneButton.setPreferredSize(new Dimension(110, 34));
        doneButton.addActionListener(e -> onDoneClicked());

        nextButton = new JButton("Next >>");
        nextButton.setFont(nextButton.getFont().deriveFont(Font.BOLD, 13f));
        nextButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextButton.setPreferredSize(new Dimension(110, 34));
        nextButton.setVisible(false); // Hidden until "Done" is clicked
        nextButton.addActionListener(e -> onNextClicked());

        saveAndExitButton = new JButton("Save & Exit");
        saveAndExitButton.setFont(saveAndExitButton.getFont().deriveFont(Font.BOLD, 13f));
        saveAndExitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveAndExitButton.setPreferredSize(new Dimension(120, 34));
        saveAndExitButton.addActionListener(e -> onSaveAndExitClicked());

        backButton = new JButton("<< Back");
        backButton.setFont(backButton.getFont().deriveFont(Font.BOLD, 13f));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setPreferredSize(new Dimension(100, 34));
        backButton.addActionListener(e -> onBackClicked());

        buttonPanel.add(saveAndExitButton);
        buttonPanel.add(backButton);
        buttonPanel.add(doneButton);
        buttonPanel.add(nextButton);

        actionRow.add(statusLabel, BorderLayout.WEST);
        actionRow.add(buttonPanel, BorderLayout.EAST);

        southPanel.add(bugPanel);
        southPanel.add(Box.createVerticalStrut(4));
        southPanel.add(actionRow);

        return southPanel;
    }

    public void displayProduct(QAProduct product) {
        this.currentProduct = product;
        checkpointUIList.clear();

        // Update Header Labels
        skuLabel.setText("SKU: " + (product.getSku() != null ? product.getSku() : "N/A"));
        typeLabel.setText("Type: " + (product.getType() != null ? product.getType() : "-"));
        subtypeLabel.setText("Subtype: " + (product.getSubtype() != null ? product.getSubtype() : "-"));
        descriptionLabel.setText("Description: " + (product.getDescription() != null ? product.getDescription() : "-"));
        rowInfoLabel.setText("Excel Row: " + (product.getRowIndex() + 1));

        // Rebuild Checkpoint List
        checkpointPanel.removeAll();

        // Table Header
        JPanel headerRow = new JPanel(new GridLayout(1, 3, 10, 5));
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel col1 = new JLabel("Checkpoint");
        col1.setFont(col1.getFont().deriveFont(Font.BOLD));
        JLabel col2 = new JLabel("Value / Status");
        col2.setFont(col2.getFont().deriveFont(Font.BOLD));
        JLabel col3 = new JLabel("Comment / Remarks");
        col3.setFont(col3.getFont().deriveFont(Font.BOLD));
        headerRow.add(col1);
        headerRow.add(col2);
        headerRow.add(col3);
        headerRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        checkpointPanel.add(headerRow);
        checkpointPanel.add(Box.createVerticalStrut(6));

        if (product.getCheckpoints() != null) {
            for (Checkpoint checkpoint : product.getCheckpoints()) {
                JPanel row = new JPanel(new GridLayout(1, 3, 10, 5));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

                JLabel nameLabel = new JLabel(checkpoint.getName());
                nameLabel.setToolTipText(checkpoint.getName());

                String[] options = checkpoint.getValues() != null
                        ? checkpoint.getValues().toArray(new String[0])
                        : new String[]{"Ok", "Need to confirm", "NA"};

                JComboBox<String> dropdown = new JComboBox<>(options);
                dropdown.setMaximumRowCount(14);
                if (checkpoint.getSelectedValue() != null && !checkpoint.getSelectedValue().isEmpty()) {
                    String currentVal = checkpoint.getSelectedValue().trim();
                    boolean found = false;
                    for (int idx = 0; idx < dropdown.getItemCount(); idx++) {
                        if (dropdown.getItemAt(idx).equalsIgnoreCase(currentVal)) {
                            dropdown.setSelectedIndex(idx);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        dropdown.addItem(currentVal);
                        dropdown.setSelectedItem(currentVal);
                    }
                }

                JTextField commentField = new JTextField();
                if (checkpoint.getComment() != null) {
                    commentField.setText(checkpoint.getComment());
                }

                row.add(nameLabel);
                row.add(dropdown);
                row.add(commentField);

                checkpointUIList.add(new CheckpointUI(checkpoint, dropdown, commentField));
                checkpointPanel.add(row);
                checkpointPanel.add(Box.createVerticalStrut(4));
            }
        }

        // Populate Bug ID & Description
        if (bugIdField != null) {
            bugIdField.setText(product.getBugIdAndDescription() != null ? product.getBugIdAndDescription() : "");
        }

        // Hide Next button until "Done" is clicked for this new row
        nextButton.setVisible(false);
        if (backButton != null) {
            backButton.setEnabled(service.hasPreviousProduct());
        }
        statusLabel.setText("Viewing SKU: " + product.getSku());

        checkpointPanel.revalidate();
        checkpointPanel.repaint();
        if (buttonPanel != null) {
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }
    }

    private void onDoneClicked() {
        if (currentProduct == null) {
            JOptionPane.showMessageDialog(frame, "No product is currently loaded.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Gather all updated values from the UI
        for (CheckpointUI item : checkpointUIList) {
            String selectedVal = (String) item.dropdown.getSelectedItem();
            String comment = item.commentField.getText();
            item.checkpoint.setSelectedValue(selectedVal);
            item.checkpoint.setComment(comment);
        }

        if (bugIdField != null) {
            currentProduct.setBugIdAndDescription(bugIdField.getText().trim());
        }

        // 2. Save back to Excel
        try {
            service.saveCurrentProduct(currentProduct);

            statusLabel.setText("Saved successfully to sheet for SKU: " + currentProduct.getSku() + " (Row " + (currentProduct.getRowIndex() + 1) + ")");
            JOptionPane.showMessageDialog(frame,
                    "Data for SKU '" + currentProduct.getSku() + "' has been successfully updated and saved to Excel!",
                    "Saved Successfully",
                    JOptionPane.INFORMATION_MESSAGE);

            // 3. Reveal the "Next" button on UI
            nextButton.setVisible(true);
            buttonPanel.revalidate();
            buttonPanel.repaint();

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Error saving to Excel file:\n" + ex.getMessage() +
                    "\n\nIf the Excel file is currently open in Microsoft Excel, please close it and click 'Done' again.",
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Save failed: " + ex.getMessage());
        }
    }

    private void onNextClicked() {
        statusLabel.setText("Loading next SKU...");

        QAProduct nextProduct = service.loadNextProduct();
        if (nextProduct != null) {
            displayProduct(nextProduct);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "No more SKU rows found in the sheet. You have reached the end!",
                    "End of Sheet",
                    JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("All SKU rows completed.");
            nextButton.setVisible(false);
            if (buttonPanel != null) {
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }
        }
    }

    private void onBackClicked() {
        statusLabel.setText("Loading previous SKU...");

        QAProduct prevProduct = service.loadPreviousProduct();
        if (prevProduct != null) {
            displayProduct(prevProduct);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "You are on the first SKU row. Cannot go further back.",
                    "First SKU",
                    JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("First SKU reached.");
        }
    }

    private void handleWindowClosing() {
        String[] options = {"Save & Exit", "Exit without Saving", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                frame,
                "You have an active QA review session.\nWould you like to save your progress before exiting?",
                "Exit Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            onSaveAndExitClicked();
        } else if (choice == 1) {
            service.close();
            frame.dispose();
            System.exit(0);
        }
        // choice == 2 or CLOSED_OPTION: Cancel / keep window open
    }

    private void onSaveAndExitClicked() {
        if (currentProduct != null) {
            for (CheckpointUI item : checkpointUIList) {
                String selectedVal = (String) item.dropdown.getSelectedItem();
                String comment = item.commentField.getText();
                item.checkpoint.setSelectedValue(selectedVal);
                item.checkpoint.setComment(comment);
            }
            if (bugIdField != null) {
                currentProduct.setBugIdAndDescription(bugIdField.getText().trim());
            }
            try {
                service.saveAndClose(currentProduct);
                JOptionPane.showMessageDialog(
                        frame,
                        "Progress saved for SKU: '" + currentProduct.getSku() + "' (Row " + (currentProduct.getRowIndex() + 1) + ").\nGoodbye!",
                        "Progress Saved",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                ex.printStackTrace();
                int proceed = JOptionPane.showConfirmDialog(
                        frame,
                        "Error saving to Excel file:\n" + ex.getMessage() +
                        "\n\nIf the Excel file is open in Microsoft Excel, please close it.\nDo you still want to exit without saving?",
                        "Save Failed",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE
                );
                if (proceed != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        } else {
            service.close();
        }
        frame.dispose();
        System.exit(0);
    }
}