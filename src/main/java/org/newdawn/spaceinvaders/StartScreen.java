package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JFrame {
    private Image backgroundImage;
    private JButton startButton;
    private JButton settingsButton;

    public StartScreen() {
        setTitle("Space Invaders - 시작화면");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);

        backgroundImage = new ImageIcon(getClass().getResource("/bg/start_background.jpg")).getImage();

        ImageIcon startIcon = new ImageIcon(getClass().getResource("/sprites/startbutton.png"));
        ImageIcon settingsIcon = new ImageIcon(getClass().getResource("/sprites/settingsbutton.png"));

        startButton = new JButton(startIcon);
        settingsButton = new JButton(settingsIcon);

        for (JButton btn : new JButton[]{startButton, settingsButton}) {
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
        }

        int buttonWidth = 250;
        int buttonHeight = 80;
        int centerX = (800 - buttonWidth) / 2;
        startButton.setBounds(centerX, 350, buttonWidth, buttonHeight);
        settingsButton.setBounds(centerX, 450, buttonWidth, buttonHeight);

        startButton.addActionListener(e -> {
            dispose();
            // 게임 화면으로 전환
            SwingUtilities.invokeLater(() -> {
                Game game = new Game();
                new Thread(game::gameLoop).start();
            });
        });

        settingsButton.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "환경설정은 준비 중입니다!", "Info", JOptionPane.INFORMATION_MESSAGE)
        );

        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setBounds(0, 0, 800, 600);
        bgPanel.setLayout(null);
        setContentPane(bgPanel);
        bgPanel.add(startButton);
        bgPanel.add(settingsButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartScreen screen = new StartScreen();
            screen.setVisible(true);
        });
    }
}
