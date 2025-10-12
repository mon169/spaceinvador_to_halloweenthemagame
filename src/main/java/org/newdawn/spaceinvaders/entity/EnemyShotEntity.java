package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

/**
 * 🎯 적 유령/보스가 발사하는 총알 엔티티
 * - 종류: shot / iceshot / bombshot
 * - 플레이어나 요새를 향해 날아감
 * - 크기 축소(0.3배) + 잔상
 * - ✅ owner(발사자) 보유: 소유자/외계인/보스에게는 절대 피해 주지 않음
 * - ✅ 방어막에 막히면 차단 처리
 */
public class EnemyShotEntity extends Entity {
    private final Game game;
    private boolean used = false;

    // 🔹 소유자/종류
    private final Entity owner;
    private final String shotKind;

    // 이동 속도
    private double vx, vy;

    // 🔹 잔상 관련
    private static final int TRAIL_LEN = 3;
    private final double[] trailX = new double[TRAIL_LEN];
    private final double[] trailY = new double[TRAIL_LEN];
    private int trailIdx = 0;
    private boolean trailFilled = false;

    // 🔹 방어막 충돌 플래그
    private boolean blockedByShield = false;

    public EnemyShotEntity(Game game, String spritePath, int x, int y,
                           double vx, double vy, String shotKind, Entity owner) {
        super(spritePath, x, y);
        this.game = game;
        this.vx = vx;
        this.vy = vy;
        this.dx = vx;
        this.dy = vy;
        this.owner = owner;
        this.shotKind = (shotKind == null) ? "shot" : shotKind;

        // 잔상 초기화
        for (int i = 0; i < TRAIL_LEN; i++) {
            trailX[i] = x;
            trailY[i] = y;
        }
    }

    public Entity getOwner() { return owner; }
    public String getShotKind() { return shotKind; }

    /** ✅ 방어막에 막혔을 때 호출 */
    public void setBlockedByShield() {
        this.blockedByShield = true;
    }

    /** ✅ 방어막에 막혔는지 여부 확인 */
    public boolean isBlockedByShield() {
        return blockedByShield;
    }

    @Override
    public void move(long delta) {
        // 잔상 기록
        trailX[trailIdx] = x;
        trailY[trailIdx] = y;
        trailIdx = (trailIdx + 1) % TRAIL_LEN;
        if (trailIdx == 0) trailFilled = true;

        super.move(delta);

        // 화면 밖 제거
        if (y < -50 || y > 650 || x < -50 || x > 850) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (used) return;
        if (blockedByShield) return;

        // ✅ 자기 소유자 or 외계인(보스 포함)은 무시
        if (other == owner) return;
        if (other instanceof AlienEntity) return;

        // ✅ 방어막 충돌
        if (other instanceof ShieldEntity) {
            ((ShieldEntity) other).onBlocked(this);
            this.setBlockedByShield();
            game.removeEntity(this);
            used = true;
            return;
        }

        // ✅ 요새 피해
        if (other instanceof FortressEntity) {
            FortressEntity fortress = (FortressEntity) other;
            fortress.damage(10);
            game.removeEntity(this);
            used = true;
            return;
        }

        // ✅ 플레이어 피해
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            ship.takeDamage(10);
            game.removeEntity(this);
            used = true;
        }
    }

    /** 💫 총알 크기 0.3배 + 잔상 그리기 */
    @Override
    public void draw(Graphics g) {
        if (sprite == null) return;
        Graphics2D g2 = (Graphics2D) g;

        // 잔상 (희미한 그림자)
        if (trailFilled) {
            for (int i = 1; i <= TRAIL_LEN; i++) {
                int idx = (trailIdx - i + TRAIL_LEN) % TRAIL_LEN;
                double tx = trailX[idx];
                double ty = trailY[idx];
                drawScaled(g2, tx, ty, 0.3, 0.5f / (i + 1));
            }
        }

        // 본탄
        drawScaled(g2, x, y, 0.3, 1.0f);
    }

    private void drawScaled(Graphics2D g2, double px, double py, double scale, float alpha) {
        Image scaled = sprite.getImage().getScaledInstance(
                (int) (sprite.getWidth() * scale),
                (int) (sprite.getHeight() * scale),
                Image.SCALE_SMOOTH
        );
        g2.drawImage(scaled, (int) px, (int) py, null);
    }
}
