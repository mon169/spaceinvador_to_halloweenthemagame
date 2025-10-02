package org.newdawn.spaceinvaders.entity;

import java.util.ArrayList;
import org.newdawn.spaceinvaders.Game;

public class BombEntity extends Entity {
    private Game game;
    private double moveSpeed = -300;  // 위로 이동
    private int damage = 100;  // 큰 데미지
    private int explosionRadius = 100;  // 폭발 반경

    public BombEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        dy = moveSpeed;  // 수직 이동 속도 설정
    }

    public void move(long delta) {
        // 이동 거리 계산
        super.setVerticalMovement(moveSpeed);
        super.move(delta);

        // 화면 상단에 도달했을 때만 폭발
        if (y < 10) {  // 화면 최상단 근처에서 폭발
            explode();
        }
    }

    private void explode() {
        // 폭발 범위 내의 모든 적을 찾아 제거
        ArrayList<Entity> hitEntities = new ArrayList<>();

        for (Entity entity : game.getEntities()) {
            if (entity instanceof AlienEntity) {
                // 폭발 반경 내에 있는지 확인
                double distance = Math.sqrt(
                    Math.pow(entity.getX() - this.x, 2) +
                    Math.pow(entity.getY() - this.y, 2)
                );

                if (distance <= explosionRadius) {
                    hitEntities.add(entity);
                }
            }
        }

        // 별도의 루프에서 엔티티 제거 (동시 수정 문제 방지)
        for (Entity entity : hitEntities) {
            AlienEntity alien = (AlienEntity) entity;
            alien.takeDamage(999999); // 무조건 즉사
            game.removeEntity(entity);
            game.notifyAlienKilled();
        }

        // 폭탄 제거
        game.removeEntity(this);
    }

    @Override
    public void collidedWith(Entity other) {
        // ShipEntity와의 충돌은 무시 (자기 자신의 폭탄이므로 데미지를 주지 않음)
        if (other instanceof ShipEntity) {
            // 아무 일도 일어나지 않음 - 자신의 배는 폭탄에 맞지 않도록 함
            return;
        }
        // 외계인과 충돌하면 폭발
        if (other instanceof AlienEntity) {
            explode();
        }
    }
}