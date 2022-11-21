import java.awt.Graphics2D;
import java.io.IOException;

public class CoinContainer extends Upgrade{
	
	private int coinQuantity = 0;//stores the number of coins
	
	CoinContainer(String t, String imageAdress) throws IOException {
		
		super(t, imageAdress);

	}//CoinContainer
	
	//updates coin quantity and player.coins
	@Override
	public void upgradeMechanic(Player p) {
		
		//sets p.coins to coinsQuantity if p.coins has not been updated
		//(this only when the game boots up)
		p.coins = (p.coins == 0)?this.coinQuantity: p.coins ;
		
		//updates coinQuantity
		this.coinQuantity = p.coins;
		
	}//coin container
	
	@Override
	public void draw(Graphics2D g, int x, int y, int w, int h) {};
	
	public int getCoinQuantity() {
		return coinQuantity;
	}//getCoinQuantity
	
	public void setCoinQuantity(int x) {
		this.coinQuantity = x;
	}//setCoinQuantity
	
}//CoinContainer
