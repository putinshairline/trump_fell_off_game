
public class Player extends Entity{

	
	private Game game;
	
	
	public Player(Game g, String r, int newX, int newY) {
		super(r, newX, newY);
		game = g;
	}

	@Override
	public void collidedWith(Entity other) {
		// TODO Auto-generated method stub
		
	}
	
	public void move(long delta) {
		// stop at left side of screenF
		if ((dx < 0) && (x < 0)) {
			return;
		} // if
			// stop at right side of screen
		if ((dx > 0) && (x > 600)) {
			return;
		} // if
		if ((dy > 0) && (y < 1080)) {
			dy += 1;
		}
		if ((dy > 0) && (y > 0)) {
			return;
		}

		super.move(delta); // calls the move method in Entity
	} // move

}
