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
 *   Stage 3 Boss: 미라
 * - 붕대공격 + 관으로 요새(바구니) 내리찍기 + HP 숫자
 * - 체력이 줄수록 공격 속도 증가
 * - 한글 폰트 정상 출력
 */
public class Boss3 extends MonsterEntity {
    private final Game game;
    private int health = 1; // 임시: 테스트용 (한 대 맞으면 죽음)
    private boolean enraged = false;

    // 붕대 휘감기 궁극기 관련
    private long lastWrapAttack = 0;
    private long wrapCooldown = 8000;
    private boolean usingWrap = false;
    private long wrapDuration = 2500;
    private long wrapEndTime = 0;

    private long lastWrapTick = 0;
    private long wrapTickInterval = 400;

    // 이동 관련
    private double baseY;
    private double verticalMoveRange = 30;
    private boolean movingRight = true;

    // 화면 흔들림
    private double shakeIntensity = 8;
    private boolean shaking = false;
    private long shakeStartTime = 0;
    private long shakeDuration = 2500;

    private final List<Sprite> bandageSprites = new ArrayList<>();
    private Sprite flashSprite;
    private Sprite spriteLeft;
    private Sprite spriteRight;

    private long lastHitTime = 0;
    private static final long HIT_COOLDOWN = 200;

    // 공격 빈도 제어용
    private long lastShotTime = 0;
    private long shotInterval = 3000;

    public Boss3(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.baseY = y;

        spriteLeft  = SpriteStore.get().getSprite("sprites/mummyl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/mummyr.png");
        sprite = spriteRight;

        bandageSprites.add(SpriteStore.get().getSprite("sprites/bandage1.png"));
        bandageSprites.add(SpriteStore.get().getSprite("sprites/bandage2.png"));
        bandageSprites.add(SpriteStore.get().getSprite("sprites/bandage3.png"));
        flashSprite = SpriteStore.get().getSprite("sprites/bandage_flash.png");
    }

    @Override
    public void move(long delta) {
        double oldX = x;
        x += Math.sin(System.currentTimeMillis() / 700.0) * 0.5 * delta;
        y = baseY + Math.sin(System.currentTimeMillis() / 1000.0) * verticalMoveRange;

        if (x < 60) x = 60;
        if (x > 680) x = 680;

        movingRight = x > oldX;
        sprite = movingRight ? spriteRight : spriteLeft;

        // 💢 체력 750 이하 시 분노 모드
        if (!enraged && health <= 750) {
            enraged = true;
            wrapCooldown = 5000;
            System.out.println("💢 미라 분노 상태!");
        }

        long now = System.currentTimeMillis();

        // 🌀 붕대 공격 발동
        if (!usingWrap && now - lastWrapAttack >= wrapCooldown) {
            startWrapAttack();
        }

        // 🌀 공격 지속
        if (usingWrap) {
            if (now - lastWrapTick >= wrapTickInterval) {
                lastWrapTick = now;
                dealWrapDamage();
            }
            if (now >= wrapEndTime) {
                usingWrap = false;
                shaking = false;
            }
        }

        // 🔫 일반 공격 (HP에 따라 빈도 가변)
        updateShotInterval();
        if (!usingWrap && now - lastShotTime >= shotInterval) {
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

    private void startWrapAttack() {
        usingWrap = true;
        shaking = true;
        shakeStartTime = System.currentTimeMillis();

        lastWrapAttack = System.currentTimeMillis();
        wrapEndTime = lastWrapAttack + wrapDuration;
        lastWrapTick = lastWrapAttack;

        System.out.println("🌀 미라 붕대 공격 발동!");
        dealWrapDamage();
    }

    private void dealWrapDamage() {
        if (game.getShip() != null) {
            game.getShip().takeDamage(18);
        }
        if (game.getFortress() != null) {
            game.getFortress().damage(12);
        }
    }

    @Override
    public boolean takeDamage(int damage) {
        long now = System.currentTimeMillis();
        if (now - lastHitTime < HIT_COOLDOWN) return false;
        lastHitTime = now;

        health -= damage;
        System.out.println("🧟 미라 피격! 남은 HP: " + health);

        if (health <= 0) {
            System.out.println("💀 미라 사망!");
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

        // 붕대 이펙트
        if (usingWrap) {
            double t = (System.currentTimeMillis() % 300) / 300.0;
            int alpha = (int)(100 + 100 * Math.sin(t * Math.PI * 2));
            g2.setColor(new Color(255, 220, 150, alpha));
            g2.fillRect(0, 0, 800, 600);

            for (Sprite s : bandageSprites) {
                int lx = (int)(Math.random() * 750);
                int ly = (int)(Math.random() * 400);
                int lw = s.getWidth() / 2;
                int lh = s.getHeight() / 2;
                g2.drawImage(s.getImage(), lx, ly, lw, lh, null);
            }
        }

        // HP바
        g2.setColor(Color.red);
        g2.fillRect((int)x - 50, (int)y - 70, 100, 6);
        g2.setColor(Color.green);
        int hpWidth = (int)(100 * (health / 1000.0));
        g2.fillRect((int)x - 50, (int)y - 70, hpWidth, 6);

        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.setColor(Color.white);
        g2.drawString(health + " / 1000", (int)x - 25, (int)y - 80);
    }
}
