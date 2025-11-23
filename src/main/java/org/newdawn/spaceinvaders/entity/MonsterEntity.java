package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import java.util.Random;

/**
 * 👻 MonsterEntity
 * - 일반 몬스터 및 보스 기반 클래스
 * - 이동, 공격, 피격, 동결, 프레임 전환 등 공통 로직 포함
 */
public class MonsterEntity extends Entity {
    protected final Game game;
    private final Random random = new Random();

    protected double moveSpeed;
    private int health = 1;
    private boolean frozen = false;
    private long freezeEndTime = 0;
    private boolean canAttack = true;

    private boolean movingRight = random.nextBoolean();
    private boolean movingDown = random.nextBoolean();
    private long lastMoveChange = 0;
    private long moveChangeDelay = 1000 + random.nextInt(2000);

    private long lastFrameChange = 0;
    private static final int FRAME_DELAY = 120;
    protected static final int MAX_FRAMES = 7;

    private String currentDir = "r";
    private String currentSpriteBase;

    private long lastAttackTime;
    private long attackDelay;
    protected String shotType = "shot"; // 기본 탄환 타입

    // =====================================================
    // ✅ 기본 생성자 (랜덤 일반 몬스터용)
    // =====================================================
    public MonsterEntity(Game game, int x, int y) {
        super("sprites/monster1r.png", x, y);
        this.game = game;

        int stage = 1;
        try {
            if (game != null) stage = game.getCurrentStage();
        } catch (Exception ignored) {}

        if (stage == 1) moveSpeed = 40;
        else if (stage == 2) moveSpeed = 60;
        else if (stage == 3) moveSpeed = 80;
        else moveSpeed = 100;

        lastAttackTime = System.currentTimeMillis() + random.nextInt(2000);
        attackDelay = 1500 + random.nextInt(2000);

        int monsterId = 1 + random.nextInt(3);
        this.currentSpriteBase = "monster" + monsterId;
        this.sprite = SpriteStore.get().getSprite("sprites/" + currentSpriteBase + currentDir + ".png");

        dx = movingRight ? moveSpeed : -moveSpeed;
        dy = movingDown ? moveSpeed : -moveSpeed;
    }

    // =====================================================
    // ✅ 보스 전용 생성자
    // =====================================================
    public MonsterEntity(Game game, String spritePath, int x, int y) {
        super(spritePath, x, y);
        this.game = game;

        int stage = 1;
        try {
            if (game != null) stage = game.getCurrentStage();
        } catch (Exception ignored) {}

        if (stage == 1) moveSpeed = 40;
        else if (stage == 2) moveSpeed = 60;
        else if (stage == 3) moveSpeed = 80;
        else moveSpeed = 100;

        lastAttackTime = System.currentTimeMillis() + random.nextInt(2000);
        attackDelay = 1500 + random.nextInt(2000);

        this.currentSpriteBase = spritePath; // 보스는 직접 지정된 스프라이트
        this.sprite = SpriteStore.get().getSprite(spritePath);

        dx = movingRight ? moveSpeed : -moveSpeed;
        dy = movingDown ? moveSpeed : -moveSpeed;
    }

    // =====================================================
    // 🔹 이동 + 프레임 변경
    // =====================================================
    @Override
    public void move(long delta) {
        long now = System.currentTimeMillis();

        // 동결 해제 체크
        if (frozen && now > freezeEndTime) {
            frozen = false;
            dx = movingRight ? moveSpeed : -moveSpeed;
            dy = movingDown ? moveSpeed : -moveSpeed;
        }

        // 이동 처리
        if (!frozen) super.move(delta);

        // 경계 충돌 시 반전
        if (x <= 10) {
            movingRight = true;
            dx = moveSpeed;
            updateDirection();
        } else if (x >= 760) {
            movingRight = false;
            dx = -moveSpeed;
            updateDirection();
        }
        if (y <= 40) {
            movingDown = true;
            dy = moveSpeed;
        } else if (y >= 520) {
            movingDown = false;
            dy = -moveSpeed;
        }

        // 랜덤 방향 변경
        if (now - lastMoveChange > moveChangeDelay) {
            lastMoveChange = now;
            moveChangeDelay = 1000 + random.nextInt(2000);
            if (random.nextBoolean()) movingRight = !movingRight;
            if (random.nextBoolean()) movingDown = !movingDown;
            dx = movingRight ? moveSpeed : -moveSpeed;
            dy = movingDown ? moveSpeed : -moveSpeed;
            updateDirection();
        }

        // 프레임 업데이트
        if (now - lastFrameChange > FRAME_DELAY) {
            lastFrameChange = now;
            updateSpriteFrame();
        }

        // 공격 쿨타임
        if (canAttack && now - lastAttackTime > attackDelay) {
            lastAttackTime = now;
            attackDelay = 1500 + random.nextInt(2000);
            if (random.nextDouble() < 0.6) {
                fireShot();
            }
        }
    }

    // =====================================================
    // 🔹 공격 (EnemyShotEntity 생성)
    // =====================================================
    public void fireShot() {
        if (game == null) return;

        int startX = getX() + sprite.getWidth() / 2;
        int startY = getY() + sprite.getHeight() / 2;

        UserEntity player = game.getShip();
        FortressEntity fortress = game.getFortress();
        if (player == null && fortress == null) return;

        double targetX = player != null ? player.getX() + player.getWidth() / 2.0 : x;
        double targetY = player != null ? player.getY() + player.getHeight() / 2.0 : y;

        double playerDist = player != null
                ? Math.hypot(targetX - startX, targetY - startY)
                : Double.MAX_VALUE;

        if (fortress != null) {
            double fortX = fortress.getX() + 40;
            double fortY = fortress.getY() + 40;
            double fortDist = Math.hypot(fortX - startX, fortY - startY);
            if (fortDist < playerDist) {
                targetX = fortX;
                targetY = fortY;
            }
        }

        double ddx = targetX - startX;
        double ddy = targetY - startY;
        double len = Math.sqrt(ddx * ddx + ddy * ddy);
        if (len == 0) return;
        ddx /= len;
        ddy /= len;

        double speed;
        String spritePath;
        switch (shotType) {
            case "iceshot":
                speed = 200;
                spritePath = "sprites/blueshot-removebg-preview.png";
                break;
            case "bombshot":
                speed = 250;
                // Use the cleaned bombshot image if available
                spritePath = "sprites/bombshot.png";
                break;
            default:
                speed = 180;
                spritePath = "sprites/shot-removebg-preview.png";
                break;
        }

        double vx = ddx * speed;
        double vy = ddy * speed;

        EnemyShotEntity shot = new EnemyShotEntity(game, spritePath, startX, startY, vx, vy, shotType, this);
        game.addEntity(shot);
    }

    // =====================================================
    // 🔹 기타 유틸 및 상태
    // =====================================================
    private void updateDirection() {
        currentDir = movingRight ? "r" : "l";
    }

    private void updateSpriteFrame() {
        String path = "sprites/" + currentSpriteBase + currentDir + ".png";
        this.sprite = SpriteStore.get().getSprite(path);
    }

    public void freeze(int duration) {
        frozen = true;
        freezeEndTime = System.currentTimeMillis() + duration;
        dx = 0;
        dy = 0;
    }

    public boolean takeDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof UserEntity) {
            UserEntity ship = (UserEntity) other;
            ship.takeDamage(10);
            game.removeEntity(this);
        }
        // 🏰 요새와 충돌 시 요새 피해 (방어막이 있으면 방어막이 먼저 충돌하여 처리됨)
        if (other instanceof FortressEntity) {
            FortressEntity fortress = (FortressEntity) other;
            fortress.damage(50); // 몬스터 충돌로 50 데미지
            game.removeEntity(this);
            System.out.println("💥 몬스터가 요새와 충돌! 요새 HP: " + fortress.getHP());
        }
    }

    @Override
    public void doLogic() {}

    @Override
    public void draw(java.awt.Graphics g) {
        if (sprite == null) return;
        double scale = 0.5;
        int newW = (int) (sprite.getWidth() * scale);
        int newH = (int) (sprite.getHeight() * scale);
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        java.awt.Image scaled = sprite.getImage().getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
        g2.drawImage(scaled, (int) x, (int) y, null);
    }

    // Getter / Setter
    public String getShotType() { return shotType; }
    public void setShotType(String type) { this.shotType = type; }
}
