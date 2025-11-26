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
import org.newdawn.spaceinvaders.entity.FortressEntity;
import org.newdawn.spaceinvaders.entity.EnemyShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;

/**
 * Stage 2 Boss: 마녀
 */
public class Boss2 extends MonsterEntity {
    private final Game game;
    private final int MAX_HEALTH = 5;
    private int health = MAX_HEALTH;
    private boolean enraged = false;

    // 궁극기 관련 (물약 폭탄 패턴)
    private long lastPotionAttack = 0;
    private long potionCooldown = 50000; // 50초마다 물약 패턴 발생
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

    // 시각 효과용 스프라이트
    private Sprite potionEffectSprite;
    private Sprite bombEffectSprite;
    private Sprite spriteLeft;
    private Sprite spriteRight;

    // 국소 시야 방해 효과 리스트
    private List<SplashEffect> activeSplashes = new ArrayList<>();

    private long lastHitTime = 0;
    private static final long HIT_COOLDOWN = 200;

    // 공격 빈도 제어용
    private long lastShotTime = 0;
    private long shotInterval = 3000; // 기본 3초 간격

    public Boss2(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.baseY = y;

        spriteLeft = SpriteStore.get().getSprite("sprites/witchl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/witchr.png");
        sprite = spriteRight;

        // 시각 효과용 스프라이트 로드
        potionEffectSprite = SpriteStore.get().getSprite("sprites/poisonpotion.png");
        bombEffectSprite = SpriteStore.get().getSprite("sprites/poisionbomb.png");

        // 보스 등장 시 배경 변경
        game.setBackground("bg/wbg.jpg");

        // 보스 등장 후 즉시 필드에 물약 폭탄 여러 개 배치
        int count = 10;       // 한 번에 등장하는 물약 수
        int minDist = 120;    // 서로 겹치지 않게 배치하기 위한 최소 거리
        List<int[]> placed = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int tries = 0;
            int px, py;
            while (true) {
                px = 40 + (int) (Math.random() * (game.getWidth() - 80));
                py = 80 + (int) (Math.random() * (game.getHeight() - 160));
                boolean ok = true;
                for (int[] p : placed) {
                    int dx = p[0] - px;
                    int dy = p[1] - py;
                    if (dx * dx + dy * dy < minDist * minDist) {
                        ok = false;
                        break;
                    }
                }
                tries++;
                if (ok || tries > 25) break;
            }
            placed.add(new int[] { px, py });
            game.addEntity(new PotionBomb(px, py));
        }
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

        // 분노 모드 (체력 기준은 필요에 따라 조정)
        if (!enraged && health <= 750) {
            enraged = true;
            potionCooldown = 30000;
            System.out.println("💢 마녀 분노 상태!");
        }

        long now = System.currentTimeMillis();

        // 궁극기 발동
        if (!usingPotion && now - lastPotionAttack >= potionCooldown) {
            startPotionAttack();
        }

        // 궁극기 지속
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

        // 일반 공격
        updateShotInterval();
        if (!usingPotion && now - lastShotTime >= shotInterval) {
            lastShotTime = now;
            fireShot();
        }

        // 만료된 스플래시 정리
        activeSplashes.removeIf(s -> s.isExpired(now));
    }

    private void updateShotInterval() {
        // MAX_HEALTH = 5 기준이면, 이 부분은 그냥 단계별로 바꾸셔도 됩니다.
        if (health > 4) shotInterval = 3000;
        else if (health > 3) shotInterval = 2000;
        else if (health > 2) shotInterval = 1200;
        else shotInterval = 800;
    }

    private void startPotionAttack() {
        usingPotion = true;
        shaking = true;
        shakeStartTime = System.currentTimeMillis();

        long now = System.currentTimeMillis();
        lastPotionAttack = now;
        potionEndTime = lastPotionAttack + potionDuration;
        lastPotionTick = lastPotionAttack;

        System.out.println("🧪 마녀의 물약 폭탄 발동!");

        // 첫 틱 데미지
        dealPotionDamage();

        // 주변에 PotionBomb 여러 개 생성
        int count = 12;
        for (int i = 0; i < count; i++) {
            int px = 40 + (int) (Math.random() * (game.getWidth() - 80));
            int py = 80 + (int) (Math.random() * (game.getHeight() - 160));
            game.addEntity(new PotionBomb(px, py));
        }
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

        AffineTransform oldTransform = g2.getTransform();

        // 흔들림 효과(보스만)
        if (shaking) {
            double elapsed = System.currentTimeMillis() - shakeStartTime;
            if (elapsed < shakeDuration) {
                int offsetX = (int) (Math.random() * shakeIntensity - shakeIntensity / 2);
                int offsetY = (int) (Math.random() * shakeIntensity - shakeIntensity / 2);
                g2.translate(offsetX, offsetY);
            }
        }

        // 보스 본체
        Image img = sprite.getImage().getScaledInstance(
                (int) (sprite.getWidth() * 0.5),
                (int) (sprite.getHeight() * 0.5),
                Image.SCALE_SMOOTH
        );
        g2.drawImage(img, (int) x - 40, (int) y - 40, null);

        g2.setTransform(oldTransform);

        long now = System.currentTimeMillis();

        // 핑크 오버레이 (SplashEffect)
        for (SplashEffect splash : activeSplashes) {
            long elapsed = now - splash.startTime;
            double progress = Math.max(0.0, Math.min(1.0, (double) elapsed / splash.duration));

            double currentRadius = splash.maxRadius * progress;

            int alpha = 255;
            g2.setColor(new Color(255, 105, 180, alpha));

            int r = (int) currentRadius;
            g2.fillOval(splash.x - r, splash.y - r, 2 * r, 2 * r);

            // 내부 스프라이트는 요청에 따라 생략
        }

        // HP 바
        g2.setColor(Color.red);
        g2.fillRect((int) x - 50, (int) y - 70, 100, 6);
        g2.setColor(Color.green);
        int hpWidth = (int) (100 * (health / (double) MAX_HEALTH));
        g2.fillRect((int) x - 50, (int) y - 70, Math.max(0, Math.min(100, hpWidth)), 6);

        // HP 숫자
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.setColor(Color.white);
        g2.drawString(health + " / " + MAX_HEALTH, (int) x - 25, (int) y - 80);
    }

    /** Boss2 전용: 화면에 생성되어 1~3초 뒤 폭발하는 물약 폭탄 */
    private class PotionBomb extends Entity {
        private final long spawnTime;
        private boolean switched = false;

        private final long explodeDelay;

        private final int origX;
        private final int origY;
        private final int shakeAmp = 40;
        private final double shakeSpeed = 8.0;

        public PotionBomb(int px, int intpy) {
            super("sprites/poisonpotion.png", px, intpy);
            this.origX = px;
            this.origY = intpy;
            this.spawnTime = System.currentTimeMillis();
            this.dx = 0;
            this.dy = 0;
            this.explodeDelay = 1000 + (int) (Math.random() * 2000);
        }

        @Override
        public void move(long delta) {
            long elapsed = System.currentTimeMillis() - spawnTime;

            if (!switched && elapsed < explodeDelay) {
                double t = (double) elapsed / 1000.0;
                x = origX + Math.sin(t * Math.PI * shakeSpeed) * shakeAmp;
                y = origY;
                return;
            }

            if (!switched && elapsed >= explodeDelay) {
                this.sprite = SpriteStore.get().getSprite("sprites/poisionbomb.png");
                switched = true;

                activeSplashes.add(
                        new SplashEffect((int) x + getWidth() / 2, (int) y + getHeight() / 2, System.currentTimeMillis())
                );

                FortressEntity fort = game.getFortress();
                if (fort != null && this.collidesWith(fort)) {
                    fort.damage(50);
                }
            }

            if (elapsed >= 3000) {
                game.removeEntity(this);
            }
        }

        @Override
        public void collidedWith(Entity other) {
            // 연출용
        }
    }

    private class SplashEffect {
        int x, y;
        long startTime;
        double maxRadius = 60;
        long duration = 2000;

        public SplashEffect(int x, int y, long startTime) {
            this.x = x;
            this.y = y;
            this.startTime = startTime;
        }

        public boolean isExpired(long now) {
            return now > startTime + duration;
        }
    }
}