
/* Game.java
 * Space Invaders Main Program
 *
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class Game extends Canvas {

	private BufferStrategy strategy; // take advantage of accelerated graphics
	private boolean waitingForKeyPress = true; // true if game held up until
	// a key is pressed
	private boolean leftPressed = false; // true if left arrow key currently pressed
	private boolean rightPressed = false; // true if right arrow key currently pressed
	private boolean upPressed = false; // true if up arrow key is pressed
	private boolean downPressed = false; // true if down arrow key is pressed
	private final int GAME_WIDTH = 600; // width of game
	private final int GAME_HEIGHT = 1080; // height of game
	private int xPos; // x position for enemy entities
	private int yPos; // x position for enemy entities
	private int lastBird = 0; // time since last bird spawn in millis
	private int cloudTime = 0;
	private int lives;
	private boolean gameRunning = true;
	private ArrayList<Entity> entities = new ArrayList<>(); // list of entities
	// in game
	private ArrayList<Entity> removeEntities = new ArrayList<>(); // list of entities
	// to remove this loop
	private Entity player; // the player
	private double moveSpeed = 600; // hor. vel. of ship (px/s)
	private String message = ""; // message to display while waiting

	/*
	 * Construct our game and set it running.
	 */
	public Game() {
		// create a frame to contain game
		JFrame container = new JFrame("Commodore 64 Space Invaders/changed");

		// get hold the content of the frame
		JPanel panel = (JPanel) container.getContentPane();

		// set up the resolution of the game
		panel.setPreferredSize(new Dimension(600, 1080));
		panel.setLayout(null);

		// set up canvas size (this) and add to frame
		setBounds(0, 0, 600, 1080);
		panel.add(this);

		// Tell AWT not to bother repainting canvas since that will
		// be done using graphics acceleration
		setIgnoreRepaint(true);

		// make the window visible
		container.pack();
		container.setResizable(false);
		container.setVisible(true);

		// if user closes window, shutdown game and jre
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // windowClosing
		});

		// add key listener to this canvas
		addKeyListener(new KeyInputHandler());

		// request focus so key events are handled by this canvas
		requestFocus();

		// create buffer strategy to take advantage of accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		// initialize entities
		initEntities();

		// start the game
		gameLoop();
	} // constructor

	/*
	 * initEntities input: none output: none purpose: Initialise the starting state
	 * of the ship and alien entities. Each entity will be added to the array of
	 * entities in the game.
	 */
	private void initEntities() {
		player = new Player(this, "sprites/playerR.gif", 260, 100, 40, 20);
		entities.add(player);
		lives = 3; // add three lives to player
	} // initEntities

	/*
	 * Notification from a game entity that the logic of the game should be run at
	 * the next opportunity
	 */

	/*
	 * Remove an entity from the game. It will no longer be moved or drawn.
	 */
	public void removeEntity(Entity entity) {
		removeEntities.add(entity);
	} // removeEntity

	/*
	 * Notification that the player has died.
	 */
	public void notifyDeath() {

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		entities.clear();
		System.exit(0);

	} // notifyDeath

	/*
	 * Notification that the play has killed all aliens
	 */

	/*
	 * gameLoop input: none output: none purpose: Main game loop. Runs throughout
	 * game play. Responsible for the following activities: - calculates speed of
	 * the game loop to update moves - moves the game entities - draws the screen
	 * contents (entities, text) - updates game events - checks input
	 */

	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();

		// Scrolling Background
		BufferedImage back = null; // background image
		Background backOne = new Background(); // first copy of background image (used for moving background)
		Background backTwo = new Background(backOne.getImageWidth(), 0); // second copy of background image (used for
																			// moving background)

		// keep loop running until game ends
		while (gameRunning) {

			// calc. time since last update, will be used to calculate
			// entities movement
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastBird += delta;
			cloudTime += delta;
			lastLoopTime = System.currentTimeMillis();

			// get graphics context for the accelerated surface and make it black
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

			// scrolling Background
			if (back == null) {
				back = (BufferedImage) (createImage(getWidth(), getHeight()));
			}

			// creates a buffer to draw to
			Graphics buffer = back.createGraphics();

			// puts the two copies of the background image onto the buffer
			backOne.draw(buffer);
			backTwo.draw(buffer);

			// draws the image onto the window
			g.drawImage(back, null, 0, 0);

			// move each entity
			if (!waitingForKeyPress) {
				// if enough time has passed to spawn new birds
				if (lastBird > 2000) {
					lastBird = 0; // reset counter

					// spawn 3 bird entities if enough time has passed
					for (int i = 0; i < 4; i++) {
						xPos = (int) ((Math.random() * 110) + 1) * 5; // x position for enemy entities
						yPos = (int) ((Math.random() * 230) + 194) * 5; // x position for enemy entities
						Entity bird = new BirdEntity(this, "sprites/bird.gif", xPos, yPos, 20, 20);
						entities.add(bird);
					} // for
					
					// spawn 3 bird entities if enough time has passed
					for (int i = 0; i < 2; i++) {
						xPos = (int) ((Math.random() * 110) + 1) * 5; // x position for enemy entities
						yPos = (int) ((Math.random() * 230) + 194) * 5; // x position for enemy entities
						Entity cloud = new CloudEntity(this, "sprites/cloud.gif", xPos, yPos, 70, 30);
						entities.add(cloud);
					} // for

				} // if
				
				//sets birds to move up
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					if (entity instanceof BirdEntity || entity instanceof CloudEntity && downPressed) {
						entity.setVerticalMovement(-800);
					} else if (entity instanceof BirdEntity || entity instanceof CloudEntity && !downPressed) {
						entity.setVerticalMovement(-600);
					}// else if
					
					//remove entities that pass the upper screen limit
					if(entity.y <= 0) {
						removeEntities.add(entity);
					}// if
					
					entity.move(delta);
				} // for
				
			} // if move

			// draw all entities
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = (Entity) entities.get(i);
				entity.draw(g);
			} // for

			// if player collided with a bird, notify death
			for (int i = 0; i < entities.size(); i++) {
				if (entities.get(i) instanceof BirdEntity) {

					BirdEntity enemy = (BirdEntity) entities.get(i);
					if (player.collidesWith(enemy)) {
						
						for(Entity bird: entities) {
							removeEntity(bird);
						}// for
						removeEntity(player);
						System.out.println("You died");
						notifyDeath();
						
					} // if birdentity collides with player
					
				}// if bird entity
				
				/*
				if(entities.get(i) instanceof CloudEntity) {
					CloudEntity cloud = (CloudEntity) entities.get(i);
					
					//if
					if(player.collidesWith(cloud)) {
						cloudTime = 0;
						moveSpeed = 100;
						for(Entity ent: entities) {
							if(ent instanceof CloudEntity || ent instanceof BirdEntity) {
								ent.setVerticalMovement(-150);
							}// if
							backOne.setBackSpeed(1); // sets background to slow down
							backTwo.setBackSpeed(1); // ibid
						}// for
					}// if
					
					// if
					if(cloudTime < 500) {
						moveSpeed = 600; // resets moveSpeed
						for(Entity ent: entities) {
							if(ent instanceof CloudEntity || ent instanceof BirdEntity) {
								ent.setVerticalMovement(-600);
							}// if
							backOne.setBackSpeed(5); // sets background to slow down
							backTwo.setBackSpeed(5); // ibid
						}// for
					}
				} // if
				*/
			} // for
			
			// remove dead entities
	        entities.removeAll(removeEntities);
	        removeEntities.clear();

			// if waiting for "any key press", draw message
			if (waitingForKeyPress) {
				g.setColor(Color.white);
				g.drawString(message, (1080 - g.getFontMetrics().stringWidth(message)) / 2, 250);
				g.drawString("Press any key", (600 - g.getFontMetrics().stringWidth("Press any key")) / 2, 300);
			} // if

			// clear graphics and flip buffer
			g.dispose();
			strategy.show();

			player.setHorizontalMovement(0);
			player.setVerticalMovement(0);

			// respond to user moving ship
			if ((leftPressed) && (!rightPressed)) {
				player.setHorizontalMovement(-moveSpeed);
			} else if ((rightPressed) && (!leftPressed)) {
				player.setHorizontalMovement(moveSpeed);
			} else if (downPressed) {
				backOne.setBackSpeed(18);
				backTwo.setBackSpeed(18);
				player.setVerticalMovement(120);
			} else if (!downPressed) {
				backOne.setBackSpeed(5);
				backTwo.setBackSpeed(5);
				player.setVerticalMovement(-400);
			}

			// pause
			// try { Thread.sleep(100); } catch (Exception e) {}

		} // while

	} // gameLoop
	

	/*
	 * startGame input: none output: none purpose: start a fresh game, clear old
	 * data
	 */
	private void startGame() {
		// clear out any existing entities and initalize a new set
		entities.clear();

		initEntities();

		// blank out any keyboard settings that might exist
		leftPressed = false;
		rightPressed = false;
		downPressed = false;
		upPressed = false;

	} // startGame

	/*
	 * inner class KeyInputHandler handles keyboard input from the user
	 */
	private class KeyInputHandler extends KeyAdapter {

		private int pressCount = 1; // the number of key presses since
									// waiting for 'any' key press

		/*
		 * The following methods are required for any class that extends the abstract
		 * class KeyAdapter. They handle keyPressed, keyReleased and keyTyped events.
		 */
		public void keyPressed(KeyEvent e) {

			// if waiting for keypress to start game, do nothing
			if (waitingForKeyPress) {
				return;
			} // if

			// respond to move left, right or fire or up
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = true;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = true;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = true;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = true;
			} // if
		} // keyPressed

		public void keyReleased(KeyEvent e) {
			// if waiting for keypress to start game, do nothing
			if (waitingForKeyPress) {
				return;
			} // if

			// respond to move left, right or fire
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = false;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = false;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = false;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = false;
			} // if

		} // keyReleased

		public void keyTyped(KeyEvent e) {

			// if waiting for key press to start game
			if (waitingForKeyPress) {
				if (pressCount == 1) {
					waitingForKeyPress = false;
					startGame();
					pressCount = 0;
				} else {
					pressCount++;
				} // else
			} // if waitingForKeyPress

			// if escape is pressed, end game
			if (e.getKeyChar() == 27) {
				System.exit(0);
			} // if escape pressed

		} // keyTyped

	} // class KeyInputHandler

	/**
	 * Main Program
	 */
	public static void main(String[] args) {
		// instantiate this object
		new Game();
	} // main
} // Game