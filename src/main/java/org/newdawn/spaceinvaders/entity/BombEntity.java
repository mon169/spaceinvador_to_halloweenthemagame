package org.newdawn.spaceinvaders.entity;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;

public class BombEntity extends Entity {
    private Game game;
    private double moveSpeed = -300;
    private int explosionRadius = 100;

    public BombEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        dy = moveSpeed;
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        // 화면 상단에 도달하면 폭발
        if (y < 10) explode();
    }

    private void explode() {
        ArrayList<Entity> hit = new ArrayList<>();
        // 폭발 반경 내의 모든 외계인 탐색
        for (Entity e : game.getEntities()) {
            if (e instanceof AlienEntity) {
                // 거리 계산
                double d = Math.sqrt(Math.pow(e.getX() - x, 2) + Math.pow(e.getY() - y, 2));
                if (d <= explosionRadius) hit.add(e);
            }
        }
        // 피격된 모든 외계인 제거
        for (Entity e : hit) {
            game.removeEntity(e);
            game.notifyAlienKilled();
        }
        // 폭탄 자신 제거
        game.removeEntity(this);
    }

    @Override
    public void collidedWith(Entity other) {
        // 외계인과 충돌 시 즉시 폭발
        if (other instanceof AlienEntity) explode();
    }
}