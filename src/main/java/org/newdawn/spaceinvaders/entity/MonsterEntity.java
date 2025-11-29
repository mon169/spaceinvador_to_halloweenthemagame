package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import java.util.Random;

public class MonsterEntity extends Entity {

    // =====================================================
    // Constants
    // =====================================================
    private static final int BOUNDARY_LEFT = 10;
    private static final int BOUNDARY_RIGHT = 760;
    private static final int BOUNDARY_TOP = 40;
    private static final int BOUNDARY_BOTTOM = 520;
    
    private static final int STAGE1_SPEED = 40;
    private static final int STAGE2_SPEED = 50;
    private static final int STAGE3_SPEED = 60;
    private static final int STAGE4_SPEED = 70;
    
    private static final int DIRECTION_CHANGE_BASE = 1000;
    private static final int DIRECTION_CHANGE_RANGE = 2000;
    
    private static final int ATTACK_BASE_DELAY = 1500;
    private static final int ATTACK_RANGE_DELAY = 2000;
    private static final int INITIAL_ATTACK_DELAY = 2000;
    private static final double ATTACK_PROBABILITY = 0.6;
    
    private static final int FORTRESS_CENTER_OFFSET = 40;
    
    private static final int ICESHOT_SPEED = 200;
    private static final int BOMBSHOT_SPEED = 250;
    private static final int NORMAL_SHOT_SPEED = 180;
    
    private static final double DRAW_SCALE = 0.5;
    private static final int COLLISION_DAMAGE = 10;
    
    private static final int STAGE_WITH_OBSTACLES = 4;
    private static final int STAGE_WITH_OBSTACLES_2 = 5;

    // =====================================================
    // Fields
    // =====================================================
    protected final Game game;
    private final Random random = new Random();

    protected double moveSpeed;
    private int health = 1;

    private boolean frozen = false;
    private long freezeEndTime = 0;

    private boolean movingRight = random.nextBoolean();
    private boolean movingDown = random.nextBoolean();
    private long lastMoveChange = 0;
    private long moveChangeDelay = randomDelay(DIRECTION_CHANGE_BASE, DIRECTION_CHANGE_RANGE);

    private long lastFrameChange = 0;
    private static final int FRAME_DELAY = 120;
    protected static final int MAX_FRAMES = 7;

    private String currentDir = "r";
    private String currentSpriteBase;

    private long lastAttackTime;
    private long attackDelay;

    protected String shotType = "shot";

    // =====================================================
    // Constructors
    // =====================================================
    public MonsterEntity(Game game, int x, int y) {
        super("sprites/monster1r.png", x, y);
        this.game = game;
        initMonsterCommon();
        initRandomSprite();
        initRandomMovement();
    }

    public MonsterEntity(Game game, String spritePath, int x, int y) {
        super(spritePath, x, y);
        this.game = game;
        initMonsterCommon();
        this.currentSpriteBase = spritePath;
        updateVelocityFromDirection();
    }

    // =====================================================
    // Initialization Helpers
    // =====================================================
    private void initMonsterCommon() {
        setSpeedByStage();
        lastAttackTime = System.currentTimeMillis() + random.nextInt(INITIAL_ATTACK_DELAY);
        attackDelay = randomDelay(ATTACK_BASE_DELAY, ATTACK_RANGE_DELAY);
    }

    private void initRandomSprite() {
        int monsterId = 1 + random.nextInt(3);
        this.currentSpriteBase = "monster" + monsterId;
        updateSprite();
    }

    private void initRandomMovement() {
        updateVelocityFromDirection();
    }

    private void setSpeedByStage() {
        int stage = 1;
        try {
            if (game != null) stage = game.getCurrentStage();
        } catch (Exception ignored) {}

        this.moveSpeed =
                (stage == 1) ? STAGE1_SPEED :
                (stage == 2) ? STAGE2_SPEED :
                (stage == 3) ? STAGE3_SPEED : STAGE4_SPEED;
    }

    private static int randomDelay(int base, int range) {
        return base + (int)(Math.random() * range);
    }

    // =====================================================
    // Movement
    // =====================================================
    @Override
    public void move(long delta) {
        long now = System.currentTimeMillis();
        updateFreeze(now);

        if (!frozen) {
            autoAdjustForStage4();
            preventObstaclePenetration(delta);
            super.move(delta);
        }

        handleBoundaryBounce();
        randomDirectionChange(now);
        updateAnimationFrame(now);
        processAttack(now);
    }

    private void updateFreeze(long now) {
        if (frozen && now > freezeEndTime) {
            frozen = false;
            updateVelocityFromDirection();
        }
    }
    
    private void updateVelocityFromDirection() {
        dx = movingRight ? moveSpeed : -moveSpeed;
        dy = movingDown ? moveSpeed : -moveSpeed;
    }

    private boolean isStageWithObstacles() {
        if (game == null) return false;
        int st = game.getCurrentStage();
        return st == STAGE_WITH_OBSTACLES || st == STAGE_WITH_OBSTACLES_2;
    }
    
    private void autoAdjustForStage4() {
        try {
            if (!isStageWithObstacles()) return;

            boolean belowHasObstacle = anyObstacleBelow();
            if (!belowHasObstacle) {
                movingDown = true;
                dy = moveSpeed;
            }
        } catch (Exception ignore) {}
    }

    private boolean anyObstacleBelow() {
        int myW = getWidth();
        int myH = getHeight();

        for (Entity e : game.getEntities()) {
            if (e instanceof ObstacleEntity) {
                if (overlapX(e.getX(), e.getWidth(), this.x, myW)
                    && e.getY() >= y + myH)
                    return true;
            }
        }
        return false;
    }

    private void preventObstaclePenetration(long delta) {
        try {
            if (!isStageWithObstacles()) return;

            double nextX = x + (delta * dx) / 1000.0;
            double nextY = y + (delta * dy) / 1000.0;

            int myW = getWidth();
            int myH = getHeight();

            for (Entity e : game.getEntities()) {
                if (e instanceof ObstacleEntity) {
                    int ox = e.getX();
                    int oy = e.getY();
                    int ow = e.getWidth();

                    boolean horizOverlap = overlapX(nextX, myW, ox, ow);
                    boolean currentlyAbove = (y + myH) <= oy;
                    boolean wouldPenetrate = (nextY + myH) > oy;

                    if (horizOverlap && currentlyAbove && wouldPenetrate && dy > 0) {
                        y = oy - myH - 1;
                        movingDown = false;
                        dy = -moveSpeed;
                        updateDirection();
                        return;
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    private boolean overlapX(double ax, int aw, double bx, int bw) {
        return (ax < bx + bw) && (ax + aw > bx);
    }

    private void handleBoundaryBounce() {
        if (x <= BOUNDARY_LEFT) {
            movingRight = true;
            dx = moveSpeed;
            updateDirection();
        } else if (x >= BOUNDARY_RIGHT) {
            movingRight = false;
            dx = -moveSpeed;
            updateDirection();
        }

        if (y <= BOUNDARY_TOP) {
            movingDown = true;
            dy = moveSpeed;
        } else if (y >= BOUNDARY_BOTTOM) {
            movingDown = false;
            dy = -moveSpeed;
        }
    }

    private void randomDirectionChange(long now) {
        if (now - lastMoveChange > moveChangeDelay) {
            lastMoveChange = now;
            moveChangeDelay = randomDelay(DIRECTION_CHANGE_BASE, DIRECTION_CHANGE_RANGE);

            if (random.nextBoolean()) movingRight = !movingRight;
            if (random.nextBoolean()) movingDown = !movingDown;

            updateVelocityFromDirection();
            updateDirection();
        }
    }

    private void updateAnimationFrame(long now) {
        if (now - lastFrameChange > FRAME_DELAY) {
            lastFrameChange = now;
            updateSprite();
        }
    }

    private void updateSprite() {
        this.sprite = SpriteStore.get().getSprite("sprites/" + currentSpriteBase + currentDir + ".png");
    }

    // =====================================================
    // Attack
    // =====================================================
    private void processAttack(long now) {
        if (now - lastAttackTime > attackDelay) {
            lastAttackTime = now;
            attackDelay = randomDelay(ATTACK_BASE_DELAY, ATTACK_RANGE_DELAY);
            if (random.nextDouble() < ATTACK_PROBABILITY) fireShot();
        }
    }

    public void fireShot() {
        if (game == null) return;

        int startX = getX() + sprite.getWidth() / 2;
        int startY = getY() + sprite.getHeight() / 2;

        double[] target = resolveTarget(startX, startY);
        double targetX = target[0], targetY = target[1];

        double[] vel = computeShotVelocity(startX, startY, targetX, targetY);
        double vx = vel[0], vy = vel[1];

        String spritePath = resolveShotSprite();
        EnemyShotEntity shot = new EnemyShotEntity(game, spritePath, startX, startY, vx, vy, shotType, this);
        game.addEntity(shot);
    }

    private double[] resolveTarget(int startX, int startY) {
        UserEntity player = game.getShip();
        FortressEntity fort = game.getFortress();

        double targetX = startX;
        double targetY = startY;

        if (player != null) {
            targetX = player.getX() + player.getWidth() / 2.0;
            targetY = player.getY() + player.getHeight() / 2.0;
        }

        if (fort != null) {
            double fx = fort.getX() + FORTRESS_CENTER_OFFSET;
            double fy = fort.getY() + FORTRESS_CENTER_OFFSET;

            double dPlayer = (player != null)
                    ? Math.hypot(targetX - startX, targetY - startY)
                    : Double.MAX_VALUE;

            double dFort = Math.hypot(fx - startX, fy - startY);

            if (dFort < dPlayer) {
                targetX = fx;
                targetY = fy;
            }
        }

        return new double[]{ targetX, targetY };
    }

    private double[] computeShotVelocity(double sx, double sy, double tx, double ty) {
        double dx = tx - sx;
        double dy = ty - sy;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return new double[]{ 0, 0 };

        dx /= len;
        dy /= len;

        double speed =
            ("iceshot".equals(shotType)) ? ICESHOT_SPEED :
            ("bombshot".equals(shotType)) ? BOMBSHOT_SPEED : NORMAL_SHOT_SPEED;

        return new double[]{ dx * speed, dy * speed };
    }

    private String resolveShotSprite() {
        switch (shotType) {
            case "iceshot": return "sprites/blueshot-removebg-preview.png";
            case "bombshot": return "sprites/bombshot.png";
            default: return "sprites/shot-removebg-preview.png";
        }
    }

    // =====================================================
    // Other methods
    // =====================================================
    private void updateDirection() {
        currentDir = movingRight ? "r" : "l";
        updateSprite();
    }

    public void freeze(int duration) {
        frozen = true;
        freezeEndTime = System.currentTimeMillis() + duration;
        dx = dy = 0;
    }

    public boolean takeDamage(int dmg) {
        health -= dmg;
        return health <= 0;
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof UserEntity) {
            ((UserEntity) other).takeDamage(COLLISION_DAMAGE);
            game.removeEntity(this);
        }
    }

    @Override
    public void draw(java.awt.Graphics g) {
        if (sprite == null) return;
        int w = (int)(sprite.getWidth() * DRAW_SCALE);
        int h = (int)(sprite.getHeight() * DRAW_SCALE);

        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        java.awt.Image scaled = sprite.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
        g2.drawImage(scaled, (int)x, (int)y, null);
    }

    // Getters / Setters
    public String getShotType() { return shotType; }
    public void setShotType(String type) { this.shotType = type; }
}