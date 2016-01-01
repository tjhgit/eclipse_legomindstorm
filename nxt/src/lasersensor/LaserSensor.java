package lasersensor;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.util.Delay;

public class LaserSensor {

	public static void main(String[] args) {
		LightSensor ls = new LightSensor(SensorPort.S1);
//		ls.setFloodlight(true);
		
        do{	
        	int value = ls.getNormalizedLightValue();	   
        	//int value = ls.getLightValue();	   
        	LCD.drawInt(value,4, 4, 1);
        	if(value < 10){
        		Sound.playNote(Sound.PIANO, 261, 500);
        		//Sound.pause(8);
        	}
        	Delay.msDelay(10);
		} while (!Button.ESCAPE.isDown());

	}

}
