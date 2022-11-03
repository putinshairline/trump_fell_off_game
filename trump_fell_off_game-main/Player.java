
public class Player extends Entity{

	
	private Game game;
	
	
	public Player(Game g, String r, int newX, int newY, int newH , int newW) {
		super(r, newX, newY, newH, newW);
		game = g;
	}

	@Override
	public void collidedWith(Entity other) {
		// TODO Auto-generated method stub
		
	}
	
	public void move(long delta) {
		// stop at left side of screenF
		if ((dx < 0) && (x < -10)) {
			return;
		} // if
			// stop at right side of screen
		if ((dx > 0) && (x > 550)) {
			return;
		} // if
		

		super.move(delta); // calls the move method in Entity
	} // move

}
