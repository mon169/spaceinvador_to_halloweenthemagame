package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * An entity which represents one of our space invader aliens.
 * 
 * @author Kevin Glass (modified)
 */
public class AlienEntity extends Entity {
    /** The speed at which the alien moves horizontally */
    private double moveSpeed = 75;
    /** The game in which the entity exists */
    private Game game;
    /** The alien's current health */
    private int health = 1;
    /** Frozen 상태 여부 */
    private boolean frozen = false;
    /** 얼음 효과가 풀리는 시간 */
    private long freezeEndTime = 0;
    /** 공격 가능 여부 (스테이지별로 다르게 적용 가능) */
    private boolean canAttack = true;

    /**
     * Create a new alien entity
     * 
     * @param game The game in which this entity is being created
     * @param x    The initial x location of this alien
     * @param y    The initial y location of this alien
     */
    public AlienEntity(Game game, int x, int y) {
        // choose a random sprite among 3 monsters
        super(getRandomSprite(), x, y);
        this.game = game;
        dx = -moveSpeed; // start moving left
    }

    /**
     * Pick one of 3 sprites randomly when alien is created
     * 
     * @return The path of the sprite image
     */
    private static String getRandomSprite() {
        int rand = (int) (Math.random() * 3); // 0~2
        if (rand == 0) return "sprites/monster1.png";
        if (rand == 1) return "sprites/monster2.png";
        return "sprites/monster3.png";
    }

    /**
     * Freeze the alien for a given duration (ms)
     * 
     * @param duration Time in milliseconds to freeze
     */
    public void freeze(int duration) {
        frozen = true;
        freezeEndTime = System.currentTimeMillis() + duration;
        dx = 0;  // 움직임 중지
    }

    /**
     * Request that this alien moved based on time elapsed
     * 
     * @param delta The time that has elapsed since last move
     */
    @Override
    public void move(long delta) {
        // 얼어있는지 확인하고 시간이 지났으면 해제
        if (frozen && System.currentTimeMillis() > freezeEndTime) {
            frozen = false;
            dx = (dx >= 0) ? moveSpeed : -moveSpeed;
        }

        // 화면 경계 체크 (스프라이트 실제 폭 사용)
        int w = (sprite != null ? sprite.getWidth() : 35);

        // if we have reached the left hand side of the screen and
        // are moving left then request a logic update 
        if ((dx < 0) && (x <= 10)) {
            x = 10;
            game.updateLogic();
        }
        // and vice versa, if we have reached the right hand side of 
        // the screen and are moving right, request a logic update
        if ((dx > 0) && (x >= 800 - w - 10)) {
            x = 800 - w - 10;
            game.updateLogic();
        }

        // proceed with normal move (unless frozen)
        if (!frozen) {
            super.move(delta);
        }
    }

    /**
     * Update the game logic related to aliens
     */
    @Override
    public void doLogic() {
        // swap over horizontal movement and move down the screen a bit
        dx = -dx;
        y += 10;

        // if dx somehow became 0, restore it
        if (dx == 0) {
            dx = (Math.random() < 0.5 ? -1 : 1) * moveSpeed;
        }

        // if we've reached the bottom of the screen then the player dies
        if (y > 570) {
            game.notifyDeath();
        }
    }

    /**
     * 외계인이 총알을 발사하는 로직을 담당합니다.
     * 이 메소드는 Game 클래스에서 호출되어야 합니다.
     */
    public void fireShot() {
        // 스테이지/상태에 따라 공격 가능 여부 체크
        if (!canAttack) {
            return;
        }

        // 스프라이트가 아직 없을 때 NPE 방지 (개발 중 안전장치)
        int w = (sprite != null ? sprite.getWidth()  : 32);
        int h = (sprite != null ? sprite.getHeight() : 32);

        // 총알이 Alien의 중앙(아래쪽)에서 나오도록 좌표 설정
        int centerX = (int)(getX() + w / 2.0);
        int centerY = (int)(getY() + h);

        // 스테이지별 발사 패턴
        int stage = game.getCurrentStage();
        int bulletCount = 1;
        double bulletSpeed = 200; // 기본 속도

        // 스테이지 2: 속도 20% 증가
        if (stage == 2) {
            bulletSpeed = 240;
        }
        // 스테이지 3,4,5… : 탄수 증가
        if (stage == 3) bulletCount = 3;
        else if (stage == 4) bulletCount = 5;
        else if (stage >= 5) bulletCount = 7;

        if (bulletCount == 1) {
            // 단일 탄: 직하향
            EnemyShotEntity shot = new EnemyShotEntity(
                game, "sprites/shot.png", centerX, centerY, 0, bulletSpeed
            );
            game.addEntity(shot);
            return;
        }

        // 다중 탄: 퍼지는 각도
        double spreadDeg = 40.0;                 // 총 퍼짐 각도(도)
        double startDeg  = -spreadDeg / 2.0;     // 좌측 시작 각도
        double stepDeg   = spreadDeg / (bulletCount - 1);

        for (int i = 0; i < bulletCount; i++) {
            double angleRad = Math.toRadians(startDeg + stepDeg * i);
            double vx = bulletSpeed * Math.sin(angleRad); // 좌우
            double vy = bulletSpeed * Math.cos(angleRad); // 아래

            EnemyShotEntity shot = new EnemyShotEntity(
                game, "sprites/shot.png", centerX, centerY, vx, vy
            );
            game.addEntity(shot);
        }
    }

    /**
     * Take damage from a hit
     * 
     * @param damage The amount of damage to take
     * @return true if the alien died from this damage, false otherwise
     */
    public boolean takeDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    /**
     * Notification that this alien has collided with another entity
     * 
     * @param other The other entity
     */
    @Override
    public void collidedWith(Entity other) {
        // ShipEntity와 충돌 시 유저에게 데미지
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            ship.takeDamage(10); // 외계인 공격력 10
            game.removeEntity(this); // 외계인 제거
        }
        // 그 외 충돌은 기존대로 무시
    }

    /** 외부에서 공격 가능 여부를 제어할 수 있도록 setter */
    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    /** 공격 가능 여부를 반환 */
    public boolean canAttack() {
        return canAttack;
    }
}