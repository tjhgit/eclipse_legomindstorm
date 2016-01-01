package motortest;

import java.io.File;
import lejos.nxt.*;
import lejos.util.Delay;


public class RoboSprich {
	public static void main(String[] args) {
		
		File sage_gehweg = new File("gehweg.wav");
		int dur = Sound.playSample(sage_gehweg,100);
		LCD.drawInt(dur, 10, 0, 0);
		Delay.msDelay(dur);
	}		
}
