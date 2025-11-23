package org.newdawn.spaceinvaders.manager;

import java.awt.*;
import java.util.List;
import javax.swing.*;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.FortressEntity;
import org.newdawn.spaceinvaders.entity.UserEntity;
import org.newdawn.spaceinvaders.shop.Item;
import org.newdawn.spaceinvaders.shop.Shop;

/**
 * 💡 UIManager — 화면에 보이는 모든 것을 그림
 *  - 메인 타이틀 / 상점 / 메시지 / HUD / 타이머 전부 처리
 */
public class UIManager {
    private final Game game;

    private final Font titleFont = new Font("맑은 고딕", Font.BOLD, 24);
    private final Font smallFont = new Font("맑은 고딕", Font.PLAIN, 14);
    private final Sprite startBtn;
    private final Sprite startBackground; // 시작 화면 배경 이미지

    public UIManager(Game game) {
        this.game = game;
        this.startBtn = SpriteStore.get().getSprite("sprites/startbutton.png");
        this.startBackground = SpriteStore.get().getSprite("bg/start_background.jpg");
    }

    /** 전체 UI 렌더링 엔트리 */
    public void drawFullUI(Graphics2D g, Game game, UserEntity ship, FortressEntity fortress,
                           List<Entity> entities, String message, boolean shopOpen, boolean waiting) {

        // Stage 3에서 체력이 제한 이하일 때는 오직 메시지 오버레이만 표시
        if (game.getCurrentStage() == 3 && ship != null && ship.getHealth() <= game.getLifeLimit()) {
            drawMessageOverlay(g, "💀 사망했습니다!\nR 키를 눌러 다시 도전하세요");
            return;
        }
        // 게임 중 HUD
        if (!waiting) {
            drawHUD(g, game, ship, fortress);
            return;
        }

        // 키 대기 상태 → 오버레이
        if (shopOpen) {
            drawShopOverlay(g, game, ship);
        } else if (message != null && !message.isEmpty()) {
            drawMessageOverlay(g, message);
        } else {
            drawStartScreen(g);
        }
    }

    private void drawHUD(Graphics2D g, Game game, UserEntity ship, FortressEntity fortress) {
        g.setFont(smallFont);
        g.setColor(Color.white);

        // Stage Info
        String stageInfo = "STAGE " + game.getCurrentStage() + " - " + stageDesc(game.getCurrentStage());
        g.drawString(stageInfo, 20, 30);

        // 남은 적 (더 오른쪽)
        g.drawString("남은 적: " + game.getAlienCount(), 350, 30);

            // 타이머 (더 오른쪽)
            int timeLimit = game.getBaseTimeLimit();
            long elapsed = (System.currentTimeMillis() - game.getStageStartTime()) / 1000;
            long remain = Math.max(0, timeLimit - elapsed);
            String timeFormat = "시간 제한: " + remain + "초";
            if (remain <= 20) { g.setColor(Color.red); }
            g.drawString(timeFormat, 500, 30);
            g.setColor(Color.white);

        // Player/Fortress Stats
        if (ship != null) {
            g.drawString("체력: " + ship.getHealth(), 20, 50);
            g.drawString("방어력: " + ship.getDefense(), 20, 70);
            g.drawString("공격력: " + ship.getAttackPower(), 20, 90);
            g.drawString("골드: " + ship.getMoney(), 20, 110);

            int y = 130;
            if (ship.hasBomb() || ship.hasIceWeapon() || ship.hasShield()) {
                g.drawString("[ 보유 중인 특수 무기 ]", 20, y);
                y += 26; // 조금 더 넉넉한 간격
            }
            if (ship.hasBomb())  { g.drawString("• 폭탄 x" + ship.getBombCount() + " (B키)", 20, y); y += 24; }
            if (ship.hasIceWeapon()) { g.drawString("• 얼음 공격 x" + ship.getIceWeaponCount() + " (I키)", 20, y); y += 24; }
            if (ship.hasShield()) { g.drawString("• 방어막 x" + ship.getShieldCount() + " (S키)", 20, y); }
        }
        // 요새 HP는 우측 상단으로 이동하여 HUD 텍스트와 겹치지 않도록 함
        if (fortress != null) {
            String fortHp = "요새 HP: " + fortress.getHP();
            int fw = g.getFontMetrics().stringWidth(fortHp);
                g.drawString(fortHp, 800 - fw - 40, 30);
        }

        // Stage3 생명제한
        if (game.getCurrentStage() == 3 && ship != null && ship.getHealth() <= game.getLifeLimit()) {
            game.notifyDeath();
        }
    }

    private void drawShopOverlay(Graphics2D g, Game game, UserEntity ship) {
        Shop shop = game.getShop();
        if (shop == null || ship == null) return;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, 800, 600);

        g.setColor(Color.white);
        g.setFont(titleFont);
        String shopTitle = "SHOP";
        int titleWidth = g.getFontMetrics().stringWidth(shopTitle);
        g.drawString(shopTitle, (800 - titleWidth) / 2, 60);

        g.setFont(smallFont);
        g.drawString("현재 보유 금액: " + ship.getMoney() + " 골드", 330, 90);

        List<Item> items = shop.getItemsForSale();
        int itemWidth = 350, itemHeight = 80, gap = 20, startX = 50, startY = 120;

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int row = i / 2, col = i % 2;
            int x = startX + col * (itemWidth + gap);
            int y = startY + row * (itemHeight + gap / 2);

            g.setColor(new Color(50, 50, 50, 150));
            g.fillRect(x, y, itemWidth, itemHeight - 5);
            g.setColor(Color.white);
            g.drawString((i + 1) + ". " + item.getName() + " (가격: " + item.getCost() + "골드)", x + 20, y + 25);

            String[] desc = item.getDescription().split("\n");
            for (int j = 0; j < desc.length; j++) {
                g.drawString("  " + desc[j], x + 20, y + 50 + j * 15);
            }
        }

        // 다음 스테이지 안내
        g.setColor(Color.yellow);
        int nextStage = game.getCurrentStage() + 1;
        String nextStageInfo = "다음 스테이지 " + nextStage + " 특성: " + stageDesc(nextStage);
        g.drawString(nextStageInfo, (800 - g.getFontMetrics().stringWidth(nextStageInfo)) / 2, 480);

        // 하단 조작법
        g.setColor(Color.white);
        int bottomY = 540;
        g.drawString("[ 조작 방법 ]  숫자키(1-" + items.size() + "): 아이템 구매   |   R: 다음 스테이지   |   ESC: 종료",
                (800 - g.getFontMetrics().stringWidth("[ 조작 방법 ]  숫자키(1-" + items.size() + "): 아이템 구매   |   R: 다음 스테이지   |   ESC: 종료")) / 2,
                bottomY);
    }

    private void drawMessageOverlay(Graphics2D g, String message) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, 800, 600);
        g.setColor(Color.white);
        g.setFont(titleFont);

        String[] lines = message.split("\n");
        int y = 260;
        for (String line : lines) {
            int w = g.getFontMetrics().stringWidth(line);
            g.drawString(line, (800 - w) / 2, y);
            y += 40;
        }
    }

    private void drawStartScreen(Graphics2D g) {
        // 배경 이미지가 있으면 사용, 없으면 반투명 검은색 배경
        if (startBackground != null) {
            startBackground.drawScaled(g, 0, 0, 800, 600);
        } else {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, 800, 600);
        }
        
        g.setColor(Color.white);

        // 버튼 (크기 조정하고 중앙 아래에 배치)
        if (startBtn != null) {
            int bw = startBtn.getWidth(), bh = startBtn.getHeight();
            // 버튼 크기를 조정 (원본의 약 70% 크기)
            double scale = 0.7;
            int dw = (int) Math.round(bw * scale);
            int dh = (int) Math.round(bh * scale);
            int btnX = (800 - dw) / 2; // 중앙 정렬
            int btnY = (600 - dh) / 2 + 100; // 중앙에서 더 아래
            startBtn.drawScaled(g, btnX, btnY, dw, dh);
        }

        g.setFont(smallFont);
        String controls = "Controls: ← → 이동, SPACE 발사  |  아무 키나 눌러 시작";
        g.drawString(controls, (800 - g.getFontMetrics().stringWidth(controls)) / 2, 500);
    }

    private String stageDesc(int stage) {
        switch (stage) {
            case 1: return "기본 모드";
            case 2: return "적 총알 발사속도 +20%";
            case 3: return "생명 제한 모드(HP 3 이하면 게임오버)";
            case 4: return "장애물 등장";
            case 5: return "이중 장애물 등장";
            default: return "—";
        }
    }
}
