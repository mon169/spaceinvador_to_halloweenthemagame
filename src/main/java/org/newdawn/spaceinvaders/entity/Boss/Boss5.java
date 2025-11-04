package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.EnemyShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;

/**
 *   Stage 5 Boss: 뱀파이어
 * - 박쥐 공격 + 화면 어둡게 만들어서 안보이게 한 뒤 기습공격
 * - 체력이 줄수록 공격 속도 증가
 * - 한글 폰트 정상 출력
 */
public class Boss5 extends MonsterEntity {
    private final Game game;
    private int health = 1; // 임시: 테스트용 (한 대 맞으면 죽음)
    private boolean enraged = false;

    // 박쥐 공격 + 암전 패턴 관련
    private long lastDarkAttack = 0;
    private long darkCooldown = 8000;
    private boolean usingDark = false;
    private long darkDuration = 2500;
    private long darkEndTime = 0;

    private long lastDarkTick = 0;
    private long darkTickInterval = 400;

    // 이동 관련
    private double baseY;
    private double verticalMoveRange = 30;
    private boolean movingRight = true;

    // 화면 흔들림
    private double shakeIntensity = 8;
    private boolean shaking = false;
    private long shakeStartTime = 0;
    private long shakeDuration = 2500;

    private final List<Sprite> batSprites = new ArrayList<>();
    private Sprite flashSprite;
    private Sprite spriteLeft;
    private Sprite spriteRight;

    private long lastHitTime = 0;
    private static final long HIT_COOLDOWN = 200;

    // 공격 빈도 제어용
    private long lastShotTime = 0;
    private long shotInterval = 3000;

    public Boss5(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.baseY = y;

        spriteLeft  = SpriteStore.get().getSprite("sprites/vampirel.png");
        spriteRight = SpriteStore.get().getSprite("sprites/vampirer.png");
        sprite = spriteRight;

        batSprites.add(SpriteStore.get().getSprite("sprites/bat1.png"));
        batSprites.add(SpriteStore.get().getSprite("sprites/bat2.png"));
        flashSprite = SpriteStore.get().getSprite("sprites/dark_flash.png");
    }

    @Override
    public void move(long delta) {
        double oldX = x;
        x += Math.sin(System.currentTimeMillis() / 600.0) * 0.6 * delta;
        y = baseY + Math.sin(System.currentTimeMillis() / 900.0) * verticalMoveRange;

        if (x < 60) x = 60;
        if (x > 680) x = 680;

        movingRight = x > oldX;
        sprite = movingRight ? spriteRight : spriteLeft;

        // 💢 체력 750 이하 시 분노 모드
        if (!enraged && health <= 750) {
            enraged = true;
            darkCooldown = 5000;
            System.out.println("💢 뱀파이어 분노 상태!");
        }

        long now = System.currentTimeMillis();

        // 🌑 어둠 공격 발동
        if (!usingDark && now - lastDarkAttack >= darkCooldown) {
            startDarkAttack();
        }

        // 🌑 공격 지속
        if (usingDark) {
            if (now - lastDarkTick >= darkTickInterval) {
                lastDarkTick = now;
                dealDarkDamage();
            }
            if (now >= darkEndTime) {
                usingDark = false;
                shaking = false;
            }
        }

        // 🔫 일반 공격
        updateShotInterval();
        if (!usingDark && now - lastShotTime >= shotInterval) {
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

    private void startDarkAttack() {
        usingDark = true;
        shaking = true;
        shakeStartTime = System.currentTimeMillis();

        lastDarkAttack = System.currentTimeMillis();
        darkEndTime = lastDarkAttack + darkDuration;
        lastDarkTick = lastDarkAttack;

        System.out.println("🌑 뱀파이어 어둠 공격 발동!");
        dealDarkDamage();
    }

    private void dealDarkDamage() {
        if (game.getShip() != null) {
            game.getShip().takeDamage(25);
        }
        if (game.getFortress() != null) {
            game.getFortress().damage(15);
        }
    }

    @Override
    public boolean takeDamage(int damage) {
        long now = System.currentTimeMillis();
        if (now - lastHitTime < HIT_COOLDOWN) return false;
        lastHitTime = now;

        health -= damage;
        System.out.println("🧛 뱀파이어 피격! 남은 HP: " + health);

        if (health <= 0) {
            System.out.println("💀 뱀파이어 사망!");
            game.removeEntity(this);
            game.bossDefeated();
            return true;
        }
        return false;
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform oldTransform = g2.getTransform();

        // 흔들림 효과
        if (shaking) {
            double elapsed = System.currentTimeMillis() - shakeStartTime;
            if (elapsed < shakeDuration) {
                int offsetX = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
                int offsetY = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
                g2.translate(offsetX, offsetY);
            }
        }

        // 보스 본체
        Image img = sprite.getImage().getScaledInstance(
                (int)(sprite.getWidth() * 0.5),
                (int)(sprite.getHeight() * 0.5),
                Image.SCALE_SMOOTH
        );
        g2.drawImage(img, (int)x - 40, (int)y - 40, null);
        g2.setTransform(oldTransform);

        // 🌑 어둠 이펙트 (화면 암전 + 박쥐)
        if (usingDark) {
            double t = (System.currentTimeMillis() % 300) / 300.0;
            int alpha = (int)(150 + 100 * Math.sin(t * Math.PI * 2));
            g2.setColor(new Color(0, 0, 0, Math.min(alpha, 220)));
            g2.fillRect(0, 0, 800, 600);

            for (Sprite s : batSprites) {
                int lx = (int)(Math.random() * 750);
                int ly = (int)(Math.random() * 400);
                int lw = s.getWidth() / 2;
                int lh = s.getHeight() / 2;
                g2.drawImage(s.getImage(), lx, ly, lw, lh, null);
            }
        }

        // ❤️ HP바
        g2.setColor(Color.red);
        g2.fillRect((int)x - 50, (int)y - 70, 100, 6);
        g2.setColor(Color.green);
        int hpWidth = (int)(100 * (health / 1000.0));
        g2.fillRect((int)x - 50, (int)y - 70, hpWidth, 6);

        // 🧠 한글 폰트 정상 표시
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.setColor(Color.white);
        g2.drawString(health + " / 1000", (int)x - 25, (int)y - 80);
    }
}
