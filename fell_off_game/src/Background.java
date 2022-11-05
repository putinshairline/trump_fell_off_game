/* Background.java
 * Creates an scrolling background.
 * Modified from - https://www.nutt.net/create-scrolling-background-java/
 * this version requires a 1920x1080 horizonally tiled image called
 * background.jpg saved in the same folder as the class files. 
 * This may be changed.
 */

import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;

public class Background {
	private Image image; //backgroud image
    private int x; //x coordinate
    private int y; //y coordinate
    private int backSpeed = 5;
 
    //constructor
    public Background() {
        this(0,0);
    }//Background
 
    //initializes constructor
    public Background(int x, int y) {
        this.x = x;
        this.y = y;
 
        // try to open the background image file 
        try {
        	image = ImageIO.read(getClass().getClassLoader().getResource("background.gif"));
        	draw(image.getGraphics());
        }
        catch (Exception e) { System.out.println(e); }
 
    }//Background
 
   	//method that draws the image onto the Graphics object passed
    public void draw(Graphics window) {
        //draw the image onto the Graphics reference
        window.drawImage(image, getX(), getY(), 600, 1080, null);
 
        
		//move the x position left for next time
        this.y -= backSpeed;
 
        //check to see if the image has gone off stage left
        if (this.y <= -1 * 1080) {
 
            //if it has, line it back up so that its left edge is lined up to the right side of the other background image
            this.y = this.y + 1080 * 2;
        }//if
 
    }//draw
 
    public void setX(int x) {
        this.x = x;
    }//setX
    public int getX() {
        return this.x;
    }//getX
    public int getY() {
        return this.y;
    }//getY
    public void setBackSpeed(int x) {
    	this.backSpeed = x;
    } // backSpeed set
    public int getBackSpeed() {
    	return this.backSpeed;
    }
    public int getImageWidth() {
        return 1080;
    }//getImageWidth
    
}//class Background