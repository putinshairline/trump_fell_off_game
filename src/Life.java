
public class Life extends Entity {
	private Game game;
	
	public Life(Game g, String r, int newX, int newY, int newH, int newW) {
		super(r, newX, newY, newH, newW);
		game = g;
	}// Life

	@Override
	public void collidedWith(Entity other) {}

}//Life
