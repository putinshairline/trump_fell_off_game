import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Buttons extends Game implements ActionListener{
	
	JButton b;
	
	public Buttons(JFrame container) {
		b = new JButton();
		b.setBounds(100, 100, 100, 50);
		container.add(b);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == b) {
			System.out.println("BUTTON_WORKS");
		}
		
	}

}
