package alpharex4;

import java.io.File;

import lejos.nxt.*;
import lejos.robotics.Color;
import lejos.util.Delay;

public class AlphaRex4 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

			TouchSensor touch1 = new TouchSensor(SensorPort.S1);
		    NXTMotor m1 = new NXTMotor(MotorPort.B);		
		    TouchSensor touch2 = new TouchSensor(SensorPort.S2);
		    NXTMotor m2 = new NXTMotor(MotorPort.C);
//		    NXTMotor m3 = new NXTMotor(MotorPort.A);
		    ColorSensor cs = new ColorSensor(SensorPort.S3);
			ArmBewegung arm = new ArmBewegung();
			
			File sGreen = new File("Green.wav");
			File sPlease = new File("Please.wav");
			File sYes = new File("Yes.wav");
			File sBlue = new File("Blue.wav");
			File sRed = new File("Red.wav");
			File sYellow = new File("Yellow.wav");
			
			Delay.msDelay(500);

			bewegebissensor(m1,touch1,45,true);
			bewegebissensor(m2,touch2,45,true);
			bewegeumdrehungen(new NXTMotor [] {m2},0.5,45,true);
			

			arm.start();
			bewegeumdrehungen(new NXTMotor [] {m1,m2},9,46,true);
	        try {
	            arm.join();
	        } catch (InterruptedException e) {
	        }
	        
			int dur = Sound.playSample(sGreen,100);
//			LCD.drawInt(dur, 10, 0, 0);
			Delay.msDelay(dur);
			dur = Sound.playSample(sPlease,100);
			Delay.msDelay(dur);
			
			int farbe;
			do {
				farbe =cs.getColor().getColor();
				switch(farbe){
				case Color.BLUE:
					dur=Sound.playSample(sBlue,100);
					Delay.msDelay(dur);	
					Motor.A.rotate((int) 1.67*360);
					break;
				case Color.RED:
					dur=Sound.playSample(sRed,100);
					Delay.msDelay(dur);	
					Motor.A.rotate((int) 1.67*360);
					break;
				case Color.YELLOW:
					dur=Sound.playSample(sYellow,100);
					Delay.msDelay(dur);	
					Motor.A.rotate((int) 1.67*360);
					break;
				}
				Delay.msDelay(1000);
			} while(farbe!=Color.GREEN);
			
			dur=Sound.playSample(sGreen,100);
			Delay.msDelay(dur);
			dur=Sound.playSample(sYes,100);	
			Delay.msDelay(dur);
			Delay.msDelay(1000);
			bewegebissensor(m1,touch1,45,true);
			bewegebissensor(m2,touch2,46,true);		
			bewegeumdrehungen(new NXTMotor [] {m1},0.6,45,true);
			bewegeumdrehungen(new NXTMotor [] {m1,m2},5,42,true);
			Delay.msDelay(1000);
			Motor.A.rotate((int) 1.67*360);
			
		}

	    static void bewegebissensor(NXTMotor m, TouchSensor t, int powerlevel, boolean forward) {
	    	m.setPower(powerlevel);
	    	if(forward)
	    		m.forward();
	    	else
	    		m.backward();       
	    	while (!t.isPressed())		    	   	
	    		Thread.yield();  	
	    	m.stop();    	   	
	    }
	    

	    static void bewegeumdrehungen(NXTMotor [] m, double turns, int powerlevel, boolean forward){
	    	int currtacho=m[0].getTachoCount();
	    	
	    	for(int ii=0;ii<m.length;ii++){
	    		m[ii].setPower(powerlevel);    	
	    		if(forward)
	    			m[ii].forward();
	    		else
	    			m[ii].backward();
	    	}
	    	
	    	while(Math.abs(m[0].getTachoCount()-currtacho)<turns*360)
	    		Thread.yield();
	    	
	    	for(int ii=0;ii<m.length;ii++){
	    		m[ii].stop();
	    	}
	    }


}

class ArmBewegung extends Thread {
	
	public void run(){
		Motor.A.setSpeed(400);
		Motor.A.rotate((int) 11.69*360);
}
}