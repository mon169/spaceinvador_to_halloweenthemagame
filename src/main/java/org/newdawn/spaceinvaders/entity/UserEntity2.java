package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

/**
 * 🎮 UserEntity2 - 2P(두 번째 플레이어) 캐릭터
 * 기존 UserEntity 로직을 그대로 상속받으며,
 * 스프라이트 / 시작위치 / 색상만 다르게 적용.
 * 
 * ⚙️ 특징:
 * - Player1(UserEntity)와 동등한 기능 (공격, 체력, 이동)
 * - Game.java의 onPacketReceived() 로 실시간 위치 동기화
 * - 직접 조작하지 않고 네트워크 입력으로만 움직임
 */
public class UserEntity2 extends UserEntity {

    private final String spriteRight2 = "sprites/user2r.png";
    private final String spriteLeft2  = "sprites/user2l.png";
    private boolean movingRight = true;

    public UserEntity2(Game game, String ref, int x, int y) {
        super(game, ref, x, y);

        // 기본 스프라이트를 2P 전용 이미지로 교체
        this.sprite = SpriteStore.get().getSprite(spriteRight2);
    }

    // =====================================================
    // 🔹 좌우 이동에 따라 스프라이트 변경 (2P용)
    // =====================================================
    @Override
    public void setHorizontalMovement(double speed) {
        super.setHorizontalMovement(speed);

        if (speed > 0 && !movingRight) {
            movingRight = true;
            this.sprite = SpriteStore.get().getSprite(spriteRight2);
        } else if (speed < 0 && movingRight) {
            movingRight = false;
            this.sprite = SpriteStore.get().getSprite(spriteLeft2);
        }
    }

    // =====================================================
    // 🔹 2P 전용 축소 렌더링 (색상/크기 구분 가능)
    // =====================================================
    @Override
    public void draw(Graphics g) {
        if (sprite == null) return;
        Graphics2D g2 = (Graphics2D) g;

        // Player2를 조금 더 작고 색다르게 보이도록
        double scale = 0.12; // 약간 더 작게
        int newW = (int) (sprite.getWidth() * scale);
        int newH = (int) (sprite.getHeight() * scale);

        Image scaled = sprite.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        g2.drawImage(scaled, (int) x, (int) y, null);
    }

    // =====================================================
    // 🔹 2P는 입력 대신 네트워크 패킷으로 좌표 갱신됨
    // =====================================================
    public void updateFromNetwork(int x, int y, int hp) {
        this.x = x;
        this.y = y;
        this.setCurrentHealth(hp);
    }

    // =====================================================
    // 🔹 체력 직접 세팅 메서드 (UserEntity에 추가 없이 사용)
    // =====================================================
    public void setCurrentHealth(int value) {
        try {
            java.lang.reflect.Field field = UserEntity.class.getDeclaredField("currentHealth");
            field.setAccessible(true);
            field.setInt(this, value);
        } catch (Exception e) {
            System.err.println("⚠️ 2P 체력 동기화 실패: " + e.getMessage());
        }
    }

    // =====================================================
    // 🔹 Game.java와 연동용 Getter / Setter (네트워크용)
    // =====================================================
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public int getHp() {
        try {
            java.lang.reflect.Field field = UserEntity.class.getDeclaredField("currentHealth");
            field.setAccessible(true);
            return field.getInt(this);
        } catch (Exception e) {
            return 0;
        }
    }

    private int score = 0;
    public int getScore() { return this.score; }
    public void addScore(int value) { this.score += value; }
}