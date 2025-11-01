package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Graphics2D;
import org.newdawn.spaceinvaders.Game;

/**
 * 요새 엔티티 (플레이어 기지를 보호함)
 * - HP가 0이 되면 파괴됨
 * - 정확한 크기 반환(getWidth/getHeight)을 오버라이드
 */
public class FortressEntity extends Entity {
    private Game game;
    private int hp = 3000; // 요새의 체력

    public FortressEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    /** 요새가 피해를 받았을 때 HP 감소 처리 */
    public void damage(int amount) {
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
        // 요새는 직접적인 충돌 반응을 하지 않음 (처리 로직은 충돌한 다른 엔티티에서 담당)
    }

    /** 스프라이트 폭 반환 (정확한 충돌 계산을 위함) */
    public int getWidth() {
        return sprite != null ? sprite.getWidth() : 60;
    }

    /** 스프라이트 높이 반환 (정확한 충돌 계산을 위함) */
    public int getHeight() {
        return sprite != null ? sprite.getHeight() : 60;

        
    }

    /** 요새 그리기 (스케일 적용) */
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