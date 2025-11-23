package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Graphics2D;
import org.newdawn.spaceinvaders.Game;

/**
 * 🏰 요새 엔티티 (플레이어 기지를 보호함)
 * - HP가 0이 되면 파괴됨
 * - 정확한 크기 반환(getWidth/getHeight) 추가
 */
public class FortressEntity extends Entity {
    private Game game;
    private int hp = 3000; // 요새의 체력

    public FortressEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    /** 요새가 피해를 받았을 때 HP 감소 */
    public void damage(int amount) {
        // 🛡 방어막이 활성화되어 있으면 피해 무시 (무적)
        if (game.hasActiveShield()) {
            System.out.println("🛡 방어막이 요새 피해를 막았습니다! (무적 상태)");
            return;
        }
        
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            game.notifyFortressDestroyed();
        }
    }

    /** 현재 HP 반환 */
    public int getHP() {
        return hp;
    }

    /** 다른 엔티티와 충돌했을 때 (요새는 직접 반응 없음) */
    @Override
    public void collidedWith(Entity other) {
        // 요새는 직접적인 충돌 반응 없음
    }

    /** 스프라이트 폭 반환 (정확한 충돌 계산용) */
    public int getWidth() {
        return sprite != null ? sprite.getWidth() : 60;
    }

    /** 스프라이트 높이 반환 (정확한 충돌 계산용) */
    public int getHeight() {
        return sprite != null ? sprite.getHeight() : 60;

        
    }

    /** 요새 그리기 (스케일 적용 가능) */
    @Override
    public void draw(Graphics g) {
        if (sprite == null) {
            System.err.println("[NULL SPRITE] FortressEntity at (" + (int)x + "," + (int)y + ")");
            return;
        }

        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            double scale = 0.65; // 요새 크기 비율 조정

            int dw = (int)(sprite.getWidth() * scale);
            int dh = (int)(sprite.getHeight() * scale);

            int drawX = (int) x;
            int drawY = (int) y;

            g2.drawImage(sprite.getImage(), drawX, drawY, dw, dh, null);
        } else {
            sprite.draw(g, (int)x, (int)y);
        }
    }
}
