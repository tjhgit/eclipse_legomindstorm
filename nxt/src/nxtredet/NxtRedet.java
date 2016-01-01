package nxtredet;
import lejos.nxt.LCD;

public class NxtRedet {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		// HIER SCHREIBST DU DEINEN CODE REIN
		
		//TEXT AUF BILDSCHIRM SCHREIBEN:
		//LCD.drawString("MEIN TEXT",spalte,reihe);
		
		LCD.drawString("Hallo willst du",0,0);
		LCD.drawString("mein Freund sein?",0,1);
		LCD.drawString("JA",7,4);	
		LCD.drawString("vielleicht",1,5);	
		
		// WARTEN:
		//Thread.sleep(ZEIT IN MILLISEKUNDEN)
		Thread.sleep(10*1000); // pause von 4 sec
	  
	}

}

