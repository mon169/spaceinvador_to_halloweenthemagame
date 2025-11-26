package org.newdawn.spaceinvaders.sound;

/**
 * 🎵 SoundManager — 게임 전역 사운드 제어
 *  - 로비(start_bgm), 인게임(game_bgm) 배경음
 *  - 버튼 클릭 효과음(click)
 */
public class SoundManager {
    private static final String BASE = "/sounds/";

    private static final SoundEffect START_BGM;
    private static final SoundEffect GAME_BGM;
    private static final SoundEffect CLICK;
    
    static {
        try {
            START_BGM = new SoundEffect(BASE + "start_bgm.wav");
            GAME_BGM = new SoundEffect(BASE + "game_bgm.wav");
            CLICK = new SoundEffect(BASE + "click.wav");
        } catch (Exception e) {
            System.err.println("❌ SoundManager 초기화 실패");
            e.printStackTrace();
            throw new RuntimeException("SoundManager 초기화 실패", e);
        }
    }

    private SoundManager() {}

    public static void playStartBgmLoop() {
        if (START_BGM != null) {
            START_BGM.loop();
        }
    }

    public static void stopStartBgm() {
        if (START_BGM != null) {
            START_BGM.stop();
        }
    }

    public static void playGameBgmLoop() {
        stopStartBgm(); // start_bgm 정지 후 game_bgm 재생
        if (GAME_BGM != null) {
            GAME_BGM.loop();
        }
    }

    public static void stopGameBgm() {
        if (GAME_BGM != null) {
            GAME_BGM.stop();
        }
    }

    public static void playClick() {
        if (CLICK != null) {
            CLICK.playOnce();
        }
    }
}

