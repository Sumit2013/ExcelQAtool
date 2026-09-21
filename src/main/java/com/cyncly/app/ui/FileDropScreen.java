package com.cyncly.app.ui;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Polished drag-and-drop welcome screen (uses shared {@link Theme} tokens).
 */
public class FileDropScreen {

    private JFrame frame;
    private final Consumer<File> onFileChosen;

    public FileDropScreen(Consumer<File> onFileChosen) {
        this.onFileChosen = onFileChosen;
    }

    /** Build and display the welcome window. */
    public void show() {
        frame = new JFrame("Excel QA Tool – Select File");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(560, 430);
        frame.setMinimumSize(new Dimension(460, 360));
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 14, 0);

        // ── Title ─────────────────────────────────────────────────────────
        JLabel title = new JLabel("Excel QA Tool", SwingConstants.CENTER);
        title.setFont(Theme.bold(26));
        title.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        root.add(title, gbc);

        JLabel subtitle = new JLabel("Drop your QA template to get started", SwingConstants.CENTER);
        subtitle.setFont(Theme.plain(13));
        subtitle.setForeground(Theme.TEXT_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 26, 0);
        root.add(subtitle, gbc);

        // ── Drop Zone ─────────────────────────────────────────────────────
        DropZonePanel dropZone = new DropZonePanel();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 18, 0);
        root.add(dropZone, gbc);

        // ── Divider ───────────────────────────────────────────────────────
        JLabel orLabel = new JLabel("— or —", SwingConstants.CENTER);
        orLabel.setFont(Theme.plain(12));
        orLabel.setForeground(Theme.TEXT_MUTED);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        root.add(orLabel, gbc);

        // ── Browse Button ─────────────────────────────────────────────────
        JButton browseBtn = createBrowseButton();
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 60, 0, 60);
        root.add(browseBtn, gbc);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    // ── Drop Zone panel ───────────────────────────────────────────────────────

    private class DropZonePanel extends JPanel {

        private boolean hovering = false;

        DropZonePanel() {
            setLayout(new GridBagLayout());
            setBackground(Theme.CARD_BG);
            setPreferredSize(new Dimension(480, 180));
            setBorder(new DashedRoundBorder(Theme.BORDER, 3, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Inner content
            JPanel inner = new JPanel();
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setOpaque(false);

            JLabel icon = new JLabel("📂", SwingConstants.CENTER);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel mainText = new JLabel("Drag & Drop Excel File Here");
            mainText.setFont(Theme.bold(15));
            mainText.setForeground(Theme.TEXT_PRIMARY);
            mainText.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subText = new JLabel("Supports .xlsx and .xls files");
            subText.setFont(Theme.plain(12));
            subText.setForeground(Theme.TEXT_MUTED);
            subText.setAlignmentX(Component.CENTER_ALIGNMENT);

            inner.add(icon);
            inner.add(Box.createVerticalStrut(10));
            inner.add(mainText);
            inner.add(Box.createVerticalStrut(4));
            inner.add(subText);
            add(inner);

            // Drag-and-drop
            new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetListener() {

                @Override public void dragEnter(DropTargetDragEvent e) {
                    if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        hovering = true;
                        setBorder(new DashedRoundBorder(Theme.BORDER_FOCUS, 3, 14));
                        setBackground(new Color(30, 45, 70));
                        repaint();
                    }
                }

                @Override public void dragOver(DropTargetDragEvent e) {}
                @Override public void dropActionChanged(DropTargetDragEvent e) {}

                @Override public void dragExit(DropTargetEvent e) {
                    hovering = false;
                    setBorder(new DashedRoundBorder(Theme.BORDER, 3, 14));
                    setBackground(Theme.CARD_BG);
                    repaint();
                }

                @Override
                public void drop(DropTargetDropEvent e) {
                    hovering = false;
                    setBorder(new DashedRoundBorder(Theme.BORDER, 3, 14));
                    setBackground(Theme.CARD_BG);
                    repaint();
                    try {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) e.getTransferable()
                                .getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) {
                            File chosen = files.get(0);
                            if (isExcelFile(chosen)) acceptFile(chosen);
                            else showError("Please drop an Excel file (.xlsx or .xls).");
                        }
                    } catch (Exception ex) {
                        showError("Could not read the dropped file:\n" + ex.getMessage());
                    }
                }
            });

            // Click to browse
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { openFileChooser(); }
                @Override public void mouseEntered(MouseEvent e) {
                    if (!hovering) { setBackground(new Color(30, 37, 60)); repaint(); }
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!hovering) { setBackground(Theme.CARD_BG); repaint(); }
                }
            });
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private JButton createBrowseButton() {
        JButton btn = new JButton("Browse for File…");
        btn.setFont(Theme.bold(13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Theme.ACCENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 40));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(Theme.ACCENT_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(Theme.ACCENT); }
        });
        btn.addActionListener(e -> openFileChooser());
        return btn;
    }

    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select QA Excel File");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            if (isExcelFile(chosen)) acceptFile(chosen);
            else showError("Selected file is not an Excel file (.xlsx or .xls).");
        }
    }

    private void acceptFile(File file) {
        frame.dispose();
        onFileChosen.accept(file);
    }

    private static boolean isExcelFile(File f) {
        if (f == null) return false;
        String n = f.getName().toLowerCase();
        return n.endsWith(".xlsx") || n.endsWith(".xls");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Invalid File", JOptionPane.ERROR_MESSAGE);
    }

    // ── Dashed rounded border ─────────────────────────────────────────────────

    private static class DashedRoundBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int arc;
        private final float[] dash = {8f, 5f};

        DashedRoundBorder(Color color, int thickness, int arc) {
            this.color = color;
            this.thickness = thickness;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            float half = thickness / 2f;
            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 10f, dash, 0f));
            g2.drawRoundRect((int)(x + half), (int)(y + half),
                    (int)(w - thickness), (int)(h - thickness), arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 6, thickness + 6, thickness + 6, thickness + 6);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(thickness + 6, thickness + 6, thickness + 6, thickness + 6);
            return insets;
        }
    }
}
