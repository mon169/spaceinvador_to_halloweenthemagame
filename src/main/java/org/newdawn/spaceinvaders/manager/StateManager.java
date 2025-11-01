package org.newdawn.spaceinvaders.manager;

import java.awt.Graphics2D;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.FortressEntity;
import org.newdawn.spaceinvaders.entity.UserEntity;

/** 🧩 StateManager — 체력/요새/사망·클리어 이벤트 */
public class StateManager {
    private final Game game;
    private final UIManager uiManager;

    public StateManager(Game game, UIManager uiManager) {
        this.game = game;
        this.uiManager = uiManager;
    }

    public void updateState(Graphics2D g, UserEntity ship, FortressEntity fortress, int currentStage) {
        if (ship != null && ship.getHealth() <= 0) game.notifyDeath();
        if (fortress != null && fortress.getHP() <= 0) game.notifyFortressDestroyed();
    }
}
