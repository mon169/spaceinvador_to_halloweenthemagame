package org.newdawn.spaceinvaders.manager;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.UserEntity;

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
        // S키, B키, I키는 waitingForKeyPress 상태와 관계없이 처리
        // 이 키들은 keyTyped에서 게임 재시작을 트리거하지 않도록 여기서 이벤트 소비
        switch (e.getKeyCode()) {
            case KeyEvent.VK_S:
                e.consume(); // keyTyped 호출 방지
                UserEntity currentShip = game.getShip();
                if (currentShip != null) {
                    int shieldCount = currentShip.getShieldCount();
                    boolean hasShield = currentShip.hasShield();
                    System.out.println("🛡 S키 입력 감지 - waitingForKeyPress=" + game.isWaitingForKeyPress() + ", ship=" + (currentShip != null) + ", shieldCount=" + shieldCount + ", hasShield=" + hasShield);
                } else {
                    System.out.println("🛡 S키 입력 감지 - ship=null");
                }
                
                if (!game.isWaitingForKeyPress() && currentShip != null && currentShip.hasShield()) {
                    System.out.println("🛡 방어막 활성화!");
                    currentShip.activateShield();
                } else {
                    if (game.isWaitingForKeyPress()) {
                        System.out.println("⚠️ S키: 게임이 중지 상태입니다 (waitingForKeyPress=true)");
                    } else if (currentShip == null) {
                        System.out.println("⚠️ S키: ship이 null입니다");
                    } else {
                        System.out.println("⚠️ S키: 방어막을 보유하고 있지 않습니다. (shieldCount=" + currentShip.getShieldCount() + ", hasShield=" + currentShip.hasShield() + ") 상점에서 구매하세요.");
                    }
                }
                return; // S키는 여기서 처리 완료, 아래 로직 실행 방지
            
            case KeyEvent.VK_B:
                e.consume(); // keyTyped 호출 방지
                System.out.println("💣 B키 입력 감지 - waitingForKeyPress=" + game.isWaitingForKeyPress() + ", ship=" + (game.getShip() != null) + ", hasBomb=" + (game.getShip() != null && game.getShip().hasBomb()));
                if (!game.isWaitingForKeyPress() && game.getShip() != null && game.getShip().hasBomb()) {
                    System.out.println("💣 폭탄 사용!");
                    game.getShip().useBomb();
                } else {
                    if (game.isWaitingForKeyPress()) {
                        System.out.println("⚠️ B키: 게임이 중지 상태입니다 (waitingForKeyPress=true)");
                    } else if (game.getShip() == null) {
                        System.out.println("⚠️ B키: ship이 null입니다");
                    } else if (!game.getShip().hasBomb()) {
                        System.out.println("⚠️ B키: 폭탄을 보유하고 있지 않습니다");
                    }
                }
                return;
            
            case KeyEvent.VK_I:
                e.consume(); // keyTyped 호출 방지
                System.out.println("🧊 I키 입력 감지 - waitingForKeyPress=" + game.isWaitingForKeyPress() + ", ship=" + (game.getShip() != null) + ", hasIceWeapon=" + (game.getShip() != null && game.getShip().hasIceWeapon()));
                if (!game.isWaitingForKeyPress() && game.getShip() != null && game.getShip().hasIceWeapon()) {
                    System.out.println("🧊 아이스 무기 사용!");
                    game.getShip().useIceWeapon();
                } else {
                    if (game.isWaitingForKeyPress()) {
                        System.out.println("⚠️ I키: 게임이 중지 상태입니다 (waitingForKeyPress=true)");
                    } else if (game.getShip() == null) {
                        System.out.println("⚠️ I키: ship이 null입니다");
                    } else if (!game.getShip().hasIceWeapon()) {
                        System.out.println("⚠️ I키: 아이스 무기를 보유하고 있지 않습니다");
                    }
                }
                return;
        }

        // 나머지 키는 waitingForKeyPress 상태일 때 무시
        if (game.isWaitingForKeyPress()) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:  game.setLeftPressed(true);  break;
            case KeyEvent.VK_RIGHT: game.setRightPressed(true); break;
            case KeyEvent.VK_SPACE: game.setFirePressed(true);  break;
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
        // keyPressed에서 consume된 이벤트는 keyTyped가 호출되지 않지만,
        // 혹시 모를 경우를 대비해 이중 방어
        char c = e.getKeyChar();

        // ESC -> 종료 (항상 작동)
        if (c == 27) { game.endGame(); return; }

        // S키, B키, I키는 keyPressed에서 이미 처리되고 consume되었으므로
        // keyTyped에서는 절대 실행되지 않아야 하지만, 혹시 모를 경우를 대비해 무시
        // 이 키들은 게임 재시작을 트리거하지 않도록 반드시 무시해야 함
        if (c == 's' || c == 'S' || c == 'b' || c == 'B' || c == 'i' || c == 'I') {
            e.consume(); // 이벤트 소비하여 다른 핸들러로 전파 방지
            System.out.println("🚫 keyTyped에서 " + c + " 키 무시 (keyPressed에서 이미 처리됨, 이 호출은 예상치 못한 경우)");
            return; // 반드시 return하여 아래 로직 실행 방지
        }

        // ⚠️ 중요: 게임 진행 중(waitingForKeyPress == false)에는 
        // 스테이지 초기화나 다음 스테이지로 이동하는 키 입력을 완전히 차단
        // 오직 아이템 키(S, B, I)만 작동해야 함
        if (!game.isWaitingForKeyPress()) {
            // 게임 진행 중에는 keyTyped에서 아무것도 하지 않음
            // (아이템 키는 이미 위에서 무시됨)
            return;
        }

        // ✅ 게임 중지 상태(waitingForKeyPress == true)일 때만 아래 로직 실행
        // 상점 열림 상태면 상점 입력
        if (game.isShopOpenFlag()) {
            System.out.println("🛒 상점 입력 처리: " + c + " (shopOpen=true, waitingForKeyPress=true)");
            game.handleShopKey(c);
            return;
        }

        // 게임 시작 대기 상태 → 아무 키나 눌러서 게임 시작
        // (R키는 상점에서 다음 스테이지로 이동하는 용도로도 사용됨)
        System.out.println("🎮 keyTyped에서 게임 시작 트리거: " + c + " (waitingForKeyPress=true)");
        game.setWaitingForKeyPress(false);
        game.startGameOrNextStage(1);
    }
}
