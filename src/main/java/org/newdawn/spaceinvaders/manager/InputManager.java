package org.newdawn.spaceinvaders.manager;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.newdawn.spaceinvaders.Game;

/**
 * 🎮 InputManager — 입력 감지 및 Game 클래스로의 이벤트 전달 (책임 분리)
 * - 순수하게 키보드 입력 상태만 Game 클래스에 보고하는 역할로 축소됨.
 */
public class InputManager extends KeyAdapter {
    private final Game game;

    public InputManager(Game game) {
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // 대기 상태에서는 이동/공격 키 입력 무시
        if (game.isWaitingForKeyPress()) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: 
                game.setLeftPressed(true); 
                break;
            case KeyEvent.VK_RIGHT: 
                game.setRightPressed(true); 
                break;
            case KeyEvent.VK_SPACE: 
                game.setFirePressed(true); 
                break;

            case KeyEvent.VK_A:
                // 아이템 사용 로직 Game 클래스로 위임
                game.useBombWeapon(); 
                break;
            case KeyEvent.VK_E:
                game.useIceWeapon();
                break;
            case KeyEvent.VK_S:
                game.activateShield();
                break;

            case KeyEvent.VK_ESCAPE:
                game.endGame(); 
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (game.isWaitingForKeyPress()) return;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: 
                game.setLeftPressed(false); 
                break;
            case KeyEvent.VK_RIGHT: 
                game.setRightPressed(false); 
                break;
            case KeyEvent.VK_SPACE: 
                game.setFirePressed(false); 
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();

        // ESC → 즉시 종료
        if (c == 27) { game.endGame(); return; }

        // 🔹 대기 상태에서의 키 입력 처리는 Game 클래스로 위임
        if (game.isWaitingForKeyPress()) {
            game.handleWaitingKeyInput(c);
        }
    }
}