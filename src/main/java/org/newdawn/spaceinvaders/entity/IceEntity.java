package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * 플레이어의 얼음 공격 - 유령을 잠시 얼림
 */
public class IceEntity extends Entity {
    private Game game;
    private double moveSpeed = -300;
    private int freezeDuration = 3000; // 3초간 얼림

    public IceEntity(Game game, String ref, int x, int y) {
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
        if (other instanceof AlienEntity) {
            AlienEntity alien = (AlienEntity) other;
            alien.freeze(freezeDuration);
            game.removeEntity(this);
        }
    }
}
