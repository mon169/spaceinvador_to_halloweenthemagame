package org.newdawn.spaceinvaders.Stage;

/** 🧩 Stage 인터페이스 */
public interface Stage {
    void init();     // 진입 1회
    void update();   // 매 프레임
    int id();        // 1~5
}
