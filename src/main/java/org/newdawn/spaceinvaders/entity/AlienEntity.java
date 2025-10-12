package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import java.util.Random;

public class AlienEntity extends Entity {
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
    private static final int MAX_FRAMES = 7;
    private String currentDir = "r";
    private String currentSpriteBase;

    private long lastAttackTime;
    private long attackDelay;
    protected String shotType = "shot"; // 기본

    // ✅ 기존 기본 생성자
    public AlienEntity(Game game, int x, int y) {
        super("sprites/monster1r.png", x, y);
        this.game = game;

        int stage = 1;
        try { stage = game.getCurrentStage(); } catch (Exception ignored) {}
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

    // ✅ 새 생성자 (보스용) : Game, String, int, int
    public AlienEntity(Game game, String spritePath, int x, int y) {
        super(spritePath, x, y);
        this.game = game;

        int stage = 1;
        try { stage = game.getCurrentStage(); } catch (Exception ignored) {}
        if (stage == 1) moveSpeed = 40;
        else if (stage == 2) moveSpeed = 60;
        else if (stage == 3) moveSpeed = 80;
        else moveSpeed = 100;

        lastAttackTime = System.currentTimeMillis() + random.nextInt(2000);
        attackDelay = 1500 + random.nextInt(2000);

        this.currentSpriteBase = spritePath; // 보스는 직접 지정된 스프라이트 사용
        this.sprite = SpriteStore.get().getSprite(spritePath);

        dx = movingRight ? moveSpeed : -moveSpeed;
        dy = movingDown ? moveSpeed : -moveSpeed;
    }

    public String getShotType() { return shotType; }
    public void setShotType(String type) { this.shotType = type; }

    @Override
    public void move(long delta) {
        if (frozen && System.currentTimeMillis() > freezeEndTime) {
            frozen = false;
            dx = movingRight ? moveSpeed : -moveSpeed;
            dy = movingDown ? moveSpeed : -moveSpeed;
        }
        if (!frozen) super.move(delta);

        if (x <= 10) { movingRight = true; dx = moveSpeed; updateDirection(); }
        else if (x >= 760) { movingRight = false; dx = -moveSpeed; updateDirection(); }
        if (y <= 40) { movingDown = true; dy = moveSpeed; }
        else if (y >= 520) { movingDown = false; dy = -moveSpeed; }

        long now = System.currentTimeMillis();
        if (now - lastMoveChange > moveChangeDelay) {
            lastMoveChange = now;
            moveChangeDelay = 1000 + random.nextInt(2000);
            if (random.nextBoolean()) movingRight = !movingRight;
            if (random.nextBoolean()) movingDown = !movingDown;
            dx = movingRight ? moveSpeed : -moveSpeed;
            dy = movingDown ? moveSpeed : -moveSpeed;
            updateDirection();
        }

        if (now - lastFrameChange > FRAME_DELAY) {
            lastFrameChange = now;
            updateSpriteFrame();
        }

        if (canAttack && now - lastAttackTime > attackDelay) {
            lastAttackTime = now;
            attackDelay = 1500 + random.nextInt(2000);
            if (random.nextDouble() < 0.6) {
                fireShot();
            }
        }
    }

    private void updateDirection() { currentDir = movingRight ? "r" : "l"; }

    private void updateSpriteFrame() {
        String path = "sprites/" + currentSpriteBase + currentDir + ".png";
        this.sprite = SpriteStore.get().getSprite(path);
    }

    /** 🎯 발사 로직 (owner 설정 포함) */
    public void fireShot() {
        int startX = getX() + sprite.getWidth() / 2;
        int startY = getY() + sprite.getHeight() / 2;

        ShipEntity player = game.getShip();
        FortressEntity fortress = game.getFortress();

        double targetX = player.getX() + player.getWidth() / 2.0;
        double targetY = player.getY() + player.getHeight() / 2.0;

        double playerDist = Math.hypot(targetX - startX, targetY - startY);
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
        ddx /= len; ddy /= len;

        double speed;
        String spritePath;
        if ("iceshot".equals(shotType)) {
            speed = 200;
            spritePath = "sprites/blueshot-removebg-preview.png";
        } else if ("bombshot".equals(shotType)) {
            speed = 250;
            spritePath = "sprites/bombshot-removebg-preview.png";
        } else {
            speed = 180;
            spritePath = "sprites/shot-removebg-preview.png";
        }

        double vx = ddx * speed;
        double vy = ddy * speed;

        // ✅ owner(this) 전달해서 자기탄 무시 가능하게
        EnemyShotEntity shot = new EnemyShotEntity(game, spritePath, startX, startY, vx, vy, shotType, this);
        game.addEntity(shot);
    }

    public void freeze(int duration) {
        frozen = true;
        freezeEndTime = System.currentTimeMillis() + duration;
        dx = 0; dy = 0;
    }

    public boolean takeDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            ship.takeDamage(10);
            game.removeEntity(this);
        }
    }

    @Override
    public void doLogic() { }

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
}
