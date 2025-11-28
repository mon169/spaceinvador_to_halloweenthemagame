package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;
import org.newdawn.spaceinvaders.sound.SoundManager;

/**
 * 게임 설정 다이얼로그
 * - StartScreen에서만 호출
 * - 로비 BGM on/off
 * - 밝기 조절
 */
public class SettingsDialog extends JDialog {
    
    private JCheckBox bgmCheckBox;
    private JSlider brightnessSlider;
    private JLabel brightnessLabel;
    private StartScreen parentScreen;
    
    /**
     * 설정 다이얼로그 생성
     * @param parent 부모 StartScreen (밝기 적용을 위해 필요)
     */
    public SettingsDialog(StartScreen parent) {
        super(parent, "⚙️ 환경설정", true); // 모달 다이얼로그
        this.parentScreen = parent;
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(null);
        
        initComponents();
    }
    
    /**
     * UI 컴포넌트 초기화
     */
    private void initComponents() {
        // === 1. 로비 BGM 설정 ===
        JLabel bgmLabel = new JLabel("🔊 로비 배경음악:");
        bgmLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        bgmLabel.setBounds(30, 30, 150, 30);
        add(bgmLabel);
        
        bgmCheckBox = new JCheckBox("활성화");
        bgmCheckBox.setSelected(GameSettings.isBgmEnabled());
        bgmCheckBox.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bgmCheckBox.setBounds(180, 30, 150, 30);
        bgmCheckBox.addActionListener(e -> toggleBgm());
        add(bgmCheckBox);
        
        // === 2. 밝기 조절 ===
        JLabel brightLabel = new JLabel("💡 화면 밝기:");
        brightLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        brightLabel.setBounds(30, 90, 150, 30);
        add(brightLabel);
        
        brightnessSlider = new JSlider(0, 100, GameSettings.getBrightness());
        brightnessSlider.setBounds(30, 130, 340, 40);
        brightnessSlider.setMajorTickSpacing(25);
        brightnessSlider.setMinorTickSpacing(5);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);
        brightnessSlider.addChangeListener(e -> updateBrightness());
        add(brightnessSlider);
        
        brightnessLabel = new JLabel("현재: " + GameSettings.getBrightness() + "%");
        brightnessLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        brightnessLabel.setBounds(30, 170, 200, 30);
        add(brightnessLabel);
        
        // === 3. 확인 버튼 ===
        JButton okButton = new JButton("✅ 확인");
        okButton.setBounds(150, 220, 100, 35);
        okButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        okButton.addActionListener(e -> {
            SoundManager.playClick();
            dispose();
        });
        add(okButton);
    }
    
    /**
     * BGM on/off 토글
     */
    private void toggleBgm() {
        SoundManager.playClick();
        GameSettings.setBgmEnabled(bgmCheckBox.isSelected());
    }
    
    /**
     * 밝기 조절 (실시간 반영)
     */
    private void updateBrightness() {
        int brightness = brightnessSlider.getValue();
        GameSettings.setBrightness(brightness);
        brightnessLabel.setText("현재: " + brightness + "%");
        
        // StartScreen에 밝기 적용 (repaint 트리거)
        if (parentScreen != null) {
            parentScreen.applyBrightness(brightness);
        }
    }
}





