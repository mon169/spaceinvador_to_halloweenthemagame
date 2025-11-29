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
 * Stage 5 Boss: 뱀파이어 (Vampire)
 * - 암전 패턴 (Dark Attack)
 */
public class Boss5 extends BossEntity {

    /* =============================
     * 암전 패턴 상태 - Boss5 고유 필드
     * ============================= */
    private long lastDarkAttack = 0;
    private long darkCooldown = 8000;
    private boolean usingDark = false;
    private long darkEndTime = 0;

    private long lastDarkTick = 0;
    private static final long DARK_TICK_INTERVAL = 400;

    /* =============================
     * 스프라이트/이펙트 - Boss5 고유 필드
     * ============================= */
    private Sprite spriteLeft;
    private Sprite spriteRight;
    private final List<Sprite> batSprites = new ArrayList<>();

    public Boss5(Game game, int x, int y) {
        super(game, "sprites/vampr.png", x, y);
        // BossEntity에서 game, baseY, health, shaking, shotInterval 등이 초기화됨.

        spriteLeft = SpriteStore.get().getSprite("sprites/vampl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/vampr.png");
        sprite = spriteRight;

        batSprites.add(SpriteStore.get().getSprite("sprites/bat1.png"));
        batSprites.add(SpriteStore.get().getSprite("sprites/bat2.png"));
        batSprites.add(SpriteStore.get().getSprite("sprites/bat3.png"));
        
        // 보스 등장 시 배경 변경
        game.setBackground("bg/vampbg.jpg");
    }

    /* ==================================================
     * UPDATE / MOVE (부모 메서드 오버라이딩)
     * ================================================== */

    @Override
    protected void updateMovement(long delta) {
        // 1. 부모의 공통 이동 및 방향 계산 로직 실행
        super.updateMovement(delta);
        
        // 2. 부모에서 계산된 movingRight에 따라 스프라이트 변경
        sprite = movingRight ? spriteRight : spriteLeft;
    }
    
    @Override
    protected void updateEnrage() {
        super.updateEnrage(); // 부모의 분노 상태 체크 (health <= 750)
        if (enraged) { // 부모 클래스의 enraged 필드 사용
            darkCooldown = 5000;
            System.out.println("💢 뱀파이어 분노 상태!");
        }
    }

    @Override
    protected void updateShotInterval() {
        // BossEntity의 기본 로직(700/400/200 기준)을 사용하므로, 오버라이딩을 통해 유지
        if (health > 700) shotInterval = 3000;
        else if (health > 400) shotInterval = 2000;
        else if (health > 200) shotInterval = 1200;
        else shotInterval = 800;
    }

    /* ==================================================
     * 특수 공격 처리 (BossEntity의 추상 메서드 구현)
     * ================================================== */
    @Override
    protected void updateSpecialAttack() {
        long now = System.currentTimeMillis();

        // 암전 공격 발동 체크
        if (!usingDark && now - lastDarkAttack >= darkCooldown) {
            startDarkAttack(now);
        }

        // 암전 공격 지속 처리
        if (usingDark) {
            updateDarkDamage(now);
            if (now >= darkEndTime) {
                endDarkAttack();
            }
        }
    }

    private void startDarkAttack(long now) {
        usingDark = true;
        shaking = true; // 부모 필드 사용
        shakeStartTime = now; // 부모 필드 사용

        lastDarkAttack = now;
        // 궁극기 지속 시간은 부모의 화면 흔들림 지속 시간(shakeDuration=2500)을 따름
        darkEndTime = now + shakeDuration; 
        lastDarkTick = now;

        System.out.println("🦇 뱀파이어 암전 패턴 발동!");
        applyDarkDamage();
    }

    private void updateDarkDamage(long now) {
        if (now - lastDarkTick >= DARK_TICK_INTERVAL) {
            lastDarkTick = now;
            applyDarkDamage();
        }
    }

    private void endDarkAttack() {
        usingDark = false;
        shaking = false; // 부모 필드 사용
    }

    private void applyDarkDamage() {
        if (game.getShip() != null) game.getShip().takeDamage(15);
        if (game.getFortress() != null) game.getFortress().damage(15);
    }

    /* ==================================================
     * 피격/충돌 (부모 메서드 오버라이딩)
     * ================================================== */
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (health > 0) {
            System.out.println("🧛 뱀파이어 피격! 남은 HP: " + health);
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
        if (!usingDark) return;
        
        // 화면 전체 암전 효과
        double t = (System.currentTimeMillis() % 300) / 300.0;
        int alpha = (int)(150 + 100 * Math.sin(t * Math.PI * 2));
        alpha = Math.min(230, alpha);

        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, 800, 600);

        // 플레이어 주변만 밝게 처리 (Destinational Alpha Composite)
        if (game.getShip() != null) {
            int sx = (int) game.getShip().getX();
            int sy = (int) game.getShip().getY();
            int radius = 180;

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.DstOut);
            g2.fillOval(sx - radius, sy - radius, radius * 2, radius * 2);
            g2.setComposite(old);
        }

        // 박쥐 스프라이트 무작위 배치 효과
        for (Sprite s : batSprites) {
            int lx = (int)(Math.random() * 750);
            int ly = (int)(Math.random() * 400);
            g2.drawImage(s.getImage(), lx, ly, s.getWidth() / 2, s.getHeight() / 2, null);
        }
    }
}