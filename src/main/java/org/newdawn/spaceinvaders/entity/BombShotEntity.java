package org.newdawn.spaceinvaders.entity;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;

/**
 * 💣 BombShotEntity
 * - 위로 이동하다가 화면 상단에서 폭발
 * - 폭발 반경 내의 모든 MonsterEntity 제거
 * - Game의 엔티티 관리(removeEntity/notifyAlienKilled)와 연동
 *
 * 요구사항:
 * - Game에 아래 메서드가 존재해야 합니다:
 *   List<Entity> getEntities();
 *   void removeEntity(Entity e);
 *   void notifyAlienKilled();
 */
public class BombShotEntity extends Entity {
    private final Game game;
    /** 위로 이동 속도(px/s) */
    private static final double MOVE_SPEED = -300;
    /** 폭발 반경(px) */
    private static final int EXPLOSION_RADIUS = 100;

    public BombShotEntity(Game game, String spriteRef, int x, int y) {
        super(spriteRef, x, y);
        this.game = game;
        this.dy = MOVE_SPEED;
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        // 화면 상단 근처에서 폭발
        if (y < 10) {
            explode();
        }
    }

    /** 💥 폭발 처리: 반경 내 몬스터 수집 후 일괄 제거 */
    private void explode() {
        List<Entity> toHit = new ArrayList<>();

        // 반경 내 MonsterEntity 수집
        for (Entity e : game.getEntities()) {
            if (e instanceof MonsterEntity) {
                double dist = Math.hypot(e.getX() - x, e.getY() - y);
                if (dist <= EXPLOSION_RADIUS) {
                    toHit.add(e);
                }
            }
        }

        // 제거 및 알림
        for (Entity e : toHit) {
            game.removeEntity(e);
            game.notifyAlienKilled();
        }

        // 자신의 탄도 제거
        game.removeEntity(this);
    }

    @Override
    public void collidedWith(Entity other) {
        // 몬스터에 직접 충돌해도 즉시 폭발
        if (other instanceof MonsterEntity) {
            explode();
        }
    }
}