import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import javax.swing.JPanel;

public class Death extends Entity{
	public Game game;
	public static void display(BufferStrategy strategy) {
		System.out.println("Display was called");
		// get graphics context for the accelerated surface and make it black
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		g.setBackground(Color.BLACK);
		g.setColor(Color.white);
		g.drawString("AAH", (1080 - g.getFontMetrics().stringWidth("AAH")) / 2, 250);
		g.drawString("You Died", (600 - g.getFontMetrics().stringWidth("You Died")) / 2, 300);
	}// display
	
	public Death(Game g, String r, int newX, int newY, int newH , int newW) {
		super(r, newX, newY, newH, newW);
		game = g;
	}
	
	@Override
	public void collidedWith(Entity other) {
		// TODO Auto-generated method stub
		
	}
} // death
