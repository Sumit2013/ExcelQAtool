package com.cyncly.app.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import com.cyncly.app.model.Checkpoint;
import com.cyncly.app.model.QAProduct;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class QAWindow {

    private QAProduct product;

    public QAWindow(QAProduct product) {
        this.product = product;
    }

    public void show() {

        JFrame frame = new JFrame("Excel QA Tool");

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

        JLabel skuLabel = new JLabel("SKU :" + product.getSku());
        JLabel typeLabel = new JLabel("Type: " + product.getType());
        JLabel subtypeLabel = new JLabel("Subtype: " + product.getSubtype());
        JLabel descriptionLabel = new JLabel("Description: " + product.getDescription());

        panel.add(skuLabel);
        panel.add(typeLabel);
        panel.add(subtypeLabel);
        panel.add(descriptionLabel);

        frame.add(panel, BorderLayout.NORTH);

        JPanel checkpointPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        checkpointPanel.add(new JLabel("Checkpoint"));
        checkpointPanel.add(new JLabel("Value"));
        checkpointPanel.add(new JLabel("Comment"));

        for (Checkpoint checkpoint : product.getCheckpoints()) {
            JLabel checkpointLabel = new JLabel(checkpoint.getName());

            String[] options = checkpoint.getValues().toArray(new String[0]);
            JComboBox<String> dropdown = new JComboBox<>(options);
            JTextField commentField = new JTextField();

            checkpointPanel.add(checkpointLabel);
            checkpointPanel.add(dropdown);
            checkpointPanel.add(commentField);
        }
        JScrollPane scrollPane = new JScrollPane(checkpointPanel);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}