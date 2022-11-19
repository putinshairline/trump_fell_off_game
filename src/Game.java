
/* Game.java
 * Space Invaders Main Program
 *
 */

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends Canvas {

	private BufferStrategy strategy; // take advantage of accelerated graphics
	private boolean leftPressed = false; // true if left arrow key currently pressed
	private boolean rightPressed = false; // true if right arrow key currently pressed
	private boolean spacePressed = false;
	private boolean cloudCollision = false;
	private boolean downPressed = false; // true if down arrow key is pressed
	private boolean mPressed = false;
	private boolean sPressed = false;
	private boolean pPressed = false;
	private boolean aPressed = false;
	private boolean bPressed = false;
	private boolean cPressed = false;
	private boolean mouseClicked = true;//true if mouse clicked
	private Point clickLocation = new Point(-1, -1);//stores the location of the mouse
	private int coinsTemp = 0;
	private int xPos; // x position for enemy entities
	private int yPos; // x position for enemy entities
	private int lastBird = 0; // time since last bird spawn in millis
	private int cloudTime = 0;
	private int isSaiyan = 0;
	private float gameSpeed = 1.0F;
	private float tempGameSpeed = 0F; // temporary game speed
	private ArrayList<Entity> entities = new ArrayList<>(); // list of entities in game
	private ArrayList<Entity> removeEntities = new ArrayList<>(); // list of entities to remove this loop
	private ArrayList<Life> lifeEntities = new ArrayList<>(); // list of entities in game
	private ArrayList<Upgrade> upgrades = new ArrayList<>();
	private Player player; // the player
	private double moveSpeed = 600; // hor. vel. of ship (px/s)
	private String message = ""; // message to display while waiting

	/*
	 * Construct our game and set it running.
	 */
	public Game() {
		
		//make code for text file import here
		
		
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
		int hx = 0; // x pos for hearts, var so it can be incremented
		
		//add player
		player = new Player(this, "sprites/playerR.png", 260, 100, 40, 20);
		
		//Applies all the upgrades to the player
		for(Upgrade u: upgrades) {
			u.upgradeMechanic(player);
		}
		
		
		entities.add(player);
		
		//add and reset the temporary coins
		if(coinsTemp != 0) {
			player.coins += coinsTemp;
			coinsTemp = 0;
		}// if
		
		if(player.addLives > 8) {
			player.addLives -= player.addLives - 8;
		}
		//add lives to player
		for(int i = 0; i < (3 + player.addLives); i++) {
			System.out.println("INIT_HEARTS_" + i);
			lifeEntities.add(new Life(this, "sprites/heart.gif", hx, 1, 40, 40));
			hx += 40;
		}// for
 
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
		long nextSecond = System.currentTimeMillis() + 1000;
		int frameInLastSecond = 0;
		int framesInCurrentSecond = 0;
		int timer = 0;
		int distance = 0;
		//storage of all of the buttons
		GraphicsButton startB = null;
		GraphicsButton storeB = null;
		GraphicsButton quitB = null;
		GraphicsButton superSaiyanBuyB = null;
		GraphicsButton heartBuyB = null;
		GraphicsButton doubleCoinBuyB = null;
		GraphicsButton menuB = null;
		
		
		//Initialization of all the buttons
		try {
			startB = new GraphicsButton(450, 910, 100, 100, "sprites/playB.jpg");
			storeB = new GraphicsButton(450, 800, 100, 100, "sprites/storeB.jpg");
			quitB = new GraphicsButton(530, 20, 50, 50, "sprites/quitB.jpg");
			superSaiyanBuyB = new GraphicsButton(100, 650, 50, 50, "sprites/buyB.jpg");
			doubleCoinBuyB = new GraphicsButton(280, 650, 50, 50, "sprites/buyB.jpg");
			heartBuyB = new GraphicsButton(430, 650, 50, 50, "sprites/buyB.jpg");
			menuB = new GraphicsButton(450, 970, 100, 100, "sprites/menuB.jpg");
			
		}
		catch(IOException e){}
		
		while (Gamestate.running) {
			
			
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			long delta = (long) ((System.currentTimeMillis() - lastLoopTime) * gameSpeed);
			lastLoopTime = System.currentTimeMillis();
			timer += delta;
			
			//fps stuff
			long currentTime = System.currentTimeMillis();
			if (currentTime > nextSecond) {
				nextSecond += 1000;
				frameInLastSecond = framesInCurrentSecond;
				framesInCurrentSecond = 0;
			}
			framesInCurrentSecond++;
			
			//fps isplay
			if(timer >= 1000) {
				timer = 0;
				System.out.println(frameInLastSecond + " fps");
			}
			
			
			// MENU = STATE
			if (Gamestate.state == Gamestate.MENU) {
				panel.paintComponents(g); // resets the panel to be blank
				try {
					BufferedImage image = ImageIO.read(new File("bin/menuBacker.jpg"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();} 	
				g.setColor(Color.WHITE);
				g.drawString("MENU", (600 - g.getFontMetrics().stringWidth("MENU")) / 2, 300);
				g.drawString("Press [up] to pause game", (600 - g.getFontMetrics().stringWidth("Press [up] to pause game")) / 2, 500);
				
				//draws all the menu buttons
				startB.draw(g);
				storeB.draw(g);
				quitB.draw(g);
				
				
				//starts the game once start button is clicked
				if(mouseClicked & startB.contains(clickLocation)) {
					initEntities();
					Gamestate.state = Gamestate.GAME;
					mouseClicked = false;
				}
				
				if(mouseClicked & storeB.contains(clickLocation)) {
					initEntities();
					Gamestate.state = Gamestate.STORE;
					mouseClicked = false;
				}
				
				if(mouseClicked & quitB.contains(clickLocation)) {
					System.exit(0);
				}
				
				int y = 200;
				for(Upgrade u : upgrades) {
					u.draw(g, 0, y, 40, 40);
					y+=40;
				}// for upgrades
				
			} // MENU = STATE

			// GAME = STATE
			else if (Gamestate.state == Gamestate.GAME) {
				distance += Math.round((delta/2.3453450) / 10);
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

					// spawn 6 ghoul entities if enough time has passed
					for (int i = 0; i < 5; i++) {
						xPos = (int) ((Math.random() * 110) + 1) * 5; // x position for enemy entities
						yPos = (int) ((Math.random() * 230) + 194) * 5; // x position for enemy entities
						Entity bird = new BirdEntity(this, "sprites/ghoul.png", xPos, yPos, 30, 30);
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
						Entity coin = new CoinEntity(this, "sprites/shrine.png", xPos, yPos,  16, 40);
						entities.add(coin);
					} // for
				} // if

				// sets enemies to move up
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					if (entity instanceof BirdEntity || entity instanceof CloudEntity || entity instanceof CoinEntity && downPressed) {
						// if cloud set speed different to bird for 3d effect
						if(entity instanceof CloudEntity) {
							entity.setVerticalMovement(-700 + (double)(Math.random() * 50) + 1);
						}// if
						else {
							entity.setVerticalMovement(-800 + (double)(Math.random() * 50) + 1);
						}// else
					} else if (entity instanceof BirdEntity || entity instanceof CloudEntity || entity instanceof CoinEntity && !downPressed) {
						// if cloud set speed different to bird for 3d effect
						if(entity instanceof CloudEntity) {
							entity.setVerticalMovement(-500 + (double)(Math.random() * 50) + 1);
						}// if
						else {
							entity.setVerticalMovement(-600 + (double)(Math.random() * 50) + 1);
						}// else
					} // else if downPressed

					// remove entities that pass the upper screen limit
					if (entity.y < 0) {
						removeEntities.add(entity);
					} // if

					entity.move(delta);
				} // for
				
				// deals with super saiyan timing logic
				if(player.canBeSaiyan && sPressed) {
					player.saiyan = true;
				}// if
				if(player.saiyan) {
					isSaiyan += delta;
				}// if
				if(isSaiyan > 3500) {
					isSaiyan = 0;
					player.saiyan = false;
				}//if 
				
				// draw all entities
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.draw(g);
				} // for
				
				// draw all hearts
				for (int i = 0; i < lifeEntities.size(); i++) {
					Life life = (Life) lifeEntities.get(i);
					life.draw(g);
				} // for
				g.setColor(Color.WHITE);
				g.drawString("Distance travelled: " + distance, 10, 950);
				
				//display coin info
				g.setFont(new Font("SansSerif", Font.BOLD, 12));
				if(player.doubleCoins) {
					g.drawString("2X " + player.coins, 420, 30);
				}
				g.drawString("Coins collected: " + player.coins, 480, 30);
				Image img = null;
				try {
					img = ImageIO.read(getClass().getClassLoader().getResource("sprites/shrine.png"));
				} catch (IOException e) {e.printStackTrace();}
				g.drawImage(img, 450, 10, null);

				// for logic
				for (int i = 0; i < entities.size(); i++) {
					
					//if coin
					if (entities.get(i) instanceof CoinEntity) {
						CoinEntity coin = (CoinEntity) entities.get(i);
						if(player.collidesWith(coin)) {
							removeEntities.add(coin);
							if(player.doubleCoins)
								player.coins+=2;
							else
								player.coins++;
						}// add coins if
					}// if
					
					//if bird
					if (entities.get(i) instanceof BirdEntity) {

						BirdEntity enemy = (BirdEntity) entities.get(i);
						if (player.collidesWith(enemy)) {
							if(!player.saiyan) {
								removeEntities.add(enemy);
								lifeEntities.remove(lifeEntities.size()-1);
								System.out.println(lifeEntities.size() + " lives left");
							}// if player is saiyan, he is invincible
						} // if birdEntity collides with player

					} // if bird entity

					// deals with cloud collsions and slo-mo
					if (entities.get(i) instanceof CloudEntity && player.collidesWith(entities.get(i))) {
						cloudCollision = true;
						cloudTime = 0;
						gameSpeed = 0.4F;
					} // if
					else if (cloudCollision) {
						if (cloudTime < 500) {
							gameSpeed = 0.4F;
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
				if (lifeEntities.isEmpty()) {
					coinsTemp += player.coins;
					removeEntities.addAll(entities);
					Gamestate.state = Gamestate.DEATH;
				} // if
			} // else if GAME = STATE
			else if (Gamestate.state == Gamestate.DEATH) {
				
				menuB.draw(g);
				
				if(mouseClicked && menuB.contains(clickLocation)) {
					Gamestate.state = Gamestate.MENU;
					mouseClicked = false;
				}
				
				message = "Press [esc] to quit";
				panel.paintComponents(g); // resets the panel to be blank
				try {
					BufferedImage image = ImageIO.read(new File("bin/menuBacker.jpg"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();} 
				g.setColor(Color.BLACK);
				g.drawString("Coins collected: " + player.coins, (600 - g.getFontMetrics().stringWidth("Coins collected: " + player.coins)) / 2, 100);
				g.drawString("YOU DIED", (600 - g.getFontMetrics().stringWidth("YOU DIED")) / 2, 300);
				g.drawString(message, (600 - g.getFontMetrics().stringWidth(message)) / 2, 800);
				message = "Press [space] to play again";
				g.drawString(message, (600 - g.getFontMetrics().stringWidth(message)) / 2, 900);
				message = " Press [m] to access menu";
				g.drawString(message, (600 - g.getFontMetrics().stringWidth(message)) / 2, 1000);
				
				
				int y = 200;
				for(Upgrade u : upgrades) {
					u.draw(g, 0, y, 40, 40);
					y+=40;
				}// for upgrades
				
				quitB.draw(g);
				
				if(mouseClicked & quitB.contains(clickLocation)) {
					System.exit(0);
				}
				
				//check for new actions
				if(spacePressed) {
					initEntities();
					Gamestate.state = Gamestate.GAME;
				}// if space
				else if(mPressed) {
					Gamestate.state = Gamestate.MENU;
				} //elif menu
				else if(sPressed) {
					Gamestate.state = Gamestate.STORE;
				} // elif store
				
			} // DEATH = STATE
			else if (Gamestate.state == Gamestate.STORE) {
				panel.paintComponents(g); // resets the panel to be blank
				try {
					BufferedImage image = ImageIO.read(new File("bin/menuBacker.jpg"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();}
				
				updateCoins(g);
			
				superSaiyanBuyB.draw(g);
				heartBuyB.draw(g);
				doubleCoinBuyB.draw(g);
				
				//draw menu message
				g.drawString("STORE", (600 - g.getFontMetrics().stringWidth("STORE")) / 2, 300);
				g.drawString("Press [m] to return to menu", (600 - g.getFontMetrics().stringWidth("Press [m] to return to menu")) / 2, 900);
				
				//force field thing power-up
				g.drawString("Super Saiyan [a]: 20", 75, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/saiyan.png"));
					g.drawImage(image, 95, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//coin doubler thing power-up
				g.drawString("Coin Doubler: 40 [b]", 250, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/2XCoins.png"));
					g.drawImage(image, 270, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//+1 life thing power-up
				g.drawString("+1 Life: 50 [c]", 425, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/heart.gif"));
					g.drawImage(image, 435, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				
				//check for new actions
				if(mPressed) {
					Gamestate.state = Gamestate.MENU;
				}// if [m]
				if(mouseClicked && superSaiyanBuyB.contains(clickLocation)) {
					System.out.println("ss");
					if(player.coins >= 2) {
							g.drawString("Succesfully purchased Super Saiyan!", 
									(600 - g.getFontMetrics().stringWidth("Succesfully purchased Super Saiyan!")) / 2, 700);
							player.coins -= 2;
							coinsTemp -= 2;
							
							//adds a new "super saiyan upgrade" to upgrades arrayList
							try {
								upgrades.add(new Upgrade("s", "sprites/saiyan.png") {
										@Override
										public void upgradeMechanic(Player p) {
											p.canBeSaiyan = true;
										}
									});
							}catch(IOException e) {System.out.println(e);}	
							
							;
					}// if
					else {
						//Player.canBeSaiyan = false;
						g.drawString("Not enough coins to purchase Super Saiyan", 
								(600 - g.getFontMetrics().stringWidth("Not enough coins to purchase Super Saiyan")) / 2, 700);
					}// else
					
					mouseClicked = false;
				}// if want to buy SS ([a])
				if(mouseClicked && heartBuyB.contains(clickLocation)) {
					System.out.println("h");
					if(player.coins >= 5) {
							g.drawString("Succesfully purchased another life!", 
									(600 - g.getFontMetrics().stringWidth("Succesfully purchased another life!")) / 2, 700);
							player.coins -= 5;
							coinsTemp -= 5;
							try {
								upgrades.add(new Upgrade("h", "sprites/heart.gif") {
										@Override
										public void upgradeMechanic(Player p) {
											p.addLives++;
										}
									});
							}catch(IOException e) {System.out.println(e);}	
							System.out.println("Additional lives: " + player.addLives);
							
							
							
					}// if
					else {
						//Player.canBeSaiyan = false;
						g.drawString("Not enough coins to purchase another life", 
								(600 - g.getFontMetrics().stringWidth("Not enough coins to purchase another life")) / 2, 700);
					}// else
					
					mouseClicked = false;
				}// if want to buy new life ([c])
				if(mouseClicked && doubleCoinBuyB.contains(clickLocation)) {
					System.out.println("dc");
					if(player.coins >= 4) {
							g.drawString("Succesfully purchased a coin Doubler", 
									(600 - g.getFontMetrics().stringWidth("Succesfully purchased a coin Doubler")) / 2, 700);
							player.coins -= 4;
							coinsTemp -= 4;
							
							//adds a new "coin doubler upgrade" to upgrades arrayList
							try {
								upgrades.add(new Upgrade("d", "sprites/2XCoins.png") {
										@Override
										public void upgradeMechanic(Player p) {
											p.doubleCoins = true;
										}
									});
							}
							catch(IOException e) {System.out.println(e);}
							
							
					}// if
					else {
						player.doubleCoins = false;
						g.drawString("Not enough coins to purchase a coin doubler", 
								(600 - g.getFontMetrics().stringWidth("Not enough coins to a coin doubler")) / 2, 700);
					}// else
					
					mouseClicked = false;
				}// if want to buy SS ([a])
				try{Thread.sleep(120);}catch(Exception e) {}
				
				//draw upgrades
				int y = 200;
				for(Upgrade u : upgrades) {
					u.draw(g, 0, y, 40, 40);
					y+=40;
				}// for upgrades
				
			} //elif STORE = STATE
			else if(Gamestate.state == Gamestate.PAUSE) {
				panel.paintComponents(g); // resets the panel to be blank
				try {
					BufferedImage image = ImageIO.read(new File("bin/menuBacker.jpg"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();} 	
				g.setColor(Color.BLACK);
				g.drawString("GAME IS PAUSED", (600 - g.getFontMetrics().stringWidth("GAME IS PAUSED")) / 2, 300);
				g.drawString("Press [up] to unpause", (600 - g.getFontMetrics().stringWidth("Press [up] to unpause")) / 2, 500);
				
				try {Thread.sleep(20);}catch(Exception e) {};
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
		g.drawString("Coins : " + player.coins, 490, 40);
		Image img = null;
		try {
			img = ImageIO.read(getClass().getClassLoader().getResource("sprites/shrine.png"));
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
		allKeysFalse();
	} // startGame

	public void allKeysFalse() {
		// blank out any keyboard settings that might exist
		leftPressed = false;
		rightPressed = false;
		downPressed = false;
		spacePressed = false;
		sPressed = false;
		mPressed = false;
		pPressed = false;
		aPressed = false;
		bPressed = false;
	}// resets all the keys
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
			
			if (e.getKeyCode() == KeyEvent.VK_C) {
				cPressed = true;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_B) {
				bPressed = true;
			} // if esc=true, close game
		} // keyPressed

		public void keyReleased(KeyEvent e) {

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
			
			if (e.getKeyCode() == KeyEvent.VK_C) {
				cPressed = false;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_B) {
				bPressed = false;
			} // if esc=true, close game
		} // keyReleased

		public void keyTyped(KeyEvent e) {
			
			// if escape is pressed, end game
			if (e.getKeyChar() == 27) {
				System.exit(0);
			} // if escape pressed

		} // keyTyped

	} // class KeyInputHandler
		
	private class MouseButtonRecogn extends MouseAdapter {
		 
		
		//gets the location of the mouse on click
		@Override
		public void mouseClicked(MouseEvent event) {
		 
		    mouseClicked = true;
		    clickLocation = event.getPoint();
		   
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
