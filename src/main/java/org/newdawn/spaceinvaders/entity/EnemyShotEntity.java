package org.newdawn.spaceinvaders.entity;

import java.util.Random;
import org.newdawn.spaceinvaders.Game;

/**
 * 적이 발사하는 총알 엔티티
 */
public class EnemyShotEntity extends Entity {
    private double moveSpeed = 300; // 기본값(아래로 이동)
    private Game game;
    private int damage = 10; // 기본 데미지
    private boolean used = false;
    private boolean blockedByShield = false; // 추가된 필드

    // 공격 패턴 타입: 0=체력감소(redshot), 1=유저 얼리기(blueshot), 2=아이템 감소(redshot)
    private int attackType = 0;
    private String shotSprite;

    public EnemyShotEntity(Game game, String sprite, int x, int y) {
        super(sprite, x, y);
        this.game = game;
        dy = moveSpeed;
    }

    public EnemyShotEntity(Game game, String sprite, int x, int y, double moveSpeed) {
        super(sprite, x, y);
        this.game = game;
        this.moveSpeed = moveSpeed;
        dy = moveSpeed;
    }

    // dx, dy 직접 지정하는 생성자 추가 (퍼지 총알용)
    public EnemyShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.moveSpeed = Math.sqrt(dx * dx + dy * dy);
        this.dx = dx;
        this.dy = dy;
        // 공격 패턴 랜덤 결정
        java.util.Random rand = new java.util.Random();
        attackType = rand.nextInt(3); // 0~2
        if (attackType == 1) {
            shotSprite = "sprites/blueshot.png";
        } else if (attackType == 2) {
            shotSprite = "sprites/redshot.png";
        } else {
            shotSprite = "sprites/shot.png";
        }
        this.sprite = org.newdawn.spaceinvaders.SpriteStore.get().getSprite(shotSprite);
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if (y > 600) { // 화면 아래로 벗어나면 제거
            game.removeEntity(this);
        }
    }

    public void setBlockedByShield() { // 추가된 메서드
        blockedByShield = true;
    }

    public boolean isBlockedByShield() { // 추가된 메서드
        return blockedByShield;
    }

    @Override
    public void collidedWith(Entity other) {
        if (used) return;
        if (other instanceof ShieldEntity) {
            setBlockedByShield();
            game.removeEntity(this);
            used = true;
            return;
        }
        if (other instanceof ShipEntity) {
            if (isBlockedByShield()) return; // 방어막에 막힌 총알은 ShipEntity에 효과 없음
            ShipEntity ship = (ShipEntity) other;
            if (attackType == 0) {
                // 체력 감소 (기본 shot)
                ship.takeDamage(damage);
            } else if (attackType == 1) {
                // 유저 얼리기 (blueshot) - 3초
                ship.freeze(3000);
            } else if (attackType == 2) {
                // 아이템 감소 (redshot)
                ship.removeOneItem();
            }
            game.removeEntity(this);
            used = true;
        }
    }
}
