package bumpercar;

import lejos.nxt.*;
import lejos.robotics.subsumption.*;

public class BumperCar {
	public static void main(String[] args) {
		Behavior DriveForward  =new  Behavior () {
			   private boolean suppressed = false;
			   
			   public boolean takeControl() {
			      return true;
			   }

			   public void suppress() {
			      suppressed = true;
			   }

			   public void action() {
			     suppressed = false;
			     Motor.A.forward();
			     Motor.C.forward();
			     while( !suppressed )
			        Thread.yield();
			     Motor.A.stop(); // clean up
			     Motor.C.stop();
			   }
			};
		
		Behavior HitWall = new Behavior() {
		    //private TouchSensor touch;
		    private boolean suppressed = false;
		    private UltrasonicSensor sonar = new UltrasonicSensor( SensorPort.S3 );
		   
		    public boolean takeControl() {
		       //return touch.isPressed() || sonar.getDistance() < 25;
		    	return sonar.getDistance() < 25;
		    }

		    public void suppress() {
		       suppressed = true;
		    }

		    public void action() {
		       suppressed = false;
		       Motor.A.rotate(-3*360, true);
		       Motor.C.rotate(3*360, true);

		       while( Motor.C.isMoving() && !suppressed )
		         Thread.yield();

		       Motor.A.stop();
		       Motor.C.stop();
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
		Behavior[] bArray = {DriveForward, HitWall, ButtonStop};
        LCD.drawString("BumperCar ", 0, 1);
        Button.waitForAnyPress();
	    (new Arbitrator(bArray)).start();
	    
	    
	}

}
