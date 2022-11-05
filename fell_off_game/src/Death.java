import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import javax.swing.JPanel;

public class Death {
	
	public static void display(BufferStrategy strategy, JPanel panel) {
		System.out.println("Display was called");
		panel.removeAll();
		// get graphics context for the accelerated surface and make it black
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		g.setBackground(Color.BLACK);
		g.setColor(Color.white);
		g.drawString("AAH", (1080 - g.getFontMetrics().stringWidth("AAH")) / 2, 250);
		g.drawString("You Died", (600 - g.getFontMetrics().stringWidth("You Died")) / 2, 300);
	}// display
	
} // death
