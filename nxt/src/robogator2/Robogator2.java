package robogator2;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Robogator2 {
	public static void main(String[] args) {
		
		
	Behavior Schnappen = new Behavior() {
	    private boolean suppressed = false;
	    private UltrasonicSensor sonar = new UltrasonicSensor( SensorPort.S4 );
	   
	    public boolean takeControl() {
	    	return sonar.getDistance() < 25;
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
	// TODO Auto-generated method stub
	Behavior[] bArray = {Schnappen, ButtonStop};
    LCD.drawString("Robogator2", 0, 1);
    Button.waitForAnyPress();
    (new Arbitrator(bArray)).start();	
	

	}

}
