package motortest;

import java.io.File;

import lejos.nxt.*;
import lejos.util.Delay;

public class MotorTest {

	public static void main(String[] args) {
		UltrasonicSensor ultraschall = new UltrasonicSensor(SensorPort.S4);
		File sage_gehweg = new File("gehweg.wav");
		
		Motor.B.forward();
		Motor.C.forward();
		
		while(ultraschall.getDistance()>25)
			Thread.yield();
		Motor.B.stop();
		Motor.C.stop();
		
		// hauen
		Motor.A.rotate(5*360);
		
		if(ultraschall.getDistance()<25) {
			int dur = Sound.playSample(sage_gehweg,100);
			LCD.drawInt(dur, 10, 0, 0);
			Delay.msDelay(dur);
			
		} else {
			Motor.B.rotate(10*360);
			Motor.C.rotate(10*360);
		}
			
		
	}
}
