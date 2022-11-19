import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GraphicsButton extends Rectangle{
	
	Image display;
	
	GraphicsButton(int x, int y, int h, int w, String imageAdress) throws IOException{
		super(x, y, h, w);

		display = ImageIO.read(getClass().getClassLoader().getResource(imageAdress));
		
	}
	
	public void draw(Graphics2D g) {
		g.draw(this);
		g.drawImage(display, this.x, this.y, this.width, this.height, null);
	}
}
