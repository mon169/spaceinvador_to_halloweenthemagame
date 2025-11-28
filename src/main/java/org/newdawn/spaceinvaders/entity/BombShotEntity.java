package org.newdawn.spaceinvaders.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Boss.BossEntity; // [수정] BossEntity 임포트 추가

/**
 * 💣 BombShotEntity 수정본
 * - 보스는 즉시 삭제하지 않고 데미지만 입힙니다.
 */
public class BombShotEntity extends Entity {
    private final Game game;
    private static final double MOVE_SPEED = -300;
    private static final int EXPLOSION_RADIUS = 250;
    private static final int BOSS_DAMAGE = 100; // [수정] 보스에게 줄 폭탄 데미지 설정

    public BombShotEntity(Game game, String spriteRef, int x, int y) {
        super(spriteRef, x, y);
        this.game = game;
        this.dy = MOVE_SPEED;
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if (y < 150) {
            explode();
        }
    }

    /** 💥 폭발 처리 수정됨 */
    private void explode() {
        List<Entity> toRemove = new ArrayList<>(); // 삭제할 일반 몬스터 리스트
        
        // 반경 내 엔티티 검사
        for (Entity e : game.getEntities()) {
            // 나 자신(폭탄)은 건너뜀
            if (e == this) continue;

            // MonsterEntity(보스 포함)인지 확인
            if (e instanceof MonsterEntity || e instanceof BossEntity) {
                double dist = Math.hypot(e.getX() - x, e.getY() - y);
                
                if (dist <= EXPLOSION_RADIUS) {
                    // [수정] 보스인 경우: 삭제하지 않고 데미지만 줌
                    if (e instanceof BossEntity) {
                        ((BossEntity) e).takeDamage(BOSS_DAMAGE);
                        System.out.println("💥 폭탄이 보스에게 " + BOSS_DAMAGE + " 데미지를 입혔습니다!");
                    } 
                    // [수정] 일반 몬스터인 경우: 삭제 리스트에 추가
                    else {
                        toRemove.add(e);
                    }
                }
            }
        }

        // 일반 몬스터들 일괄 삭제
        System.out.println("💥 폭발로 일반 몬스터 " + toRemove.size() + "마리 처치!");
        for (Entity e : toRemove) {
            game.removeEntity(e);
            game.notifyAlienKilled();
        }

        // 폭탄 자체 제거
        game.removeEntity(this);
    }

    @Override
    public void collidedWith(Entity other) {
        // 몬스터나 보스에 충돌하면 즉시 폭발
        if (other instanceof MonsterEntity || other instanceof BossEntity) {
            explode();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            Image scaled = sprite.getImage().getScaledInstance((int) (sprite.getWidth() * 0.5), (int) (sprite.getHeight() * 0.5), Image.SCALE_SMOOTH);
            g2.drawImage(scaled, (int) x, (int) y, null);
        }
    }
}