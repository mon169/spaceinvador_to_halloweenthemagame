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

public class Boss4 extends MonsterEntity {

    /* ================================
     * 기본 상태 관련
     * ================================ */
    private final Game game;
    private int health = 1000;
    private boolean enraged = false;

    private static final int MAX_Y_BOUNDARY = 370;
    private static final double VERTICAL_RANGE = 30;

    /* ================================
     * 공격 관련 (던지기)
     * ================================ */
    private boolean usingThrow = false;
    private long lastThrowAttack = 0;
    private long throwCooldown = 8000;
    private static final long THROW_DURATION = 2500;
    private long throwEndTime = 0;

    private long lastThrowTick = 0;
    private static final long THROW_TICK_INTERVAL = 400;

    /* ================================
     * 일반 공격
     * ================================ */
    private long lastShotTime = 0;
    private long shotInterval = 3000;

    /* ================================
     * 이동/방향 관련
     * ================================ */
    private double baseY;
    private boolean movingRight = true;

    /* ================================
     * 흔들림 효과
     * ================================ */
    private boolean shaking = false;
    private long shakeStartTime = 0;
    private static final long SHAKE_DURATION = 2500;
    private static final double SHAKE_INTENSITY = 8;

    /* ================================
     * 스프라이트/이펙트
     * ================================ */
    private Sprite spriteLeft;
    private Sprite spriteRight;
    private final List<Sprite> limbSprites = new ArrayList<>();

    /* ================================
     * 피격 쿨다운
     * ================================ */
    private long lastHitTime = 0;
    private static final long HIT_COOLDOWN = 200;


    public Boss4(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.baseY = y;

        spriteLeft = SpriteStore.get().getSprite("sprites/zombiel.png");
        spriteRight = SpriteStore.get().getSprite("sprites/zombier.png");
        sprite = spriteRight;

        limbSprites.add(SpriteStore.get().getSprite("sprites/arm1.png"));
        limbSprites.add(SpriteStore.get().getSprite("sprites/leg1.png"));
        limbSprites.add(SpriteStore.get().getSprite("sprites/heart1.png"));
    }

    /* ==================================================
     * 메인 업데이트(move)
     * ================================================== */
    @Override
    public void move(long delta) {
        double prevX = x;

        updatePosition(delta);
        limitBoundary();
        updateSpriteDirection(prevX);
        handleRageMode();

        long now = System.currentTimeMillis();

        handleThrowAttack(now);
        handleNormalAttack(now);
    }


    /* -----------------------------
     * 이동 처리
     * ----------------------------- */
    private void updatePosition(long delta) {
        x += Math.sin(System.currentTimeMillis() / 750.0) * 0.5 * delta;
        y = baseY + Math.sin(System.currentTimeMillis() / 1000.0) * VERTICAL_RANGE;
    }

    private void limitBoundary() {
        if (x < 60) x = 60;
        if (x > 680) x = 680;
        if (y > MAX_Y_BOUNDARY) y = MAX_Y_BOUNDARY;
    }

    private void updateSpriteDirection(double oldX) {
        movingRight = x > oldX;
        sprite = movingRight ? spriteRight : spriteLeft;
    }


    /* -----------------------------
     * 분노 모드
     * ----------------------------- */
    private void handleRageMode() {
        if (!enraged && health <= 750) {
            enraged = true;
            throwCooldown = 5000;
            System.out.println("💢 좀비 분노 상태!");
        }
    }


    /* ==================================================
     * 던지기 공격
     * ================================================== */
    private void handleThrowAttack(long now) {
        if (!usingThrow && now - lastThrowAttack >= throwCooldown) {
            startThrowAttack(now);
        }

        if (usingThrow) {
            updateThrowDamage(now);
            if (now >= throwEndTime) {
                endThrowAttack();
            }
        }
    }

    private void startThrowAttack(long now) {
        usingThrow = true;
        shaking = true;

        lastThrowAttack = now;
        throwEndTime = now + THROW_DURATION;
        lastThrowTick = now;

        System.out.println("🧠 좀비 신체 던지기 발동!");
        applyThrowDamage();
    }

    private void updateThrowDamage(long now) {
        if (now - lastThrowTick >= THROW_TICK_INTERVAL) {
            lastThrowTick = now;
            applyThrowDamage();
        }
    }

    private void endThrowAttack() {
        usingThrow = false;
        shaking = false;
    }

    private void applyThrowDamage() {
        if (game.getShip() != null) game.getShip().takeDamage(20);
        if (game.getFortress() != null) game.getFortress().damage(12);
    }


    /* ==================================================
     * 일반 공격 (총알)
     * ================================================== */
    private void handleNormalAttack(long now) {
        updateShotInterval();
        if (!usingThrow && now - lastShotTime >= shotInterval) {
            lastShotTime = now;
            fireShot();
        }
    }

    private void updateShotInterval() {
        if (health > 1000) shotInterval = 3000;
        else if (health > 700) shotInterval = 2000;
        else if (health > 400) shotInterval = 1200;
        else shotInterval = 800;
    }


    /* ==================================================
     * 피격(피해)
     * ================================================== */
    @Override
    public boolean takeDamage(int damage) {
        long now = System.currentTimeMillis();
        if (now - lastHitTime < HIT_COOLDOWN) return false;
        lastHitTime = now;

        health -= damage;
        System.out.println("🧟 좀비 피격! 남은 HP: " + health);

        if (health <= 0) {
            die();
            return true;
        }
        return false;
    }

    private void die() {
        System.out.println("💀 좀비 사망!");
        game.removeEntity(this);
        game.bossDefeated();
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;
    }


    /* ==================================================
     * Draw
     * ================================================== */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform old = g2.getTransform();

        applyShakeEffect(g2);
        drawBody(g2);
        g2.setTransform(old);

        if (usingThrow) drawThrowEffect(g2);

        drawHpBar(g2);
    }

    private void applyShakeEffect(Graphics2D g2) {
        if (!shaking) return;

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        if (elapsed < SHAKE_DURATION) {
            int dx = (int)(Math.random() * SHAKE_INTENSITY - SHAKE_INTENSITY / 2);
            int dy = (int)(Math.random() * SHAKE_INTENSITY - SHAKE_INTENSITY / 2);
            g2.translate(dx, dy);
        }
    }

    private void drawBody(Graphics2D g2) {
        Image img = sprite.getImage().getScaledInstance(
                (int)(sprite.getWidth() * 0.5),
                (int)(sprite.getHeight() * 0.5),
                Image.SCALE_SMOOTH);

        g2.drawImage(img, (int)x - 40, (int)y - 40, null);
    }

    private void drawThrowEffect(Graphics2D g2) {
        double t = (System.currentTimeMillis() % 300) / 300.0;
        int alpha = (int)(120 + 100 * Math.sin(t * Math.PI * 2));

        g2.setColor(new Color(180, 255, 180, alpha));
        g2.fillRect(0, 0, 800, 600);

        for (Sprite s : limbSprites) {
            int lx = (int)(Math.random() * 750);
            int ly = (int)(Math.random() * 400);
            g2.drawImage(s.getImage(), lx, ly, s.getWidth() / 2, s.getHeight() / 2, null);
        }
    }

    private void drawHpBar(Graphics2D g2) {
        int barX = (int)x - 50;
        int barY = (int)y - 70;

        g2.setColor(Color.red);
        g2.fillRect(barX, barY, 100, 6);

        g2.setColor(Color.green);
        int hpW = (int)(100 * (health / 1000.0));
        g2.fillRect(barX, barY, hpW, 6);

        g2.setColor(Color.white);
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.drawString(health + " / 1000", (int)x - 25, (int)y - 80);
    }
}
