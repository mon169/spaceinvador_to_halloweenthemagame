package org.newdawn.spaceinvaders.manager;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.newdawn.spaceinvaders.Game;

/**
 * 🎮 InputManager — 입력 처리
 *  - 이동/공격/ESC 및 상점·시작·재시작 입력(R키 등) 처리 완전판
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

                case KeyEvent.VK_A:
                    if (game.getShip() != null && game.getShip().hasBomb())
                        game.getShip().useBomb();
                    break;
                case KeyEvent.VK_E:
                    if (game.getShip() != null && game.getShip().hasIceWeapon())
                        game.getShip().useIceWeapon();
                    break;
            case KeyEvent.VK_S:
                if (game.getShip() != null && game.getShip().hasShield())
                    game.getShip().activateShield();
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

        // ESC → 즉시 종료
        if (c == 27) { game.endGame(); return; }

        // 🔹 대기 상태에서의 키 입력 처리
        if (game.isWaitingForKeyPress()) {

            // ✅ 상점 열림 상태 → 상점 입력 처리
            if (game.isShopOpenFlag()) {
                game.handleShopKey(c);
                return;
            }

            // ✅ 사망/요새 파괴 후 R키 → 현재 스테이지 재시작
            if (c == 'r' || c == 'R') {
                System.out.println("🔁 R키 입력 — 현재 스테이지 재도전 실행");
                game.restartCurrentStage();
                return;
            }

            // ✅ 그 외 아무 키 → 새 게임 시작 (Stage1부터)
            game.setWaitingForKeyPress(false);
            System.out.println("▶ 새 게임 시작 (Stage1)");
            game.startGameOrNextStage(1);
        }
    }
}
