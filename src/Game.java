
/* Game.java
 * Space Invaders Main Program
 *
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Game extends Canvas {

	private BufferStrategy strategy; // take advantage of accelerated graphics
	private boolean waitingForKeyPress = true; // true if game held up until a key is pressed
	private boolean leftPressed = false; // true if left arrow key currently pressed
	private boolean rightPressed = false; // true if right arrow key currently pressed
	private boolean spacePressed = false;
	private boolean cloudCollision = false;
	private boolean downPressed = false; // true if down arrow key is pressed
	private boolean mPressed = false;
	private boolean sPressed = false;
	private boolean pPressed = false;
	private boolean aPressed = false;
	private int xPos; // x position for enemy entities
	private int yPos; // x position for enemy entities
	private int lastBird = 0; // time since last bird spawn in millis
	private int cloudTime = 0;
	private boolean keyP = true;
	private float gameSpeed = 1.0F;
	private float tempGameSpeed = 0F;
	private int lives; // lives counter
	Life l1;
	Life l2;
	Life l3;
	private boolean lifeDrawn = true;
	private ArrayList<Entity> entities = new ArrayList<>(); // list of entities in game
	private ArrayList<Entity> removeEntities = new ArrayList<>(); // list of entities to remove this loop
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
		// livePanel = new JPanel();

		// set up the resolution of the game
		panel.setPreferredSize(new Dimension(600, 1080));
		panel.setLayout(null);
		/*
		 * livePanel.setLocation(0,0); livePanel.setSize(120, 40);
		 */
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
		addMouseListener(new MouseButtonRecogn());

		// request focus so key events are handled by this canvas
		requestFocus();

		// create buffer strategy to take advantage of accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		// initialize entities

		Gamestate.running = true;
		// start the game
		gameLoop(panel);
	} // constructor

	/*
	 * initEntities input: none output: none purpose: Initialise the starting state
	 * of the ship and alien entities. Each entity will be added to the array of
	 * entities in the game.
	 */
	private void initEntities() {
		player = new Player(this, "sprites/playerR.gif", 260, 100, 40, 20);
		entities.add(player);

		// add three hearts to the screen
		l1 = new Life(this, "sprites/heart.gif", 0, 1, 40, 40);
		entities.add(l1);
		l2 = new Life(this, "sprites/heart.gif", 40, 1, 40, 40);
		entities.add(l2);
		l3 = new Life(this, "sprites/heart.gif", 80, 1, 40, 40);
		entities.add(l3);

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

	/*
	 * Notification that the play has killed all aliens
	 */

	/*
	 * gameLoop input: none output: none purpose: Main game loop. Runs throughout
	 * game play. Responsible for the following activities: - calculates speed of
	 * the game loop to update moves - moves the game entities - draws the screen
	 * contents (entities, text) - updates game events - checks input
	 */

	public void gameLoop(JPanel panel) {

		long lastLoopTime = System.currentTimeMillis();

		// Scrolling Background
		BufferedImage back = null; // background image
		Background backOne = new Background(); // first copy of background image (used for moving background)
		Background backTwo = new Background(backOne.getImageHeight(), 0); // second copy of background image (used for
		int timer = 0;																	// moving background)
		boolean ded = false; // ded?

		while (Gamestate.running) {
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			long delta = (long) ((System.currentTimeMillis() - lastLoopTime) * gameSpeed);
			lastLoopTime = System.currentTimeMillis();

			// MENU = STATE
			if (Gamestate.state == Gamestate.MENU) {
				panel.paintComponents(g); // resets the panel to be blank
				this.setBackground(Color.DARK_GRAY);
				g.setColor(Color.BLACK);
				g.drawString("MENU", (600 - g.getFontMetrics().stringWidth("MENU")) / 2, 300);
				g.drawString("Press [up] to pause game", (600 - g.getFontMetrics().stringWidth("Press [up] to pause game")) / 2, 500);
				
				if(sPressed) {
					Gamestate.state = Gamestate.STORE;
				} // if store
				else if (!waitingForKeyPress) {
					System.out.println("starting");
					initEntities();
					Gamestate.state = Gamestate.GAME;
					
				} // if
				
			} // MENU = STATE

			// GAME = STATE
			else if (Gamestate.state == Gamestate.GAME) {
				
				//logic timers
				lastBird += delta;
				cloudTime += delta;

				// scrolling Background
				if (back == null) {
					back = (BufferedImage) (createImage(getWidth(), getHeight()));
				} // if

				// creates a buffer to draw to
				Graphics buffer = back.createGraphics();

				// puts the two copies of the background image onto the buffer
				backOne.draw(buffer);
				backTwo.draw(buffer);

				// draws the image onto the window
				g.drawImage(back, null, 0, 0);

				// if enough time has passed to spawn new birds
				if (lastBird > 1000) {
					lastBird = 0; // reset counter

					// spawn 6 bird entities if enough time has passed
					for (int i = 0; i < 5; i++) {
						xPos = (int) ((Math.random() * 110) + 1) * 5; // x position for enemy entities
						yPos = (int) ((Math.random() * 230) + 194) * 5; // x position for enemy entities
						Entity bird = new BirdEntity(this, "sprites/bird.gif", xPos, yPos, 20, 20);
						entities.add(bird);
					} // for

					// spawn 4 cloud entities if enough time has passed
					for (int i = 0; i < 3; i++) {
						xPos = (int) ((Math.random() * 110) + 1) * 5; // x position for enemy entities
						yPos = (int) ((Math.random() * 230) + 194) * 5; // x position for enemy entities
						Entity cloud = new CloudEntity(this, "sprites/cloud.gif", xPos, yPos, 70, 30);
						entities.add(cloud);
					} // for
					
					// spawn 4 coin entities if enough time has passed
					for (int i = 0; i < 4; i++) {
						xPos = (int) ((Math.random() * 110) + 1) * 5; // x position for enemy entities
						yPos = (int) ((Math.random() * 230) + 194) * 5; // x position for enemy entities
						Entity coin = new CoinEntity(this, "sprites/coin.png", xPos, yPos,  30, 30);
						entities.add(coin);
					} // for
				} // if

				// sets enemies to move up
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					if (entity instanceof BirdEntity || entity instanceof CloudEntity || entity instanceof CoinEntity && downPressed) {
						entity.setVerticalMovement(-800);
					} else if (entity instanceof BirdEntity || entity instanceof CloudEntity || entity instanceof CoinEntity && !downPressed) {
						entity.setVerticalMovement(-600);
					} // else if

					// remove entities that pass the upper screen limit
					if (entity.y < 0) {
						removeEntities.add(entity);
					} // if

					entity.move(delta);
				} // for

				// draw all entities
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.draw(g);
				} // for
				
				//display coin info
				g.setColor(Color.WHITE);
				g.setFont(new Font("SansSerif", Font.BOLD, 12));
				g.drawString("Coins collected: " + player.coins, 480, 30);
				Image img = null;
				try {
					img = ImageIO.read(getClass().getClassLoader().getResource("sprites/coin.png"));
				} catch (IOException e) {e.printStackTrace();}
				g.drawImage(img, 450, 10, null);

				// for logic
				for (int i = 0; i < entities.size(); i++) {
					
					//if coin
					if (entities.get(i) instanceof CoinEntity) {
						CoinEntity coin = (CoinEntity) entities.get(i);
						if(player.collidesWith(coin)) {
							removeEntities.add(coin);
							player.coins++;
						}// add coins if
					}// if
					
					//if bird
					if (entities.get(i) instanceof BirdEntity) {

						BirdEntity enemy = (BirdEntity) entities.get(i);
						if (player.collidesWith(enemy)) {
							try {
								Thread.sleep(20);
							} catch (Exception e) {}
							
							removeEntities.add(enemy);
							lives--;
							System.out.println(lives + " lives left");
						} // if birdEntity collides with player

					} // if bird entity

					// deals with cloud collsions and slo-mo
					if (entities.get(i) instanceof CloudEntity && player.collidesWith(entities.get(i))) {
						cloudCollision = true;
						cloudTime = 0;
						gameSpeed = 0.9F;
					} // if
					else if (cloudCollision) {
						if (cloudTime < 500) {
							gameSpeed = 0.9F;
						} // if
						else if (cloudTime > 500) {
							cloudCollision = false;
							cloudTime = 0;
							gameSpeed = 1.0F;
						} // elif

					} // elif cloud collision
				} // for

				// remove dead entities
				entities.removeAll(removeEntities);
				removeEntities.clear();

				player.setHorizontalMovement(0);
				player.setVerticalMovement(0);

				// respond to user moving ship
				if ((leftPressed) && (!rightPressed)) {
					player.setHorizontalMovement(-moveSpeed);
				} else if ((rightPressed) && (!leftPressed)) {
					player.setHorizontalMovement(moveSpeed);
				} else if (downPressed) {
					gameSpeed = 1.3F;
					player.setVerticalMovement(120);
				} else if (!downPressed && !cloudCollision) {
					gameSpeed = 1.0F;
					player.setVerticalMovement(-400);
				} // elif
				
				// pause
				if(pPressed) {
					tempGameSpeed = gameSpeed;
					gameSpeed = 0F;
					Gamestate.state = Gamestate.PAUSE;
				}// if [p]
				
				//lives check
				if (lives == 2) {
					removeEntities.add(l3);
				} else if (lives == 1) {
					removeEntities.add(l2);
				} else if (lives == 0) {
					removeEntities.addAll(entities);
					Gamestate.state = Gamestate.DEATH;
				} // if

			} // else if GAME = STATE
			else if (Gamestate.state == Gamestate.DEATH) {
				message = "Press [esc] to quit";
				panel.paintComponents(g); // resets the panel to be blank
				this.setBackground(Color.DARK_GRAY);
				g.setColor(Color.BLACK);
				g.drawString("Coins collected: " + player.coins, (600 - g.getFontMetrics().stringWidth("Coins collected: " + player.coins)) / 2, 100);
				g.drawString("YOU DIED", (600 - g.getFontMetrics().stringWidth("YOU DIED")) / 2, 300);
				g.drawString(message, (600 - g.getFontMetrics().stringWidth(message)) / 2, 800);
				message = "Press [space] to play again";
				g.drawString(message, (600 - g.getFontMetrics().stringWidth(message)) / 2, 900);
				message = " Press [m] to access menu";
				g.drawString(message, (600 - g.getFontMetrics().stringWidth(message)) / 2, 1000);
				
				try {
					BufferedImage image = ImageIO.read(new File("bin/deadBird.png"));
					g.drawImage(image, 145, 400, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//check for new actions
				if(spacePressed) {
					initEntities();
					Gamestate.state = Gamestate.GAME;
				}// if space
				else if(mPressed) {
					Gamestate.state = Gamestate.MENU;
					waitingForKeyPress = true;
				} //elif menu
				else if(sPressed) {
					Gamestate.state = Gamestate.STORE;
				} // elif store
				
			} // DEATH = STATE
			else if (Gamestate.state == Gamestate.STORE) {
				
				timer += (int) delta;
				panel.paintComponents(g); // resets the panel to be blank
				this.setBackground(Color.darkGray);
				
				updateCoins(g);
			
				//draw menu message
				g.drawString("STORE", (600 - g.getFontMetrics().stringWidth("STORE")) / 2, 300);
				g.drawString("Press [m] to return to menu", (600 - g.getFontMetrics().stringWidth("Press [m] to return to menu")) / 2, 900);
				
				//force field thing power-up
				g.drawString("Super Saiyan [a]: 20", 75, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/saiyanPlayer.png"));
					g.drawImage(image, 95, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//coin doubler thing power-up
				g.drawString("Coin Doubler: 40", 250, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/2XCoins.png"));
					g.drawImage(image, 270, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//+1 life thing power-up
				g.drawString("+1 Life: 50", 425, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/heart.gif"));
					g.drawImage(image, 435, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//check for new actions
				if(mPressed) {
					Gamestate.state = Gamestate.MENU;
					waitingForKeyPress = true;
				}// if [m]
				if(aPressed) {
					if(player.coins >= 2) {
						if(timer > 3000) {
							timer = 0;
							g.drawString("Succesfully purchased Super Saiyan!", 
									(600 - g.getFontMetrics().stringWidth("Succesfully purchased Super Saiyan!")) / 2, 700);
							player.coins -= 2;
						}
					}// if
					else {
						if(timer > 3000) {
							timer = 0;
						g.drawString("Not enough coins to purchase Super Saiyan", 
								(600 - g.getFontMetrics().stringWidth("Not enough coins to purchase Super Saiyan")) / 2, 700);
						}
					}// else
				}// if want to buy SS
			} //elif STORE = STATE
			else if(Gamestate.state == Gamestate.PAUSE) {
				panel.paintComponents(g); // resets the panel to be blank
				this.setBackground(Color.DARK_GRAY);
				g.setColor(Color.BLACK);
				g.drawString("GAME IS PAUSED", (600 - g.getFontMetrics().stringWidth("GAME IS PAUSED")) / 2, 300);
				g.drawString("Press [up] to unpause", (600 - g.getFontMetrics().stringWidth("Press [up] to unpause")) / 2, 500);
				//check for new actions
				if(pPressed) {
					gameSpeed = tempGameSpeed;
					Gamestate.state = Gamestate.GAME;
				}// if [p]
			}// elif PAUSE = STATE
			
			// clear graphics and flip buffer
			g.dispose();
			strategy.show();
			
		} // while (runing)

	} // gameLoop

	private void updateCoins(Graphics2D g) {
		//display coins
		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif", Font.BOLD, 12));
		g.drawString("Coins : " + player.coins, 490, 30);
		Image img = null;
		try {
			img = ImageIO.read(getClass().getClassLoader().getResource("sprites/coin.png"));
			g.drawImage(img, 450, 10, null);
		} catch (IOException e) {e.printStackTrace();}
	}// coin update

	/*
	 * startGame input: none output: none purpose: start a fresh game, clear old
	 * data
	 */
	private void startGame() {
		// clear out any existing entities and initalize a new set
		entities.clear();

		// blank out any keyboard settings that might exist
		leftPressed = false;
		rightPressed = false;
		downPressed = false;
		spacePressed = false;
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

			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				System.out.println("Game exited with code _0");
				System.exit(0);
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				spacePressed = true;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_M) {
				mPressed = true;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_S) {
				sPressed = true;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				pPressed = true;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_A) {
				aPressed = true;
			} // if esc=true, close game
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
			
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				spacePressed = false;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_M) {
				mPressed = false;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_S) {
				sPressed = false;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				pPressed = false;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_A) {
				aPressed = false;
			} // if esc=true, close game
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
		
	private class MouseButtonRecogn extends MouseAdapter {
		 
		  @Override
		  public void mouseClicked(MouseEvent event) {
		 
		    if ((event.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
		 
		    	System.out.println("Left click detected" + (event.getPoint()));
		    }
		 
		  }
		}
	
	/**
	 * Main Program
	 */
	public static void main(String[] args) {
		// instantiate this object
		new Game();
	} // main
} // Game
