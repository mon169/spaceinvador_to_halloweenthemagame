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
import org.newdawn.spaceinvaders.entity.UserEntity2; // 🔥[ADDED] 2P 캐릭터 클래스
import org.newdawn.spaceinvaders.entity.FortressEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.MonsterEntity;
import org.newdawn.spaceinvaders.entity.ShieldEntity;

import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

import org.newdawn.spaceinvaders.shop.Shop;

import org.newdawn.spaceinvaders.manager.EntityManager;
import org.newdawn.spaceinvaders.manager.BackgroundManager;
import org.newdawn.spaceinvaders.manager.StageManager;
import org.newdawn.spaceinvaders.manager.StateManager;
import org.newdawn.spaceinvaders.manager.InputManager;
import org.newdawn.spaceinvaders.manager.UIManager;
import org.newdawn.spaceinvaders.sound.SoundManager;

// 🔥[ADDED] 네트워크(소켓) 협동 플레이용 import
import network.GameClient;
import network.Packet;
import java.io.IOException;
import java.util.UUID;

/**
 * 🎮 Game — 메인 루프 & 게임 상태 관리자
 * - 죽으면 현재 스테이지 그대로 재시작
 * - 보스 처치 시 상점 → R 키로 다음 스테이지 이동
 * - Stage1~5 완전 호환
 */
public class Game extends Canvas {
    private static final int MAX_STAGE = 5;
    private static final int BASE_TIME_LIMIT = 150;
    private static final int LIFE_LIMIT = 3;

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

    // 🔥[ADDED] 2P(상대 플레이어) 엔티티
    private UserEntity2 ship2; // 상대 플레이어 표현용

    // ========= 매니저 =========
    private EntityManager entityManager;
    private BackgroundManager backgroundManager;
    private StageManager stageManager;
    private StateManager stateManager;
    private InputManager inputManager;
    private UIManager uiManager;
    private org.newdawn.spaceinvaders.manager.RewardManager rewardManager;

    // 🔥[ADDED] 소켓 네트워크 필드
    private GameClient client;              // 클라이언트 소켓
    private boolean networkConnected = false; // 소켓 연결 여부
    private final String playerId = UUID.randomUUID().toString().substring(0, 6); // 내 플레이어 식별자
    private boolean socketClosedNotified = false; // 🔥 추가됨 — 무한 반복 방지 플래그

    // ========= 게임 상태 =========
    private boolean waitingForKeyPress = true;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;

    private boolean shopOpen = false;
    private boolean bossSpawned = false;

    private int currentStage = 1;

    private long stageStartTime = 0;
    private long lastFpsTime = 0;
    private int fps = 0;
    private long lastFire = 0;

    private int alienCount = 0; // 🧮 현재 몬스터 수

    private String message = "";
    // flag: 다음 startGameOrNextStage 호출 시 이전 플레이어 상태를 유지할지 여부
    private boolean retainPlayerOnNextStart = false;


    private Sprite bg;
    private Shop shop = new Shop();

    // ========= 생성자 =========
    public Game() {
        initWindow();
        initManagers();
        initEntities();
        stageStartTime = System.currentTimeMillis();

        // 🔥[ADDED] 소켓 초기화 (GameServer가 켜져 있으면 자동 연결)
        initSocket();
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
        rewardManager = new org.newdawn.spaceinvaders.manager.RewardManager();
        addKeyListener(inputManager);

        bg = SpriteStore.get().getSprite("bg/level1_background.jpg");
    }

    private void initEntities() {
        entities.clear();

        ship = new UserEntity(this, "sprites/userr.png", 370, 520);
        entities.add(ship);

        fortress = new FortressEntity(this, "sprites/candybucket.png", 320, 460);
        entities.add(fortress);

        // 🔥[ADDED] 2P 엔티티(상대)는 네트워크 연결 시에만 생성
        // initSocket()이 호출된 후 networkConnected 상태를 확인하여 생성

        stageManager.loadStage(currentStage);
        stageManager.resetAllStageFlags(); // ✅ 보스/웨이브 리셋 호출

        // ✅ 스테이지 로드 후 즉시 몬스터 수 집계
        countMonsters();
    }

    // 🔥[ADDED] 소켓 연결 (없으면 무시하고 싱글로 동작)
    private void initSocket() {
        try {
            client = new GameClient("localhost", 9999, this::onPacketReceived);
            networkConnected = true;
            System.out.println("✅ 소켓 연결 성공 — 2인 협동 활성화 (ID: " + playerId + ")");
            
            // 네트워크 연결 성공 시에만 2P 엔티티 생성
            try {
                ship2 = new UserEntity2(this, "sprites/user2r.png", 420, 520);
                entities.add(ship2);
            } catch (Exception ignore) {
                // 만약 리소스가 아직 없다면 생략해도 게임은 동작
            }
        } catch (IOException e) {
            System.out.println("⚠️ 소켓 서버 연결 실패 — 싱글 모드로 실행합니다.");
            networkConnected = false; // 🔥 추가
        }
    }

    // 🔥[ADDED] 패킷 수신 콜백 → 상대(ship2) 상태 갱신
    private void onPacketReceived(Packet packet) {
        if (packet == null || packet.playerId == null) return;
        if (packet.playerId.equals(playerId)) return; // 내 패킷은 무시

        if (ship2 != null) {
            ship2.updateFromNetwork(packet.x, packet.y, packet.hp);
            System.out.println("📡 2P 위치 수신 → x=" + packet.x + ", y=" + packet.y);
        }
    }

    // ========= 실시간 몬스터 수 집계 =========
    public void countMonsters() {
        int count = 0;
        for (Entity e : entities) {
            if (e instanceof MonsterEntity || e.getClass().getSimpleName().equals("BombMonsterEntity")) {
                count++;
            }
        }
        alienCount = count;
        System.out.println("📊 현재 몬스터 수: " + alienCount);
    }

    // ========= 메인 루프 =========
    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();

        while (gameRunning) {
            try {
                long now = System.currentTimeMillis();
                long delta = now - lastLoopTime;
                lastLoopTime = now;

                lastFpsTime += delta;
                fps++;
                if (lastFpsTime >= 1000) {
                    container.setTitle(windowTitle + " (FPS: " + fps + ")");
                    lastFpsTime = 0;
                    fps = 0;
                }

                Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

                // 배경 (시작 화면이 아닐 때만 게임 배경 그리기)
                if (!waitingForKeyPress || shopOpen || (message != null && !message.isEmpty())) {
                    backgroundManager.draw(g, bg, 0);
                }

                if (!waitingForKeyPress) {
                    stageManager.spawnWave(currentStage, stageStartTime);
                    entityManager.moveEntities(delta);
                    entityManager.checkCollisions();
                    entityManager.cleanupEntities();
                }

                // 엔티티 그리기 (방어막은 마지막에 그려서 다른 엔티티가 위에 오도록)
                // 복사본으로 순회하여 ConcurrentModification 예외 방지
                List<Entity> entitiesCopy = new ArrayList<>(entities);
                for (Entity e : entitiesCopy) {
                    if (e instanceof ShieldEntity) {
                        // 방어막은 나중에 그리기 위해 스킵
                        continue;
                    }
                    e.draw(g);
                }
                // 방어막은 마지막에 그리기 (다른 엔티티 위에 표시되지만 투명도 조절)
                for (Entity e : entitiesCopy) {
                    if (e instanceof ShieldEntity) {
                        e.draw(g);
                    }
                }

                // UI
                uiManager.drawFullUI(g, this, ship, fortress, entities, message, shopOpen, waitingForKeyPress);

                // 보상 메시지 렌더링 (우상단 토스트)
                if (rewardManager != null) {
                    rewardManager.drawRewardMessages(g);
                }

                g.dispose();
                strategy.show();

                handleMovement();
                handleFiring();

                // 🔥 수정됨 — 연결이 끊긴 후 무한 출력 방지
                if (networkConnected && client != null && ship != null) {
                    try {
                        client.send(new Packet(
                                playerId,
                                (int) ship.getX(),
                                (int) ship.getY(),
                                firePressed,
                                ship.getHp(),
                                ship.getScore()
                        ));
                    } catch (IOException io) {
                        if (!socketClosedNotified) {
                            System.out.println("⚠️ 서버 연결 끊김 — 싱글 모드로 전환합니다.");
                            socketClosedNotified = true;
                        }
                        networkConnected = false;
                    }
                }

                Thread.sleep(10);
            } catch (Exception ex) {
                System.err.println("⚠️ 게임 루프 오류: " + ex.getMessage());
                ex.printStackTrace();
                safelyRestartCurrentStage();
            }
        }
    }

    // ========= 입력 처리 =========
    private void handleMovement() {
        if (ship == null) return;
        ship.setHorizontalMovement(0);
        if (leftPressed && !rightPressed) ship.setHorizontalMovement(-ship.getMoveSpeed());
        else if (rightPressed && !leftPressed) ship.setHorizontalMovement(ship.getMoveSpeed());
    }

    private void handleFiring() {
        if (ship == null || !firePressed) return;
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
    public void startGameOrNextStage(int stageToRestart) {
        // 0이면 완전 처음부터, 아니면 해당 스테이지에서 재시작
        if (stageToRestart <= 0) currentStage = 1;
        else currentStage = stageToRestart;

        stageStartTime = System.currentTimeMillis();
        // 기존 플레이어 상태 보존을 위해 기존 ship 참조 보관
        UserEntity oldShip = this.ship;

        entities.clear();

        UserEntity newShip = new UserEntity(this, "sprites/userr.png", 370, 520);
        // 다음 시작에서 이전 상태를 보존하도록 표시된 경우에만 복사
        if (retainPlayerOnNextStart && oldShip != null) {
            newShip.copyStateFrom(oldShip);
        }
        // 보존 플래그는 일회성
        retainPlayerOnNextStart = false;

        ship = newShip;
        entities.add(ship);

        fortress = new FortressEntity(this, "sprites/candybucket.png", 320, 460);
        entities.add(fortress);

        // 🔥[ADDED] 재시작 시에도 네트워크 연결되어 있으면 2P 엔티티 추가
        if (networkConnected) {
            try {
                ship2 = new UserEntity2(this, "sprites/user2r.png", 420, 520);
                entities.add(ship2);
            } catch (Exception ignore) {}
        }

        stageManager.loadStage(currentStage);

        leftPressed = rightPressed = firePressed = false;
        waitingForKeyPress = false;
        shopOpen = false;
        bossSpawned = false;
        message = "";

        countMonsters();

        System.out.println("🔁 Stage " + currentStage + " 재시작 완료");

        // 게임 BGM 재생
        SoundManager.playGameBgmLoop();
    }

    // ========= 사망 처리 =========
    public void gameOver() {
        waitingForKeyPress = true;
        message = "💀 사망했습니다!\nR 키를 눌러 다시 도전하세요";
        shopOpen = false;
    }

    public void restartCurrentStage() {
        System.out.println("💀 Stage " + currentStage + " 재도전 시작");
        startGameOrNextStage(currentStage);
    }

    // ========= 보스 처치 이벤트 =========
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

    public void notifyAlienKilled() {
        alienCount--;
        if (alienCount < 0) alienCount = 0;
        System.out.println("💥 몬스터 처치됨 (남은 적: " + alienCount + ")");
        
        // 🎁 랜덤 보상 지급
        if (rewardManager != null && ship != null) {
            rewardManager.grantReward(ship);
        }
    }

    public void notifyDeath() {
        message = "💀 패배했습니다! R 키로 다시 도전!";
        waitingForKeyPress = true;
        shopOpen = false;
    }

    public void notifyFortressDestroyed() {
        message = "🏰 요새가 파괴되었습니다!\nR 키를 눌러 다시 도전하세요!";
        waitingForKeyPress = true;
        shopOpen = false;
    }

    public void notifyWin() {
        message = "축하합니다! 모든 스테이지를 클리어했습니다!\nESC키를 누르면 게임이 종료됩니다.";
        waitingForKeyPress = true;
        shopOpen = false;
    }

    // ========= 상점 =========
    public void handleShopKey(char key) {
        if (!shopOpen || shop == null || ship == null) return;

        if (key >= '1' && key <= '9') {
            purchaseItem(key - '1');
        } else if (key == 'r' || key == 'R') {
            if (!shopOpen && waitingForKeyPress) {
                // 💀 사망 상태에서 R → 스테이지 재도전
                restartCurrentStage();
                return;
            }
            if (currentStage == MAX_STAGE) {
                message = "🎆 모든 스테이지 완료!";
                shopOpen = false;
                waitingForKeyPress = true;
            } else {
                // ✅ 다음 스테이지로 이동
                currentStage++;
                // 다음 시작에서는 플레이어가 상점에서 구매한 상태를 유지
                this.retainPlayerOnNextStart = true;
                waitingForKeyPress = false;
                shopOpen = false;
                stageStartTime = System.currentTimeMillis();
                System.out.println("🚀 다음 스테이지로 이동: Stage " + currentStage);
                startGameOrNextStage(currentStage);
            }
        } else if (key == 27) System.exit(0);
    }

    public void purchaseItem(int index) {
        try {
            shop.purchaseItem(ship, index);
        } catch (Exception e) {
            System.err.println("⚠️ 아이템 구매 오류: " + e.getMessage());
        }
    }

    // ========= 안전 초기화 =========
    private void safelyRestartCurrentStage() {
        System.out.println("⚠️ 예외 발생 — 현재 스테이지 재시작");
        startGameOrNextStage(currentStage);
    }

    // ========= 조작 =========
    public void endGame() { System.exit(0); }
    public void setLeftPressed(boolean v) { leftPressed = v; }
    public void setRightPressed(boolean v) { rightPressed = v; }
    public void setFirePressed(boolean v) { firePressed = v; }
    public boolean isWaitingForKeyPress() { return waitingForKeyPress; }
    public void setWaitingForKeyPress(boolean v) { waitingForKeyPress = v; }
    public boolean isShopOpenFlag() { return shopOpen; }
    public void setShopOpenFlag(boolean v) { shopOpen = v; }

    // ========= getters =========
    public UserEntity getShip() { return ship; }
    public FortressEntity getFortress() { return fortress; }
    public List<Entity> getEntities() { return entities; }

    public void addEntity(Entity e) {
        entities.add(e);
        // Debug: 로그를 찍어 어떤 엔티티가 추가되는지 확인
        System.out.println("➕ 엔티티 추가: " + e.getClass().getSimpleName() + " 위치(" + e.getX() + "," + e.getY() + ")");
        if (e instanceof MonsterEntity || e.getClass().getSimpleName().equals("BombMonsterEntity")) {
            alienCount++;
            System.out.println("👾 몬스터 추가됨: 총 " + alienCount + "마리");
        }
    }

    public void removeEntity(Entity e) {
        if (!removeList.contains(e)) removeList.add(e);
    }
    
    /** 활성화된 방어막이 있는지 확인 */
    public boolean hasActiveShield() {
        for (Entity e : entities) {
            if (e instanceof ShieldEntity) {
                ShieldEntity shield = (ShieldEntity) e;
                if (shield.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getStageStartTime() { return stageStartTime; }
    public int getCurrentStage() { return currentStage; }
    public int getAlienCount() { return alienCount; }
    public void setAlienCount(int count) { alienCount = count; }
    public int getBaseTimeLimit() { return BASE_TIME_LIMIT; }
    public int getLifeLimit() { return LIFE_LIMIT; }
    public Shop getShop() { return this.shop; }

    // 배경 변경 메서드
    public void setBackground(String bgPath) {
        this.bg = SpriteStore.get().getSprite(bgPath);
    }

    // ✅ 메인 실행 진입점 추가
    public static void main(String[] args) {
        Game game = new Game();
        game.gameLoop();
    }
}