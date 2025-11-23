package org.newdawn.spaceinvaders.manager;

import java.awt.*;
import java.util.List;

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
    
    // 상점 UI 스프라이트 (선택적 - 파일이 없으면 null)
    private Sprite shopBackground;
    private Sprite shopItemSlot;
    private Sprite shopTitle;

    public UIManager(Game game) {
        this.game = game;
        this.startBtn = SpriteStore.get().getSprite("sprites/startbutton.png");
        
        // 상점 스프라이트 로드 (파일이 없으면 null이 됨)
        // SpriteStore는 파일이 없어도 placeholder를 반환하므로,
        // 실제로는 파일 존재 여부를 확인하기 어렵습니다.
        // 스프라이트를 올려주시면 자동으로 사용됩니다.
        this.shopBackground = null; // 스프라이트 파일을 올려주시면 여기에 경로 지정
        this.shopItemSlot = null;
        this.shopTitle = null;
        
        // 스프라이트 파일이 준비되면 아래 주석을 해제하고 파일명을 지정하세요
        // this.shopBackground = SpriteStore.get().getSprite("sprites/shop_background.png");
        // this.shopItemSlot = SpriteStore.get().getSprite("sprites/shop_item_slot.png");
        // this.shopTitle = SpriteStore.get().getSprite("sprites/shop_title.png");
    }

    /** 전체 UI 렌더링 엔트리 */
    public void drawFullUI(Graphics2D g, Game game, UserEntity ship, FortressEntity fortress,
                           List<Entity> entities, String message, boolean shopOpen, boolean waiting) {

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

        // 남은 적
        g.drawString("남은 적: " + game.getAlienCount(), 250, 30);

        // 타이머
        int timeLimit = game.getBaseTimeLimit();
        long elapsed = (System.currentTimeMillis() - game.getStageStartTime()) / 1000;
        long remain = Math.max(0, timeLimit - elapsed);
        String timeFormat = "시간 제한: " + remain + "초";
        if (remain <= 20) { g.setColor(Color.red); }
        g.drawString(timeFormat, 350, 30);
        g.setColor(Color.white);

        // Player/Fortress Stats
        if (ship != null) {
            g.drawString("체력: " + ship.getHealth(), 20, 50);
            g.drawString("방어력: " + ship.getDefense(), 20, 70);
            g.drawString("공격력: " + ship.getAttackPower(), 20, 90);
            g.drawString("골드: " + ship.getMoney(), 20, 110);
        }
        // 요새 HP는 우측 상단으로 이동하여 HUD 텍스트와 겹치지 않도록 함
        if (fortress != null) {
            String fortHp = "요새 HP: " + fortress.getHP();
            int fw = g.getFontMetrics().stringWidth(fortHp);
            g.drawString(fortHp, 800 - fw - 20, 30);
        }

        // 오른쪽 상단에 보유 아이템 목록 표시
        if (ship != null) {
            drawInventoryList(g, ship);
        }

        // Stage3 생명제한
        if (game.getCurrentStage() == 3 && ship != null && ship.getHealth() <= game.getLifeLimit()) {
            game.notifyDeath();
        }
    }

    /** 오른쪽 상단에 보유 아이템 목록을 표시 */
    private void drawInventoryList(Graphics2D g, UserEntity ship) {
        int startX = 600;  // 오른쪽 상단 시작 X 좌표
        int startY = 30;   // 상단에서 시작
        int lineHeight = 18;
        
        g.setColor(new Color(0, 0, 0, 180)); // 반투명 검은 배경
        g.fillRect(startX - 10, startY - 20, 190, 150);
        
        g.setColor(Color.white);
        g.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        g.drawString("[ 보유 아이템 ]", startX, startY);
        
        g.setFont(new Font("맑은 고딕", Font.BOLD, 14)); // 폰트 크기 증가
        int y = startY + lineHeight + 5;
        
        // 폭탄
        if (ship.getBombCount() > 0) {
            g.setColor(new Color(255, 165, 0)); // 더 진한 주황색
            g.drawString("• 폭탄 x" + ship.getBombCount(), startX, y);
            y += lineHeight + 3;
        }
        
        // 얼음 공격
        if (ship.getIceWeaponCount() > 0) {
            g.setColor(new Color(0, 255, 255)); // 더 진한 청록색
            g.drawString("• 얼음 공격 x" + ship.getIceWeaponCount(), startX, y);
            y += lineHeight + 3;
        }
        
        // 방어막
        if (ship.getShieldCount() > 0) {
            g.setColor(new Color(255, 255, 0)); // 더 진한 노란색
            g.drawString("• 방어막 x" + ship.getShieldCount(), startX, y);
            y += lineHeight + 3;
            // 사용법 안내
            g.setColor(new Color(200, 200, 200)); // 더 밝은 회색
            g.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            g.drawString("  (S키: 요새 보호)", startX + 5, y);
            y += 18;
            g.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        }
        
        // 아이템이 없을 때
        if (ship.getBombCount() == 0 && ship.getIceWeaponCount() == 0 && ship.getShieldCount() == 0) {
            g.setColor(Color.gray);
            g.drawString("보유한 아이템 없음", startX, y);
        }
    }

    private void drawShopOverlay(Graphics2D g, Game game, UserEntity ship) {
        Shop shop = game.getShop();
        if (shop == null || ship == null) return;

        // 배경 - 스프라이트가 있으면 사용, 없으면 반투명 검은색
        if (shopBackground != null) {
            // 배경 스프라이트를 전체 화면에 맞게 그리기
            shopBackground.drawScaled(g, 0, 0, 800, 600);
        } else {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, 800, 600);
        }

        // 타이틀 - 스프라이트가 있으면 사용, 없으면 텍스트
        if (shopTitle != null) {
            int titleX = (800 - shopTitle.getWidth()) / 2;
            shopTitle.draw(g, titleX, 20);
        } else {
            g.setColor(Color.white);
            g.setFont(titleFont);
            g.drawString("★ SHOP ★", 360, 60);
        }

        // 보유 금액 표시
        g.setFont(smallFont);
        g.setColor(Color.white);
        g.drawString("현재 보유 금액: " + ship.getMoney() + " 골드", 330, 90);

        List<Item> items = shop.getItemsForSale();
        int itemWidth = 350, itemHeight = 80, gap = 20, startX = 50, startY = 120;

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int row = i / 2, col = i % 2;
            int x = startX + col * (itemWidth + gap);
            int y = startY + row * (itemHeight + gap / 2);

            // 아이템 슬롯 - 스프라이트가 있으면 사용, 없으면 사각형
            if (shopItemSlot != null) {
                shopItemSlot.drawScaled(g, x, y, itemWidth, itemHeight - 5);
            } else {
                g.setColor(new Color(50, 50, 50, 150));
                g.fillRect(x, y, itemWidth, itemHeight - 5);
            }

            // 아이템 정보 텍스트
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
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, 800, 600);
        g.setColor(Color.white);

        // 제목
        g.setFont(titleFont);
        String title = "SPACE INVADERS";
        g.drawString(title, (800 - g.getFontMetrics().stringWidth(title)) / 2, 200);

        // 버튼
        if (startBtn != null) {
            int MAX_BTN_W = 700, MAX_BTN_H = 500;
            int bw = startBtn.getWidth(), bh = startBtn.getHeight();
            double scale = Math.min(MAX_BTN_W / (double) bw, MAX_BTN_H / (double) bh);
            int dw = (int) Math.round(bw * scale);
            int dh = (int) Math.round(bh * scale);
            int btnX = (800 - dw) / 2;
            int btnY = (600 - dh) / 2 + 40;
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
