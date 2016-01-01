package shooterbot;

import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.robotics.Color;
//import lejos.util.TextMenu;
import lejos.util.Delay;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.robotics.objectdetection.RangeFeatureDetector;

public class ShooterBot5 implements FeatureListener { 
	public static int MAX_DETECT = 80;

	public static void main(String [] args) throws Exception {
		int colors[] = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE, Color.NONE};
		int portNo = 3;
		ColorSensor cs = new ColorSensor(SensorPort.getInstance(portNo));
		ColorSensor.Color vals = cs.getColor();
		LCD.drawString("ShooterBot", 0, 1);		
		Button.ENTER.waitForPressAndRelease();
		
		int speed = 720;
		Motor.A.setSpeed(speed);
		Motor.C.setSpeed(speed);
		Motor.A.rotate(10*360,true);
		Motor.C.rotate(10*360,true);

		int mode = 0;
		do {
			cs.setFloodlight(colors[mode]);
			vals = cs.getColor();	
			if(vals.getColor()==0) {
				break;
			}
		} while(!Button.ESCAPE.isDown());
	
		
		Motor.A.rotate(-2*360,true);
		Motor.C.rotate(-2*360);

		
		// Initialize the detection objects:
		ShooterBot5 listener = new ShooterBot5();
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S3);
		RangeFeatureDetector fd = new RangeFeatureDetector(us, MAX_DETECT, 500);
		fd.addListener(listener);
		
		Sound.beepSequenceUp();
		System.out.println("Autodetect ON");
		fd.enableDetection(true);
		while(!Button.ESCAPE.isDown()) {
			Thread.yield();
		}		
		System.exit(0);
      
	}
	
	public void featureDetected(Feature feature, FeatureDetector detector) {
		int range = (int)feature.getRangeReading().getRange();
		Sound.playTone(1200 - (range * 10), 100);
		System.out.println("Range:" + range);
		
		// shoot
		NXTMotor motor = new NXTMotor(MotorPort.B);
		motor.setPower(90); // need quite some power to release the balls
		motor.forward();
		Delay.msDelay(3000);
		motor.stop();
	}
}