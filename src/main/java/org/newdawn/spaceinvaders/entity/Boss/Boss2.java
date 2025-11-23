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
 *   Stage 2 Boss: 마녀
 * - 물약 폭탄 + 지그재그로 오는 공격 + HP 숫자 
 * - 체력이 줄수록 공격 속도 증가
 * - 한글 폰트 정상 출력
 */
public class Boss2 extends MonsterEntity {
    private final Game game;
    private int health = 10;
    private boolean enraged = false;

    // 궁극기 관련 (물약 폭탄 패턴)
    private long lastPotionAttack = 0;
    private long potionCooldown = 8000;
    private boolean usingPotion = false;
    private long potionDuration = 2500;
    private long potionEndTime = 0;

    private long lastPotionTick = 0;
    private long potionTickInterval = 400;

    // 이동 관련
    private double baseY;
    private double verticalMoveRange = 30;
    private boolean movingRight = true;

    // 화면 흔들림
    private double shakeIntensity = 8;
    private boolean shaking = false;
    private long shakeStartTime = 0;
    private long shakeDuration = 2500;

    private final List<Sprite> potionSprites = new ArrayList<>();
    private Sprite spriteLeft;
    private Sprite spriteRight;

    private long lastHitTime = 0;
    private static final long HIT_COOLDOWN = 200;

    // 공격 빈도 제어용
    private long lastShotTime = 0;
    private long shotInterval = 3000; // 기본 3초 간격

    public Boss2(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.baseY = y;

        spriteLeft  = SpriteStore.get().getSprite("sprites/witchl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/witchr.png");
        sprite = spriteRight;

        potionSprites.add(SpriteStore.get().getSprite("sprites/poisonpotion.png"));
        potionSprites.add(SpriteStore.get().getSprite("sprites/poisonpotion.png"));
        potionSprites.add(SpriteStore.get().getSprite("sprites/poisonpotion.png"));
    }

    @Override
    public void move(long delta) {
        double oldX = x;
        x += Math.sin(System.currentTimeMillis() / 600.0) * 0.6 * delta; // 지그재그 이동
        y = baseY + Math.sin(System.currentTimeMillis() / 800.0) * verticalMoveRange;

        if (x < 60) x = 60;
        if (x > 680) x = 680;

        movingRight = x > oldX;
        sprite = movingRight ? spriteRight : spriteLeft;

        // 💢 체력 750 이하 시 분노 모드
        if (!enraged && health <= 750) {
            enraged = true;
            potionCooldown = 5000;
            System.out.println("💢 마녀 분노 상태!");
        }

        long now = System.currentTimeMillis();

        // ☠️ 궁극기 발동
        if (!usingPotion && now - lastPotionAttack >= potionCooldown) {
            startPotionAttack();
        }

        // ☠️ 궁극기 지속
        if (usingPotion) {
            if (now - lastPotionTick >= potionTickInterval) {
                lastPotionTick = now;
                dealPotionDamage();
            }
            if (now >= potionEndTime) {
                usingPotion = false;
                shaking = false;
            }
        }

        // 🔫 일반 공격 (HP에 따라 빈도 가변)
        updateShotInterval();
        if (!usingPotion && now - lastShotTime >= shotInterval) {
            lastShotTime = now;
            fireShot(); // MonsterEntity 제공
        }
    }

    private void updateShotInterval() {
        // 체력 줄수록 공격 빈도 증가
        if (health > 1000) shotInterval = 3000;
        else if (health > 700) shotInterval = 2000;
        else if (health > 400) shotInterval = 1200;
        else shotInterval = 800;
    }

    private void startPotionAttack() {
        usingPotion = true;
        shaking = true;
        shakeStartTime = System.currentTimeMillis();

        lastPotionAttack = System.currentTimeMillis();
        potionEndTime = lastPotionAttack + potionDuration;
        lastPotionTick = lastPotionAttack;

        System.out.println("🧪 마녀의 물약 폭탄 발동!");
        dealPotionDamage();
    }

    private void dealPotionDamage() {
        if (game.getShip() != null) {
            game.getShip().takeDamage(15);
        }
        if (game.getFortress() != null) {
            game.getFortress().damage(8);
        }
    }

    @Override
    public boolean takeDamage(int damage) {
        long now = System.currentTimeMillis();
        if (now - lastHitTime < HIT_COOLDOWN) return false;
        lastHitTime = now;

        health -= damage;
        System.out.println("🧪 마녀 피격! 남은 HP: " + health);

        if (health <= 0) {
            System.out.println("💀 마녀 사망!");
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

        // 🎯 현재 transform 저장
        AffineTransform oldTransform = g2.getTransform();

        // 🔥 흔들림 효과 (보스만)
        if (shaking) {
            double elapsed = System.currentTimeMillis() - shakeStartTime;
            if (elapsed < shakeDuration) {
                int offsetX = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
                int offsetY = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
                g2.translate(offsetX, offsetY);
            }
        }

        // 👻 보스 본체
        Image img = sprite.getImage().getScaledInstance(
                (int)(sprite.getWidth() * 0.5),
                (int)(sprite.getHeight() * 0.5),
                Image.SCALE_SMOOTH
        );
        g2.drawImage(img, (int)x - 40, (int)y - 40, null);

        // 🔄 transform 원복
        g2.setTransform(oldTransform);

        // 💥 물약 폭탄 효과
        if (usingPotion) {
            double t = (System.currentTimeMillis() % 300) / 300.0;
            int alpha = (int)(100 + 100 * Math.sin(t * Math.PI * 2));
            g2.setColor(new Color(255, 150, 200, alpha));
            g2.fillRect(0, 0, 800, 600);

            for (Sprite s : potionSprites) {
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
