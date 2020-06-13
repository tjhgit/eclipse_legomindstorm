package schlitten;

import java.io.File;

import lejos.nxt.*;
import lejos.robotics.Color;
import lejos.util.Delay;


class SageFarbe extends Thread {
	boolean flag_stop = false;
	public void run() {
		ColorSensor color = new ColorSensor(SensorPort.S4);
		
		File sGreen = new File("Green.wav");
		File sBlue = new File("Blue.wav");
		File sRed = new File("Red.wav");
		File sYellow = new File("Yellow.wav");
		
		while(!flag_stop) {
			int farbe = color.getColor().getColor();
			int raw = color.getRawLightValue();
			
			LCD.drawInt(farbe,4, 0, 1);
			LCD.drawInt(raw,4, 0, 2);
			
			switch(farbe){
			case Color.BLUE:
				int dur=Sound.playSample(sBlue,100);
				Delay.msDelay(dur);	
				break;
			case Color.RED:
				dur=Sound.playSample(sRed,100);
				Delay.msDelay(dur);	
				break;
			case Color.YELLOW:
				dur=Sound.playSample(sYellow,100);
				Delay.msDelay(dur);	
				break;
			case Color.GREEN:
				dur=Sound.playSample(sGreen,100);
				Delay.msDelay(dur);	
				break;
			}
		}
		
	}
}
public class Schlitten {

	public static void main(String[] args) {
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);
		TouchSensor touch = new TouchSensor(SensorPort.S2);

		
		
		Motor.B.forward();
		Motor.C.forward();
		
		SageFarbe sf = new SageFarbe();
		sf.start();
		
		while(us.getDistance()>25)
			Thread.yield();
		
		Motor.B.backward();
		Motor.C.backward();
		
		while(!touch.isPressed())
			Thread.yield();
		
		Motor.B.stop();
		Motor.C.stop();
		sf.flag_stop = true;
		
	}

}
