package robogator1;
import lejos.nxt.LCD;
import lejos.nxt.Button;
import lejos.nxt.Motor;


public class RoboGator1 {

	public static void main(String[] args) throws Exception  {
		// TODO Auto-generated method stub
		LCD.drawString("Robogator1",0,0);
		Button.waitForAnyPress();
		for(int i =0;i<5;i++){
			Motor.A.rotate(30);
			Motor.A.rotateTo(0);
			Thread.sleep(500);
		}
	}

}
