package org.newdawn.spaceinvaders.entity.Boss;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.BombShotEntity;
import org.newdawn.spaceinvaders.entity.IceShotEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.ShieldEntity;

public abstract class BossEntity extends Entity {
    protected int health = 20;
    protected Game game;

    // 동결 관련
    protected boolean frozen = false;
    protected long freezeEndTime = 0;

    // 피격 쿨다운
    protected long lastHitTime = 0;
    protected static final long HIT_COOLDOWN = 200; // 피격 무적 시간

    public BossEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    public void takeDamage(int damage) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHitTime < HIT_COOLDOWN) {
            return; // 쿨다운 중 무시
        }
        lastHitTime = currentTime;

        health -= damage;
        System.out.println("보스 피격! 남은 HP: " + health);
        if (health <= 0) {
            game.bossDefeated();
            game.removeEntity(this);
        }
    }

    public int getHealth() {
        return health;
    }

    protected void updateFreeze() {
        if (frozen && System.currentTimeMillis() > freezeEndTime) {
            frozen = false;
            System.out.println("❄️ 동결 해제!");
        }
    }

    public void freeze(long duration) {
        frozen = true;
        freezeEndTime = System.currentTimeMillis() + duration;
        System.out.println("❄️ 보스 동결! (" + (duration / 1000) + "초)");
    }

    // 아이템 데미지 적용
    public void collidedWithItem(Entity other) {
        if (other instanceof BombShotEntity) {
            takeDamage(100); // 폭탄 데미지
            game.removeEntity(other);
        } else if (other instanceof IceShotEntity) {
            freeze(3000); // 3초 동결
            game.removeEntity(other);
        } else if (other instanceof ShotEntity) {
            takeDamage(1); // 일반 샷 데미지
            game.removeEntity(other);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 아이템과의 충돌 처리
        collidedWithItem(other);
    }
}
