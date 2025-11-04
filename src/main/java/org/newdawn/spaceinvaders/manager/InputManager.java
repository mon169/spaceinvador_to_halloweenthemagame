package org.newdawn.spaceinvaders.manager;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.newdawn.spaceinvaders.Game;

/**
 * 🎮 InputManager — 입력 처리
 *  - 이동/공격/ESC 및 상점/시작 화면 입력 (숫자/R/ESC) 모두 처리
 */
public class InputManager extends KeyAdapter {
    private final Game game;

    public InputManager(Game game) {
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (game.isWaitingForKeyPress()) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:  game.setLeftPressed(true);  break;
            case KeyEvent.VK_RIGHT: game.setRightPressed(true); break;
            case KeyEvent.VK_SPACE: game.setFirePressed(true);  break;

            case KeyEvent.VK_B:
                if (game.getShip() != null && game.getShip().hasBomb()) game.getShip().useBomb();
                break;
            case KeyEvent.VK_I:
                if (game.getShip() != null && game.getShip().hasIceWeapon()) game.getShip().useIceWeapon();
                break;
            case KeyEvent.VK_S:
                if (game.getShip() != null) {
                    // S키: 요새 방어막 활성화
                    game.getShip().activateShield();
                }
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
            case KeyEvent.VK_LEFT:  game.setLeftPressed(false);  break;
            case KeyEvent.VK_RIGHT: game.setRightPressed(false); break;
            case KeyEvent.VK_SPACE: game.setFirePressed(false);  break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();

        // ESC -> 종료
        if (c == 27) { game.endGame(); return; }

        if (game.isWaitingForKeyPress()) {
            // 상점 열림 상태면 상점 입력
            if (game.isShopOpenFlag()) {
                game.handleShopKey(c);
                return;
            }

            // 메시지 상태 또는 첫 시작 → 아무 키나 시작
            // R키는 재시작/다음 스테이지에도 사용되지만,
            // 여기서는 “시작”으로 처리
            game.setWaitingForKeyPress(false);
            game.startGameOrNextStage(false);
        }
    }
}
