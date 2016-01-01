package robogator3;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Robogator3 {

	public static void main(String[] args) {
		final UltrasonicSensor sonar = new UltrasonicSensor( SensorPort.S4 );
		   
			
		Behavior AngriffLaufen = new Behavior() {
		    private boolean suppressed = false;
		    
		    public boolean takeControl() {
		    	return (sonar.getDistance() < 60 && sonar.getDistance() > 15);
		    }

		    public void suppress() {
		       suppressed = true;
		    }

		    public void action() {
		       suppressed = false;     
		       Motor.B.rotate(-360,true);
		       Motor.C.rotate(-360,true);
		       while( Motor.B.isMoving() && !suppressed){Thread.yield();}
		       Motor.B.stop();
		       Motor.C.stop();
		    }
		};
		
		Behavior Schnappen = new Behavior() {
		    private boolean suppressed = false;
		    
		    public boolean takeControl() {
		    	return sonar.getDistance() <= 25;
		    }

		    public void suppress() {
		       suppressed = true;
		    }

		    public void action() {
		       suppressed = false;     
		       Motor.A.rotate(30,true);
		       while( Motor.A.isMoving() && !suppressed){Thread.yield();}
		       Motor.A.rotateTo(0,true);
		       while( Motor.A.isMoving() && !suppressed){Thread.yield();} 
		    }
		};
		
		Behavior ButtonStop = new Behavior () {
			public void suppress(){}
			public boolean takeControl(){
				return Button.ESCAPE.isDown();
			}
			public void action(){
				System.exit(0);
			}
		};
		
		Behavior[] bArray = {AngriffLaufen, Schnappen, ButtonStop};
	    LCD.drawString("Robogator3", 0, 1);
	    Button.waitForAnyPress();
	    (new Arbitrator(bArray)).start();	
	}

}
