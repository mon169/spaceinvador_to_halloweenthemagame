package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

/**
 * An entity which represents one of our space invader aliens.
 * * @author Kevin Glass
 */
public class AlienEntity extends Entity {
    /** The speed at which the alient moves horizontally */
    private double moveSpeed = 75;
    private double originalMoveSpeed = 75;
    /** The game in which the entity exists */
    private Game game;
    /** The animation frames */
    private Sprite[] frames = new Sprite[4];
    /** The time since the last frame change took place */
    private long lastFrameChange;
    /** The frame duration in milliseconds, i.e. how long any given frame of animation lasts */
    private long frameDuration = 250;
    /** The current frame of animation being displayed */
    private int frameNumber;
    /** The alien's current health */
    private int health = 1;
    private boolean frozen = false;
    private long freezeEndTime = 0;
    private boolean canAttack = true;

    // 이 변수들은 이제 Game 클래스에서 관리합니다.
    // private long lastShotTime = 0;
    // private long shotInterval = 2000; 

    /**
     * Create a new alien entity
     * * @param game The game in which this entity is being created
     * @param x The intial x location of this alien
     * @param y The intial y location of this alient
     */
    public AlienEntity(Game game,int x,int y) {
        super("sprites/alien.gif",x,y);
        
        // setup the animatin frames
        frames[0] = sprite;
        frames[1] = SpriteStore.get().getSprite("sprites/alien2.gif");
        frames[2] = sprite;
        frames[3] = SpriteStore.get().getSprite("sprites/alien3.gif");
        
        this.game = game;
        dx = -moveSpeed;
    }

    /**
     * Request that this alien moved based on time elapsed
     * * @param delta The time that has elapsed since last move
     */
    public void freeze(int duration) {
        frozen = true;
        freezeEndTime = System.currentTimeMillis() + duration;
        dx = 0; // 움직임 중지
    }
    
    /**
     * 외계인이 총알을 발사하는 로직을 담당합니다.
     * 이 메소드는 Game 클래스에서 호출되어야 합니다.
     */
    public void fireShot() {
        // 2스테이지 이상일 때만 발사
        if (game.getCurrentStage() < 2 || !canAttack) {
            return;
        }

        double bulletSpeed;
        int stage = game.getCurrentStage();
        int bulletCount = 1;
        
        // 스테이지별 총알 수 설정
        if (stage == 3) bulletCount = 3;
        else if (stage == 4) bulletCount = 5;
        else if (stage >= 5) bulletCount = 7;
        
        // 스테이지별 총알 속도 설정
        if (stage <= 2) {
            bulletSpeed = 200; 
        } else if (stage <= 4) {
            bulletSpeed = 400; 
        } else {
            bulletSpeed = 700;
        }
        
        int centerX = (int)(getX() + sprite.getWidth() / 2.0);
        int centerY = (int)(getY() + sprite.getHeight() / 2.0);

        if (bulletCount == 1) {
            // 단일 총알: 아래 방향
            org.newdawn.spaceinvaders.entity.EnemyShotEntity enemyShot = new org.newdawn.spaceinvaders.entity.EnemyShotEntity(
                game, "sprites/shot.gif", centerX, centerY, 0, bulletSpeed
            );
            game.addEntity(enemyShot);
        } else {
            // 다중 총알: 퍼지는 각도로 발사
            double spreadAngle = 40; // 총알 퍼짐 각도(도)
            double startAngle = -spreadAngle/2;
            double angleStep = spreadAngle/(bulletCount-1);
            
            for (int i = 0; i < bulletCount; i++) {
                // 각도를 라디안으로 변환 (0도는 아래 방향)
                double angle = Math.toRadians(startAngle + angleStep * i);
                
                // sin은 X축 방향 (좌우), cos은 Y축 방향 (아래) 속도
                double dx = bulletSpeed * Math.sin(angle);
                double dy = bulletSpeed * Math.cos(angle);
                
                org.newdawn.spaceinvaders.entity.EnemyShotEntity enemyShot = new org.newdawn.spaceinvaders.entity.EnemyShotEntity(
                    game, "sprites/shot.gif", centerX, centerY, dx, dy
                );
                game.addEntity(enemyShot);
            }
        }
    }


    public void move(long delta) {
        // 얼어있는지 확인하고 시간이 지났으면 해제
        if (frozen && System.currentTimeMillis() > freezeEndTime) {
            frozen = false;
            // 멈추기 전 방향으로 복귀
            dx = (dx >= 0) ? moveSpeed : -moveSpeed; 
        }

        if (!frozen) {
            lastFrameChange += delta;
            if (lastFrameChange > frameDuration) {
                lastFrameChange = 0;
                frameNumber++;
                if (frameNumber >= frames.length) {
                    frameNumber = 0;
                }
                sprite = frames[frameNumber];
            }
        }

        // if we have reached the left hand side of the screen and
        // are moving left then request a logic update 
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
        }
        // and vice vesa, if we have reached the right hand side of 
        // the screen and are moving right, request a logic update
        if ((dx > 0) && (x > 750)) {
            game.updateLogic();
        }

        // **총알 발사 로직 제거됨. Game 클래스에서 fireShot()을 호출하여 발사**

        // proceed with normal move
        super.move(delta);
    }
    
    /**
     * Update the game logic related to aliens
     */
    public void doLogic() {
        dx = -dx;
        boolean hitObstacle = false;
        for (Entity e : game.getEntities()) {
            if (e instanceof ObstacleEntity && this.collidesWith(e)) {
                hitObstacle = true;
                break;
            }
        }
        if (!hitObstacle) {
            y += 10;
        }
        // if we've reached the bottom of the screen then the player dies
        if (y > 570) {
            game.notifyDeath();
        }
    }
    
    /**
     * Take damage from a hit
     * * @param damage The amount of damage to take
     * @return true if the alien died from this damage, false otherwise
     */
    public boolean takeDamage(int damage) {
        health -= damage;
        return health <= 0;
    }
    
    /**
     * Notification that this alien has collided with another entity
     * * @param other The other entity
     */
    public void collidedWith(Entity other) {
        // ShipEntity와 충돌 시 유저에게 데미지
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            ship.takeDamage(10); // 외계인 공격력 10
            game.removeEntity(this); // 외계인 제거
            return;
        }
        // 그 외 충돌은 기존대로 무시
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    public boolean canAttack() {
        return canAttack;
    }
}