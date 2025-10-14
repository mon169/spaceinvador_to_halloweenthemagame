package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 🎬 StartScreen — 게임 시작 전 UI
 * - 배경 이미지 + 시작/설정 버튼
 * - Game.java 실행과 연결
 */
public class StartScreen extends JFrame {
    private Image backgroundImage;
    private JButton startButton;
    private JButton settingsButton;

    public StartScreen() {
        setTitle("🎃 Halloween Space Invaders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);

        // ✅ 배경 이미지 로드 (없으면 기본 배경)
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/bg/start_background.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("⚠️ start_background.jpg 로드 실패");
            backgroundImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        }

        // ✅ 버튼 이미지 로드
        ImageIcon startIcon = loadIcon("/sprites/startbutton.png", "시작 버튼");
        ImageIcon settingsIcon = loadIcon("/sprites/settingsbutton.png", "설정 버튼");

        startButton = new JButton(startIcon);
        settingsButton = new JButton(settingsIcon);

        for (JButton btn : new JButton[]{startButton, settingsButton}) {
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
        }

        // 버튼 위치 중앙 정렬
        int buttonWidth = 250;
        int buttonHeight = 80;
        int centerX = (800 - buttonWidth) / 2;
        startButton.setBounds(centerX, 360, buttonWidth, buttonHeight);
        settingsButton.setBounds(centerX, 460, buttonWidth, buttonHeight);

        // ✅ 시작 버튼 동작
        startButton.addActionListener(e -> {
            dispose(); // 현재 창 닫기
            SwingUtilities.invokeLater(() -> {
                Game game = new Game();
                new Thread(game::gameLoop).start();
            });
        });

        // ✅ 설정 버튼 동작
        settingsButton.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "⚙️ 환경설정은 준비 중입니다!", "Info", JOptionPane.INFORMATION_MESSAGE)
        );

        // ✅ 배경 패널
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

        // 타이틀 텍스트 추가
        JLabel title = new JLabel("🎃 HALLOWEEN DEFENSE 🎃", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        title.setForeground(Color.ORANGE);
        title.setBounds(0, 180, 800, 80);
        bgPanel.add(title);
    }

    private ImageIcon loadIcon(String path, String name) {
        try {
            return new ImageIcon(getClass().getResource(path));
        } catch (Exception e) {
            System.err.println("⚠️ " + name + " 이미지 로드 실패: " + path);
            return new ImageIcon();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartScreen screen = new StartScreen();
            screen.setVisible(true);
        });
    }
}
