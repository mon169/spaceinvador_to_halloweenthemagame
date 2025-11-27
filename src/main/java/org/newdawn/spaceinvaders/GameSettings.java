package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.sound.SoundManager;

/**
 * 게임 전역 설정을 관리하는 유틸리티 클래스
 * - 로비 BGM on/off
 * - 화면 밝기 (0~100)
 * - 효과음 볼륨 (0.0~1.0)
 */
public class GameSettings {
    
    // 전역 설정값 (메모리 저장)
    private static boolean bgmEnabled = true;
    private static int brightness = 100; // 0~100 (100 = 원본)
    private static float soundVolume = 1.0f; // 0.0~1.0 (1.0 = 최대)
    
    // 외부에서 인스턴스화 방지
    private GameSettings() {}
    
    // === BGM 설정 ===
    
    /**
     * 로비 BGM 활성화 여부 조회
     * @return true면 BGM 활성화
     */
    public static boolean isBgmEnabled() {
        return bgmEnabled;
    }
    
    /**
     * 로비 BGM on/off 설정
     * @param enabled true면 BGM 재생, false면 중지
     */
    public static void setBgmEnabled(boolean enabled) {
        bgmEnabled = enabled;
        
        // 즉시 반영
        if (enabled) {
            SoundManager.playStartBgmLoop();
        } else {
            SoundManager.stopStartBgm();
        }
    }
    
    // === 밝기 설정 ===
    
    /**
     * 화면 밝기 조회
     * @return 0~100 (100 = 원본)
     */
    public static int getBrightness() {
        return brightness;
    }
    
    /**
     * 화면 밝기 설정
     * @param value 0~100 (0 = 최소, 100 = 원본)
     */
    public static void setBrightness(int value) {
        brightness = Math.max(0, Math.min(100, value));
    }
    
    /**
     * 밝기를 0.0~1.0 비율로 반환 (Graphics2D 오버레이용)
     * @return 0.0~1.0
     */
    public static float getBrightnessRatio() {
        return brightness / 100.0f;
    }
    
    // === 효과음 볼륨 설정 (향후 확장용) ===
    
    /**
     * 효과음 볼륨 조회
     * @return 0.0~1.0 (1.0 = 최대)
     */
    public static float getSoundVolume() {
        return soundVolume;
    }
    
    /**
     * 효과음 볼륨 설정
     * @param volume 0.0~1.0 (0.0 = 음소거, 1.0 = 최대)
     */
    public static void setSoundVolume(float volume) {
        soundVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * 모든 설정 초기화
     */
    public static void resetToDefaults() {
        bgmEnabled = true;
        brightness = 100;
        soundVolume = 1.0f;
    }
}




