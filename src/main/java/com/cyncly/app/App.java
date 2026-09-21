package com.cyncly.app;

import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.cyncly.app.model.QAProduct;
import com.cyncly.app.service.QAservice;
import com.cyncly.app.service.QAservice.ProgressRecord;
import com.cyncly.app.ui.FileDropScreen;
import com.cyncly.app.ui.QAWindow;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            FileDropScreen dropScreen = new FileDropScreen(App::startSession);
            dropScreen.show();
        });
    }

    /**
     * Called on the EDT after the user has chosen a file from the drop screen.
     * Handles the resume-session dialog, then opens the main QAWindow.
     */
    private static void startSession(File excelFile) {
        try {
            String filePath = excelFile.getAbsolutePath();
            QAservice service = new QAservice();
            int initialRow = 2;

            ProgressRecord savedProgress = service.loadProgressRecord();
            if (savedProgress != null && savedProgress.lastRowIndex >= 2) {
                String skuInfo = (savedProgress.sku != null && !savedProgress.sku.isEmpty())
                        ? " (SKU: " + savedProgress.sku + ")"
                        : "";
                String[] options = {"Resume", "Start from Beginning"};
                int choice = JOptionPane.showOptionDialog(
                        null,
                        "Previous session detected at Excel Row " + (savedProgress.lastRowIndex + 1) + skuInfo + ".\n"
                        + "Would you like to resume your review session from where you left off?",
                        "Resume Session",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );
                if (choice == 0) {
                    initialRow = savedProgress.lastRowIndex;
                }
            }

            QAProduct product = service.startSession(filePath, initialRow);
            QAWindow window = new QAWindow(service, product);
            window.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to open the selected Excel file:\n" + ex.getMessage()
                    + "\n\nPlease make sure the file contains a 'Validation' sheet.",
                    "Error Loading File",
                    JOptionPane.ERROR_MESSAGE
            );
            // Re-open the drop screen so the user can try another file
            SwingUtilities.invokeLater(() -> new FileDropScreen(App::startSession).show());
        }
    }
}

