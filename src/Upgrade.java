import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Upgrade {
	
	private Image display;
	private String tag;
	
	Upgrade(String t, String imageAdress) throws IOException{
		tag = t;
		display = ImageIO.read(getClass().getClassLoader().getResource(imageAdress));
	}
	
	
	public String getTag(){
		return tag;
	}
	
	public void draw(Graphics2D g, int x, int y, int w, int h) {

		g.drawImage(display, x, y, w, h, null);
	}
	
	public void upgradeMechanic(Player p) {}
}
