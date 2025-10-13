package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.FortressEntity;
import org.newdawn.spaceinvaders.entity.FrankenBossEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.shop.Shop;
import org.newdawn.spaceinvaders.shop.Item;

//배경추가
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

import org.newdawn.spaceinvaders.entity.ObstacleEntity;
import org.newdawn.spaceinvaders.shop.Shop;
import org.newdawn.spaceinvaders.shop.Item;

/**
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic. 
 * 
 * Display management will consist of a loop that cycles round all
 * entities in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 * 
 * As a mediator it will be informed when entities within our game
 * detect events (e.g. alient killed, played died) and will take
 * appropriate game actions.
 * 
 * @author Kevin Glass
 */
public class Game extends Canvas 
{
	/** The stragey that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
	/** The list of all the entities that exist in our game */
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public List<Entity> getEntities() {
		return new ArrayList<>(entities);
	}
	
	/** The list of entities that need to be removed from the game this loop */
	private ArrayList<Entity> removeList = new ArrayList<Entity>();
	/** The entity representing the player */
	/** The time at which last fired a shot */
	private long lastFire = 0;
	private long lastAlienShotTime = 0;
	/** The number of aliens left on the screen */
	private int alienCount;
	private int currentStage = 1;
	
	// 스테이지 관련 변수
	private long stageStartTime = 0;      // 스테이지 시작 시간
	private final int BASE_TIME_LIMIT = 150; // 기본 제한 시간 (초)
	private boolean itemsAllowed = true;  // 아이템 사용 가능 여부
	private int lifeLimit = 0;            // 생명 제한 (0이면 무제한)
	
	// 웨이브 생성 여부 확인
	private boolean wave1Spawned = false;
	private boolean wave2Spawned = false;
	private boolean wave3Spawned = false;
	private boolean bossSpawned = false;

	private final int MAX_STAGE = 5;

	public int getCurrentStage() {
        return currentStage;
    }

	/** 현재 스테이지에서 아이템 사용 가능 여부 반환 */
	public boolean itemsAllowed() {
		return itemsAllowed;
	}
	
	/** 게임 상태를 안전하게 초기화 */
	private void safelyResetGameState() {
		try {
			// 엔티티와 상태 초기화
			entities.clear();
			alienCount = 0;
			waitingForKeyPress = true;
			leftPressed = false;
			rightPressed = false;
			firePressed = false;
			shopOpen = false;
			message = "";
			
			// 플레이어 선박 새로 생성
			ship = new ShipEntity(this, "sprites/ship.png", 370, 520);
			entities.add(ship);
			
			// 스테이지 초기화
			currentStage = 1;
			setStageFeatures();
		} catch (Exception e) {
			System.out.println("게임 초기화 중 오류: " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	/** The message to display which waiting for a key press */
	private String message = "";
	/** True if we're holding up game play until a key has been pressed */
	private boolean waitingForKeyPress = true;
	/** True if the left cursor key is currently pressed */
	private boolean leftPressed = false;
	/** True if the right cursor key is currently pressed */
	private boolean rightPressed = false;
	/** True if we are firing */
	private boolean firePressed = false;
	/** True if game logic needs to be applied this loop, normally as a result of a game event */
	private boolean logicRequiredThisLoop = false;
	/** The last time at which we recorded the frame rate */
	private long lastFpsTime;
	/** The current number of frames recorded */
	private int fps;
	/** The normal title of the game window */
	private String windowTitle = "Space Invaders 102";
	/** The game window that we'll update with the frame count */
	private JFrame container;
	private ShipEntity ship;
	private FortressEntity fortress; // 요새
	private Shop shop;
	private boolean shopOpen = false;
	//background

	private Sprite bg;
	private double bgY = 0;       // 배경 세로 위치
	private double bgSpeed = 30;  // 배경 스크롤 속도(px/초), 0이면 고정



	private Sprite startBtn;
	private double startBtnScale = 0.75;

	public ShipEntity getShip() {
		return ship;
	}
	
	/**
	 * Construct our game and set it running.
	 */
	public Game() {
		// create a frame to contain our game
		container = new JFrame("Space Invaders 102");
		
		// get hold the content of the frame and set up the resolution of the game
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(800,600));
		panel.setLayout(null);
		
		// setup our canvas size and put it into the content of the frame
		setBounds(0,0,800,600);
		panel.add(this);
		
		// Tell AWT not to bother repainting our canvas since we're
		// going to do that our self in accelerated mode
		setIgnoreRepaint(true);
		
		// finally make the window visible 
		container.pack();
		container.setResizable(false);
		container.setVisible(true);
		
		// add a listener to respond to the user closing the window. If they
		// do we'd like to exit the game
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		// add a key input system (defined below) to our canvas
		// so we can respond to key pressed
		addKeyListener(new KeyInputHandler());
		
		// request the focus so key events come to us
		requestFocus();

		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		
		// === BG load ===
		bg = SpriteStore.get().getSprite("bg/level1_background.jpg");

		// === Start button load ===
		startBtn = SpriteStore.get().getSprite("sprites/startbutton.png");

		// initialise the entities in our game so there's something
		// to see at startup
		initEntities();
		shop = new Shop(); 
	}
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
		private void startGame() {
		ShipEntity oldShip = null;
		if (ship != null) {
			// 이전 우주선 상태 저장
			oldShip = ship;
			leftPressed = false;
			rightPressed = false;
			firePressed = false;
			shopOpen = false;
			bossSpawned = false; // 보스 초기화
			stageStartTime = System.currentTimeMillis(); // 새 스테이지 시작 시간
		}
		
		// 처음 시작이거나 재시작일 때 스테이지 초기화
		if (message.contains("restart")) {
			currentStage = 1;
			oldShip = null; // 재시작 시 새 우주선 생성
		} else if (message.isEmpty()) {
			currentStage = 1; // 첫 시작 시 스테이지 1로 설정
		}
		// 스테이지 클리어 시에는 currentStage 유지

		// 스테이지 특성 적용
		setStageFeatures();
		stageStartTime = System.currentTimeMillis();

		try {
			// 엔티티 초기화
			entities.clear();
			initEntities(oldShip);
		} catch (Exception e) {
			System.out.println("게임 재시작 중 오류: " + e.getMessage());
			e.printStackTrace();
			safelyResetGameState();
		}

		// 키 입력 상태 초기화
		leftPressed = false;
		rightPressed = false;
		firePressed = false;
		shopOpen = false;
	}
	
	/** 현재 스테이지 특성 설정 */
	private void setStageFeatures() {
		// 기본값
		itemsAllowed = true;
		lifeLimit = 0;
		
		switch (currentStage) {
			case 2:
				// 스테이지 2: 적의 공격 속도 약간 증가 (AlienEntity에서 처리)
				break;
			case 3:
				// 스테이지 3: 생명 제한 모드
				lifeLimit = 3; // 체력 3 이하일 때 게임 오버
				break;
			// 스테이지 5: 장애물 강화, 아이템 제한 없음
		}
	}

	
		/** 게임 시작 시 엔티티(우주선, 적 등) 초기화 */
	private void initEntities() {
		initEntities(null);
	}
	
	/** 이전 우주선 상태를 이어받을 수 있는 엔티티 초기화 */
	private void initEntities(ShipEntity oldShip) {
	    // 플레이어 우주선 생성
	    if (oldShip == null) {
	        ship = new ShipEntity(this, "sprites/ship.png", 370, 520);
	    } else {
	        ship = new ShipEntity(this, "sprites/ship.png", 370, 520);
	        ship.copyStateFrom(oldShip); // 이전 상태 복사
	    }
	    entities.add(ship);
	    alienCount = 0;

	    // 기본 적 생성
	    for (int i = 0; i < 6; i++) {
	        AlienEntity alien = new AlienEntity(this, 100 + (i * 100), 80);
	        alien.setShotType("normal");
	        entities.add(alien);
	        alienCount++;
	    }

	    // 스테이지 4 이상일 때 장애물 생성
	    if (currentStage >= 4) {
	        int obstacleRows = (currentStage >= 5) ? 2 : 1;
	        int panelWidth = 800;
	        int obstacleWidth = 32;
	        int obstacleCount = panelWidth / obstacleWidth;
	        int startX = 0;

	        for (int row = 0; row < obstacleRows; row++) {
	            for (int x = 0; x < obstacleCount; x++) {
	                int obsX = startX + (x * obstacleWidth);
	                int obsY = 380 + (row * 40);
	                ObstacleEntity obstacle = new ObstacleEntity(this, obsX, obsY);
	                entities.add(obstacle);
	            }
	        }
	    }

	    // 요새(사탕 바구니) 추가
	    fortress = new FortressEntity(this, "sprites/candybucket.png", 320, 460);
	    entities.add(fortress);
	}

	/** 다음 루프에서 게임 로직 업데이트 요청 */
	public void updateLogic() {
		logicRequiredThisLoop = true;
	}
	
	/** 지정된 엔티티 제거 */
	public void removeEntity(Entity entity) {
		removeList.add(entity);
	}
	
	/** 요새가 파괴되었을 때 처리 */
	public void notifyFortressDestroyed() {
	    message = "요새가 파괴되었습니다! 게임 오버!";
	    waitingForKeyPress = true;
	}

	/** 플레이어가 사망했을 때 처리 */
	public void notifyDeath() {
		message = "당신의 우주선이 파괴되었습니다! 다시 시도해보세요.";
		waitingForKeyPress = true;
	}
	
	/** 모든 적 처치 시 승리 처리 */
	public void notifyWin() {
		message = "축하합니다! 모든 스테이지를 클리어했습니다!\nESC키를 누르면 게임이 종료됩니다.";
		waitingForKeyPress = true;
		shopOpen = false;
	}

	/** 엔티티 추가 */
	public void addEntity(Entity entity) {
		entities.add(entity);
	}


// 보스 처치 시 호출
public void bossDefeated() {
    System.out.println("보스 처치 완료! 스테이지 클리어");

    alienCount = Math.max(0, alienCount - 1);
    bossSpawned = false;

    if (ship != null) {
        ship.earnMoney(500);
        System.out.println("보상 500 골드 지급");
    }

    message = "Stage " + currentStage + " 클리어!\n보스를 물리쳤습니다!";
    waitingForKeyPress = true;
    shopOpen = true;
}

private void drawBgCover(Graphics2D g, Sprite s, int offY) {
    if (s == null) return;

    int iw = s.getWidth(), ih = s.getHeight();
    double scale = Math.max(800.0 / iw, 600.0 / ih);
    int dw = (int) Math.round(iw * scale);
    int dh = (int) Math.round(ih * scale);
    int dx = (800 - dw) / 2;

    int sy = -(offY % dh);
    s.drawScaled(g, dx, sy, dw, dh);
    s.drawScaled(g, dx, sy + dh, dw, dh);
}

private void updateAlienCount() {
    int count = 0;
    for (Entity entity : entities) {
        if (entity instanceof AlienEntity) {
            count++;
        }
    }
    alienCount = count;
}

public void notifyAlienKilled() {
    ship.earnMoney(30);
    updateAlienCount();
}

public void tryToFire() {
    if (System.currentTimeMillis() - lastFire < ship.getFiringInterval()) {
        return;
    }

    lastFire = System.currentTimeMillis();
    ShotEntity shot = new ShotEntity(this, "sprites/shot.png", ship.getX() + 10, ship.getY() - 30);
    entities.add(shot);
}

public void gameLoop() {
    long lastLoopTime = SystemTimer.getTime();
    while (gameRunning) {
        try {
            long delta = SystemTimer.getTime() - lastLoopTime;
            lastLoopTime = SystemTimer.getTime();

            lastFpsTime += delta;
            fps++;

            if (lastFpsTime >= 1000) {
                container.setTitle(windowTitle + " (FPS: " + fps + ")");
                lastFpsTime = 0;
                fps = 0;
            }

            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

            drawBgCover(g, bg, (int) bgY);

            long elapsedSec = (System.currentTimeMillis() - stageStartTime) / 1000;

            if (!waitingForKeyPress) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = entities.get(i);
                    entity.move(delta);
                }

                long now = System.currentTimeMillis();

                if (elapsedSec < 60 && now - lastAlienShotTime > 5000) {
                    for (int i = 0; i < 6; i++) {
                        AlienEntity alien = new AlienEntity(this, 100 + (int)(Math.random() * 600), 80 + (int)(Math.random() * 50));
                        alien.setShotType("shot");
                        entities.add(alien);
                        alienCount++;
                    }
                    lastAlienShotTime = now;
                    System.out.println("NORMAL 몬스터 생성 (5초 주기)");
                }

                if (elapsedSec >= 60 && elapsedSec < 80 && now - lastAlienShotTime > 10000) {
                    for (int i = 0; i < 4; i++) {
                        AlienEntity alien = new AlienEntity(this, 100 + (int)(Math.random() * 600), 120 + (int)(Math.random() * 50));
                        alien.setShotType("iceshot");
                        entities.add(alien);
                        alienCount++;
                    }
                    lastAlienShotTime = now;
                    System.out.println("ICE 몬스터 생성 (10초 주기)");
                }

                if (elapsedSec >= 80 && now - lastAlienShotTime > 10000) {
                    AlienEntity boss = new AlienEntity(this, 350 + (int)(Math.random() * 100 - 50), 150);
                    boss.setShotType("bombshot");
                    entities.add(boss);
                    alienCount++;
                    lastAlienShotTime = now;
                    System.out.println("BOMB 몬스터 생성 (10초 주기)");
                }


				// stage1 보스 프랑켄슈타인 등장
				if (currentStage == 1 && elapsedSec >= 60 && !bossSpawned) {
					FrankenBossEntity boss = new FrankenBossEntity(this, 350, 120);
					entities.add(boss);
					alienCount++;
					bossSpawned = true;
					System.out.println("⚡ 프랑켄슈타인 보스 등장!");
				}



                if (alienCount > 0) {
                    long nowShot = SystemTimer.getTime();
                    long alienShotInterval = 2000 - (getCurrentStage() * 200);
                    if (getCurrentStage() == 1) {
                        alienShotInterval = 2800;
                    }
                    // 스테이지 2 : 20% 더 빠르게 발사
                    if (getCurrentStage() == 2) {
                        alienShotInterval = (long) (alienShotInterval * 0.8);
                    }
                    if (alienShotInterval < 500) alienShotInterval = 500;
                    if (nowShot - lastAlienShotTime >= alienShotInterval) {
                        lastAlienShotTime = nowShot;
                        java.util.List<AlienEntity> aliens = new java.util.ArrayList<>();
                        for (Entity entity : entities) {
                            if (entity instanceof AlienEntity) {
                                aliens.add((AlienEntity) entity);
                            }
                        }
                        if (!aliens.isEmpty()) {
                            int randomIndex = (int) (Math.random() * aliens.size());
                            AlienEntity shootingAlien = aliens.get(randomIndex);
                            shootingAlien.fireShot();
                        }
                    }
                }
            }

            // 모든 엔티티 그리기
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = (Entity) entities.get(i);
                entity.draw(g);
            }

            // 충돌 처리
            for (int p = 0; p < entities.size(); p++) {
                for (int s = p + 1; s < entities.size(); s++) {
                    Entity me = (Entity) entities.get(p);
                    Entity him = (Entity) entities.get(s);

                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    }
                }
            }

            // 제거 대상 엔티티 삭제
            for (Entity entity : removeList) {
                entities.remove(entity);
            }
            removeList.clear();

            // 상태 변경 후 매 프레임마다 적 수 갱신
            updateAlienCount();

 			// 모든 적이 제거되었는지 확인
            // 보스 사망 시 bossDefeated()에서만 클리어 처리
            // 각 엔티티의 로직 처리
            if (logicRequiredThisLoop) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);
                    entity.doLogic();
                }

                logicRequiredThisLoop = false;
            }

            if (waitingForKeyPress) {
                g.setColor(Color.white);

                if (shopOpen) {
                    g.setColor(new Color(0, 0, 0, 200));
                    g.fillRect(0, 0, 800, 600);
                    g.setColor(Color.white);

                    g.drawString("★ SHOP ★", 370, 50);
                    g.drawString("현재 보유 금액: " + ship.getMoney() + " 골드", 330, 80);

                    List<Item> items = shop.getItemsForSale();
                    int itemWidth = 350;
                    int itemHeight = 80;
                    int gap = 20;
                    int startX = 50;
                    int startY = 100;

                    for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);
                        int row = i / 2;
                        int col = i % 2;
                        int x = startX + col * (itemWidth + gap);
                        int y = startY + row * (itemHeight + gap / 2);
                        g.setColor(new Color(50, 50, 50, 150));
                        g.fillRect(x, y, itemWidth, itemHeight - 5);
                        g.setColor(Color.white);
                        g.drawString((i + 1) + ". " + item.getName() + " (가격: " + item.getCost() + "골드)", x + 20, y + 25);
                        String[] descLines = item.getDescription().split("\n");
                        for (int j = 0; j < descLines.length; j++) {
                            g.drawString("  " + descLines[j], x + 20, y + 50 + j * 15);
                        }
                    }

                    g.setColor(new Color(0, 0, 0, 200));
                    g.fillRect(0, 510, 800, 100);
                    g.setColor(Color.white);

                    g.setColor(Color.yellow);
                    int nextStage = currentStage + 1;
                    String nextStageInfo = "다음 스테이지 " + nextStage + " 특성: ";
                    if (nextStage == 2) {
                        nextStageInfo += "적의 총알 발사 속도 20% 증가";
                    } else if (nextStage == 3) {
                        nextStageInfo += "생명 제한 모드 (체력 3 이하시 게임 오버)";
                    } else if (nextStage == 4) {
                        nextStageInfo += "장애물이 등장합니다!";
                    } else if (nextStage == 5) {
                        nextStageInfo += "이중 장애물 등장!!";
                    }

                    g.drawString(nextStageInfo, (800 - g.getFontMetrics().stringWidth(nextStageInfo)) / 2, 480);
                    g.setColor(Color.white);
                    int bottomY = 540;
                    g.drawString("[ 조작 방법 ]", 350, bottomY);
                    g.drawString("숫자 키(1-" + items.size() + "): 아이템 구매   |", 200, bottomY + 25);
                    g.drawString("R: 다음 스테이지   |", 360, bottomY + 25);
                    g.drawString("ESC: 게임 종료", 470, bottomY + 25);
                } else if (message.contains("got you")) {
                    g.drawString(message, (800 - g.getFontMetrics().stringWidth(message)) / 2, 250);
                    g.drawString("Press R to restart or any other key to continue",
                            (800 - g.getFontMetrics().stringWidth("Press R to restart or any other key to continue")) / 2, 300);
                } else if (message.contains("축하합니다")) {
                    g.setColor(new Color(0, 0, 0, 200));
                    g.fillRect(0, 0, 800, 600);
                    g.setColor(Color.white);
                    String[] lines = message.split("\n");
                    g.drawString(lines[0], (800 - g.getFontMetrics().stringWidth(lines[0])) / 2, 250);
                    g.drawString(lines[1], (800 - g.getFontMetrics().stringWidth(lines[1])) / 2, 300);
                } else if (message.contains("Stage")) {
                    g.setColor(new Color(0, 0, 0, 200));
                    g.fillRect(0, 0, 800, 600);
                    g.setColor(Color.white);
                    g.drawString(message, (800 - g.getFontMetrics().stringWidth(message)) / 2, 250);
                    String pressAnyKey = "상점이 열렸습니다. 아이템을 구매하세요!";
                    g.drawString(pressAnyKey, (800 - g.getFontMetrics().stringWidth(pressAnyKey)) / 2, 300);
                } else {
                    g.setColor(new Color(0, 0, 0, 200));
                    g.fillRect(0, 0, 800, 600);
                    g.setColor(Color.white);
                    int MAX_BTN_W = 700;
                    int MAX_BTN_H = 500;
                    int bw = startBtn.getWidth();
                    int bh = startBtn.getHeight();
                    double scale = Math.min(MAX_BTN_W / (double) bw, MAX_BTN_H / (double) bh);
                    int dw = (int) Math.round(bw * scale);
                    int dh = (int) Math.round(bh * scale);
                    int btnX = (800 - dw) / 2;
                    int btnY = (600 - dh) / 2 + 40;
                    startBtn.drawScaled(g, btnX, btnY, dw, dh);
                    String title = "SPACE INVADERS";
                    g.drawString(title, (800 - g.getFontMetrics().stringWidth(title)) / 2, 200);
                    String controls = "Controls: ← → to move, SPACE to fire";
                    g.drawString(controls, (800 - g.getFontMetrics().stringWidth(controls)) / 2, 500);
                }
            }

            // 즉시 패배 조건
            if (ship.getHealth() <= 0) {
                notifyDeath();
            } else if (fortress.getHP() <= 0) {
                notifyFortressDestroyed();
            }


            if (!waitingForKeyPress) {
                g.setColor(Color.white);

                String stageInfo = "STAGE " + currentStage + " - ";
                if (currentStage == 1) {
                    stageInfo += "기본 모드";
                } else if (currentStage == 2) {
                    stageInfo += "적의 총알 발사 속도 20% 증가";
                } else if (currentStage == 3) {
                    stageInfo += "생명 제한 모드";
                } else if (currentStage == 4) {
                    stageInfo += "장애물 등장 모드";
                } else if (currentStage == 5) {
                    stageInfo += "이중 장애물 등장 모드";
                }

                g.drawString(stageInfo, 20, 30);
                g.drawString("남은 적: " + alienCount, 250, 30);

                int timeLimit = 150;

                long elapsedTime = (System.currentTimeMillis() - stageStartTime) / 1000;
                long remainingTime = timeLimit - elapsedTime;

                if (remainingTime <= 0 && bossSpawned) {
                    message = "시간 초과! 프랑켄슈타인을 물리치지 못했습니다!";
                    waitingForKeyPress = true;
                } else {
                    String timeFormat = String.format("시간 제한: %d초", remainingTime);
                    if (remainingTime <= 20) {
                        g.setColor(Color.red);
                        g.drawString(timeFormat, 350, 30);
                        g.setColor(Color.white);
                    } else {
                        g.drawString(timeFormat, 350, 30);
                    }
                }


                g.drawString("체력: " + ship.getHealth(), 20, 50);
                g.drawString("방어력: " + ship.getDefense(), 20, 70);
                g.drawString("공격력: " + ship.getAttackPower(), 20, 90);
                g.drawString("골드: " + ship.getMoney(), 20, 110);
                g.drawString("요새 HP: " + fortress.getHP(), 20, 130);

                if (currentStage == 3 && ship.getHealth() <= lifeLimit) {
                    notifyDeath();
                }

                int weaponY = 130;
                if (ship.hasBomb() || ship.hasIceWeapon() || ship.hasShield()) {
                    g.drawString("[ 보유 중인 특수 무기 ]", 20, weaponY);
                    weaponY += 20;
                }
                if (ship.hasBomb()) {
                    g.drawString(String.format("• 폭탄 x%d (B키로 사용)", ship.getBombCount()), 20, weaponY);
                    weaponY += 20;
                }
                if (ship.hasIceWeapon()) {
                    g.drawString(String.format("• 얼음 공격x%d (I키로 사용)", ship.getIceWeaponCount()), 20, weaponY);
                    weaponY += 20;
                }
                if (ship.hasShield()) {
                    g.drawString(String.format("• 방어막 x%d (S키로 사용)", ship.getShieldCount()), 20, weaponY);
                }
            }

            g.dispose();
            strategy.show();

            ship.setHorizontalMovement(0);
            if ((leftPressed) && (!rightPressed)) {
                ship.setHorizontalMovement(-ship.getMoveSpeed());
            } else if ((rightPressed) && (!leftPressed)) {
                ship.setHorizontalMovement(ship.getMoveSpeed());
            }

            if (firePressed) {
                tryToFire();
            }

            SystemTimer.sleep(lastLoopTime + 10 - SystemTimer.getTime());
        } catch (Exception ex) {
            System.out.println("게임 루프 중 오류 발생: " + ex.getMessage());
            ex.printStackTrace();
            try {
                safelyResetGameState();
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }
        }
    }
}


    /**
     * 유저의 키보드 입력을 처리하는 클래스
     */
    private class KeyInputHandler extends KeyAdapter {
        private int pressCount = 1;

        public void keyPressed(KeyEvent e) {
            if (waitingForKeyPress) {
                return;
            }
            
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_B) {
                if (ship.hasBomb()) {
                    ship.useBomb();
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_I) {
                if (ship.hasIceWeapon()) {
                    ship.useIceWeapon();
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                System.out.println("S키 입력됨! 방어막 개수: " + ship.getShieldCount());
                ship.activateShield();
            }
        } 
        
        public void keyReleased(KeyEvent e) {
            if (waitingForKeyPress) {
                return;
            }
            
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = false;
            }
        }

        public void keyTyped(KeyEvent e) {
            if (waitingForKeyPress) {
                if (shopOpen) {
                    // 상점이 열려있을 때 숫자 키 입력 처리
                    char keyChar = e.getKeyChar();
                    if (keyChar >= '1' && keyChar <= '9') {
                        int itemIndex = keyChar - '1';
                        shop.purchaseItem(ship, itemIndex);
                    } else if (keyChar == 'r' || keyChar == 'R') {
                        // 다음 스테이지 시작
                        currentStage++;
                        waitingForKeyPress = false;
                        startGame();
                    } else if (keyChar == 27) { // ESC
                        System.exit(0);
                    }
                } else if (message.contains("got you") || message.contains("축하합니다")) {
                    // 게임 오버 또는 게임 클리어 상태
                    char keyChar = e.getKeyChar();
                    if (keyChar == 'r' || keyChar == 'R') {
                        if (message.contains("축하합니다")) {
                            // 게임 클리어 후 R: 게임 종료
                            System.exit(0);
                        } else {
                            // 게임 오버 후 R: 게임 재시작
                            try {
                                message = "restart";
                                waitingForKeyPress = false;
                                startGame();
                            } catch (Exception ex) {
                                System.out.println("R키 재시작 중 오류 발생: " + ex.getMessage());
                                ex.printStackTrace();
                                safelyResetGameState();
                                waitingForKeyPress = true;
                            }
                        }
                    } else if (pressCount == 1) {
                        // 게임 오버 상태에서 아무 키나 누를 시 재시작
                        if (!message.contains("축하합니다")) {
                            try {
                                message = "restart";
                                waitingForKeyPress = false;
                                pressCount = 0;
                                startGame();
                            } catch (Exception ex) {
                                System.out.println("게임 재시작 중 오류 발생: " + ex.getMessage());
                                ex.printStackTrace();
                                safelyResetGameState();
                                waitingForKeyPress = true;
                                pressCount = 1;
                            }
                        }
                    } else {
                        pressCount++;
                    }
                } else if (pressCount == 1) {
                    // 게임 시작
                    waitingForKeyPress = false;
                    startGame();
                    pressCount = 0;
                } else {
                    pressCount++;
                }
            }
            
            if (e.getKeyChar() == 27) { // ESC
                System.exit(0);
            }
        }
    }

    public long getStageStartTime() {
    return stageStartTime;
    }

    public FortressEntity getFortress() {
    return fortress;
}
    
    /*public static void main(String argv[]) {
        Game g = new Game();
        g.gameLoop();
    }*/
}