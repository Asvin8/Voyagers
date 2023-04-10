/*
 * Names: Asvin and Alex
 * Date: Tuesday, January 24, 2023
 * This game is called voyagers. 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Main extends JPanel implements Runnable, KeyListener {

	Thread thread;
	int FPS = 60;
	int screenWidth = 700;
	int screenHeight = 700;

	// stats
	int lives = 3;
	int coins = 0;
	int highScore = coins;
	static final int TIME = 10;
	static int timeForGame = TIME;

	// break block stuff
	long breakBlockStart;
	long breakBlockFinish;
	long breakBlockDuration;

	// timer stuff
	static Timer timer;
	static boolean timerOn;
	static int time;
	static int timeAllowed;

	// player variables
	Rectangle player = new Rectangle(0, 0, 30, 50);
	boolean left, right, up, down, jump;
	double xVel = 0, yVel = 0;
	double scale = 2;
	double airSpeed = 0;
	double gravity = 0.4 * scale;
	double jumpSpeed = -5.3 * scale;
	double playerSpeed = 1.0;
	double fallSpeedAfterCollision = 0;
	boolean inAir = false;
	boolean moving;
	boolean breakBlock;
	int startingXPlayer = 370, startingYPlayer = 19;

	// images in program
	Image background;
	Image platformImage;
	Image enemyImage;
	Image projectileImage, leftProjectile;
	Image playerImageLeft, playerImageRight;
	Image coinBlockImage;
	Image timePowerImage;
	Image heartPowerImage;
	Image heartImage;
	Image coinImage;

	// keeps track of which screen to use
	static int screenType = 1;

	/*
	 * Here is the key we will use to determine which screen is used at the moment:
	 * 1 --> start screen
	 * 2 --> instructions screen
	 * 3 --> about screen
	 * 4 --> game over screen
	 * 5 --> bacgkround 1 screen
	 * 6 --> background 2 screen
	 * 7 --> background 3 screen
	 */

	// various screens
	Image gameOverScreen;
	Image instructionsScreen;
	Image aboutScreen;
	Image startScreen;
	Image backgroundImage1, backgroundImage2, backgroundImage3, backgroundImage4;

	// size of each platform block
	final int platformSize = 70;

	/**
	 * This is the array that models the objects on the screen
	 * Notation:
	 * 0 --> empty
	 * 1 --> platform
	 * 2 --> enemy
	 * 3 --> coin block
	 * 4 --> time power block
	 * 5 --> health power block
	 */
	int[][] screen = new int[10][10];
	final int ENEMY = 2;
	final int COINBLOCK = 3;
	final int COINUPGRADE = 4;
	final int HEALTHBLOCK = 5;

	// platforms
	Platform[] platforms = new Platform[10];

	// helps with collision detection of platforms
	Rectangle[] platformRectangles = new Rectangle[10];

	// Position of every enemy
	Enemy[] enemyPos = new Enemy[20];

	// helps with collision detection of enemies
	Rectangle[] enemyRectangles = new Rectangle[20];

	// Position of every projectile
	Projectile[] projectilePos = new Projectile[20];// maybe change size of array idk

	// direction of projectiles
	int projectileDirection[] = new int[2];

	// projectile rectangles for collision detection
	Rectangle[] projectileRectangle = new Rectangle[2];

	// coinBlock position and rectangle
	coinBlock[] coinBlockPos = new coinBlock[4];
	Rectangle[] coinBlockRectangle = new Rectangle[4];

	// coinBlock position and rectangle
	timePower[] timePowerPos = new timePower[2];
	Rectangle[] timePowerRectangle = new Rectangle[2];

	// coinBlock position and rectangle
	heartPower[] heartPowerPos = new heartPower[2];
	Rectangle[] heartPowerRectangle = new Rectangle[2];

	// direction of the projectiles
	int direction = 1;

	// speed of projectile
	int projectileSpeed = 5;

	Clip backgroundMusic, defeatSound, damageSound, coinSound, breakSound;

	public Main() {
		setPreferredSize(new Dimension(700, 700));
		setVisible(true);		

		// variables that help keep track of keystrokes
		jump = false;
		left = false;
		right = false;
		breakBlock = false;

		// import the sounds into the program and assign them to variables
		try {

			// background music
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File("src/gameSong.wav"));
			backgroundMusic = AudioSystem.getClip();
			backgroundMusic.open(sound);

			// defeat music
			sound = AudioSystem.getAudioInputStream(new File("src/defeatSound.wav"));
			defeatSound = AudioSystem.getClip();
			defeatSound.open(sound);

			// damage sound
			sound = AudioSystem.getAudioInputStream(new File("src/damageSound.wav"));
			damageSound = AudioSystem.getClip();
			damageSound.open(sound);

			// coin sound
			sound = AudioSystem.getAudioInputStream(new File("src/coinSound.wav"));
			coinSound = AudioSystem.getClip();
			coinSound.open(sound);

			// breaking block sound
			sound = AudioSystem.getAudioInputStream(new File("src/breakSound.wav"));
			breakSound = AudioSystem.getClip();
			breakSound.open(sound);

		} catch (Exception e) {
			System.out.println("----------------------\nERROR OCCURED\n--------------------");
			System.exit(1);
		}

		thread = new Thread(this);
		thread.start();
	}

	// This code runs the entire game
	@Override
	public void run() {

		// start game
		initializeGame();

		// play background music
		backgroundMusic.start();

		while (true) {
			move();
			keepInBound();
			moveProjectile();

			this.repaint();

			try {
				Thread.sleep(1000 / FPS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// This is the paint component
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// start screen
		if (screenType == 1) {
			g2.drawImage(startScreen, 0, 0, null);
		}

		// instructions screen
		else if (screenType == 2) {
			g2.drawImage(instructionsScreen, 0, 0, null);
		}

		// about page
		else if (screenType == 3) {
			g2.drawImage(aboutScreen, 0, 0, null);
		}

		// game over screen
		else if (screenType == 4) {
			g2.drawImage(gameOverScreen, 0, 0, null);
			g2.setColor(new Color(255, 255, 255));
			g2.setFont(new Font("Arial", Font.BOLD, 80));
			g2.drawString("" + coins, 320, 625);
			highScore = Math.max(highScore, coins);
			g2.drawString("" + highScore, 440, 690);
		}

		// background screen (game is in progress)
		else {

			if (screenType == 5) { // background 1
				g2.drawImage(backgroundImage1, 0, 0, null);
			} else if (screenType == 6) { // background 2
				g2.drawImage(backgroundImage2, 0, 0, null);
			} else if (screenType == 7) { // background 3
				g2.drawImage(backgroundImage3, 0, 0, null);
			} else if (screenType == 8) { // background 3
				g2.drawImage(backgroundImage4, 0, 0, null);
			}

			// draw platforms
			AlphaComposite transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			g2.setComposite(transparency);
			for (int i = 0; i < screen.length; i++) {
				for (int j = 0; j < screen[i].length; j++) {
					if (screen[i][j] == 1) {
						transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0f);
						g2.setComposite(transparency);
						g2.drawRect(j * platformSize, i * platformSize, platformSize, platformSize);
						transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
						g2.setComposite(transparency);
						g2.drawImage(platformImage, j * platformSize, i * platformSize, null);
					}
				}
			}
 
			// draw transparent projectile rectangles (for collisions)
			transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0f);
			g2.setComposite(transparency);
			for (int i = 0; i < 2; i++) {
				g2.fill3DRect(projectileRectangle[i].x, projectileRectangle[i].y, 70, 70, true);
			}

			// draw projectile images
			transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			g2.setComposite(transparency);
			for (int i = 0; i < projectileRectangle.length; i++) {

				if(projectileDirection[i] == 1) { // right
					g2.drawImage(projectileImage, projectileRectangle[i].x, projectileRectangle[i].y, null);
				} else if(projectileDirection[i] == -1) {
					g2.drawImage(leftProjectile, projectileRectangle[i].x, projectileRectangle[i].y, null);
				}
			}

			// draw stuff on screen
			for (int i = 0; i < screen.length; i++) {
				for (int j = 0; j < screen[i].length; j++) {

					// draw enemies
					if (screen[i][j] == 2) {
						int row = j * platformSize;
						int col = i * platformSize;
						g2.drawImage(enemyImage, row, col, null);
					}

					// draw coin block
					else if (screen[i][j] == 3) {
						int row = j * platformSize;
						int col = i * platformSize;
						g2.drawImage(coinBlockImage, row, col, null);
					}

					else if (screen[i][j] == 4) {
						int row = j * platformSize;
						int col = i * platformSize;
						g2.drawImage(timePowerImage, row, col, null);
					}

					// draw health
					else if (screen[i][j] == 5) {
						int row = j * platformSize;
						int col = i * platformSize;
						g2.drawImage(heartPowerImage, row, col, null);
					}
				}
			}

			// draw hearts
			if (lives >= 1)
				g2.drawImage(heartImage, 630, 440, null);

			if (lives >= 2)
				g2.drawImage(heartImage, 630, 410, null);

			if (lives >= 3) {
				g2.drawImage(heartImage, 630, 380, null);
				lives = 3;
			}

			if (lives == 0) { // player has lost
				screenType = 4;
				defeatSound.setFramePosition(0);
				defeatSound.start();
				lives = 3;
			}

			// draw transparent rectangle around player
			transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0f);
			g2.setComposite(transparency);
			g2.draw(player);

			// draw player
			transparency = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
			g2.setComposite(transparency);

			// determines which direction the player is facing
			if (left) {
				g2.drawImage(playerImageLeft, player.x, player.y, null);
			} else {
				g2.drawImage(playerImageRight, player.x, player.y, null);
			}

			// draw coin
			g2.drawImage(coinImage, 630, 300, null);
			g2.setColor(new Color(255, 255, 255));
			g2.setFont(new Font("Arial", Font.BOLD, 40));
			g2.drawString("" + coins, 645, 430);
		}
	}

	// This method initializes the game when it starts
	public void initializeGame() {
		player.y = startingYPlayer;
		player.x = startingXPlayer;
		resetScreenArray();
		loadBackgroundAndPlatforms();
		generatePlatforms();
		generateEnemy();
		generateProjectile();
		generateCoinBlock();
		generateTimePower();
		generateHeartPower();
		//timer(); 
	}

	// This function resets the screen array
	void resetScreenArray() {
		for (int i = 0; i < screen.length; i++) {
			for (int j = 0; j < screen[i].length; j++) {
				screen[i][j] = 0;
			}
		}
	}

	// render background and platforms
	public void loadBackgroundAndPlatforms() {
		MediaTracker tracker = new MediaTracker(this);

		try {

			// rendering game images
			backgroundImage1 = Toolkit.getDefaultToolkit().getImage("src/back1.gif");
			tracker.addImage(background, 0);
			platformImage = Toolkit.getDefaultToolkit().getImage("src/platformImage.gif");
			tracker.addImage(platformImage, 1);
			enemyImage = Toolkit.getDefaultToolkit().getImage("src/enemy.gif");
			tracker.addImage(enemyImage, 2);
			playerImageLeft = Toolkit.getDefaultToolkit().getImage("src/playLeft.gif");
			tracker.addImage(playerImageLeft, 3);
			playerImageRight = Toolkit.getDefaultToolkit().getImage("src/playRight.gif");
			tracker.addImage(playerImageRight, 4);
			coinBlockImage = Toolkit.getDefaultToolkit().getImage("src/coinBlock.gif");
			tracker.addImage(coinBlockImage, 5);
			timePowerImage = Toolkit.getDefaultToolkit().getImage("src/timePower.gif");
			tracker.addImage(coinBlockImage, 6);
			heartPowerImage = Toolkit.getDefaultToolkit().getImage("src/heartPower.gif");
			tracker.addImage(heartPowerImage, 7);
			projectileImage = Toolkit.getDefaultToolkit().getImage("src/projectile.gif");
			tracker.addImage(projectileImage, 8);
			backgroundImage2 = Toolkit.getDefaultToolkit().getImage("src/back2.gif");
			tracker.addImage(background, 9);
			backgroundImage3 = Toolkit.getDefaultToolkit().getImage("src/back3.gif");
			tracker.addImage(background, 10);
			backgroundImage4 = Toolkit.getDefaultToolkit().getImage("src/back4.gif");
			tracker.addImage(background, 11);

			// rendering screen images
			startScreen = Toolkit.getDefaultToolkit().getImage("src/backHome.gif");
			tracker.addImage(startScreen, 12);
			gameOverScreen = Toolkit.getDefaultToolkit().getImage("src/backdefeat.gif");
			tracker.addImage(gameOverScreen, 13);
			instructionsScreen = Toolkit.getDefaultToolkit().getImage("src/howto.gif");
			tracker.addImage(instructionsScreen, 14);
			heartImage = Toolkit.getDefaultToolkit().getImage("src/heart1.gif");
			tracker.addImage(aboutScreen, 15);
			coinImage = Toolkit.getDefaultToolkit().getImage("src/coin.gif");
			tracker.addImage(aboutScreen, 16);
			aboutScreen = Toolkit.getDefaultToolkit().getImage("src/about.gif");
			tracker.addImage(aboutScreen, 17);
			leftProjectile = Toolkit.getDefaultToolkit().getImage("src/projectileLeft.gif");
			tracker.addImage(background, 18);

		} catch (Exception e) {
			System.out.println("Error loading images");
		}

		// make sure the images are loaded before continuing
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
		}
	}

	// This function moves the projectiles
	public void moveProjectile() {
		for (int i = 0; i < projectileRectangle.length; i++) {
			if (projectileRectangle[i].x >= screenWidth - 2 * platformSize) {
				projectileDirection[i] = -1; // change projectile direction to left
			} else if (projectileRectangle[i].x <= platformSize) {
				projectileDirection[i] = 1; // change projectile direction right
			}
			if (projectileDirection[i] == -1)
				projectileRectangle[i].x -= projectileSpeed; // projectile moves left
			else
				projectileRectangle[i].x += projectileSpeed; // projectile moves right
		}
	}

	// This function generates random platforms of varying lengths at every row in
	// an array
	public void generatePlatforms() {

		// platform that player stands on
		platforms[0] = new Platform(350, 70, 1);
		platformRectangles[0] = new Rectangle(platforms[0].x * platformSize,
				platforms[0].y * platformSize, platformSize, platformSize);
		screen[1][5] = 1;

		// add 1 platform on every row of the screen
		for (int i = 2; i < screen.length; i++) {
			int lengthOfPlatform = (int) (Math.random() * 3) + 1;
			int x = (int) (Math.random() * (8 - (lengthOfPlatform - 1))) + 1;
			for (int j = 0; j < lengthOfPlatform; j++) {
				screen[i][x + j] = 1;
			}
			platforms[i - 1] = new Platform(x, i, lengthOfPlatform);
			platformRectangles[i - 1] = new Rectangle(platforms[i - 1].x * platformSize,
					platforms[i - 1].y * platformSize, lengthOfPlatform * platformSize, platformSize);
		}
	}

	// enemy generation
	void generateEnemy() {
		enemyRectangles[0] = new Rectangle(0, 0, 0, 0);
		enemyPos[0] = new Enemy(0, 0);
		enemyPos[1] = new Enemy(0, 0);
		// System.out.println("boom");
		for (int i = 2; i < screen.length; i++) {
			for (int j = 0; j < screen[i].length; j++) {
				if (screen[i][j] == 1 && screen[i - 1][j] == 0) {
					int randNum = (int) (Math.random() * 2) + 1; // 1/2 chance to spawn enemy
					if (randNum == 1) {
						screen[i - 1][j] = 2;
						enemyPos[i] = new Enemy(i - 1, j);
						// int x = j * platformSize; int col = i * platformSize;
						// g2.drawImage(enemyImage, row, col, null);
						enemyRectangles[i] = new Rectangle();
					}
				}
			}

		}
	}

	// projectile generation
	public void generateProjectile() {
		int projectileX;
		int[] projectileBlockPos = { 70, 140, 210, 280, 350, 420, 490, 560, 630, 700 };

		for (int i = 0; i < 2; i++) {
			int randPos = (int) (Math.random() * 9) + 1;
			int projectileY = projectileBlockPos[randPos];

			int randSide = (int) (Math.random() * 2) + 1; // 1/2 chance to spawn enemy
			if (randSide == 1) {
				projectileX = platformSize;
				projectileDirection[i] = 1;
			} else {
				projectileX = screenWidth - platformSize;
				projectileDirection[i] = -1;
			}
			projectilePos[i] = new Projectile(projectileX, projectileY);
			projectileRectangle[i] = new Rectangle(projectilePos[i].x, projectilePos[i].y, 70, 70);
		}
	}

	// generate coin block
	public void generateCoinBlock() {
		int randX = 0;
		int randY = 0;
		boolean isFilled = true;
		coinBlockRectangle[0] = new Rectangle(0, 0, 0, 0);
		coinBlockPos[0] = new coinBlock(0, 0);

		// generate random num of coins 1-4
		int randNum = (int) (Math.random() * 3) + 1;
		// choose coordinate that is not occupied
		for (int i = 0; i < randNum; i++) {
			isFilled = true;
			while (isFilled) {
				randX = (int) (Math.random() * 9) + 1;
				randY = (int) (Math.random() * 8) + 1;
				if (screen[randX][randY] == 0) {
					isFilled = false;
				}
			}
			screen[randX][randY] = 3;
			coinBlockPos[i] = new coinBlock(randX, randY);
			screen[randX][randY] = 3;
			coinBlockRectangle[i] = new Rectangle();
		}
	}

	// generate time power up
	public void generateTimePower() {
		int randX = 0;
		int randY = 0;
		boolean isFilled = true;
		timePowerRectangle[0] = new Rectangle(0, 0, 0, 0);
		timePowerPos[0] = new timePower(0, 0);

		// generate random num of timePower (1/4 chance)
		int randNum = (int) (Math.random() * 2) + 1;
		if (randNum == 1) {
			while (isFilled) {
				randX = (int) (Math.random() * 9) + 1;
				randY = (int) (Math.random() * 8) + 1;
				if (screen[randX][randY] != 0) {
					isFilled = false;
					break;
				}
				screen[randX][randY] = 4;
				timePowerPos[0] = new timePower(randX, randY);
				timePowerRectangle[0] = new Rectangle();
				return;
			}
		}
	}

	// This function generates the health powers on the actual screen and in the
	// screen array
	public void generateHeartPower() {
		int randX = 0;
		int randY = 0;
		boolean isFilled = true;
		heartPowerRectangle[0] = new Rectangle(0, 0, 0, 0);
		heartPowerPos[0] = new heartPower(0, 0);

		// generate random num of timePower (1/4 chance)
		int randNum = (int) (Math.random() * 2) + 1;
		if (randNum == 1) {
			while (isFilled) {
				randX = (int) (Math.random() * 9) + 1;
				randY = (int) (Math.random() * 8) + 1;
				if (screen[randX][randY] != 0) {
					isFilled = false;
					break;
				}
				// insert health power in screen array
				screen[randX][randY] = 5;
				heartPowerPos[0] = new heartPower(randX, randY);
				heartPowerRectangle[0] = new Rectangle();
				return;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	// This function determines where the character moves based on user input
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_A && screenType >= 5) { // left
			left = true;
			right = false;
		} else if (key == KeyEvent.VK_D) { // right
			right = true;
			left = false;
		} else if (key == KeyEvent.VK_W) { // jump
			jump = true;
		} else if (key == KeyEvent.VK_S) { // break block beneath you
			breakSound.setFramePosition(0);
			breakSound.start();
		} else if (key == KeyEvent.VK_P && screenType == 1) { // play game
			screenType = 5;
			coins = 0;
		} else if (key == KeyEvent.VK_H && screenType == 1) { // instructions
			screenType = 2;
		} else if (key == KeyEvent.VK_A && screenType == 1) { // about page
			screenType = 3;
		} else if (key == KeyEvent.VK_R && screenType == 2) { // return to home page
			screenType = 1;
		} else if (key == KeyEvent.VK_R && screenType == 3) { // return to home page
			screenType = 1;
		} else if (key == KeyEvent.VK_R && screenType == 4) { // return to home page
			screenType = 1;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_A) {
			left = false;
		} else if (key == KeyEvent.VK_D) {
			right = false;
		} else if (key == KeyEvent.VK_W) {
			jump = false;
		} else if (key == KeyEvent.VK_S) {
			breakBlock = true;
			breakBlockFinish = System.currentTimeMillis();
			breakBlockDuration = breakBlockFinish - breakBlockStart; // finds how long key was pressed for
		} else if (key == KeyEvent.VK_P && screenType == 1) { // play game
			screenType = 5;
			timeForGame = TIME;
		} else if (key == KeyEvent.VK_H && screenType == 1) { // instructions
			screenType = 2;
		} else if (key == KeyEvent.VK_A && screenType == 1) { // about page
			screenType = 3;
		} else if (key == KeyEvent.VK_R && screenType == 2) { // return to home page
			screenType = 1;
		} else if (key == KeyEvent.VK_R && screenType == 3) { // return to home page
			screenType = 1;
		} else if (key == KeyEvent.VK_R && screenType == 4) { // return to home page
			screenType = 1;
		}
	}

	// This function handles the movement of the player
	void move() {

		// if player is currently jumping
		if (jump)
			jump();

		// check if player hit projectile ==> projectile is not stored on screen array
		if (player.intersects(projectileRectangle[0]) || player.intersects(projectileRectangle[1])) {
			lives--;
			damageSound.setFramePosition(0);
			damageSound.start();
			initializeGame();
		}

		// player reaches bottom of screen
		if (player.y + player.height >= screenHeight) {
			int randNum = (int) (Math.random() * 4) + 5; // 1/2 chance to spawn enemy
			screenType = randNum;
			initializeGame();
		}

		// undraw platform beneath player
		if (breakBlock) {
			int curRow = (int) (player.y / platformSize) + 1, curCol = (int) (player.x / platformSize);
			screen[curRow][curCol] = 0;
//			System.out.println("-------BLOCK WAS BROKEN---------");
			breakBlock = false;
			jump(); // player jumps when they break block
		}

		// detecting if player is hitting something
		int curRow = (int) (player.y / platformSize), curCol = (int) (player.x / platformSize);
		if (screen[curRow][curCol] > 1) {
			collision(screen[curRow][curCol]);
			screen[curRow][curCol] = 0;
		}

		// if user is not pressing any keys, player does not move
		if (!left && !right && !inAir)
			return;

		double xSpeed = 0;

		// user presses left key
		if (left && !right)
			xSpeed = -playerSpeed;

		// user presses right key
		if (right && !left)
			xSpeed = playerSpeed;

		// update x pos of player
		player.x += xSpeed;

		// if player is on ground
		if (!inAir) {
			if (!isEntityOnFloor()) {
				inAir = true;
			}
		}

		// if(inAir) {
		if (CanMoveHere(player.x, player.y + airSpeed)) {
			player.y += airSpeed;
			airSpeed += gravity;
			updateXPos(xSpeed);
		} else {
			player.y = (int) GetEntityYPosUnderRoofOrAboveFloor(airSpeed);
			// resetInAir();
			if (airSpeed > 0)
				resetInAir();
			else
				airSpeed = 0;
			// player.y =
			// updateXPos(xSpeed);
		}

		// } else {
		updateXPos(xSpeed);
		// }
	}

	void collision(int item) {
		if (item == ENEMY) { // enemy
			lives--;
			damageSound.setFramePosition(0);
			damageSound.start();
			initializeGame();
		} else if (item == COINBLOCK) { // coins
			coins++;
			coinSound.setFramePosition(0);
			coinSound.start();
		} else if (item == HEALTHBLOCK) { // health boost
			lives++;
			coinSound.setFramePosition(0);
			coinSound.start();
		} else if (item == COINUPGRADE) { // time boost
			coins += 5; 
			coinSound.setFramePosition(0);
			coinSound.start();
		}
	}

	// This function detects of player is on floor
	boolean isEntityOnFloor() {
		if (player.y <= screenHeight)
			return false;
		return true;
	}

	// This function models the jump of the player
	void jump() {
		if (inAir)
			return;
		inAir = true;
		airSpeed = jumpSpeed;
	}

	// this resets the player's motion in the air
	void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}

	/**
	 * This function models the fall or jump of a player
	 * @param airSpeed is the speed that the player travels in air
	 * @return location of y coordinate of nearest platform
	 */
	double GetEntityYPosUnderRoofOrAboveFloor(double airSpeed) {
		int currentPlatform = (int) (player.y / platformSize);
		if (airSpeed > 0) { // falling
			int platformYPos = currentPlatform * platformSize;
			int yOffset = (int) (platformSize - player.height);
			return platformYPos + yOffset - 1;
		} else { // jumping
			return player.y + gravity;
		}
	}


	/**
	 * This function moves the player right beside the platform
	 * @param xSpeed current speed of the player in x direction
	 * @return x position of platform closest to it
	 */
	double GetEntityXPosNextToWall(double xSpeed) {
		int currentPlatform = (int) (player.x / platformSize);
		if (xSpeed > 0) { // right
			int platformXPos = currentPlatform * platformSize;
			int xOffset = (int) (platformSize - player.width);
			return platformXPos + xOffset - 1;
		} else { // left
			int platformXPos = (currentPlatform) * platformSize;
			int xOffset = (int) (platformSize - player.width);
			return platformXPos + player.width + 1;
		}
	}

	/**
	 * This function updates the x position of the player
	 * @param xSpeed current speed in x direction
	 */
	void updateXPos(double xSpeed) {
		if (CanMoveHere(player.x + xSpeed, player.y)) {
			player.x += xSpeed;
		} else {
			player.x = (int) GetEntityXPosNextToWall(xSpeed);
		}
	}

	/**
	 * This function determines if it is possible for a player to move to a location
	 * @param xPos current x coordinate of player
	 * @param yPos current y coordinate of player
	 * @return whether or not the player can move to specified location
	 */
	boolean CanMoveHere(double xPos, double yPos) {	
		if (!isPlatform(xPos, yPos)) { // check top left corner
			if (!isPlatform(xPos + player.width, yPos + player.height)) { // check bottom right corner
				if (!isPlatform(xPos + player.width, yPos)) { // check top right corner
					if (!isPlatform(xPos, yPos + player.height)) { // check bottom left corner
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * This function checks if current square is platform or not
	 * @param x coordinate
	 * @param y coordinate
	 * @return whether or not the current pos is platform
	 */
	boolean isPlatform(double x, double y) {
		int xPos = (int) (x / platformSize), yPos = (int) (y / platformSize);
		if (xPos >= 10)
			xPos = 9;
		if (yPos >= 10)
			yPos = 9;
		if (screen[yPos][xPos] == 1)
			return true;
		return false;
	}

	// This function keeps the player within the boundaries
	void keepInBound() {
		if (player.x < 70) {
			player.x = 70;
		} else if (player.x > screenWidth - player.width - 70) {
			player.x = screenWidth - player.width - 70;
		}

		if (player.y < 0) {
			player.y = 0;
			yVel = 0;
		} else if (player.y > screenHeight - player.height) {
			player.y = screenHeight - player.height;
			inAir = false;
			yVel = 0;
		}
	}

	public static void main(String[] args) {

		JFrame frame = new JFrame("Alex and Asvin ISU");
		Main myPanel = new Main();
		frame.add(myPanel);
		frame.addKeyListener(myPanel);
		frame.setVisible(true);
		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}

// Platform class
class Platform {
	int x, y, len;

	public Platform(int x, int y, int len) {
		this.x = x;
		this.y = y;
		this.len = len;
	}
}

// Enemy class
class Enemy {
	int x, y;

	public Enemy(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

// Projectile class
class Projectile {
	int x, y;

	public Projectile(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

// Coin block class
class coinBlock {
	int x, y;

	public coinBlock(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

// Time powerup class
class timePower {
	int x, y;

	public timePower(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

// Heart class
class heartPower {
	int x, y;

	public heartPower(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
