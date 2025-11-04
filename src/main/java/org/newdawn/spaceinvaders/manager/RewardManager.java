package org.newdawn.spaceinvaders.manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.newdawn.spaceinvaders.entity.UserEntity;

/**
 * 🎁 RewardManager — 적 처치 시 랜덤 보상 관리
 * - 기본 보상: 항상 30골드 지급
 * - 랜덤 드롭: 추가 골드, 폭탄, 얼음 무기, 방어막
 * - 토스트 메시지: 우상단에 2초간 표시
 */
public class RewardManager {
    private static final int REWARD_SHOW_MS = 2000; // 2초간 표시
    private static final int MAX_REWARD_LOG = 5;     // 최대 5개 메시지

    /**
     * 보상 메시지 내부 클래스
     */
    private static class RewardMsg {
        String text;
        long untilMs;

        RewardMsg(String text, long untilMs) {
            this.text = text;
            this.untilMs = untilMs;
        }
    }

    private List<RewardMsg> rewardLog = new ArrayList<>();
    private Font messageFont = new Font("맑은 고딕", Font.PLAIN, 14);

    /**
     * 적 처치 시 보상 지급
     */
    public void grantReward(UserEntity ship) {
        if (ship == null) return;

        // 1. 기본 보상: 항상 30골드
        ship.earnMoney(30);
        showRewardMessage("💰 +30 골드");

        // 2. 확률 분기 (랜덤 드롭)
        double r = Math.random();

        if (r < 0.60) {
            // 60% 확률: 추가 골드 10-40
            int extraGold = 10 + (int)(Math.random() * 31); // 10~40
            ship.earnMoney(extraGold);
            showRewardMessage("💰 +" + extraGold + " 골드");
        } else if (r < 0.80) {
            // 20% 확률: 폭탄 +1
            ship.giveBomb();
            showRewardMessage("💣 폭탄 +1");
        } else if (r < 0.95) {
            // 15% 확률: 얼음 무기 +1
            ship.giveIceWeapon();
            showRewardMessage("🧊 얼음 공격 +1");
        } else {
            // 5% 확률: 방어막 +1
            ship.giveShield();
            showRewardMessage("🛡 방어막 +1");
        }
    }

    /**
     * 보상 메시지 추가 (토스트)
     */
    private void showRewardMessage(String text) {
        long untilMs = System.currentTimeMillis() + REWARD_SHOW_MS;
        rewardLog.add(new RewardMsg(text, untilMs));

        // 최대 개수 초과 시 오래된 것부터 제거
        while (rewardLog.size() > MAX_REWARD_LOG) {
            rewardLog.remove(0);
        }
    }

    /**
     * 만료된 메시지 제거
     */
    public void pruneRewardLog() {
        long now = System.currentTimeMillis();
        Iterator<RewardMsg> it = rewardLog.iterator();
        while (it.hasNext()) {
            RewardMsg msg = it.next();
            if (now >= msg.untilMs) {
                it.remove();
            }
        }
    }

    /**
     * 우상단에 보상 메시지 그리기
     */
    public void drawRewardMessages(Graphics2D g) {
        pruneRewardLog();

        if (rewardLog.isEmpty()) return;

        g.setColor(Color.yellow);
        g.setFont(messageFont);

        int startX = 580;  // 우상단 X 좌표
        int startY = 200;  // 보유 아이템 패널 아래쪽 (겹치지 않도록)
        int lineHeight = 22; // 줄 간격

        // 반투명 배경
        g.setColor(new Color(0, 0, 0, 150));
        int bgHeight = rewardLog.size() * lineHeight + 10;
        g.fillRect(startX - 10, startY - 25, 220, bgHeight);

        // 메시지 그리기 (위에서 아래로)
        g.setColor(Color.yellow);
        for (int i = 0; i < rewardLog.size(); i++) {
            RewardMsg msg = rewardLog.get(i);
            g.drawString(msg.text, startX, startY + (i * lineHeight));
        }
    }
}

