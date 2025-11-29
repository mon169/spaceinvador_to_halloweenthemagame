package org.newdawn.spaceinvaders.entity.Boss;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

/**
 * Stage 1 Boss: 프랑켄슈타인
 * - 전기 궁극기 + 화면 흔들림 + HP 숫자
 */
public class Boss1 extends BossEntity {
    // 전기 궁극기 관련
    private long lastElectricAttack = 0;
    private long electricCooldown = 8000;
    private boolean usingElectric = false;
    private long electricDuration = 2500;
    private long electricEndTime = 0;

    private long lastElectricTick = 0;
    private long electricTickInterval = 400;

    private final List<Sprite> lightningSprites = new ArrayList<>();
    private Sprite spriteLeft;
    private Sprite spriteRight;

    public Boss1(Game game, int x, int y) {
        super(game, "sprites/frankenr.png", x, y);

        // BaseEntity에서 this.health = 1000, baseY가 초기화됨.

        spriteLeft  = SpriteStore.get().getSprite("sprites/frankenl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/frankenr.png");
        sprite = spriteRight; // 부모 클래스의 sprite 필드 사용

        // 라이트닝 스프라이트 로딩
        lightningSprites.add(SpriteStore.get().getSprite("sprites/lightning1.png"));
        lightningSprites.add(SpriteStore.get().getSprite("sprites/lightning1.png"));
        lightningSprites.add(SpriteStore.get().getSprite("sprites/lightning1.png"));
        
        game.setBackground("bg/zombiebg.jpg");
    }

    // ------------------------------------
    // 부모 클래스의 move()에서 호출됨
    // ------------------------------------
    @Override
    protected void updateSpecialAttack() {
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
                shaking = false; // shaking도 부모 필드
            }
        }
    }
    
    // ------------------------------------
    // 부모 클래스의 updateEnrage()에서 호출됨 (분노 처리 오버라이드)
    // ------------------------------------
    @Override
    protected void updateEnrage() {
        super.updateEnrage(); // 부모의 분노 상태 체크 로직 호출
        if (enraged) {
            electricCooldown = 5000; // 궁극기 쿨타임 감소
            System.out.println("프랑켄슈타인 분노 상태!");
        }
    }
    
    private void startElectricAttack() {
        usingElectric = true;
        shaking = true; // shaking은 부모 필드
        shakeStartTime = System.currentTimeMillis(); // shakeStartTime도 부모 필드

        lastElectricAttack = System.currentTimeMillis();
        electricEndTime = lastElectricAttack + electricDuration;
        lastElectricTick = lastElectricAttack;

        System.out.println("프랑켄슈타인 궁극기 발동!");
        dealElectricDamage(); // 즉시 피해 1회
    }

    private void dealElectricDamage() {
        if (game.getShip() != null) game.getShip().takeDamage(20);
        if (game.getFortress() != null) game.getFortress().damage(10);
    }

    // ------------------------------------
    // 부모 클래스의 draw()에서 호출됨
    // ------------------------------------
    @Override
    protected void drawSpecialEffect(Graphics2D g2) {
        if (!usingElectric) return;

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
    
    // ------------------------------------
    // 오버라이딩: 스프라이트 방향 결정 시 부모의 sprite 필드 사용
    // ------------------------------------
    @Override
    protected void updateMovement(long delta) {
        super.updateMovement(delta);
        sprite = movingRight ? spriteRight : spriteLeft;
    }
}