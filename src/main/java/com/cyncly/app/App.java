package com.cyncly.app;

import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.cyncly.app.model.QAProduct;
import com.cyncly.app.service.QAservice;
import com.cyncly.app.service.QAservice.ProgressRecord;
import com.cyncly.app.ui.QAWindow;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        String filePath = "D:\\ExcelQAtool\\my-app\\src\\QA Template.xlsx";
        File localFile = new File("src/QA Template.xlsx");
        if (localFile.exists()) {
            filePath = localFile.getAbsolutePath();
        }

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

        SwingUtilities.invokeLater(() -> {
            QAWindow window = new QAWindow(service, product);
            window.show();
        });
    }
}
