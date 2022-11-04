
public class BirdEntity extends Entity{
	private Game game;
	
	public BirdEntity(Game g, String r, int newX, int newY, int newH, int newW) {
		super(r, newX, newY, newH, newW);
		game = g;
	}// BirdEntity

	@Override
	public void collidedWith(Entity other) {
		// TODO Auto-generated method stub
		
	} // collided with
	
	@Override
	public void move(long delta) {
		// stop at top side of screenF
		if ((dy < 0) && (y < 10)) {
			return;
		} // if

		super.move(delta); // calls the move method in Entity
	} // move

}// BirdEntity
