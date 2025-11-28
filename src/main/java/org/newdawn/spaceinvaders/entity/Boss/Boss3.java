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
import org.newdawn.spaceinvaders.entity.UserEntity;

public class Boss3 extends BossEntity {

    /* ===========================================================
       기본 필드
       =========================================================== */
    private final Game game;

    private boolean enraged = false;

    private long lastHitTime = 0;
    private static final long HIT_COOLDOWN = 200;

    /* ===========================================================
       붕대 공격 (Wrap Attack)
       =========================================================== */
    private long lastWrapAttack = 0;
    private long wrapCooldown = 8000;
    private boolean usingWrap = false;
    private long wrapDuration = 2500;
    private long wrapEndTime = 0;

    private long lastWrapTick = 0;
    private long wrapTickInterval = 400;

    /* ===========================================================
       이동 관련
       =========================================================== */
    private final double baseY;
    private double verticalMoveRange = 30;
    private boolean movingRight = true;

    /* ===========================================================
       화면 흔들림
       =========================================================== */
    private boolean shaking = false;
    private long shakeStartTime = 0;
    private long shakeDuration = 2500;
    private double shakeIntensity = 8;

    /* ===========================================================
       스프라이트
       =========================================================== */
    private final List<Sprite> bandageSprites = new ArrayList<>();
    private Sprite spriteLeft;
    private Sprite spriteRight;

    /* ===========================================================
       일반 공격
       =========================================================== */
    private long lastShotTime = 0;
    private long shotInterval = 3000;

    public Boss3(Game game, int x, int y) {
        super(game, "sprites/mummyr.png", x, y);
        this.health = 1000;
        this.game = game;
        this.baseY = y;

        spriteLeft  = SpriteStore.get().getSprite("sprites/mummyl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/mummyr.png");
        sprite = spriteRight;

    // bandageSprites and flashSprite previously used a 'bug' visual; removed per design choice

    // 보스 등장 시 배경 변경 (desert.JPG)
    game.setBackground("bg/desert.JPG");
    }

    /* ===========================================================
       UPDATE / MOVE
       =========================================================== */
    @Override
    public void move(long delta) {
        updateFreeze();
        if (!frozen) {
            updateMovement(delta);
            checkEnrageState();
            processWrapAttack();
            processNormalShot();
        }
    }

    /* ===========================================================
       이동
       =========================================================== */
    private void updateMovement(long delta) {
        double oldX = x;

        x += Math.sin(System.currentTimeMillis() / 700.0) * 0.5 * delta;
        y = baseY + Math.sin(System.currentTimeMillis() / 1000.0) * verticalMoveRange;

        clampPosition();
        updateSpriteDirection(oldX);
    }

    private void clampPosition() {
        if (x < 60) x = 60;
        if (x > 680) x = 680;
    }

    private void updateSpriteDirection(double oldX) {
        movingRight = x > oldX;
        sprite = movingRight ? spriteRight : spriteLeft;
    }

    /* ===========================================================
       분노 상태
       =========================================================== */
    private void checkEnrageState() {
        if (!enraged && health <= 750) {
            enraged = true;
            wrapCooldown = 5000;
            System.out.println("💢 미라 분노 상태!");
        }
    }

    /* ===========================================================
       붕대 공격 처리
       =========================================================== */
    private void processWrapAttack() {
        long now = System.currentTimeMillis();

        if (!usingWrap && now - lastWrapAttack >= wrapCooldown) {
            startWrapAttack();
        }

        if (usingWrap) {
            updateWrapTick(now);
            if (now >= wrapEndTime) endWrapAttack();
        }
    }

    private void startWrapAttack() {
        usingWrap = true;
        shaking = true;

        lastWrapAttack = System.currentTimeMillis();
        shakeStartTime = lastWrapAttack;
        wrapEndTime = lastWrapAttack + wrapDuration;
        lastWrapTick = lastWrapAttack;

        System.out.println("🌀 미라 붕대 공격 발동!");
        dealWrapDamage(); // 첫 틱 즉시 데미지
    }

    private void updateWrapTick(long now) {
        if (now - lastWrapTick >= wrapTickInterval) {
            lastWrapTick = now;
            dealWrapDamage();
        }
    }

    private void endWrapAttack() {
        usingWrap = false;
        shaking = false;
    }

    private void dealWrapDamage() {
        if (game.getShip() != null) game.getShip().takeDamage(18);
        if (game.getFortress() != null) game.getFortress().damage(12);
    }

    /* ===========================================================
       일반 공격
       =========================================================== */
    private void processNormalShot() {
        long now = System.currentTimeMillis();
        updateShotInterval();

        if (usingWrap) return;

        if (now - lastShotTime >= shotInterval) {
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

    /* ===========================================================
       데미지 처리
       =========================================================== */
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (health > 0) {
            System.out.println("🧟 미라 피격! 남은 HP: " + health);
        }
    }

    private void die() {
        System.out.println("💀 미라 사망!");
        game.removeEntity(this);
        game.bossDefeated();
    }

    /* ===========================================================
       충돌 처리
       =========================================================== */
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;

        // 아이템 데미지 적용
        collidedWithItem(other);
    }

    /* ===========================================================
       DRAW
       =========================================================== */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform oldTransform = g2.getTransform();

        applyShakeEffect(g2);
        drawBossImage(g2);
        g2.setTransform(oldTransform);

        if (usingWrap) drawWrapEffect(g2);
        drawHpBar(g2);
    }

    private void applyShakeEffect(Graphics2D g2) {
        if (!shaking) return;

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        if (elapsed >= shakeDuration) return;

        int offsetX = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
        int offsetY = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
        g2.translate(offsetX, offsetY);
    }

    private void drawBossImage(Graphics2D g2) {
        Image img = sprite.getImage().getScaledInstance(
                (int)(sprite.getWidth() * 0.5),
                (int)(sprite.getHeight() * 0.5),
                Image.SCALE_SMOOTH
        );
        g2.drawImage(img, (int)x - 40, (int)y - 40, null);
    }

    private void drawWrapEffect(Graphics2D g2) {
        double t = (System.currentTimeMillis() % 300) / 300.0;
        int alpha = (int)(100 + 100 * Math.sin(t * Math.PI * 2));
        g2.setColor(new Color(255, 220, 150, alpha));

        g2.fillRect(0, 0, 800, 600);

        for (Sprite s : bandageSprites) {
            int lx = (int)(Math.random() * 750);
            int ly = (int)(Math.random() * 400);
            g2.drawImage(s.getImage(), lx, ly, s.getWidth() / 2, s.getHeight() / 2, null);
        }
    }

    private void drawHpBar(Graphics2D g2) {
        g2.setColor(Color.red);
        g2.fillRect((int)x - 50, (int)y - 70, 100, 6);

        g2.setColor(Color.green);
        int hpWidth = (int)(100 * (health / 1000.0));
        g2.fillRect((int)x - 50, (int)y - 70, hpWidth, 6);

        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.setColor(Color.white);
        g2.drawString(health + " / 1000", (int)x - 25, (int)y - 80);
    }

    @Override
    protected void fireShot() {
        // Normal shot
        int startX = getX() + sprite.getWidth() / 2;
        int startY = getY() + sprite.getHeight() / 2;
        UserEntity player = game.getShip();
        double targetX = startX;
        double targetY = startY;
        if (player != null) {
            targetX = player.getX() + player.getWidth() / 2.0;
            targetY = player.getY() + player.getHeight() / 2.0;
        }
        double vx = (targetX - startX) / 50;
        double vy = (targetY - startY) / 50;
        EnemyShotEntity shot = new EnemyShotEntity(game, "sprites/shot.gif", startX, startY, vx, vy, "shot", this);
        game.addEntity(shot);
    }
}
