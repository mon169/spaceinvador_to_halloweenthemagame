package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.UserEntity;
import org.newdawn.spaceinvaders.entity.FortressEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

import org.newdawn.spaceinvaders.shop.Shop;

import org.newdawn.spaceinvaders.manager.EntityManager;
import org.newdawn.spaceinvaders.manager.BackgroundManager;
import org.newdawn.spaceinvaders.manager.StageManager;
import org.newdawn.spaceinvaders.manager.StateManager;
import org.newdawn.spaceinvaders.manager.InputManager;
import org.newdawn.spaceinvaders.manager.UIManager;

/**
 * 🎮 Game — 메인 루프 & 게임 상태 관리자
 * - StartScreen → Game() → gameLoop() 순서로 진입
 * - Stage1~5(네가 준 MonsterEntity/Boss 구조)와 완전 호환
 * - UIManager로 HUD/상점/메시지 출력
 */
public class Game extends Canvas {

    // ========= 기본 디스플레이/루프 =========
    private BufferStrategy strategy;
    private JFrame container;
    private boolean gameRunning = true;

    private String windowTitle = "🎃 Halloween Space Invaders";

    // ========= 엔티티 =========
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> removeList = new ArrayList<>();

    private UserEntity ship;
    private FortressEntity fortress;

    // ========= 매니저 =========
    private EntityManager entityManager;
    private BackgroundManager backgroundManager;
    private StageManager stageManager;
    private StateManager stateManager;
    private InputManager inputManager;
    private UIManager uiManager;

    // ========= 게임 상태 =========
    private boolean waitingForKeyPress = true;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;

    private boolean shopOpen = false;
    private boolean bossSpawned = false; // 보스 스폰 여부 필요 시 사용

    private int currentStage = 1;
    private final int MAX_STAGE = 5;

    private long stageStartTime = 0;
    private long lastFpsTime = 0;
    private int fps = 0;
    private long lastFire = 0;

    private int alienCount = 0; // UI/스테이지/클리어 로직에서 참조

    // UI 메시지
    private String message = "";

    // ========= 규칙값 (UIManager가 Game에 물어봄) =========
    private final int BASE_TIME_LIMIT = 150; // ⏱ 기본 150초
    private final int LIFE_LIMIT = 3;        // Stage3 제한 체력

    // ========= 기타 =========
    private Sprite bg;
    private Shop shop = new Shop();

    // ========= 생성자 =========
    public Game() {
        initWindow();
        initManagers();
        initEntities();
        stageStartTime = System.currentTimeMillis();
    }

    // ========= 초기화 =========
    private void initWindow() {
        container = new JFrame(windowTitle);

        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(800, 600));
        panel.setLayout(null);

        setBounds(0, 0, 800, 600);
        panel.add(this);

        setIgnoreRepaint(true);

        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        createBufferStrategy(2);
        strategy = getBufferStrategy();

        requestFocus();
    }

    private void initManagers() {
        entityManager = new EntityManager(this, entities, removeList);
        backgroundManager = new BackgroundManager();
        uiManager = new UIManager(this);
        stateManager = new StateManager(this, uiManager);
        stageManager = new StageManager(this, entityManager);
        inputManager = new InputManager(this);
        addKeyListener(inputManager);

        bg = SpriteStore.get().getSprite("bg/level1_background.jpg");
    }

    private void initEntities() {
        entities.clear();

        // 플레이어
        ship = new UserEntity(this, "sprites/userr.png", 370, 520);
        entities.add(ship);

        // 요새
        fortress = new FortressEntity(this, "sprites/candybucket.png", 320, 460);
        entities.add(fortress);

        // 스테이지 로드
        stageManager.loadStage(currentStage);
    }

    // ========= 메인 루프 =========
    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();

        while (gameRunning) {
            try {
                long now = System.currentTimeMillis();
                long delta = now - lastLoopTime;
                lastLoopTime = now;

                // FPS
                lastFpsTime += delta;
                fps++;
                if (lastFpsTime >= 1000) {
                    container.setTitle(windowTitle + " (FPS: " + fps + ")");
                    lastFpsTime = 0;
                    fps = 0;
                }

                Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

                // 배경
                backgroundManager.draw(g, bg, 0);

                // 스테이지/엔티티 갱신
                if (!waitingForKeyPress) {
                    stageManager.spawnWave(currentStage, stageStartTime);
                    entityManager.moveEntities(delta);
                    entityManager.checkCollisions();
                    entityManager.cleanupEntities();
                }

                // 엔티티 그리기
                for (int i = 0; i < entities.size(); i++) {
                    entities.get(i).draw(g);
                }

                // UI (HUD/상점/메시지 등)
                uiManager.drawFullUI(
                    g,
                    this,
                    ship,
                    fortress,
                    entities,
                    message,
                    shopOpen,
                    waitingForKeyPress
                );

                g.dispose();
                strategy.show();

                // 입력 반영
                handleMovement();
                handleFiring();

                Thread.sleep(10);
            } catch (Exception ex) {
                System.err.println("⚠️ 게임 루프 오류: " + ex.getMessage());
                ex.printStackTrace();
                safelyResetGameState();
            }
        }
    }

    // ========= 입력 처리 =========
    private void handleMovement() {
        if (ship == null) return;

        ship.setHorizontalMovement(0);
        if (leftPressed && !rightPressed) {
            ship.setHorizontalMovement(-ship.getMoveSpeed());
        } else if (rightPressed && !leftPressed) {
            ship.setHorizontalMovement(ship.getMoveSpeed());
        }
    }

    private void handleFiring() {
        if (ship == null) return;
        if (!firePressed) return;

        tryToFire();
    }

    // ========= 공격 =========
    public void tryToFire() {
        if (System.currentTimeMillis() - lastFire < ship.getFiringInterval()) return;

        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(this, "sprites/shot.png", ship.getX() + 10, ship.getY() - 30);
        entities.add(shot);
    }

    // ========= 스테이지 제어 =========
    public void startGameOrNextStage(boolean restartFromZero) {
        UserEntity prev = ship;
        if (restartFromZero) prev = null;

        stageStartTime = System.currentTimeMillis();
        entities.clear();

        if (prev == null) {
            ship = new UserEntity(this, "sprites/userr.png", 370, 520);
        } else {
            ship = new UserEntity(this, "sprites/userr.png", 370, 520);
            ship.copyStateFrom(prev);
        }
        entities.add(ship);

        fortress = new FortressEntity(this, "sprites/candybucket.png", 320, 460);
        entities.add(fortress);

        stageManager.loadStage(currentStage);

        leftPressed = rightPressed = firePressed = false;
        waitingForKeyPress = false;
        shopOpen = false;
        bossSpawned = false;
        message = "";
        alienCount = 0;
    }

    private void nextStage() {
        if (currentStage >= MAX_STAGE) {
            notifyWin();
            return;
        }
        currentStage++;
        startGameOrNextStage(false);
    }

    // ========= 이벤트(보스/승패/적 처치) =========
    public void bossDefeated() {
        bossSpawned = false;
        if (ship != null) ship.earnMoney(500);

        message = "🎉 Stage " + currentStage + " 클리어!\n보스를 물리쳤습니다!";
        waitingForKeyPress = true;
        shopOpen = currentStage < MAX_STAGE;

        if (currentStage == MAX_STAGE) {
            message = "👑 모든 스테이지 클리어!\n축하합니다!";
            shopOpen = false;
        }
    }

    public void notifyDeath() {
        message = "💀 패배했습니다! 다시 도전하시겠습니까?";
        waitingForKeyPress = true;
        shopOpen = false;
    }

    public void notifyFortressDestroyed() {
        message = "🏰 요새가 파괴되었습니다!";
        waitingForKeyPress = true;
        shopOpen = false;
    }

    public void notifyWin() {
        message = "축하합니다! 모든 스테이지를 클리어했습니다!\nESC키를 누르면 게임이 종료됩니다.";
        waitingForKeyPress = true;
        shopOpen = false;
    }

    /** 🔔 적 처치 시(ShotEntity, BombShotEntity 등에서 호출) */
    public void notifyAlienKilled() {
        alienCount--;
        if (alienCount < 0) alienCount = 0;
        System.out.println("👻 남은 적: " + alienCount);
    }

    // ========= 상점 =========
    public void handleShopKey(char key) {
        if (!shopOpen || shop == null || ship == null) return;

        if (key >= '1' && key <= '9') {
            int index = key - '1';
            purchaseItem(index);
        } else if (key == 'r' || key == 'R') {
            if (currentStage == MAX_STAGE) {
                message = "🎆 모든 스테이지 완료!";
                shopOpen = false;
                waitingForKeyPress = true;
            } else {
                currentStage++;
                startGameOrNextStage(false);
            }
        } else if (key == 27) { // ESC
            System.exit(0);
        }
    }

    public void purchaseItem(int index) {
        try {
            shop.purchaseItem(ship, index);
        } catch (Exception e) {
            System.err.println("⚠️ 아이템 구매 오류: " + e.getMessage());
        }
    }

    // ========= 안전 초기화 =========
    private void safelyResetGameState() {
        entities.clear();
        removeList.clear();
        waitingForKeyPress = true;
        shopOpen = false;
        currentStage = 1;
        message = "";
        initEntities();
    }

    // ========= 외부에서 쓰는 조작/종료 =========
    public void endGame() {
        System.exit(0);
    }

    // ========= setters (입력용) =========
    public void setLeftPressed(boolean v) { leftPressed = v; }
    public void setRightPressed(boolean v) { rightPressed = v; }
    public void setFirePressed(boolean v) { firePressed = v; }

    public boolean isWaitingForKeyPress() { return waitingForKeyPress; }
    public void setWaitingForKeyPress(boolean v) { waitingForKeyPress = v; }

    public boolean isShopOpenFlag() { return shopOpen; }
    public void setShopOpenFlag(boolean v) { shopOpen = v; }

    // ========= getters (엔티티/매니저/규칙) =========
    public UserEntity getShip() { return ship; }
    public FortressEntity getFortress() { return fortress; }
    public List<Entity> getEntities() { return entities; }

    public void addEntity(Entity e) { entities.add(e); }
    public void removeEntity(Entity e) { removeList.add(e); }

    public long getStageStartTime() { return stageStartTime; }
    public int getCurrentStage() { return currentStage; }

    public int getAlienCount() { return alienCount; }
    public void setAlienCount(int count) { alienCount = count; }

    public int getBaseTimeLimit() { return BASE_TIME_LIMIT; }
    public int getLifeLimit() { return LIFE_LIMIT; }

    public Shop getShop() { return this.shop; }
}
