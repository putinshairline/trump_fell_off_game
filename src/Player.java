
public class Player extends Entity{
	public boolean saiyan;
	public boolean canBeSaiyan;
	public boolean doubleCoins;
	public int addLives;
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
		if(dx < 0) {
			if(saiyan) {
				super.sprite = (SpriteStore.get()).getSprite("sprites/playerLS.png");
			} //if
			else {
				super.sprite = (SpriteStore.get()).getSprite("sprites/playerL.png");
			}// else
		}// if
		
		if(dx > 0) {
			if(saiyan) {
				super.sprite = (SpriteStore.get()).getSprite("sprites/playerRS.png");
			} //if
			else {
				super.sprite = (SpriteStore.get()).getSprite("sprites/playerR.png");
			}//else
		} // if
		// stop at left side of screenF
		if ((dx < 0) && (x < -10)) {
			return;
		} // if
		// stop at right side of screen
		if ((dx > 0) && (x > 550)) {
			return;
		} // if
		// stop at top side of screenF
		if ((dy < 0) && (y < 102)) {
			return;
		} // if
		// stops at max dive distance
		if((dy > 0) && (y > 250)) {
			dy = -400;
			return;
		}// if

		super.move(delta); // calls the move method in Entity
	} // move

}
