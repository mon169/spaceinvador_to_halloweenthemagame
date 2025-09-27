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
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
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
	/** The number of aliens left on the screen */
	private int alienCount;
	private int currentStage = 1;
	private final int MAX_STAGE = 5;
	
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
	private Shop shop;
	private boolean shopOpen = false;
	
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
			// 기존 우주선의 상태 저장
			oldShip = ship;
		}
		
		// 게임 재시작이나 처음 시작할 때만 스테이지 초기화
		if (message.contains("restart")) {
			currentStage = 1;
		} else if (message.isEmpty()) {
			// 처음 시작할 때도 스테이지 1로 설정
			currentStage = 1;
		}
		// 스테이지 클리어 후에는 currentStage 유지 (다음 스테이지로 진행)
		
		// clear out any existing entities and intialise a new set
		entities.clear();
		initEntities(oldShip);
		
		// blank out any keyboard settings we might currently have
		leftPressed = false;
		rightPressed = false;
		firePressed = false;
		shopOpen = false;
	}
	
	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */
	private void initEntities() {
		initEntities(null);
	}
	
	/**
	 * Initialise the starting state of the entities with an optional previous ship state
	 */
	private void initEntities(ShipEntity oldShip) {
		// create the player ship and place it roughly in the center of the screen
		if (oldShip == null) {
			ship = new ShipEntity(this,"sprites/ship.gif",370,550);
		} else {
			// 이전 우주선의 상태를 새 우주선에 복사
			ship = new ShipEntity(this,"sprites/ship.gif",370,550);
			ship.copyStateFrom(oldShip);
		}
		entities.add(ship);
		
		// create a block of aliens (5 rows, by 12 aliens, spaced evenly)
		alienCount = 0;
		for (int row=0;row<5;row++) {
			for (int x=0;x<12;x++) {
				Entity alien = new AlienEntity(this,100+(x*50),(50)+row*30);
				entities.add(alien);
				alienCount++;
			}
		}
	}
	
	/**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 */
	public void updateLogic() {
		logicRequiredThisLoop = true;
	}
	
	/**
	 * Remove an entity from the game. The entity removed will
	 * no longer move or be drawn.
	 * 
	 * @param entity The entity that should be removed
	 */
	public void removeEntity(Entity entity) {
		removeList.add(entity);
	}
	
	/**
	 * Notification that the player has died. 
	 */
	public void notifyDeath() {
		message = "Oh no! They got you, try again?";
		waitingForKeyPress = true;
	}
	
	/**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
	public void notifyWin() {
		message = "축하합니다! 모든 스테이지를 클리어했습니다!\nESC키를 누르면 게임이 종료됩니다.";
		waitingForKeyPress = true;
		shopOpen = false;  // 마지막에는 상점을 열지 않음
	}
	
	/**
	 * Notification that an alien has been killed
	 */
	public void addEntity(Entity entity) {
		entities.add(entity);
	}



	private void updateAlienCount() {
		// 현재 화면에 있는 실제 적의 수를 세기
		int count = 0;
		for (Entity entity : entities) {
			if (entity instanceof AlienEntity) {
				count++;
			}
		}
		alienCount = count;
	}

	public void notifyAlienKilled() {
		ship.earnMoney(30);  // 적 처치 보상 30골드로 수정
		updateAlienCount();  // 남은 적 수 갱신
	}
	
	/**
	 * Attempt to fire a shot from the player. Its called "try"
	 * since we must first check that the player can fire at this 
	 * point, i.e. has he/she waited long enough between shots
	 */
	public void tryToFire() {
		// check that we have waiting long enough to fire
		if (System.currentTimeMillis() - lastFire < ship.getFiringInterval()) {
			return;
		}
		
		// if we waited long enough, create the shot entity, and record the time.
		lastFire = System.currentTimeMillis();
		ShotEntity shot = new ShotEntity(this,"sprites/shot.gif",ship.getX()+10,ship.getY()-30);
		entities.add(shot);
	}
	
	/**
	 * The main game loop. This loop is running during all game
	 * play as is responsible for the following activities:
	 * <p>
	 * - Working out the speed of the game loop to update moves
	 * - Moving the game entities
	 * - Drawing the screen contents (entities, text)
	 * - Updating game events
	 * - Checking Input
	 * <p>
	 */
	public void gameLoop() {
		long lastLoopTime = SystemTimer.getTime();
		
		// keep looping round til the game ends
		while (gameRunning) {
			// work out how long its been since the last update, this
			// will be used to calculate how far the entities should
			// move this loop
			long delta = SystemTimer.getTime() - lastLoopTime;
			lastLoopTime = SystemTimer.getTime();

			// update the frame counter
			lastFpsTime += delta;
			fps++;
			
			// update our FPS counter if a second has passed since
			// we last recorded
			if (lastFpsTime >= 1000) {
				container.setTitle(windowTitle+" (FPS: "+fps+")");
				lastFpsTime = 0;
				fps = 0;
			}
			
			// Get hold of a graphics context for the accelerated 
			// surface and blank it out
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setColor(Color.black);
			g.fillRect(0,0,800,600);
			
			// cycle round asking each entity to move itself
			if (!waitingForKeyPress) {
				for (int i=0;i<entities.size();i++) {
					Entity entity = (Entity) entities.get(i);
					
					entity.move(delta);
				}
			}
			
			// cycle round drawing all the entities we have in the game
			for (int i=0;i<entities.size();i++) {
				Entity entity = (Entity) entities.get(i);
				
				entity.draw(g);
			}
			
			// brute force collisions, compare every entity against
			// every other entity. If any of them collide notify 
			// both entities that the collision has occured
			for (int p=0;p<entities.size();p++) {
				for (int s=p+1;s<entities.size();s++) {
					Entity me = (Entity) entities.get(p);
					Entity him = (Entity) entities.get(s);
					
					if (me.collidesWith(him)) {
						me.collidedWith(him);
						him.collidedWith(me);
					}
				}
			}
			
			// remove any entity that has been marked for clear up
			for (Entity entity : removeList) {
				entities.remove(entity);
			}
			removeList.clear();

			// 상태 변경 후 매 프레임마다 적 수 갱신
			updateAlienCount();
			
			// 모든 적이 제거되었는지 확인
			if (alienCount == 0 && !waitingForKeyPress) {
				if (currentStage == MAX_STAGE) {
					notifyWin();  // 최종 스테이지 클리어
				} else {
					message = "Stage " + currentStage + " 클리어!";
					waitingForKeyPress = true;
					shopOpen = true;
				}
			}

			// if a game event has indicated that game logic should
			// be resolved, cycle round every entity requesting that
			// their personal logic should be considered.
			if (logicRequiredThisLoop) {
				for (int i=0;i<entities.size();i++) {
					Entity entity = (Entity) entities.get(i);
					entity.doLogic();
				}
				
				logicRequiredThisLoop = false;
			}
			
			// if we're waiting for an "any key" press then draw the 
			// current message or shop screen
			if (waitingForKeyPress) {
				g.setColor(Color.white);
				
				if (shopOpen) {
					// 상점 화면 배경
					g.setColor(new Color(0, 0, 0, 200));
					g.fillRect(0, 0, 800, 600);
					g.setColor(Color.white);
					
					// 상점 제목
					g.drawString("★ SHOP ★", 370, 50);
					g.drawString("현재 보유 금액: " + ship.getMoney() + " 골드", 330, 80);
					
					// 아이템 목록
					List<Item> items = shop.getItemsForSale();
					int itemWidth = 350;  // 아이템 박스 너비
					int itemHeight = 80;  // 아이템 박스 높이
					int gap = 20;         // 아이템 사이 간격
					int startX = 50;      // 시작 X 좌표
					int startY = 130;     // 시작 Y 좌표
					
					for (int i = 0; i < items.size(); i++) {
						Item item = items.get(i);
						// 아이템의 행과 열 위치 계산
						int row = i / 2;  // 2열로 나누기
						int col = i % 2;  // 왼쪽/오른쪽 열
						
						int x = startX + col * (itemWidth + gap);
						int y = startY + row * (itemHeight + gap/2);
						
						// 아이템 배경
						g.setColor(new Color(50, 50, 50, 150));
						g.fillRect(x, y, itemWidth, itemHeight - 5);
						g.setColor(Color.white);
						
						// 아이템 정보
						g.drawString((i+1) + ". " + item.getName(), x + 20, y + 25);
						g.drawString("가격: " + item.getCost() + " 골드", x + 20, y + 45);
						g.drawString("  " + item.getDescription(), x + 20, y + 65);
					}
					
					// 조작 안내 배경
					g.setColor(new Color(0, 0, 0, 200));
					g.fillRect(0, 500, 800, 100);
					g.setColor(Color.white);
					
					// 조작 안내
					int bottomY = 530;
					g.drawString("[ 조작 방법 ]", 350, bottomY);
					
					// 조작 안내를 가로로 배치
					g.drawString("숫자 키(1-" + items.size() + "): 아이템 구매   |", 200, bottomY + 25);
					g.drawString("R: 다음 스테이지   |", 420, bottomY + 25);
					g.drawString("ESC: 게임 종료", 550, bottomY + 25);
				} else if (message.contains("got you")) {
					// 게임 오버 메시지
					g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
					g.drawString("Press R to restart or any other key to continue",(800-g.getFontMetrics().stringWidth("Press R to restart or any other key to continue"))/2,300);
				} else if (message.contains("축하합니다")) {
					// 게임 클리어 메시지
					g.setColor(new Color(0, 0, 0, 200));
					g.fillRect(0, 0, 800, 600);
					g.setColor(Color.white);
					
					String[] lines = message.split("\n");
					g.drawString(lines[0], (800-g.getFontMetrics().stringWidth(lines[0]))/2, 250);
					g.drawString(lines[1], (800-g.getFontMetrics().stringWidth(lines[1]))/2, 300);
				} else {
					// 게임 시작 화면
					g.setColor(new Color(0, 0, 0, 200));
					g.fillRect(0, 0, 800, 600);
					g.setColor(Color.white);
					
					String title = "SPACE INVADERS";
					g.drawString(title, (800-g.getFontMetrics().stringWidth(title))/2, 250);
					String startMessage = "Press any key to start";
					g.drawString(startMessage, (800-g.getFontMetrics().stringWidth(startMessage))/2, 300);
					
					String controls = "Controls: ← → to move, SPACE to fire";
					g.drawString(controls, (800-g.getFontMetrics().stringWidth(controls))/2, 350);
				}
			}
			
			// 플레이어 상태 표시
			if (!waitingForKeyPress) {
				g.setColor(Color.white);
				g.drawString("STAGE " + currentStage, 20, 30);
				g.drawString("남은 적: " + alienCount, 120, 30);
				g.drawString("체력: " + ship.getHealth(), 20, 50);
				g.drawString("방어력: " + ship.getDefense(), 20, 70);
				g.drawString("공격력: " + ship.getAttackPower(), 20, 90);
				g.drawString("골드: " + ship.getMoney(), 20, 110);
			}
			
			// 특수 무기 소지 여부 표시
			// 특수 무기 보유 현황
			if (!waitingForKeyPress) {
				int weaponY = 130;
				if (ship.hasBomb() || ship.hasIceWeapon()) {
					g.drawString("[ 보유 중인 특수 무기 ]", 20, weaponY);
					weaponY += 20;
				}
				if (ship.hasBomb()) {
					g.drawString(String.format("• 폭탄 x%d (B키로 사용)", ship.getBombCount()), 20, weaponY);
					weaponY += 20;
				}
				if (ship.hasIceWeapon()) {
					g.drawString(String.format("• 얼음 무기 x%d (I키로 사용)", ship.getIceWeaponCount()), 20, weaponY);
				}
			}

			// finally, we've completed drawing so clear up the graphics
			// and flip the buffer over
			g.dispose();
			strategy.show();
			
			// resolve the movement of the ship. First assume the ship 
			// isn't moving. If either cursor key is pressed then
			// update the movement appropraitely
			ship.setHorizontalMovement(0);
			
			if ((leftPressed) && (!rightPressed)) {
				ship.setHorizontalMovement(-ship.getMoveSpeed());
			} else if ((rightPressed) && (!leftPressed)) {
				ship.setHorizontalMovement(ship.getMoveSpeed());
			}
			
			// if we're pressing fire, attempt to fire
			if (firePressed) {
				tryToFire();
			}
			
			// we want each frame to take 10 milliseconds, to do this
			// we've recorded when we started the frame. We add 10 milliseconds
			// to this and then factor in the current time to give 
			// us our final value to wait for
			SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
		}
	}
	
	/**
	 * A class to handle keyboard input from the user. The class
	 * handles both dynamic input during game play, i.e. left/right 
	 * and shoot, and more static type input (i.e. press any key to
	 * continue)
	 * 
	 * This has been implemented as an inner class more through 
	 * habbit then anything else. Its perfectly normal to implement
	 * this as seperate class if slight less convienient.
	 * 
	 * @author Kevin Glass
	 */
	private class KeyInputHandler extends KeyAdapter {
		/** The number of key presses we've had while waiting for an "any key" press */
		private int pressCount = 1;
		
		/**
		 * Notification from AWT that a key has been pressed. Note that
		 * a key being pressed is equal to being pushed down but *NOT*
		 * released. Thats where keyTyped() comes in.
		 *
		 * @param e The details of the key that was pressed 
		 */
		public void keyPressed(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
			// want to do anything with just a "press"
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
				ship.useBomb();
			}
			if (e.getKeyCode() == KeyEvent.VK_I) {
				ship.useIceWeapon();
			}
		} 
		
		/**
		 * Notification from AWT that a key has been released.
		 *
		 * @param e The details of the key that was released 
		 */
		public void keyReleased(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
			// want to do anything with just a "released"
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

		/**
		 * Notification from AWT that a key has been typed. Note that
		 * typing a key means to both press and then release it.
		 *
		 * @param e The details of the key that was typed. 
		 */
		public void keyTyped(KeyEvent e) {
			// if we're waiting for a "any key" type then
			// check if we've recieved any recently. We may
			// have had a keyType() event from the user releasing
			// the shoot or move keys, hence the use of the "pressCount"
			// counter.
			if (waitingForKeyPress) {
				if (shopOpen) {
					// 상점이 열려있을 때는 숫자 키 입력을 처리
					char keyChar = e.getKeyChar();
					if (keyChar >= '1' && keyChar <= '9') {
						int itemIndex = keyChar - '1';
						shop.purchaseItem(ship, itemIndex);
					} else if (keyChar == 'r' || keyChar == 'R') {
						// 다음 스테이지 시작
						currentStage++; // 여기서 스테이지를 증가
						waitingForKeyPress = false;
						startGame();
					} else if (keyChar == 27) { // ESC 키
						System.exit(0);
					}
				} else if (message.contains("got you") || message.contains("축하합니다")) {
					// 게임 오버 상태 또는 게임 클리어 상태
					char keyChar = e.getKeyChar();
					if (keyChar == 'r' || keyChar == 'R') {
						if (message.contains("축하합니다")) {
							// 게임 클리어 후 R키 - 게임 종료
							System.exit(0);
						} else {
							// 게임 오버 후 R키 - 게임 재시작
							message = "restart";
							waitingForKeyPress = false;
							startGame();
						}
					} else if (pressCount == 1) {
						// 게임 계속하기 (게임 오버 상태에서만)
						if (!message.contains("축하합니다")) {
							waitingForKeyPress = false;
							startGame();
							pressCount = 0;
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
			
			// ESC 키를 누르면 게임 종료
			if (e.getKeyChar() == 27) {
				System.exit(0);
			}
		}
	}
	
	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		Game g = new Game();

		// Start the main game loop, note: this method will not
		// return until the game has finished running. Hence we are
		// using the actual main thread to run the game.
		g.gameLoop();
	}
}
