
public class BirdEntity extends Entity {
	private Game game;

	public BirdEntity(Game g, String r, int newX, int newY, int newH, int newW) {
		super(r, newX, newY, newH, newW);
		game = g;
	}// BirdEntity

	@Override
	public void collidedWith(Entity other) {}

	@Override
	public void move(long delta) {
		super.move(delta); // calls the move method in Entity
	} // move

}// BirdEntity