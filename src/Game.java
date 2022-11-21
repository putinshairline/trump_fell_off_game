
/* Game.java
 * Space Invaders Main Program
 *
 */

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
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
import java.util.Objects;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends Canvas {

	private BufferStrategy strategy; // take advantage of accelerated graphics
	
	private boolean cloudCollision = false;//true if the player has collided with A cloud
	
	//key listener states(true if the button specified in the name is clicked
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean downPressed = false;
	private boolean mPressed = false;
	private boolean sPressed = false;

	
	private boolean mouseClicked = true;//true if mouse clicked
	
	private Point clickLocation = new Point(-1, -1);//stores the location of the mouse

	private int xPos; // x position for enemy entities
	private int yPos; // x position for enemy entities
	private int lastBird = 0; // time since last bird spawn in millis
	private int cloudTime = 0;//stores the time since the player collided with a cloud in millis
	private int isSaiyan = 0;
	private float gameSpeed = 0.5F;
	
	private ArrayList<Entity> entities = new ArrayList<>(); // list of entities in game
	private ArrayList<Entity> removeEntities = new ArrayList<>(); // list of entities to remove this loop
	private ArrayList<Life> lifeEntities = new ArrayList<>(); // list of entities in game
	
	//stores the list of all upgrades
	//index 0 contains a CoinContainer
	private ArrayList<Upgrade> upgrades = new ArrayList<>();
	
	
	private Player player; // the player
	private double moveSpeed = 600; // velocity of the player
	
	
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

		// add key listener to this canvas
		addKeyListener(new KeyInputHandler());
		addMouseListener(new MouseButtonRecogn());
		
		// if user closes window, shutdown game and jre
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // windowClosing
		});
		
		// request focus so key events are handled by this canvas
		requestFocus();

		// create buffer strategy to take advantage of accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		
		//prepares all of the automatic save and loading functions
		prepareSaveSystem();
		
		Gamestate.running = true;
		// start the game
		gameLoop(panel);
	} // constructor

	public void prepareSaveSystem() {
		
		File save = null;//stores the text file that the save data will be loaded to
		boolean isFirstLine = true;//stores if the scanner is going through the first line
		
		//Creates a CoinContainer in the upgrades ArrayList to store the players coins between each playthrough 
		try {
			upgrades.add(new CoinContainer("%", "sprites/2XCoins.png"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//catch
		
		
		//loads all of the upgrades and coins from the save file 
		try {
			
			save = new File("bin/save.txt");//creates the save file
			
			//scanner to run through the save file
			Scanner saveScanner = new Scanner(save);
			
			//loops through each of the save text file
			while(saveScanner.hasNextLine()) {
				
				//stores the number of coins from the first line of the save file in the CoinContainer Upgrade
				if(isFirstLine) { 
					((CoinContainer)upgrades.get(0)).setCoinQuantity(saveScanner.nextInt());
					isFirstLine = false;
					saveScanner.nextLine();
				}//if
				else {
					
					//stores the upgrades from the save file based on the upgrades personal tag
					switch(saveScanner.nextLine().charAt(0)) {
						case 's':
							
							//creates and stores a super saiyan upgrade
							upgrades.add(new Upgrade("s", "sprites/saiyan.png") {
									@Override
									public void upgradeMechanic(Player p) {
										p.canBeSaiyan = true;
									}//upgradeMechanic
								});
							break;
						case 'h':
							
							//creates and stores a heart upgrade
							upgrades.add(new Upgrade("h", "sprites/heart.gif") {
									@Override
									public void upgradeMechanic(Player p) {
										p.addLives++;
									}//upgradeMechanic
								});
							break;
						case 'd':
							
							//creates and stores a double coins upgrade
							upgrades.add(new Upgrade("d", "sprites/2XCoins.png") {
									@Override
									public void upgradeMechanic(Player p) {
										p.doubleCoins = true;
									}//upgradeMechanic
								});
							break;
						
					}//switch
					
				}//else
				
			}//while
			
		} catch (Exception e) {
			
		}//catch
		
		//creates a shutdown hook thread that sleeps until the JVM begins its shutdown process
		Runtime.getRuntime().addShutdownHook(new GameSaveFileController(upgrades, save));
		
	}//prepareSaveSystem
	
	private void initEntities() {
		int hx = 0; // x pos for hearts, var so it can be incremented
		
		//removes previous player if it exists
		if(!Objects.isNull(player)) {
			entities.remove(entities.indexOf(player));
		}//if
		
		//add player
		player = new Player(this, "sprites/playerR.png", 260, 100, 40, 20);

		//Applies all the upgrades to the player
		for(Upgrade u: upgrades) {
			u.upgradeMechanic(player);
		}//for
		
		
		entities.add(player);
		//add and reset the temporary coins
		
		if(player.addLives > 8) {
			player.addLives -= player.addLives - 8;
		}//if
		
		//add lives to player
		for(int i = 0; i < (3 + player.addLives); i++) {

			lifeEntities.add(new Life(this, "sprites/heart.gif", hx, 1, 40, 40));
			hx += 40;
			
		}// for
 
	} // initEntities

	//removes an entity from the game
	public void removeEntity(Entity entity) {
		removeEntities.add(entity);
	} // removeEntity

	
	//primary game loop where the main functions and menus are store
	public void gameLoop(JPanel panel) {

		long lastLoopTime = System.currentTimeMillis();//store the time in millisection that the last loop occured at
		boolean firstDeathLoop = false;//true if its the first loop that the player is dead
		int distance = 0;
		
		//storage of all of the buttons in all the menus
		GraphicsButton startB = null;
		GraphicsButton storeMenuB = null;
		GraphicsButton quitB = null;
		GraphicsButton superSaiyanBuyB = null;
		GraphicsButton heartBuyB = null;
		GraphicsButton doubleCoinBuyB = null;
		GraphicsButton menuB = null;
		GraphicsButton storeDeathB = null;
		GraphicsButton restartB = null;
		GraphicsButton menuStoreB = null;
		
		//Initialization of all the buttons
		try {
			startB = new GraphicsButton(450, 910, 100, 100, "sprites/playB.jpg");
			storeMenuB = new GraphicsButton(450, 800, 100, 100, "sprites/storeB.jpg");
			quitB = new GraphicsButton(530, 20, 50, 50, "sprites/quitB.jpg");
			superSaiyanBuyB = new GraphicsButton(100, 650, 50, 50, "sprites/buyB.jpg");
			doubleCoinBuyB = new GraphicsButton(275, 650, 50, 50, "sprites/buyB.jpg");
			heartBuyB = new GraphicsButton(430, 650, 50, 50, "sprites/buyB.jpg");
			menuB = new GraphicsButton(350, 930, 100, 100, "sprites/menuB.jpg");
			storeDeathB = new GraphicsButton(250, 930, 100, 100, "sprites/storeB.jpg");
			restartB = new GraphicsButton(150, 930, 100, 100, "sprites/restartB.jpg");
			menuStoreB = new GraphicsButton(250, 930, 100, 100, "sprites/menuB.jpg");
		}catch(IOException e){}
		
		while (Gamestate.running) {
			
			//creates and resets the graphics that the game loop uses to display itself
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			
			//stores the time and sets the speed at which the game should run
			long delta = (long) ((System.currentTimeMillis() - lastLoopTime) * gameSpeed);
			lastLoopTime = System.currentTimeMillis();
			
			//sets the text drawing color for the game
			g.setColor(new Color(12917160));
			
			
			
			// STATE = GAME
			if (Gamestate.state == Gamestate.MENU) {
				
				panel.paintComponents(g); // resets the panel to be blank
				
				//sets the background to the menu background
				try {
					BufferedImage image = ImageIO.read(new File("bin/menuBacker.jpg"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();} 	
				
				
				//draws all the menu buttons
				startB.draw(g);
				storeMenuB.draw(g);
				quitB.draw(g);
				
				
				//starts the game once start button is clicked
				if(mouseClicked & startB.contains(clickLocation)) {
					
					//inishalises the primary game 
					initEntities();

					Gamestate.state = Gamestate.GAME;
					mouseClicked = false;
				}//if
				
				//goes to the store if the store button is clicked
				else if(mouseClicked & storeMenuB.contains(clickLocation)) {
					
					//inishalises the primary game
					initEntities();
						
					Gamestate.state = Gamestate.STORE;
					mouseClicked = false;
				}//else if
				
				//exits the program if the quit button is clicked 
				else if(mouseClicked & quitB.contains(clickLocation)) {
					System.exit(0);
				}//else if
				
				//draws all the upgrades that the player has
				
				int y = 200;//stores the current draw location
				
				for(Upgrade u : upgrades) {
					u.draw(g, 0, y, 40, 40);
					y+=40;
				}// for upgrades
				
			} //if STATE = MENU

			// STATE = GAME
			else if (Gamestate.state == Gamestate.GAME) {
				
				firstDeathLoop = true;//sets the player back to not having died yet
				
				distance += Math.round((delta/2.3453450) / 10);//updates the distance 
 
				lastBird += delta;//updates the count for the last bird spawn
				cloudTime += delta;//updates the count for the last cloud spawn
				
				//draws the background for the game
				try {
					BufferedImage image = ImageIO.read(new File("bin/gameBackground.png"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();} 	

				
				// if enough time has passed to spawn new birds
				if (lastBird > 1000) {
					lastBird = 0; // reset counter

					// spawn 6 ghoul entities if enough time has passed
					for (int i = 0; i < 5; i++) {
						
						xPos = (int) ((Math.random() * 110) + 1) * 5; // x position for enemy entities
						yPos = (int) ((Math.random() * 230) + 194) * 5; // x position for enemy entities
						
						Entity bird = new BirdEntity(this, "sprites/ghoul.gif", xPos, yPos, 30, 30);
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
					
					//moves the entity
					entity.move(delta);
					
				} // for
				
				// deals with super saiyan timing logic
				if(player.canBeSaiyan && sPressed) {
					
					//checks if there are any saiyan upgrades left
					for(int i = 0; i < upgrades.size(); i++) {
						
						if(upgrades.get(i).getTag().equals("s")) {
							
							player.saiyan = true;
							
						}//if
						
					}//for
					
				}// if
				
				if(player.saiyan) {
					
					//counts the amount of time during super saiyan
					isSaiyan += delta;
				}// if
				
				//cancels the super saiyan once 3500 millis has passed
				if(isSaiyan > 3500) {
					
					//sats saiyan to false
					isSaiyan = 0;
					player.saiyan = false;
					

					//removes one of the saiyan upgrades once they are used
					boolean hasRemoved = false;
					for(int i = 0; i < upgrades.size() && !hasRemoved; i++) {
						
						if(upgrades.get(i).getTag().equals("s")) {
							
							upgrades.remove(i);
							hasRemoved = true;
							
						}//if
						
					}//for
					
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
				
				//display the distance traveled
				g.drawString("Distance travelled: " + distance, 10, 950);
				
				//display coin info
				g.setFont(new Font("SansSerif", Font.BOLD, 12));
				
				//displays that the player has double coins active
				if(player.doubleCoins) {
					g.drawString("2X " + player.coins, 420, 30);
				}//if
				
				g.drawString("Shards collected: " + player.coins, 480, 30);
				
				Image img = null;
				try {
					
					img = ImageIO.read(getClass().getClassLoader().getResource("sprites/shrine.png"));
					
				} catch (IOException e) {e.printStackTrace();}
				
				g.drawImage(img, 450, 10, null);

				
				// calculates all of the player collisions
				for (int i = 0; i < entities.size(); i++) {
					
					//if currently checked entity is a coin
					if (entities.get(i) instanceof CoinEntity) {
						
						//stores a coin
						CoinEntity coin = (CoinEntity) entities.get(i);
						
						//checks if the player collides with the coin
						if(player.collidesWith(coin)) {
							
							//removes the coin
							removeEntities.add(coin);
							
							//updates player coin
							if(player.doubleCoins)
								
								//doiubles coins if the player has double coins upgrade
								player.coins+=2;
							else
								player.coins++;
							
						}//if
						
					}// if
					
					//if currently checked entity is a coin
					if (entities.get(i) instanceof BirdEntity) {
						
						//stores a temporary birt entity
						BirdEntity enemy = (BirdEntity) entities.get(i);
						
						//checks if the player collided the enemy
						if (player.collidesWith(enemy)) {
							
							//checks if the player is invulnerable 
							if(!player.saiyan) {
								
								//removes the enemy
								removeEntities.add(enemy);
								
								//removes a life
								lifeEntities.remove(lifeEntities.size()-1);
								
								//removes one heart upgrade once the player loses it
								
								//removes a life upgrade
								boolean hasRemoved = false;
								for(int k = 0; k < upgrades.size() && !hasRemoved; k++) {
									
									if(upgrades.get(k).getTag().equals("h")) {

										upgrades.remove(k);
										hasRemoved = true;
										
									}//if
									
								}//for
								
							}// if
							
						} // if

					} // if

					// if current checked entity is a cloud and the player collides with it
					if (entities.get(i) instanceof CloudEntity && player.collidesWith(entities.get(i))) {
						
						cloudCollision = true;
						
						cloudTime = 0;// resets cloud time
						
						gameSpeed = 0.4F;//slows the game speed
						
					} // if
					else if (cloudCollision) {
						
						//keeps game slows for 500 millis after the player collides
						if (cloudTime < 500) {
							gameSpeed = 0.2F;
						} // if
						
						//resets the game time to normal after 500 millis
						else if (cloudTime > 500) {
							
							cloudCollision = false;
							cloudTime = 0;
							gameSpeed = 0.5F;
							
						} // elif

					} // elif cloud collision
					
				} // for

				// remove dead entities
				entities.removeAll(removeEntities);
				removeEntities.clear();
				
				//stops player from drifting
				player.setHorizontalMovement(0);
				player.setVerticalMovement(0);

				// respond to user moving ship
				if ((leftPressed) && (!rightPressed)) {
					
					player.setHorizontalMovement(-moveSpeed);
				} else if ((rightPressed) && (!leftPressed)) {
					
					player.setHorizontalMovement(moveSpeed);
					
				} else if (downPressed && !cloudCollision) {
					
					//increase speed while pressing down
					gameSpeed = 0.8F;
					player.setVerticalMovement(120);
					
				} else if(downPressed && cloudCollision) {
					
					//increase speed when pressing down whilst the game has been slowed by a cloud
					gameSpeed = 0.4F;
					player.setVerticalMovement(60);
					
				}else if (!downPressed && !cloudCollision) {
					
					//resets player position and the game speed
					gameSpeed = 0.5F;
					player.setVerticalMovement(-400);
				} // else if
				
				
				//lchecks if player is out of lives
				if (lifeEntities.isEmpty()) {
					
					//wipes the game and sets the player to dead
					removeEntities.addAll(entities);
					Gamestate.state = Gamestate.DEATH;
					
				} // if
				
			} // else if GAME = STATE
			
			//STATE = DEATH
			else if (Gamestate.state == Gamestate.DEATH) {
				
				//removes one double coins upgrade when the player dies
				if(firstDeathLoop) {
					
					firstDeathLoop = false;
					
					boolean hasRemoved = false;
					for(int k = 0; k < upgrades.size() && !hasRemoved; k++) {
						
						if(upgrades.get(k).getTag().equals("d")) {
							
							upgrades.remove(k);
							hasRemoved = true;
							
						}//if
						
					}//for
					
				}//if
				
				
				panel.paintComponents(g); // resets the panel to be blank
				
				//draws the DEATH background image
				try {
					BufferedImage image = ImageIO.read(new File("bin/DeathBackground.gif"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();} 
				
				
				//draws all the upgrades the player has
				int y = 200;
				
				for(Upgrade u : upgrades) {
					u.draw(g, 0, y, 40, 40);
					y+=40;
				}// for upgrades
				
				//draws all the death screen buttons
				menuB.draw(g);
				quitB.draw(g);
				storeDeathB.draw(g);
				restartB.draw(g);
				
				//draws the number of coins the player has
				g.drawString("Shards collected : " + player.coins , 250, 900);
				
				//closes the program if the quit button is clicked
				if(mouseClicked & quitB.contains(clickLocation)) {
					System.exit(0);
				}//if
				
				//goes to the menu if the menu button is clicked
				else if(mouseClicked && menuB.contains(clickLocation)) {
					Gamestate.state = Gamestate.MENU;
					mouseClicked = false;
				}//else if
				
				//goes to the store if the store button is clicked 
				else if(mouseClicked && storeDeathB.contains(clickLocation)) {
					Gamestate.state = Gamestate.STORE;
					mouseClicked = false;
				}//else if 
				
				//goes back to the game if the restart button is clicked
				else if(mouseClicked && restartB.contains(clickLocation)) {
					
					//Initializes entities
					initEntities();
					
					Gamestate.state = Gamestate.GAME;
					mouseClicked = false;
					
				}//else if
				
			} // else if STATE = DEATH
			
			//STATE = STORE
			else if (Gamestate.state == Gamestate.STORE) {
				
				panel.paintComponents(g); // resets the panel to be blank
				
				//draws store background image
				try {
					BufferedImage image = ImageIO.read(new File("bin/storeBackground.gif"));
					g.drawImage(image, 0, 0, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//updates the store coin display
				updateCoins(g);
			
				superSaiyanBuyB.draw(g);
				heartBuyB.draw(g);
				doubleCoinBuyB.draw(g);
			
				//super saiyan power up display
				g.drawString("Super Saiyan: 20", 75, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/saiyan.png"));
					g.drawImage(image, 95, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//coin doubler power-up display
				g.drawString("Coin Doubler: 80", 250, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/2XCoins.png"));
					g.drawImage(image, 270, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//+1 life power-up display
				g.drawString("+1 Life: 30", 425, 550);
				try {
					BufferedImage image = ImageIO.read(new File("bin/sprites/heart.gif"));
					g.drawImage(image, 435, 565, null);
				} catch (IOException e) {e.printStackTrace();}
				
				//draws the menu button
				menuStoreB.draw(g);

				//goes to the menu if the menuButton is clicked
				if(mouseClicked && menuStoreB.contains(clickLocation)) {
					Gamestate.state = Gamestate.MENU;
					mouseClicked = false;
				}//if
				
				//checks if the player presses the superSaiyanBuyB button
				if(mouseClicked && superSaiyanBuyB.contains(clickLocation)) {
					
					//checks if the player has enough coins
					if(player.coins >= 20) {
						
							//displays that super saiyan was successfully bought 
							g.drawString("Succesfully purchased Super Saiyan!", 
									(600 - g.getFontMetrics().stringWidth("Succesfully purchased Super Saiyan!")) / 2, 720);
							
							player.coins -= 20;//updates player coins
							
							//adds a new "super saiyan upgrade" to upgrades arrayList
							try {
								upgrades.add(new Upgrade("s", "sprites/saiyan.png") {
										@Override
										public void upgradeMechanic(Player p) {
											p.canBeSaiyan = true;
										}//upgradeMechanic
									});
							}catch(IOException e) {System.out.println(e);}	
							
					}// if
					else {
						
						//displays that the player does not have enough coins
						g.drawString("Not enough coins to purchase Super Saiyan", 
								(600 - g.getFontMetrics().stringWidth("Not enough coins to purchase Super Saiyan")) / 2, 720);
					}// else
					
					mouseClicked = false;
					
				}// if
				
				//checks if the player presses the heartBuyB
				if(mouseClicked && heartBuyB.contains(clickLocation)) {
					
					//checks if the player has enough coins
					if(player.coins >= 30) {
						
							// that the heart upgrade was successfully bought
							g.drawString("Succesfully purchased another life!", 
									(600 - g.getFontMetrics().stringWidth("Succesfully purchased another life!")) / 2, 720);
							
							player.coins -= 30;//updates player coins
							
							//adds new upgrade "extra heart"
							try {
								upgrades.add(new Upgrade("h", "sprites/heart.gif") {
										@Override
										public void upgradeMechanic(Player p) {
											p.addLives++;
										}//upgradeMechanic
									});
							}catch(IOException e) {System.out.println(e);}	
	
					}// if
					else {
						
						//displays that the player doesn't have enough coins
						g.drawString("Not enough coins to purchase another life", 
								(600 - g.getFontMetrics().stringWidth("Not enough coins to purchase another life")) / 2, 720);
					
					}// else
					
					mouseClicked = false;
					
				}// if want to buy new life ([c])
				
				//checks if the player presses the doubelCoinBuyB button 
				if(mouseClicked && doubleCoinBuyB.contains(clickLocation)) {
					
					//checks if the player has enough coins
					if(player.coins >= 80) {
						
							//displays that the player successfully bought the extra heart upgrade
							g.drawString("Succesfully purchased a coin Doubler", 
									(600 - g.getFontMetrics().stringWidth("Succesfully purchased a coin Doubler")) / 2, 720);
							
							player.coins -= 80;//updates player coins
							
							//adds a new "coin doubler upgrade" to upgrades arrayList
							try {
								upgrades.add(new Upgrade("d", "sprites/2XCoins.png") {
										@Override
										public void upgradeMechanic(Player p) {
											p.doubleCoins = true;
										}//upgradeMechanic
									});
							}catch(IOException e) {System.out.println(e);}
							
					}// if
					else {

						//displays that the player doesn't have enough coins
						g.drawString("Not enough coins to purchase a coin doubler", 
								(600 - g.getFontMetrics().stringWidth("Not enough coins to a coin doubler")) / 2, 720);
						
					}// else
					
					mouseClicked = false;
					
				}// if
				
				//sleeps the thread to prevent accidently reading double inputs
				try{Thread.sleep(120);}catch(Exception e) {}
				
				//draw upgrades
				int y = 200;
				for(Upgrade u : upgrades) {
					u.draw(g, 0, y, 40, 40);
					y+=40;
				}// for upgrades
				
			} //else if STATE = STORE
			
			
			//updates the the player coins and CoinContainer in upgrades
			if(!Objects.isNull(player)) {
				upgrades.get(0).upgradeMechanic(player);
			}//if
			
			// clear graphics and flip buffer
			g.dispose();
			strategy.show();
			
		} // while (running)

	} // gameLoop

	//updates the coin display in the store state
	private void updateCoins(Graphics2D g) {
		
		// sets the graphics drawing conditions
		g.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		//draw the string
		g.drawString("Shards : " + player.coins, 490, 40);
		
		//draws the coin image
		Image img = null;
		try {
			img = ImageIO.read(getClass().getClassLoader().getResource("sprites/shrine.png"));
			g.drawImage(img, 450, 10, null);
		} catch (IOException e) {e.printStackTrace();}
	}// coin update

	
	//nested class to interpret key inputs
	private class KeyInputHandler extends KeyAdapter {

		//checks when a key is pressed 
		public void keyPressed(KeyEvent e) {
			
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
				System.exit(0);
			} // if
			
			
			if (e.getKeyCode() == KeyEvent.VK_M) {
				mPressed = true;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_S) {
				sPressed = true;
			} // if 
			
		} // keyPressed

		//checks when key is pressed
		public void keyReleased(KeyEvent e) {

			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = false;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = false;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_M) {
				mPressed = false;
			} // if esc=true, close game
			
			if (e.getKeyCode() == KeyEvent.VK_S) {
				sPressed = false;
			} // if 

		} // keyReleased

		public void keyTyped(KeyEvent e) {
			
			// if escape is pressed, end game
			if (e.getKeyChar() == 27) {
				System.exit(0);
			} // if escape pressed

		} // keyTyped

	} // class KeyInputHandler
		
	//nested class to interpret mouse inputs
	private class MouseButtonRecogn extends MouseAdapter {
		 
		//gets the location of the mouse on click
		@Override
		public void mouseClicked(MouseEvent event) {
		 
		    mouseClicked = true;
		    clickLocation = event.getPoint();
		   
		}//mouse clicked
		
	}//mouse adapter
	
	//stores the gamestate
	private enum Gamestate {
		GAME, DEATH, MENU, STORE;
		
		public static Gamestate state = MENU;
		public static boolean running = true;//stores whether the game is running or not
		
	} // GameState

	
	public static void main(String[] args) {

		// instantiate this object
		new Game();
		
	} // main
	
} // Game
