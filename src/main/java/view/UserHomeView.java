package view;

import interface_adapter.ViewNetworth.UserHomeViewModel;
import interface_adapter.ViewNetworth.UserHomeViewModelListener;
import interface_adapter.ViewNetworth.ViewNetworthController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class UserHomeView extends JPanel implements UserHomeViewModelListener {
    private final JLabel usernameLabel = new JLabel();
    private final JLabel networthLabel = new JLabel();
    private final JButton viewNetworthButton = new JButton("View Net Worth History");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private ViewNetworthController viewNetworthController;

    public UserHomeView(UserHomeViewModel userHomeViewModel) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(700, 450));
        setBackground(Color.WHITE);

        userHomeViewModel.addListener(this);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(13, 71, 161));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(usernameLabel);

        // 2. Middle panel: net worth, centered
        JLabel captionLabel = new JLabel("Current Net Worth");
        captionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        captionLabel.setForeground(Color.GRAY);
        captionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        networthLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        networthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Box networthBox = Box.createVerticalBox();
        networthBox.add(captionLabel);
        networthBox.add(Box.createVerticalStrut(8));
        networthBox.add(networthLabel);

        JPanel middlePanel = new JPanel(new GridBagLayout()); // GridBagLayout centers its child
        middlePanel.setBackground(Color.WHITE);
        middlePanel.add(networthBox);

        // 3. Bottom panel: button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        viewNetworthButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        viewNetworthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                viewNetworthController.execute("Fake UserId");
                // get the actual one once Log-in is done
            }
        });

        bottomPanel.add(viewNetworthButton);

        add(topPanel, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setController(ViewNetworthController viewNetworthController) {
        this.viewNetworthController = viewNetworthController;
    }
}