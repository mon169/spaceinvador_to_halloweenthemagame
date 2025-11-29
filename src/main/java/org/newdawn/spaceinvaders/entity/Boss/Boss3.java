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
 * Stage 3 Boss: 미라
 * - 눈부심 공격 (Wrap Attack)
 */
public class Boss3 extends BossEntity {

    /* ===========================================================
       눈부심 공격 (Wrap Attack) - Boss3 고유 필드
       =========================================================== */
    private long lastWrapAttack = 0;
    private long wrapCooldown = 8000; // 초기값
    private boolean usingWrap = false;
    private long wrapDuration = 2500;
    private long wrapEndTime = 0;

    private long lastWrapTick = 0;
    private long wrapTickInterval = 400;

    /* ===========================================================
       스프라이트 - Boss3 고유 필드
       =========================================================== */
    private final List<Sprite> bandageSprites = new ArrayList<>();
    private Sprite spriteLeft;
    private Sprite spriteRight;

    public Boss3(Game game, int x, int y) {
        super(game, "sprites/mummyr.png", x, y);
        // 부모 클래스(BossEntity)에서 game, baseY, health, sprite 등 공통 필드가 초기화됩니다.

        spriteLeft  = SpriteStore.get().getSprite("sprites/mummyl.png");
        spriteRight = SpriteStore.get().getSprite("sprites/mummyr.png");
        sprite = spriteRight;

        // 보스 등장 시 배경 변경
        game.setBackground("bg/desert.JPG");
    }

    /* ===========================================================
       UPDATE / MOVE (부모 메서드 오버라이딩)
       =========================================================== */

    @Override
    protected void updateMovement(long delta) {
        super.updateMovement(delta);
        // 부모의 이동 로직을 수행한 후, 방향에 따라 스프라이트를 결정
        sprite = movingRight ? spriteRight : spriteLeft;
    }

    @Override
    protected void updateEnrage() {
        super.updateEnrage(); // 부모의 분노 상태 체크 (health <= 750)
        if (enraged) { // 부모 클래스의 enraged 필드 사용
            wrapCooldown = 5000;
            System.out.println("💢 미라 분노 상태!");
        }
    }

    @Override
    protected void updateShotInterval() {
        // 기존 Boss3의 로직을 유지하여 공격 빈도 조절
        if (health > 700) shotInterval = 2000;
        else if (health > 400) shotInterval = 1200;
        else shotInterval = 800;
    }

    /* ===========================================================
       특수 공격 처리 (BossEntity의 추상 메서드 구현)
       =========================================================== */
    @Override
    protected void updateSpecialAttack() {
        long now = System.currentTimeMillis();

        // 눈부심 공격 발동 체크
        if (!usingWrap && now - lastWrapAttack >= wrapCooldown) {
            startWrapAttack();
        }

        // 눈부심 공격 지속 처리
        if (usingWrap) {
            updateWrapTick(now);
            if (now >= wrapEndTime) endWrapAttack();
        }
    }

    private void startWrapAttack() {
        usingWrap = true;
        shaking = true; // 부모 필드 사용
        shakeStartTime = System.currentTimeMillis(); // 부모 필드 사용

        lastWrapAttack = System.currentTimeMillis();
        wrapEndTime = lastWrapAttack + wrapDuration;
        lastWrapTick = lastWrapAttack;

        System.out.println("🌀 미라 눈부심 공격 발동!");
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
        shaking = false; // 부모 필드 사용
    }

    private void dealWrapDamage() {
        if (game.getShip() != null) game.getShip().takeDamage(15);
        if (game.getFortress() != null) game.getFortress().damage(15);
    }

    /* ===========================================================
       데미지 / 충돌 처리 (부모 메서드 오버라이딩)
       =========================================================== */

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (health > 0) { // 부모에서 health 체크 후 처리
            System.out.println("🧟 미라 피격! 남은 HP: " + health);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 충돌 방지 대상 체크 (기존 기능 유지)
        if (other instanceof EnemyShotEntity || other instanceof MonsterEntity) return;

        // 아이템 데미지 적용 및 일반 충돌 처리는 부모의 로직으로 위임
        super.collidedWith(other);
    }

    /* ===========================================================
       DRAW (특수 효과만 구현)
       =========================================================== */
    @Override
    protected void drawSpecialEffect(Graphics2D g2) {
        if (!usingWrap) return;

        // 눈부심 효과: 화면 전체 색상 변화
        double t = (System.currentTimeMillis() % 300) / 300.0;
        int alpha = (int)(100 + 100 * Math.sin(t * Math.PI * 2));
        g2.setColor(new Color(255, 220, 150, alpha)); // 노란색 계열의 밝은 색상
        g2.fillRect(0, 0, 800, 600);

        // 붕대 스프라이트 무작위 배치 효과
        for (Sprite s : bandageSprites) {
            int lx = (int)(Math.random() * 750);
            int ly = (int)(Math.random() * 400);
            g2.drawImage(s.getImage(), lx, ly, s.getWidth() / 2, s.getHeight() / 2, null);
        }
    }
}