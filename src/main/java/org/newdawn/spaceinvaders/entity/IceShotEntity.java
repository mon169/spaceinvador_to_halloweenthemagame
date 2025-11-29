package org.newdawn.spaceinvaders.entity;

import java.util.ArrayList;
import java.util.List;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Boss.BossEntity;

/**
 * 플레이어의 얼음 공격 - 유령을 잠시 얼림 (광역 효과)
 */
public class IceShotEntity extends Entity {
    private Game game;
    private double moveSpeed = -300;
    private int freezeDuration = 3000; 
    private static final int FREEZE_RADIUS = 150; // 폭탄(250)보다 작은 광역 범위
    private boolean used = false;

    public IceShotEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    public void move(long delta) {
        setVerticalMovement(moveSpeed);
        super.move(delta);
        if (y < -50) {
            game.removeEntity(this);
        }
    }

    public void collidedWith(Entity other) {
        if (used) return;
        
        if (other instanceof MonsterEntity || other instanceof BossEntity) {
            // 충돌 지점 기준으로 광역 얼림 효과
            freezeArea();
            used = true;
        }
    }
    
    /** ❄️ 광역 얼림 효과 */
    private void freezeArea() {
        List<Entity> toFreeze = new ArrayList<>();
        
        // 반경 내 MonsterEntity 및 BossEntity 수집
        for (Entity e : game.getEntities()) {
            if (e instanceof MonsterEntity || e instanceof BossEntity) {
                double dist = Math.hypot(e.getX() - x, e.getY() - y);
                if (dist <= FREEZE_RADIUS) {
                    toFreeze.add(e);
                }
            }
        }
        
        // 얼림 효과 적용
        System.out.println("❄️ 얼음 공격으로 " + toFreeze.size() + "마리 동결!");
        for (Entity e : toFreeze) {
            if (e instanceof BossEntity) {
                ((BossEntity) e).freeze(freezeDuration);
            } else if (e instanceof MonsterEntity) {
                ((MonsterEntity) e).freeze(freezeDuration);
            }
        }
        
        game.removeEntity(this);
    }
}
