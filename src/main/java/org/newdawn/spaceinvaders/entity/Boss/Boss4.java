package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.EnemyShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;

/**
 * Stage 4 Boss: 좀비
 * - 신체 던지기 공격 (Throw Attack)
 */
public class Boss4 extends BossEntity {

    /* ================================
     * 고유 상수
     * ================================ */
    private static final int MAX_Y_BOUNDARY = 370; // Boss4 고유의 Y축 경계 제한

    /* ================================
     * 공격 관련 (던지기) - Boss4 고유 필드
     * ================================ */
    private boolean usingThrow = false;
    private long lastThrowAttack = 0;
    private long throwCooldown = 8000;
    private long throwEndTime = 0;
    private long lastThrowTick = 0;
    private static final long THROW_TICK_INTERVAL = 400;

    /* ================================
     * 스프라이트/이펙트 - Boss4 고유 필드
     * ================================ */
    private Sprite spriteLeft;
    private Sprite spriteRight;
    private final List<Sprite> limbSprites = new ArrayList<>();

    public Boss4(Game game, int x, int y) {
        super(game, "sprites/zombier.png", x, y);
        // BossEntity에서 game, baseY, health, shaking, shotInterval 등이 초기화됨.

        spriteLeft = SpriteStore.get().getSprite("sprites/zombiel.png");
        spriteRight = SpriteStore.get().getSprite("sprites/zombier.png");
        sprite = spriteRight;

        limbSprites.add(SpriteStore.get().getSprite("sprites/arm1.png"));
        limbSprites.add(SpriteStore.get().getSprite("sprites/leg1.png"));
        limbSprites.add(SpriteStore.get().getSprite("sprites/heart1.png"));
        
        // 보스 등장 시 배경 변경
        game.setBackground("bg/zombiebg.jpg");
    }

    /* ==================================================
     * UPDATE / MOVE (부모 메서드 오버라이딩)
     * ================================================== */

    @Override
    protected void updateMovement(long delta) {
        // 1. 부모의 공통 이동 및 방향 계산 로직 실행
        super.updateMovement(delta);
        
        // 2. Boss4 고유의 Y축 경계 제한 적용 (기존 기능 유지)
        if (y > MAX_Y_BOUNDARY) y = MAX_Y_BOUNDARY;

        // 3. 부모에서 계산된 movingRight에 따라 스프라이트 변경
        sprite = movingRight ? spriteRight : spriteLeft;
    }
    
    @Override
    protected void updateEnrage() {
        super.updateEnrage(); // 부모의 분노 상태 체크 (health <= 750)
        if (enraged) { // 부모 클래스의 enraged 필드 사용
            throwCooldown = 5000;
            System.out.println("💢 좀비 분노 상태!");
        }
    }

    // updateShotInterval()은 BossEntity의 기본 로직(700, 400)을 사용하므로 오버라이딩하지 않음.

    /* ==================================================
     * 특수 공격 처리 (BossEntity의 추상 메서드 구현)
     * ================================================== */
    @Override
    protected void updateSpecialAttack() {
        long now = System.currentTimeMillis();

        // 던지기 공격 발동 체크
        if (!usingThrow && now - lastThrowAttack >= throwCooldown) {
            startThrowAttack(now);
        }

        // 던지기 공격 지속 처리
        if (usingThrow) {
            updateThrowDamage(now);
            if (now >= throwEndTime) {
                endThrowAttack();
            }
        }
    }

    private void startThrowAttack(long now) {
        usingThrow = true;
        shaking = true; // 부모 필드 사용
        shakeStartTime = now; // 부모 필드 사용

        lastThrowAttack = now;
        // 궁극기 지속 시간은 부모의 화면 흔들림 지속 시간(shakeDuration=2500)을 따름
        throwEndTime = now + shakeDuration; 
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
        shaking = false; // 부모 필드 사용
    }

    private void applyThrowDamage() {
        if (game.getShip() != null) game.getShip().takeDamage(30);
        if (game.getFortress() != null) game.getFortress().damage(30);
    }

    /* ==================================================
     * 피격/충돌 (부모 메서드 오버라이딩)
     * ================================================== */
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (health > 0) {
            System.out.println("🧟 좀비 피격! 남은 HP: " + health);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 충돌 방지 대상 체크 (기존 기능 유지)
        if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;

        // 아이템 데미지 적용 및 일반 충돌 처리는 부모의 로직으로 위임
        super.collidedWith(other);
    }

    /* ==================================================
     * Draw (특수 효과만 구현)
     * ================================================== */
    @Override
    protected void drawSpecialEffect(Graphics2D g2) {
        if (!usingThrow) return;
        
        // 화면 전체 색상 변화 (연한 초록색)
        double t = (System.currentTimeMillis() % 300) / 300.0;
        int alpha = (int)(120 + 100 * Math.sin(t * Math.PI * 2));

        g2.setColor(new Color(180, 255, 180, alpha));
        g2.fillRect(0, 0, 800, 600);

        // 신체 부위 스프라이트 무작위 배치 효과
        for (Sprite s : limbSprites) {
            int lx = (int)(Math.random() * 750);
            int ly = (int)(Math.random() * 400);
            g2.drawImage(s.getImage(), lx, ly, s.getWidth() / 2, s.getHeight() / 2, null);
        }
    }
}