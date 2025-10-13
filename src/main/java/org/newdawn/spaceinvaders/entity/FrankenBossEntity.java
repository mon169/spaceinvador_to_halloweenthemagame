package org.newdawn.spaceinvaders.entity;

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

/**
 * Stage 1 Boss: 프랑켄슈타인 엔티티
 * - 전기 궁극기(화면 흔들림 포함) 기능
 * - 체력이 줄어들수록 공격 속도가 증가함
 * - HP가 숫자로 표시되며 한글 폰트 지원
 */
public class FrankenBossEntity extends AlienEntity {
    private final Game game;
    private int health = 1500; // 체력 복원 (1500)
    private boolean enraged = false;

    // 전기 궁극기 관련
    private long lastElectricAttack = 0;
    private long electricCooldown = 8000;
    private boolean usingElectric = false;
    private long electricDuration = 2500;
    private long electricEndTime = 0;

    private long lastElectricTick = 0;
    private long electricTickInterval = 400;

    // 이동 관련
    private double baseY;
    private double verticalMoveRange = 30;
    private boolean movingRight = true;

    // 화면 흔들림
    private double shakeIntensity = 8;
    private boolean shaking = false;
    private long shakeStartTime = 0;
    private long shakeDuration = 2500;

    private final List<Sprite> lightningSprites = new ArrayList<>();
    private Sprite flashSprite;
    private Sprite spriteLeft;
    private Sprite spriteRight;

    private long lastHitTime = 0;
    private static final long HIT_COOLDOWN = 200; // 피격 무적 시간

    // 공격 빈도 제어용
    private long lastShotTime = 0;
    private long shotInterval = 3000; // 기본 3초 간격

    public FrankenBossEntity(Game game, int x, int y) {
        super(game, x, y);
        this.game = game;
        this.baseY = y;

        spriteLeft  = SpriteStore.get().getSprite("sprites/frankenl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/frankenr.png");
        sprite = spriteRight;

        // 라이트닝 스프라이트 로딩
        lightningSprites.add(SpriteStore.get().getSprite("sprites/lightning1.png"));
        lightningSprites.add(SpriteStore.get().getSprite("sprites/lightning1.png"));
        lightningSprites.add(SpriteStore.get().getSprite("sprites/lightning1.png"));
        flashSprite = SpriteStore.get().getSprite("sprites/lightning1.png");
    }

    @Override
    public void move(long delta) {
        double oldX = x;
        // 사인 함수를 이용한 수평/수직 이동
        x += Math.sin(System.currentTimeMillis() / 800.0) * 0.4 * delta;
        y = baseY + Math.sin(System.currentTimeMillis() / 1200.0) * verticalMoveRange;

        // 경계 제한
        if (x < 60) x = 60;
        if (x > 680) x = 680;

        // 이동 방향에 따른 스프라이트 결정
        movingRight = x > oldX;
        sprite = movingRight ? spriteRight : spriteLeft;

        // 체력 750 이하 시 분노 모드 돌입
        if (!enraged && health <= 750) {
            enraged = true;
            electricCooldown = 5000; // 궁극기 쿨타임 감소
            System.out.println("프랑켄슈타인 분노 상태!");
        }

        long now = System.currentTimeMillis();

        // 전기 궁극기 발동 체크
        if (!usingElectric && now - lastElectricAttack >= electricCooldown) {
            startElectricAttack();
        }

        // 전기 궁극기 지속 처리
        if (usingElectric) {
            if (now - lastElectricTick >= electricTickInterval) {
                lastElectricTick = now;
                dealElectricDamage();
            }
            // 지속 시간 종료 체크
            if (now >= electricEndTime) {
                usingElectric = false;
                shaking = false;
            }
        }

        // 일반 공격 (HP에 따라 빈도 변경)
        updateShotInterval();
        if (!usingElectric && now - lastShotTime >= shotInterval) {
            lastShotTime = now;
            fireShot();
        }
    }

    private void updateShotInterval() {
        // 체력에 따라 공격 빈도 조정 (체력 줄수록 더 자주 발사)
        if (health > 1000) shotInterval = 3000;
        else if (health > 700) shotInterval = 2000;
        else if (health > 400) shotInterval = 1200;
        else shotInterval = 800; // 빈사 시 공격 속도 최대로
    }

    private void startElectricAttack() {
        usingElectric = true;
        shaking = true; // 화면 흔들림 시작
        shakeStartTime = System.currentTimeMillis();

        lastElectricAttack = System.currentTimeMillis();
        electricEndTime = lastElectricAttack + electricDuration;
        lastElectricTick = lastElectricAttack;

        System.out.println("프랑켄슈타인 궁극기 발동!");
        dealElectricDamage(); // 즉시 피해 1회
    }

    private void dealElectricDamage() {
        // 플레이어에게 피해
        if (game.getShip() != null) {
            game.getShip().takeDamage(20);
        }
        // 요새에 피해
        if (game.getFortress() != null) {
            game.getFortress().damage(10);
        }
    }

    @Override
    public boolean takeDamage(int damage) {
        // 피격 무적 시간 처리
        long now = System.currentTimeMillis();
        if (now - lastHitTime < HIT_COOLDOWN) return false;
        lastHitTime = now;

        health -= damage;
        System.out.println("프랑켄슈타인 피격! 남은 HP: " + health);

        if (health <= 0) {
            System.out.println("프랑켄슈타인 사망!");
            game.removeEntity(this);
            game.bossDefeated();
            return true;
        }
        return false;
    }

    @Override
    public void collidedWith(Entity other) {
        // 적 총알이나 다른 외계인과는 충돌 무시
        if (other instanceof EnemyShotEntity || other instanceof AlienEntity) return;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // 현재 Transform 상태 저장
        AffineTransform oldTransform = g2.getTransform();

        // 화면 흔들림 효과 적용 (보스만)
        if (shaking) {
            double elapsed = System.currentTimeMillis() - shakeStartTime;
            if (elapsed < shakeDuration) {
                int offsetX = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
                int offsetY = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
                g2.translate(offsetX, offsetY);
            }
        }

        // 보스 본체 그리기
        Image img = sprite.getImage().getScaledInstance(
                (int)(sprite.getWidth() * 0.5),
                (int)(sprite.getHeight() * 0.5),
                Image.SCALE_SMOOTH
        );
        g2.drawImage(img, (int)x - 40, (int)y - 40, null);

        // Transform 원상 복구 (이후 요소는 흔들리지 않게)
        g2.setTransform(oldTransform);

        // 전기 궁극기 시각 효과
        if (usingElectric) {
            // 화면 전체 번쩍임 (노란색 알파값 변화)
            double t = (System.currentTimeMillis() % 300) / 300.0;
            int alpha = (int)(100 + 100 * Math.sin(t * Math.PI * 2));
            g2.setColor(new Color(255, 255, 100, alpha));
            g2.fillRect(0, 0, 800, 600);

            // 무작위 번개 효과
            for (Sprite s : lightningSprites) {
                int lx = (int)(Math.random() * 750);
                int ly = (int)(Math.random() * 400);
                int lw = s.getWidth() / 2;
                int lh = s.getHeight() / 2;
                g2.drawImage(s.getImage(), lx, ly, lw, lh, null);
            }
        }

        // HP 바 그리기
        g2.setColor(Color.red);
        g2.fillRect((int)x - 50, (int)y - 70, 100, 6);
        g2.setColor(Color.green);
        int hpWidth = (int)(100 * (health / 1500.0)); // HP 1500 기준 계산
        g2.fillRect((int)x - 50, (int)y - 70, hpWidth, 6);

        // HP 숫자 표시 (한글 폰트 적용)
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.setColor(Color.white);
        g2.drawString(health + " / 1500", (int)x - 25, (int)y - 80);
    }
}