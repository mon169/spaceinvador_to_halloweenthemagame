package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.EnemyShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;

public class Boss5 extends BossEntity {

    /* =============================
     *        기본 상태
     * ============================= */
    private final Game game;
    // use inherited health from BossEntity
    private boolean enraged = false;

    /* =============================
     *        암전 패턴 상태
     * ============================= */
    private long lastDarkAttack = 0;
    private long darkCooldown = 8000;
    private boolean usingDark = false;
    private long darkDuration = 2500;
    private long darkEndTime = 0;

    private long lastDarkTick = 0;
    private long darkTickInterval = 400;

    /* =============================
     *        이동 관련
     * ============================= */
    private double baseY;
    private double verticalMoveRange = 30;
    private boolean movingRight = true;

    /* =============================
     *        화면 흔들림
     * ============================= */
    private double shakeIntensity = 8;
    private boolean shaking = false;
    private long shakeStartTime = 0;
    private long shakeDuration = 2500;

    /* =============================
     *        스프라이트/이미지
     * ============================= */
    private final List<Sprite> batSprites = new ArrayList<>();
    private Sprite spriteLeft;
    private Sprite spriteRight;

    /* =============================
     *        총알 공격
     * ============================= */
    private long lastShotTime = 0;
    private long shotInterval = 3000;

    public Boss5(Game game, int x, int y) {
        super(game, "sprites/vampirer.png", x, y);
        this.game = game;
        this.baseY = y;
        this.health = 1000; // 보스 체력 설정 (부모 필드)

        spriteLeft = SpriteStore.get().getSprite("sprites/vampirel.png");
        spriteRight = SpriteStore.get().getSprite("sprites/vampirer.png");
        sprite = spriteRight;

        batSprites.add(SpriteStore.get().getSprite("sprites/bat.png"));
        batSprites.add(SpriteStore.get().getSprite("sprites/bat.png"));

        // 보스 등장 시 배경 변경 (bossbg.jpg)
        game.setBackground("bg/bossbg.jpg");
    }

    /* ===============================================
     *                    MOVE
     * =============================================== */
    @Override
    public void move(long delta) {
        updateFreeze();
        if (frozen) return;

        long now = System.currentTimeMillis();
        double oldX = x;

        updatePosition(delta);
        clampPosition();
        updateSpriteDirection(oldX);

        checkEnrage();

        processDarkAttack(now);
        tryStartDarkAttack(now);

        updateShotInterval();
        tryNormalShot(now);
    }

    private void updatePosition(long delta) {
        x += Math.sin(System.currentTimeMillis() / 600.0) * 0.6 * delta;
        y = baseY + Math.sin(System.currentTimeMillis() / 900.0) * verticalMoveRange;
    }

    private void clampPosition() {
        if (x < 60) x = 60;
        if (x > 680) x = 680;
    }

    private void updateSpriteDirection(double oldX) {
        movingRight = x > oldX;
        sprite = movingRight ? spriteRight : spriteLeft;
    }

    private void checkEnrage() {
        if (!enraged && health <= 750) {
            enraged = true;
            darkCooldown = 5000;
            System.out.println("💢 뱀파이어 분노 상태!");
        }
    }

    /* ===============================================
     *                DARK ATTACK
     * =============================================== */
    private void tryStartDarkAttack(long now) {
        if (!usingDark && now - lastDarkAttack >= darkCooldown) {
            startDarkAttack();
        }
    }

    private void startDarkAttack() {
        usingDark = true;
        shaking = true;

        shakeStartTime = System.currentTimeMillis();
        lastDarkAttack = shakeStartTime;
        darkEndTime = shakeStartTime + darkDuration;
        lastDarkTick = shakeStartTime;

        System.out.println("🌑 뱀파이어 어둠 공격 발동!");
        dealDarkDamage();
    }

    private void processDarkAttack(long now) {
        if (!usingDark) return;

        if (now - lastDarkTick >= darkTickInterval) {
            lastDarkTick = now;
            dealDarkDamage();
        }

        if (now >= darkEndTime) {
            usingDark = false;
            shaking = false;
        }
    }

    private void dealDarkDamage() {
        if (game.getShip() != null) {
            game.getShip().takeDamage(25);
        }
        if (game.getFortress() != null) {
            game.getFortress().damage(15);
        }
    }

    /* ===============================================
     *                NORMAL ATTACK
     * =============================================== */
    private void tryNormalShot(long now) {
        if (usingDark) return;

        if (now - lastShotTime >= shotInterval) {
            lastShotTime = now;
            // fireShot(); // 제거: shot 발사 안 함
        }
    }

    private void updateShotInterval() {
        if (health > 1000) shotInterval = 3000;
        else if (health > 700) shotInterval = 2000;
        else if (health > 400) shotInterval = 1200;
        else shotInterval = 800;
    }

    /* ===============================================
     *                DAMAGE HANDLING
     * =============================================== */
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        System.out.println("🧛 뱀파이어 피격! 남은 HP: " + health);
    }

    public void collidedWith(Entity other) {
        if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;

        // 아이템 데미지 적용
        collidedWithItem(other);
    }

    /* ===============================================
     *                     DRAW
     * =============================================== */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform old = g2.getTransform();

        drawShake(g2);
        drawBody(g2);
        g2.setTransform(old);

        if (usingDark) drawDarkEffect(g2);
        drawHP(g2);
        drawHPText(g2);
    }

    private void drawShake(Graphics2D g2) {
        if (!shaking) return;

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        if (elapsed < shakeDuration) {
            int ox = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
            int oy = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
            g2.translate(ox, oy);
        }
    }

    private void drawBody(Graphics2D g2) {
        Image img = sprite.getImage().getScaledInstance(
            (int)(sprite.getWidth() * 0.5),
            (int)(sprite.getHeight() * 0.5),
            Image.SCALE_SMOOTH);
        g2.drawImage(img, (int)x - 40, (int)y - 40, null);
    }

    private void drawDarkEffect(Graphics2D g2) {
        double t = (System.currentTimeMillis() % 300) / 300.0;
        int alpha = (int)(150 + 100 * Math.sin(t * Math.PI * 2));
        alpha = Math.min(230, alpha);

        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, 800, 600);

        if (game.getShip() != null) {
            int sx = (int) game.getShip().getX();
            int sy = (int) game.getShip().getY();
            int radius = 180;

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.DstOut);
            g2.fillOval(sx - radius, sy - radius, radius * 2, radius * 2);
            g2.setComposite(old);
        }

        for (Sprite s : batSprites) {
            int lx = (int)(Math.random() * 750);
            int ly = (int)(Math.random() * 400);
            g2.drawImage(s.getImage(), lx, ly, s.getWidth() / 2, s.getHeight() / 2, null);
        }
    }

    private void drawHP(Graphics2D g2) {
        g2.setColor(Color.red);
        g2.fillRect((int)x - 50, (int)y - 70, 100, 6);

        g2.setColor(Color.green);
        int hpWidth = (int)(100 * (health / 1000.0));
        g2.fillRect((int)x - 50, (int)y - 70, hpWidth, 6);
    }

    private void drawHPText(Graphics2D g2) {
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.setColor(Color.white);
        g2.drawString(health + " / 1000", (int)x - 25, (int)y - 80);
    }
}
