package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * ShipEntity의 방어막(Shield) 엔티티
 */
public class ShieldEntity extends Entity {
    private Game game;
    private ShipEntity ship;
    private int duration = 0; // 방어막 지속 시간 (ms)
    private long endTime = 0;
    private boolean active = false;

    public ShieldEntity(Game game, ShipEntity ship, int duration) {
        super("sprites/shield.png", ship.getX() + ship.sprite.getWidth()/2 - 16, ship.getY() + ship.sprite.getHeight()/2 - 32); // 오른쪽으로 16px 이동
        this.game = game;
        this.ship = ship;
        this.duration = duration;
        this.endTime = System.currentTimeMillis() + duration;
        this.active = true;
    }

    @Override
    public void move(long delta) {
        // ShipEntity 위치에 따라 방어막 위치 갱신
        this.x = ship.getX() + ship.sprite.getWidth()/2 - 16;
        this.y = ship.getY() + ship.sprite.getHeight()/2 - 48;
        if (System.currentTimeMillis() > endTime) {
            active = false;
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 적 총알(EnemyShotEntity)과 충돌 시 총알 제거, 방어막도 즉시 사라짐
        if (other instanceof EnemyShotEntity) {
            ((EnemyShotEntity)other).setBlockedByShield(); // 총알에 방어막에 막혔다는 플래그 설정
            game.removeEntity(other);
            game.removeEntity(this); // 총알 맞으면 방어막 즉시 사라짐
        }
    }

    @Override
    public void draw(java.awt.Graphics g) {
        sprite.draw(g, (int)x, (int)y);
    }

    public boolean isActive() {
        return active;
    }
}
