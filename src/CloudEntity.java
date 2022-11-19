
public class CloudEntity extends Entity {
	private Game game;

	public CloudEntity(Game g, String r, int newX, int newY, int newH, int newW) {
		super(r, newX, newY, newH, newW);
		game = g;
	}// BirdEntity

	@Override
	public void collidedWith(Entity other) {
		// TODO Auto-generated method stub

	} // collided with

	@Override
	public void move(long delta) {
		super.move(delta); // calls the move method in Entity
	} // move

}// BirdEntity