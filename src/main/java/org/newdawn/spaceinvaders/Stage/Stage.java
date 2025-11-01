package org.newdawn.spaceinvaders.Stage;

/** 🧩 Stage 인터페이스 */
public interface Stage {
    void init();     // 진입 1회
    void update();   // 매 프레임
    int id();        // 1~5

    /** ✅ 각 스테이지 리셋 시 내부 상태 초기화 */
    default void resetStageFlags() {}
}