package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

/**
 * EnemyShotEntity - 적(몬스터/보스)이 발사하는 총알
 */
public class EnemyShotEntity extends Entity {
    private final Game game;
    private boolean used = false;

    private final Entity owner;
    private final String shotKind;

    // 잔상
    private static final int TRAIL_LEN = 3;
    private final double[] trailX = new double[TRAIL_LEN];
    private final double[] trailY = new double[TRAIL_LEN];
    private int trailIdx = 0;
    private boolean trailFilled = false;

    private boolean blockedByShield = false;

    public EnemyShotEntity(Game game, String spritePath, int x, int y,
                           double vx, double vy, String shotKind, Entity owner) {
        super(spritePath, x, y);
        this.game = game;
        this.dx = vx;
        this.dy = vy;
        this.owner = owner;
        this.shotKind = (shotKind == null) ? "shot" : shotKind;

        for (int i = 0; i < TRAIL_LEN; i++) {
            trailX[i] = x;
            trailY[i] = y;
        }
    }

    public Entity getOwner() { return owner; }
    public String getShotKind() { return shotKind; }

    public void setBlockedByShield() { blockedByShield = true; }
    public boolean isBlockedByShield() { return blockedByShield; }

    @Override
    public void move(long delta) {
        trailX[trailIdx] = x;
        trailY[trailIdx] = y;
        trailIdx = (trailIdx + 1) % TRAIL_LEN;
        if (trailIdx == 0) trailFilled = true;

        super.move(delta);

        if (y < -50 || y > 650 || x < -50 || x > 850) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (used) return;
        if (blockedByShield) return;

        // 발사자나 다른 몬스터와의 충돌은 무시
        if (other == owner) return;
        if (other instanceof MonsterEntity) return;

        // 방어막 처리
        if (other instanceof ShieldEntity) {
            ((ShieldEntity) other).onBlocked(this);
            setBlockedByShield();
            game.removeEntity(this);
            used = true;
            return;
        }

        // 요새 피해
        if (other instanceof FortressEntity) {
            FortressEntity fortress = (FortressEntity) other;
            fortress.damage(10);
            game.removeEntity(this);
            used = true;
            return;
        }

        // 플레이어 피해
        if (other instanceof UserEntity) {
            UserEntity ship = (UserEntity) other;
            ship.takeDamage(10);
            game.removeEntity(this);
            used = true;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (sprite == null) return;
        Graphics2D g2 = (Graphics2D) g;

        if (trailFilled) {
            for (int i = 1; i <= TRAIL_LEN; i++) {
                int idx = (trailIdx - i + TRAIL_LEN) % TRAIL_LEN;
                double tx = trailX[idx];
                double ty = trailY[idx];
                drawScaled(g2, tx, ty, 0.3, 0.5f / (i + 1));
            }
        }

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
