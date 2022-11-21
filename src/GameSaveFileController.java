import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/*
 * this Class is a thread that remains sleeping 
 *until the JVM starts to shut down from witch it activates and runs the run method
 */
public class GameSaveFileController extends Thread{
	
	private ArrayList<Upgrade> i;//stores a pointer to the upgrades ArrayList in game
	
	private File saveFile;//stores the save file that will be written to
	
	GameSaveFileController(ArrayList<Upgrade> info, File f){
		super();
		
		//sets the pointer
		i = info;
		
		//sets the save file to be written to
		this.saveFile = f;
	}//GameSaveFileController
	
	//runs when the JVM begins its shut down process
	@Override
	public void run(){
		
		System.out.println("saving game "  + saveFile);
		
		try {
			
			//creates a buffered writer to write to the save file
			BufferedWriter b = new  BufferedWriter(new FileWriter(saveFile));
			
			for(Upgrade j: i) {
				
				//writes the amount of coins on shutdown to the save file
				if(j instanceof CoinContainer) {
					
					b.write(String.valueOf(((CoinContainer)j).getCoinQuantity()));
					
				}//if
				
				//writes all the player upgrades to the save file based on there tag
				else {
					b.write(j.getTag(), 0, 1);
				}//else
				
				b.newLine();//sets the writer to a new line
				
			}//for
			
		b.close();//Closes the writer
		
		}catch(Exception e) {
			System.out.println("an error occured while updating save file");
		}//catch
		
	}//run
	
}//GameSaveFileController
