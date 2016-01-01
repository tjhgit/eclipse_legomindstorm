package malbild;
import javax.microedition.lcdui.Graphics;
//import java.lang.Math;

public class Malbild {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// HIER SCHREIBST DU DEINEN CODE REIN
		Graphics g = new Graphics();
		// dreieck
		int schiebeX=40;
		int schiebeY=20;
	    g.drawLine(schiebeX+0, schiebeY+20,schiebeX+10, schiebeY+10);
	    //Thread.sleep(1*1000); // pause von 2 sec
	    g.drawLine(schiebeX+10, schiebeY+10,schiebeX+20, schiebeY+20);
	    //Thread.sleep(1*1000); // pause von 2 sec
	    g.drawLine(schiebeX+0,schiebeY+20,schiebeX+20,schiebeY+20);
	    //Thread.sleep(1*1000); // pause von 2 sec
	    g.drawRect(schiebeX+0, schiebeY+20, 20, 20);
	    //Thread.sleep(1*1000); // pause von 2 sec
	    g.fillArc(70, 0, 30, 30, 0, 270);// /180*Math.PI));
		//System.out.println("2*PI*r"+" = "+Math.PI*2);
	    g.fillArc(35, 0, 30, 15, 0, 360);
	    
		Thread.sleep(10*1000); // pause von 2 sec
	}

}
