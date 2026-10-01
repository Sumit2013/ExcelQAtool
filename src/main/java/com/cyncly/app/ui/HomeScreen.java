package com.cyncly.app.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Business-oriented visual home screen. Only Edit QA Sheet is active for now. */
public class HomeScreen {

    private final Consumer<File> onFileChosen;
    private JFrame frame;
    private JPanel contentHost;

    public HomeScreen(Consumer<File> onFileChosen) {
        this.onFileChosen = onFileChosen;
    }

    public void show() {
        frame = new JFrame("Excel QA Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1080, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SEA_SHELL);
        root.add(createSidebar(), BorderLayout.WEST);

        contentHost = new JPanel(new BorderLayout());
        contentHost.setBackground(Theme.SEA_SHELL);
        contentHost.add(createContent(), BorderLayout.CENTER);
        root.add(contentHost, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(Theme.PURPLE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 18, 24, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("Excel QA Tool");
        brand.setFont(Theme.bold(21));
        brand.setForeground(Color.WHITE);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 12, 3, 0));
        sidebar.add(brand);

        JLabel caption = new JLabel("Product validation workspace");
        caption.setFont(Theme.plain(11));
        caption.setForeground(new Color(232, 228, 255));
        caption.setBorder(BorderFactory.createEmptyBorder(0, 12, 30, 0));
        sidebar.add(caption);

        sidebar.add(menuButton("Home", true, null));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(menuButton("Edit QA Sheet", true, e -> {
            showFileSelection();
        }));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(menuButton("Raise Bug  ·  Coming soon", false, null));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(menuButton("Dashboard  ·  Coming soon", false, null));

        sidebar.add(Box.createVerticalGlue());
        JLabel version = new JLabel("QA workspace  •  UI preview");
        version.setFont(Theme.plain(11));
        version.setForeground(new Color(220, 215, 250));
        version.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        sidebar.add(version);
        return sidebar;
    }

    private void showFileSelection() {
        FileDropScreen fileSelector = new FileDropScreen(
                file -> {
                    frame.dispose();
                    onFileChosen.accept(file);
                },
                this::showHomeContent);

        contentHost.removeAll();
        contentHost.add(fileSelector.createPanel(), BorderLayout.CENTER);
        contentHost.revalidate();
        contentHost.repaint();
    }

    private void showHomeContent() {
        contentHost.removeAll();
        contentHost.add(createContent(), BorderLayout.CENTER);
        contentHost.revalidate();
        contentHost.repaint();
    }

    private JButton menuButton(String text, boolean active, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(active ? Theme.bold(13) : Theme.plain(13));
        button.setForeground(active ? Color.WHITE : new Color(218, 213, 245));
        button.setBackground(active ? Theme.PURPLE_DIM : Theme.PURPLE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setEnabled(true);
        button.setCursor(active ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        if (action != null) {
            button.addActionListener(action);
            button.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { button.setBackground(Theme.PURPLE_HV); }
                @Override public void mouseExited(MouseEvent e) { button.setBackground(Theme.PURPLE_DIM); }
            });
        }
        return button;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setBackground(Theme.SEA_SHELL);
        content.setBorder(BorderFactory.createEmptyBorder(42, 48, 42, 48));

        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Welcome to Excel QA Tool");
        title.setFont(Theme.bold(30));
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Choose a workspace to get started");
        subtitle.setFont(Theme.plain(15));
        subtitle.setForeground(Theme.TEXT_MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);
        main.add(heading);
        main.add(Box.createVerticalStrut(28));

        JPanel cards = new JPanel(new GridLayout(1, 2, 18, 0));
        cards.setOpaque(false);
        cards.add(createFeatureCard("Edit QA Sheet", "Open an Excel QA template and review products and checkpoints.", true));
        cards.add(createFeatureCard("Raise Bug / Dashboard", "These business modules are planned and will be enabled in a later phase.", false));
        main.add(cards);
        content.add(main, BorderLayout.CENTER);

        JPanel note = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        note.setOpaque(false);
        JLabel noteText = new JLabel("Current active module: Edit QA Sheet");
        noteText.setFont(Theme.bold(13));
        noteText.setForeground(Theme.PURPLE_DIM);
        note.add(noteText);
        content.add(note, BorderLayout.SOUTH);
        return content;
    }

    private JPanel createFeatureCard(String titleText, String description, boolean active) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(titleText);
        title.setFont(Theme.bold(18));
        title.setForeground(active ? Theme.PURPLE_DIM : Theme.TEXT_MUTED);
        JLabel body = new JLabel("<html><div style='width:260px'>" + description + "</div></html>");
        body.setFont(Theme.plain(13));
        body.setForeground(Theme.TEXT_MUTED);
        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(body);
        return card;
    }
}