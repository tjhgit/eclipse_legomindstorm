package robogator4;

import lejos.util.Delay;
import lejos.nxt.Button;
import lejos.nxt.TouchSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class RobotGator4 {
	private static UltrasonicSensor sonar = new UltrasonicSensor( SensorPort.S4 );
	private static TouchSensor touch1 = new TouchSensor(SensorPort.S1);
    private static NXTMotor m1 = new NXTMotor(MotorPort.B);		
    private static TouchSensor touch2 = new TouchSensor(SensorPort.S2);
    private static NXTMotor m2 = new NXTMotor(MotorPort.C);
    private static NXTMotor m3 = new NXTMotor(MotorPort.A);
    
    private static void align() {
	       Motor.B.setSpeed(400);
	       Motor.C.setSpeed(400);
	       Motor.B.rotate(-360,true);
	       //while (!touch2.isPressed() && !suppressed ) {
	       while (!touch2.isPressed() ) {
	    	   Thread.yield();
	       }
	       Motor.B.stop();
	       
	       Motor.C.rotate(-360,true);
//	       while (!touch1.isPressed() && !suppressed ) {
	       while (!touch1.isPressed()){
	    	   Thread.yield();
	       }
	       Motor.C.stop();
 };
	public static void main(String[] args) {

	   
		Behavior AngriffLaufen = new Behavior() {
		    private boolean suppressed = false;
		    
		    public boolean takeControl() {
		    	return (sonar.getDistance() < 60 && sonar.getDistance()>25);
		    }

		    public void suppress() {
		       suppressed = true;
		    }

		    public void action() {
		       suppressed = false;   
		      
		       align();
		       
		       Motor.B.setSpeed(Motor.B.getMaxSpeed());
		       Motor.C.setSpeed(Motor.C.getMaxSpeed());
		       
		       Motor.B.rotate(-3*360,true);
		       Motor.C.rotate(-3*360,true);
//		       while( Motor.B.isMoving()){Thread.yield();}
		       while( Motor.B.isMoving() && !suppressed){Thread.yield();}
		       Motor.B.stop(); Motor.C.stop();
		       

		       
		       if(false){
			       // align the motors
			       m1.setPower(100);
			       m1.backward();       
			       while (!touch2.isPressed() && !suppressed ) {		    	   	
			    	   Thread.yield();
			       }
			       m1.stop();
			       
			       m2.setPower(100);
			       m2.backward();
			       while (!touch1.isPressed() && !suppressed) {   
			    	   Thread.yield();
			       }
			       m2.stop();
		    	   m1.setPower(100);
		       m2.setPower(100);
		       m1.backward();
		       m2.backward();
		       if(!suppressed) {Delay.msDelay(1000);}
		       m1.stop();
		       m2.stop();
		       
		       //Motor.B.rotate(-180,true);
		       //while( Motor.B.isMoving() && !suppressed){Thread.yield();}
		       
		       m1.setPower(100);
		       m2.setPower(100);
		       m1.forward();
		       m2.forward();
		       if(!suppressed) {Delay.msDelay(5000);}
		       m1.stop();
		       m2.stop();
		       }
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
		       
		       // beissen 
		       m3.setPower(100);
		       for(int i=0;i<5;i++){
		       if(suppressed){ break;}
		       Motor.A.rotate(30,true);
		       while( Motor.A.isMoving() && !suppressed){Thread.yield();}
		       Motor.A.rotateTo(0,true);
		       while( Motor.A.isMoving() && !suppressed){Thread.yield();} 
		       }
		       
		       // r�ckzug
		       align();
		       Motor.B.rotate(6*360,true);
		       Motor.C.rotate(6*360,true);
//		       while( Motor.B.isMoving() ){Thread.yield();}       
		       while( Motor.B.isMoving() && !suppressed){Thread.yield();}       
		       Motor.B.stop(); Motor.C.stop();
		       
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
		//Behavior[] bArray = {AngriffLaufen, ButtonStop};
	    LCD.drawString("Robogator4", 0, 1);
	    Button.waitForAnyPress();
	    (new Arbitrator(bArray)).start();	
	}

}
