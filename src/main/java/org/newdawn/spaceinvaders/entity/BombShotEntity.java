package org.newdawn.spaceinvaders.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Boss.BossEntity;

/**
 * 💣 BombShotEntity
 * - 위로 이동하다가 화면 상단에서 폭발
 * - 폭발 반경 내의 모든 MonsterEntity 제거
 * - Game의 엔티티 관리(removeEntity/notifyAlienKilled)와 연동
 *
 * 요구사항:
 * - Game에 아래 메서드가 존재해야 합니다:
 *   List<Entity> getEntities( );
 *   void removeEntity(Entity e);
 *   void notifyAlienKilled();
 */
public class BombShotEntity extends Entity {
    private final Game game;
    /** 위로 이동 속도(px/s) */
    private static final double MOVE_SPEED = -300;
    /** 폭발 반경(px) - 적당한 범위 */
    private static final int EXPLOSION_RADIUS = 150;

    public BombShotEntity(Game game, String spriteRef, int x, int y) {
        super(spriteRef, x, y);
        this.game = game;
        this.dy = MOVE_SPEED;
        System.out.println("💣 BombShotEntity 생성 — 위치(" + x + "," + y + ") 속도 dy=" + this.dy);
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        // 화면 상단 부근에서 폭발
        if (y < 150) {
            explode();
        }
    }

    /** 💥 폭발 처리: 반경 내 몬스터 수집 후 일괄 제거 */
    private void explode() {
        List<Entity> toHit = new ArrayList<>();
        System.out.println("💥 폭발 실행 — 위치(" + x + "," + y + ") 반경=" + EXPLOSION_RADIUS);

        // 반경 내 MonsterEntity 및 BossEntity 수집 (보스 포함), 최대 2마리
        for (Entity e : game.getEntities()) {
            if ((e instanceof MonsterEntity || e instanceof BossEntity) && toHit.size() < 2) {
                double dist = Math.hypot(e.getX() - x, e.getY() - y);
                if (dist <= EXPLOSION_RADIUS) {
                    toHit.add(e);
                }
            }
        }

        // 제거 및 알림
        System.out.println("💥 폭발로 " + toHit.size() + "마리 처치!");
        for (Entity e : toHit) {
            if (e instanceof BossEntity) {
                // 보스는 체력 기반으로 피해 받음
                ((BossEntity) e).takeDamage(100); // 폭탄 피해
            } else {
                game.removeEntity(e);
                game.notifyAlienKilled();
            }
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

    @Override
    public void draw(Graphics g) {
        // 기본 스프라이트가 있으면 축소해서 그리기
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            Image scaled = sprite.getImage().getScaledInstance((int) (sprite.getWidth() * 0.5), (int) (sprite.getHeight() * 0.5), Image.SCALE_SMOOTH);
            g2.drawImage(scaled, (int) x, (int) y, null);
        }

        // 디버그용 시각 표시 (보이지 않는 경우를 대비)
        g.setColor(Color.RED);
        g.fillRect((int) x + 6, (int) y + 6, 6, 6);
    }
}