package thomas.nxt.laserbot;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;


public class LaserBot {
	private static NXTMotor mC = new NXTMotor(MotorPort.C);	
	private static TouchSensor touch2 = new TouchSensor(SensorPort.S2);
	private static UltrasonicSensor sonar = new UltrasonicSensor( SensorPort.S4 );
	private static LightSensor licht = new LightSensor(SensorPort.S1);
	private static int sign1 = 1;
	
	public static void finescan() {
		Motor.A.setSpeed(10);
		Motor.B.setSpeed(10);
		Motor.A.backward();
		Motor.B.forward();		
		while(licht.getNormalizedLightValue()>0); // can be an infinite loop
		Motor.A.stop(true);
		Motor.B.stop(true);	
		while(Motor.B.isMoving());
	}
	
	class DirectionCorrection extends Thread {
		boolean stopthread = false;
		public void run() {
			while(!stopthread) {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // correct every second
				int geschwindigkeit = Motor.A.getSpeed();
				if(!stopthread) {
					Motor.A.stop(true);
					Motor.B.stop(true);
					Motor.A.setSpeed(10);
					
					long timer = System.nanoTime();
					double winkel1 = 40;
					double winkel2 = winkel1;
					int loop = 0;
					while(licht.getNormalizedLightValue()>0){ // can be an infinite loop
						// suche nach dem reflektor
						Motor.A.rotate((int) (sign1*winkel1),true);
						while(Motor.A.isMoving() && licht.getNormalizedLightValue()>0);
						Motor.A.stop();
						
						if(licht.getNormalizedLightValue()>0) { 
							// reflektor nicht gefunden
							Motor.A.rotate((int) (-sign1*winkel2),true);
							while(Motor.A.isMoving() && licht.getNormalizedLightValue()>0);
							Motor.A.stop();			
						}
						if(loop%2==0) {// even 
							winkel2 = winkel1*1.1; // extend the search range
						} else {
							winkel1 = winkel2*1.1;
						}
						loop = loop+1;
					}
					
					if(System.nanoTime()-timer > 4e9) { // more than 4 sec for correction 
						sign1 *= -1;
					}
	
					Motor.A.setSpeed( geschwindigkeit );
					Motor.B.setSpeed( geschwindigkeit );		
					Motor.A.forward();
					Motor.B.forward();
				}	
			}
			
		}
	}
	
	public static void main(String[] args) {
	
		LCD.drawString("LaserBot", 0, 1);
	    Button.waitForAnyPress();
	    
	    // klammer auf
		mC.setPower(50);
		mC.forward();
		while(!touch2.isPressed());
		mC.stop();
		
		int geschwindigkeit = Motor.A.getSpeed();
		
		//if(false) {
		// scanning		
		Motor.A.setSpeed( (int)(geschwindigkeit/4.0 ) );
		Motor.B.setSpeed( (int)(geschwindigkeit/4.0 ) );		
		Motor.A.forward();
		Motor.B.backward();
		while(licht.getNormalizedLightValue()>0);
		Motor.A.stop(true);
		Motor.B.stop(true);
		while(Motor.B.isMoving());
		
		// fine scanning
		finescan();
		
		Motor.A.setSpeed(geschwindigkeit);
		Motor.B.setSpeed(geschwindigkeit);		
		Motor.A.forward();
		Motor.B.forward();
		
		LaserBot lb= new LaserBot();
		DirectionCorrection dc = lb.new DirectionCorrection();
		dc.start();
		
		Thread.yield();
		
		//}
		
//		Motor.A.rotate(3*360,true);
//		Motor.B.rotate(-3*360,true);
//		while( Motor.B.isMoving() ) { Thread.yield();}
		

		
		while(sonar.getDistance()>10);
		
		dc.stopthread=true;
		Motor.A.stop(true);
		Motor.B.stop(true);
		while(Motor.B.isMoving());
		
		try {
			dc.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// annäherung greifprozedur
		Motor.A.setSpeed(50);
		Motor.B.setSpeed(50);
		
		// suche nach dem reflektor
		Motor.A.rotate(180,true);
		while(Motor.A.isMoving() && licht.getNormalizedLightValue()>0);
		Motor.A.stop();
		
		if(licht.getNormalizedLightValue()>0) { 
			// reflektor nicht gefunden
			Motor.A.rotate(-360,true);
			while(Motor.A.isMoving() && licht.getNormalizedLightValue()>0);
			Motor.A.stop();			
		}
		
		Motor.A.forward();
		Motor.B.forward();
		Delay.msDelay(1200);
		
		mC.backward();
		Delay.msDelay(300);
		mC.stop();
		//}
		
		Motor.A.setSpeed(geschwindigkeit);
		Motor.B.setSpeed(geschwindigkeit);	
		Motor.A.rotate(-(2*360+40),true);
		Motor.B.rotate((2*360+40),true);
		while( Motor.B.isMoving() ) { Thread.yield();}
		
		
		Motor.A.forward();
		Motor.B.forward();
		Delay.msDelay(8000);
		Motor.A.stop(true);
		Motor.B.stop(true);
		while(Motor.B.isMoving());
		
		//while(Button.ESCAPE.isUp());
		
		
	}

}
