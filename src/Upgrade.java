import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Upgrade {
	
	private Image display;//stores the image representation of this upgrade
	private String tag;//stores the tag that represents the type of upgrade that this is
	
	Upgrade(String t, String imageAdress) throws IOException{
		
		tag = t;//sets the tag
		
		//gets the upgrades image from the file system
		display = ImageIO.read(getClass().getClassLoader().getResource(imageAdress));
		
	}//Upgrade
	
	public String getTag(){
		return tag;
	}//getTag
	
	//draws this upgrade
	public void draw(Graphics2D g, int x, int y, int w, int h) {

		g.drawImage(display, x, y, w, h, null);
		
	}//draw
	
	public void upgradeMechanic(Player p) {}


}//Upgrade
