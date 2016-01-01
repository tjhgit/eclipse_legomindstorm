package linefollower;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.ColorSensor;
import lejos.robotics.Color;
import lejos.nxt.SensorPort;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.RotateMoveController;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.util.PilotProps;

public class LineFollower {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		PilotProps pp = new PilotProps();
    	pp.loadPersistentValues();
    	float wheelDiameter = Float.parseFloat(pp.getProperty(PilotProps.KEY_WHEELDIAMETER, "3.0"));
    	float trackWidth = Float.parseFloat(pp.getProperty(PilotProps.KEY_TRACKWIDTH, "13.0"));
    	RegulatedMotor leftMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_LEFTMOTOR, "A"));
    	RegulatedMotor rightMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_RIGHTMOTOR, "C"));
    	boolean reverse = Boolean.parseBoolean(pp.getProperty(PilotProps.KEY_REVERSE,"false"));
    	
		// Change last parameter of Pilot to specify on which 
		// direction you want to be "forward" for your vehicle.
		// The wheel and axle dimension parameters should be
		// set for your robot, but are not critical.
		final RotateMoveController pilot = new DifferentialPilot(wheelDiameter, trackWidth, leftMotor, rightMotor, reverse);
		final ColorSensor light = new ColorSensor(SensorPort.S4);
        pilot.setRotateSpeed(180);
        pilot.setTravelSpeed(10);
        /**
         * this behavior wants to take control when the light sensor sees the line
         */
		Behavior DriveForward = new Behavior()
		{
			public boolean takeControl() {
				light.setFloodlight(Color.WHITE);
				return light.getColor().getColor() != Color.WHITE;}
			
			public void suppress() {
				pilot.stop();
			}
			public void action() {
				pilot.forward();
				light.setFloodlight(Color.WHITE);
                while(light.getColor().getColor() != Color.WHITE) Thread.yield(); //action complete when not on line
			}					
		};
		
		Behavior OffLine = new Behavior()
		{
			private boolean suppress = false;
			
			public boolean takeControl() {
				light.setFloodlight(Color.WHITE);
				return light.getColor().getColor() == Color.WHITE;
				}

			public void suppress() {
				suppress = true;
			}
			
			public void action() {
				int sweep = -10;
				while (!suppress) {
					pilot.rotate(sweep,true);
					while (!suppress && pilot.isMoving()) Thread.yield();
					sweep *= -2;
				}
				pilot.stop();
				suppress = false;
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

		Behavior[] bArray = { OffLine, DriveForward, ButtonStop};
        LCD.drawString("Line ", 0, 1);
        Button.waitForAnyPress();
	    (new Arbitrator(bArray)).start();
	}
}


