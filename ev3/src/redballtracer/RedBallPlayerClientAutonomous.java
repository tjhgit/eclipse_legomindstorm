package redballtracer;
// This is an autonomous yellow ball seeker using the behavior based robotics concept
// A more robust version ( :) ) is RedBallPlayerClientAutonomous1, not using behaviors.


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.OptionalDouble;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.video.Video;

import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

import lejos.utility.Delay;
import lejos.utility.PilotProps;

public class RedBallPlayerClientAutonomous {
	// from RedBallTraceClient
	// video vars
	private static final int WIDTH = 160;
	private static final int HEIGHT = 120;
	private byte[] frame;
	private static Video video;

	// communication 
	private static final String SERVER = "10.0.1.2"; // via bluetooth using pand
	private static final int PORT = 55555;
	private Socket sock;
	private BufferedOutputStream bos;
	private DataOutputStream bos_data;
	private DataInputStream bis_data;


	// from samples pilottest
	static RegulatedMotor leftMotor;
	static RegulatedMotor rightMotor;

	// Constructor
	RedBallPlayerClientAutonomous() throws InterruptedException {
		while(true) {
			try {
				EV3 ev3 = (EV3) BrickFinder.getLocal();
				video = ev3.getVideo();
				video.open(WIDTH, HEIGHT);
				frame = video.createFrame();
							
				sock = new Socket(SERVER, PORT);
				bos = new BufferedOutputStream(sock.getOutputStream()); // for byte transfer of video
				bos_data = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
				bis_data = new DataInputStream(new BufferedInputStream(sock.getInputStream()));


				LCD.drawString("Connected to server", 0,1);
				if (sock != null) { break; }

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	void close() {
		try {            
			bos.close();
			bos_data.close();
			bis_data.close();
			sock.close();
			video.close();   

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void sendCommHdr(int response, int resp_len) throws IOException {
		bos_data.writeInt(response);
		bos_data.writeInt(resp_len);
		bos_data.flush();			
	}	


	// public interface
	public float [] getBallPosition() throws IOException {
		for(int i=0;i<10;i++) { // need to grab several frames, since buffer is full with old ones?!
			video.grabFrame(this.frame);
		}		
		System.out.println("video grabbed");

		sendCommHdr(RedBallTraceResponses.ALLOK,WIDTH * HEIGHT*2);		
		bos.write(this.frame);
		bos.flush();
		System.out.println("video sent");

		// waiting for server to send response
		int message;
		do {
			message = bis_data.readInt(); // OK or NOK
			System.out.println("mess: "+message);
		}while(message ==-1 && Button.ESCAPE.isUp());				
		int howmany = bis_data.readInt(); // howmany to read
		System.out.println("getting pos");
		if(howmany>0) {
			float position [] = new float[howmany]; // pix_x, pix_y, distance [m]
			for(int posi=0;posi<howmany;posi++)
				position[posi] = bis_data.readFloat();
			return position;
		} else {
			return null;
		}

	}

	public static void showCount(DifferentialPilot robot, int i)
	{
		LCD.drawInt(leftMotor.getTachoCount(),0,i);
		LCD.drawInt(rightMotor.getTachoCount(),7,i);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		RedBallPlayerClient rbc= new RedBallPlayerClient();
		
		
		PilotProps pp = new PilotProps();
		pp.loadPersistentValues();
		float wheelDiameter = Float.parseFloat(pp.getProperty(PilotProps.KEY_WHEELDIAMETER, "4.0"));
		float trackWidth = Float.parseFloat(pp.getProperty(PilotProps.KEY_TRACKWIDTH, "18.0"));
//		System.out.println("Wheel diameter is " + wheelDiameter);
//		System.out.println("Track width is " +trackWidth);
		leftMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_LEFTMOTOR, "A"));
		rightMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_RIGHTMOTOR, "C"));
		boolean reverse = Boolean.parseBoolean(pp.getProperty(PilotProps.KEY_REVERSE,"false"));
		DifferentialPilot robot = new DifferentialPilot(wheelDiameter,trackWidth,leftMotor,rightMotor,reverse);		
				
		robot.setAcceleration(100);
		robot.setTravelSpeed(5.); // cm/sec
		robot.setRotateSpeed(5.); // deg/sec
		
		// This robot will always drive forward if there is a yellow bellow in front and no obstacle is in the way and no red mark is crossed
		Behavior b0 = new SeekDriveForward(robot,rbc);
		Behavior b2 = new DetectObstacle(robot);
		Behavior b3 = new DetectFloorMark(robot);
		Behavior[] behaviorList = { b0, b2, b3 }; // left to right from min to max priority
		// Behavior[] behaviorList = { b0, b2, b3 }; // funktioniert einwandfrei
		Arbitrator arbitrator = new Arbitrator(behaviorList);
		
		Button.LEDPattern(6);
		arbitrator.start();

		rbc.close();
	}

}

class SeekDriveForward implements Behavior {
	private static DifferentialPilot robot;
	private static RedBallPlayerClient rbp;
	private float [] ballpos;

	public SeekDriveForward(DifferentialPilot bot, RedBallPlayerClient robotrbc) {
		rbp = robotrbc;
		robot = bot;
	}
	public static float mean(float[] m) {
		double sum = 0;
		for (int i = 0; i < m.length; i++) {
			sum += m[i];
		}
		return (float) (sum / m.length);
	}	
	public boolean takeControl() {
		if (Button.readButtons() != 0) { // some button pressed
			robot.stop();
			Button.LEDPattern(6);
			Button.discardEvents();
			if ((Button.waitForAnyPress() & Button.ID_ESCAPE) != 0) {
				Button.LEDPattern(0);
				System.exit(0); // if button ESCAPE is pressed stop
			}
			Button.waitForAnyEvent();
		}

		return (!robot.isMoving()); // this behavior wants control if the robot is not moving
	}

	public void suppress() {
		//robot.stop(); // this seek action cannot be suppressed !!
	}

	public void action() {

		try {
			// TAKE A PICTURE LOOKING FOR A BALL
			ballpos = rbp.getBallPosition();
			LCD.clear();

			// TEST 2 search for ball, and throw it out of playing field
			if(ballpos == null) { // no ball found

				System.out.println("no ball found");
				//Delay.msDelay(2000);

				robot.rotate(-360.,true);
				// wait while the motor is rotating and no ball is detected
				while((ballpos = rbp.getBallPosition())==null &&  robot.isMoving());
				// STILL ROTATE UNTIL BALL CENTERED ON CAMERA

			} 
			if(ballpos!=null){	 // a ball was detected
				if(!robot.isMoving()) robot.rotate(-360.,true);

				float ballposition;
				do{ // while ball is not centered on camera lateral wise
					ballpos = rbp.getBallPosition(); 
					ballposition = (ballpos==null)?0:ballpos[0];
				} while(ballposition<50 || ballposition>110);

				robot.stop();
				System.out.println("Ball pos:" + ballpos[0] + " " + ballpos[1]+ " "+ ballpos[2]);

				// THROW THE BALL OUT OF THE PLAYING FIELD. Only stop when red marks of playing field detected or object in the way
				robot.forward();
			}

		} catch(IOException e) {
			e.printStackTrace();
		}

	}

}


class DetectFloorMark implements Behavior {
	private static Port colorSensorPort = SensorPort.S1;
    private static EV3ColorSensor colorSensor;
    private static SampleProvider colorsp;
    private static int colorss;
    
    private static DifferentialPilot robot;
    
	public DetectFloorMark(DifferentialPilot bot) {
		// Initialize color sensor
        colorSensor = new EV3ColorSensor(colorSensorPort);
        colorsp = colorSensor.getRedMode();
        colorss = colorsp.sampleSize();
    	robot = bot;
	}
	
    public static float mean(float[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return (float) (sum / m.length);
    }
    
	private boolean checkFloorMark() {		
        float[] sample = new float[colorss];
        // Gets the sample an returns it
        colorsp.fetchSample( sample, 0);      
        float redintens = mean(sample);          
		if (redintens > 0.4) { // take control if red mark on floor detected
			Button.LEDPattern(2);
			return true;
		} else {
			Button.LEDPattern(1);
			return false;
		}
	}

	public boolean takeControl() {
		return checkFloorMark();
	}

	public void suppress() {
		// Since this is highest priority behavior, suppress will never be
		// called.
	}

	public void action() {
		// stop and move backward
		robot.stop();
		robot.rotate(-180.);
	}
}


class DetectObstacle implements Behavior {
	private static EV3IRSensor irSensor;
	private static Port irSensorPort = SensorPort.S4;
    private static SampleProvider irsp;
    private static int irss;
    private static DifferentialPilot robot;
    
	public DetectObstacle(DifferentialPilot bot) {
        irSensor = new EV3IRSensor(irSensorPort);
    	irsp = irSensor.getDistanceMode();
    	irss = irsp.sampleSize(); // seems to be 1 and outputs NaNs :(
    	robot = bot;
	}

	private boolean checkDistance() {		
        float[] sample = new float[irss];
        // Gets the sample an returns it
        irsp.fetchSample( sample, 0);
        float dist = sample[0];
        if(Float.isNaN(dist)) {dist = (float) 1000.;};        
		if (dist < 8.) { // take control if distance of object is below 8cm
			Button.LEDPattern(2);
			return true;
		} else {
			Button.LEDPattern(1);
			return false;
		}
	}

	public boolean takeControl() {
		return checkDistance();
	}

	public void suppress() {
		// Since this is highest priority behavior, suppress will never be
		// called.
	}

	public void action() {
		// stop and move backward
		robot.stop();
		robot.rotate(-180.);
	}
}
