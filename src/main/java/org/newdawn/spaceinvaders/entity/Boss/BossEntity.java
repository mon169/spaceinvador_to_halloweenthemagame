package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.BombShotEntity;
import org.newdawn.spaceinvaders.entity.IceShotEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

public abstract class BossEntity extends Entity {
    protected static final int MAX_HEALTH = 1000;
    protected int health = MAX_HEALTH;
    protected Game game;

    // --------------------------
    // 이동/방향 (공통)
    // --------------------------
    protected double baseY;
    protected double verticalMoveRange = 30;
    protected boolean movingRight = true;
    protected boolean enraged = false; // 분노 상태

    // --------------------------
    // 공격 (공통)
    // --------------------------
    protected long lastShotTime = 0;
    protected long shotInterval = 3000; // 기본 3초 간격

    // --------------------------
    // 동결/피격 (공통)
    // --------------------------
    protected boolean frozen = false;
    protected long freezeEndTime = 0;
    protected long lastHitTime = 0;
    protected static final long HIT_COOLDOWN = 200; // 피격 무적 시간

    // --------------------------
    // 화면 흔들림 (궁극기 공통)
    // --------------------------
    protected double shakeIntensity = 8;
    protected boolean shaking = false;
    protected long shakeStartTime = 0;
    protected long shakeDuration = 2500;

    public BossEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        this.baseY = y; // 초기 Y 좌표 저장
    }

    /* ===================================================
       UPDATE / MOVE (공통 로직: 템플릿 메서드)
       =================================================== */
    @Override
    public void move(long delta) {
        updateFreeze();
        if (frozen) return; // 동결 시 이동 정지

        updateMovement(delta);
        updateEnrage();
        updateNormalAttack();
        
        // **자식 클래스 오버라이딩 필요: 보스별 특수 공격 로직**
        updateSpecialAttack(); 
    }
    
    // 특수 공격 로직은 자식 클래스에서 구현 (Template Hook)
    protected abstract void updateSpecialAttack();

    protected void updateMovement(long delta) {
        double oldX = x;
        // 사인 함수를 이용한 수평/수직 지그재그 이동
        x += Math.sin(System.currentTimeMillis() / 750.0) * 0.5 * delta;
        y = baseY + Math.sin(System.currentTimeMillis() / 1000.0) * verticalMoveRange;

        // 경계 제한
        x = Math.max(60, Math.min(680, x));
        
        // 이동 방향 계산
        movingRight = x > oldX;
    }

    protected void updateEnrage() {
        // 체력 750 이하 시 분노 상태 진입
        if (!enraged && health <= 750) {
            enraged = true;
            // 자식 클래스에서 쿨다운 변경 등의 분노 처리 로직을 오버라이딩하여 구현
        }
    }

    protected void updateNormalAttack() {
        updateShotInterval(); // 공격 빈도 업데이트
        long now = System.currentTimeMillis();

        // 궁극기 사용 중(shaking)이 아닐 때만 일반 공격 시도
        if (!shaking && now - lastShotTime >= shotInterval) {
            lastShotTime = now;
            // 기존 코드에 fireShot() 로직이 없었으므로 주석 처리
            // fireShot(); 
        }
    }
    
    protected void updateShotInterval() {
        // 체력에 따른 기본 공격 빈도 조정
        if (health > 700) shotInterval = 3000;
        else if (health > 400) shotInterval = 2000;
        else if (health > 200) shotInterval = 1200;
        else shotInterval = 800; 
    }


    /* ===================================================
       데미지 / 충돌 / 동결 (공통 로직)
       =================================================== */
    public void takeDamage(int damage) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHitTime < HIT_COOLDOWN) {
            return; // 쿨다운 중 무적
        }
        lastHitTime = currentTime;

        health -= damage;
        System.out.println("보스 피격! 남은 HP: " + health);
        if (health <= 0) {
            game.bossDefeated();
            game.removeEntity(this);
        }
    }

    protected void updateFreeze() {
        if (frozen && System.currentTimeMillis() > freezeEndTime) {
            frozen = false;
            System.out.println("❄️ 동결 해제!");
        }
    }

    public void freeze(long duration) {
        frozen = true;
        freezeEndTime = System.currentTimeMillis() + duration;
        System.out.println("❄️ 보스 동결! (" + (duration / 1000) + "초)");
    }

    // 아이템 충돌 처리 로직 (Boss1~Boss5 공통)
    public void collidedWithItem(Entity other) {
        if (other instanceof BombShotEntity) {
            takeDamage(100);
            game.removeEntity(other);
        } else if (other instanceof IceShotEntity) {
            freeze(3000);
            game.removeEntity(other);
        } else if (other instanceof ShotEntity) {
            takeDamage(10);
            game.removeEntity(other);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        collidedWithItem(other);
    }
    
    /* ===================================================
       DRAW (공통 로직: 템플릿 메서드)
       =================================================== */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform oldTransform = g2.getTransform();

        applyShakeEffect(g2); // 화면 흔들림 적용
        drawBossBody(g2);      // 보스 이미지 그리기
        g2.setTransform(oldTransform); // Transform 원상 복구

        // **자식 클래스 오버라이딩 필요: 특수 공격 시각 효과**
        drawSpecialEffect(g2); 
        drawHpBar(g2);         // HP 바 그리기
    }

    // 특수 공격 효과는 자식 클래스에서 구현 (Template Hook)
    protected abstract void drawSpecialEffect(Graphics2D g2);

    protected void applyShakeEffect(Graphics2D g2) {
        if (!shaking) return;

        long elapsed = System.currentTimeMillis() - shakeStartTime;
        if (elapsed < shakeDuration) {
            // 랜덤한 흔들림 오프셋 적용
            int offsetX = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
            int offsetY = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
            g2.translate(offsetX, offsetY);
        } else {
             shaking = false; // 지속 시간 초과 시 흔들림 종료
        }
    }
    
    protected void drawBossBody(Graphics2D g2) {
        // 이미지 스케일 조정 후 그리기 (Boss1~Boss5 공통 스케일)
        Image img = sprite.getImage().getScaledInstance(
            (int)(sprite.getWidth() * 0.5),
            (int)(sprite.getHeight() * 0.5),
            Image.SCALE_SMOOTH
        );
        g2.drawImage(img, (int)x - 40, (int)y - 40, null);
    }

    protected void drawHpBar(Graphics2D g2) {
        int barX = (int)x - 50;
        int barY = (int)y - 70;
        
        // HP 바 (배경: Red)
        g2.setColor(Color.red);
        g2.fillRect(barX, barY, 100, 6);

        // HP (전경: Green) - MAX_HEALTH 상수 사용
        g2.setColor(Color.green);
        int hpWidth = (int)(100 * (health / (double)MAX_HEALTH));
        g2.fillRect(barX, barY, hpWidth, 6);

        // HP 숫자 표시
        g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        g2.setColor(Color.white);
        g2.drawString(health + " / " + MAX_HEALTH, (int)x - 25, (int)y - 80);
    }
    
    public int getHealth() {
        return health;
    }
}