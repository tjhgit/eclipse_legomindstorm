package redballtracer;

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
import lejos.utility.Delay;
import lejos.utility.PilotProps;

public class RedBallPlayerClient {
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
	
	private static Port colorSensorPort = SensorPort.S1;
    private static EV3ColorSensor colorSensor;
    private static Port irSensorPort = SensorPort.S4;
    private static EV3IRSensor irSensor;
    private static SampleProvider colorsp,irsp;
    private static int colorss, irss;

	// Constructor
	RedBallPlayerClient() throws InterruptedException {
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
	
    private static float [] getSample(int ss, SampleProvider sp) {
        // Initializes the array for holding samples
        float[] sample = new float[ss];
        // Gets the sample an returns it
        sp.fetchSample( sample, 0);
        return sample;
    }

    public static float mean(float[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return (float) (sum / m.length);
    }
    
	public static void main(String[] args) throws IOException, InterruptedException {

		RedBallPlayerClient rbp= new RedBallPlayerClient();
		
		PilotProps pp = new PilotProps();
		pp.loadPersistentValues();
		float wheelDiameter = Float.parseFloat(pp.getProperty(PilotProps.KEY_WHEELDIAMETER, "4.0"));
		float trackWidth = Float.parseFloat(pp.getProperty(PilotProps.KEY_TRACKWIDTH, "18.0"));

		System.out.println("Wheel diameter is " + wheelDiameter);
		System.out.println("Track width is " +trackWidth);

		leftMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_LEFTMOTOR, "A"));
		rightMotor = PilotProps.getMotor(pp.getProperty(PilotProps.KEY_RIGHTMOTOR, "C"));
		boolean reverse = Boolean.parseBoolean(pp.getProperty(PilotProps.KEY_REVERSE,"false"));
		DifferentialPilot robot = new DifferentialPilot(wheelDiameter,trackWidth,leftMotor,rightMotor,reverse);		
				
		robot.setAcceleration(100);
		robot.setTravelSpeed(5.); // cm/sec
		robot.setRotateSpeed(5.); // deg/sec
		
		// Initialize color sensor
        colorSensor = new EV3ColorSensor(colorSensorPort);
        colorsp = colorSensor.getRedMode();
        colorss = colorsp.sampleSize();

        // Initialize IR Distance Sensor
        irSensor = new EV3IRSensor(SensorPort.S4);
    	irsp = irSensor.getDistanceMode();
    	irss = irsp.sampleSize(); // seems to be 1 and outputs NaNs :(
    	
//    	for(int i=0; i < 50;i++) {
//    		float dist =  getSample(irss,irsp)[0];
//    		
//    		if(Float.isNaN(dist))
//    			System.out.println("No object in range");
//    		else
//    			System.out.println("Distance: " + dist);
//    		
//    		Delay.msDelay(500);
//    	}
    	
    	
		float [] ballpos = rbp.getBallPosition();
		LCD.clear();
		if(ballpos != null) {
			System.out.println("Ball pos:" + ballpos[0] + " " + ballpos[1]+ " "+ ballpos[2]);	
		} 	
		
//		// TEST 3 aquire images		
//		for(int i = 0; i<100;i++){
//			ballpos = rbp.getBallPosition();
//		}
//		// TEST 3 ends here
		
		// TEST 2 search for ball, and throw it out of playing field
		if(ballpos == null) { // no ball found
			System.out.println("no ball found");
			Delay.msDelay(2000);
			
			robot.rotate(4*360.,true);
			
			// wait while the motor is rotating and no ball is detected
			while((ballpos = rbp.getBallPosition())==null &&  robot.isMoving());
			
			
			if(ballpos!=null) {	 // a ball was detected		
				while(ballpos[0]<50 || ballpos[0]>110){ // while ball is not centered on camera lateral wise
					ballpos = rbp.getBallPosition(); 
					if(ballpos==null) {
						ballpos = new float[]{0,0,0};
					}
				}
				robot.stop();
				System.out.println("Ball pos:" + ballpos[0] + " " + ballpos[1]+ " "+ ballpos[2]);
				Delay.msDelay(2000);
				
				robot.travel(100.,true);
				
				float [] mw1;
				float dist; 
				do{ // while no red stripe is detected & and no object closer than 8 cm
					mw1 = getSample(colorss,colorsp); // colorsensor red check
					dist = getSample(irss,irsp)[0]; // irsensor distance check
					if(Float.isNaN(dist)) {dist = (float) 1000.;}; // if no object or too close set to 1000
				} while(mean(mw1)<0.4 && dist > 8.0 && robot.isMoving());				
				robot.stop();
				
				
				ballpos = rbp.getBallPosition();
				if(ballpos != null) {
					LCD.clear();
					System.out.println("Ball pos:" + ballpos[0] + " " + ballpos[1]+ " "+ ballpos[2]);	
				} else {
					System.out.println("no ball found");
				}
				Delay.msDelay(2000);
				
			}		
		}
		// TEST 2 ends here
		
		// TEST 1 do maneuvers
//		System.out.println("Going forwards");
//		robot.forward();
//		Delay.msDelay(1000);
//		System.out.println("Stopping");
//		robot.stop();
//		System.out.println("Stopped");
//		showCount(robot, 0);
//		
//		LCD.clear();
//		ballpos = rbp.getBallPosition();
//		if(ballpos != null) {
//			LCD.clear();
//			System.out.println("Ball pos:" + ballpos[0] + " " + ballpos[1]+ " "+ ballpos[2]);	
//		} else {
//			System.out.println("no ball found");
//		}
//		Delay.msDelay(5000);
//		
//				
//		System.out.println("Going backwards");
//		robot.backward();
//		Delay.msDelay(1000);
//		robot.stop();
//		showCount(robot, 1);
//		
//		LCD.clear();
//		ballpos = rbp.getBallPosition();
//		if(ballpos != null) {
//			LCD.clear();
//			System.out.println("Ball pos:" + ballpos[0] + " " + ballpos[1]+ " "+ ballpos[2]);	
//		} else {
//			System.out.println("no ball found");
//		}
//		Delay.msDelay(5000);		
		// TEST 1 ENDS HERE
		
		
		
		
//		System.out.println("Travel started");
//		robot.travel(10,true);
//		System.out.println("Waiting for move to end");
//		while(robot.isMoving())Thread.yield();
//		System.out.println("Move ended");
//		showCount(robot, 2);
//		System.out.println("Travel back");
//		robot.travel(-10);
//		System.out.println("Travel back ended");
//		showCount(robot, 3);
//		System.out.println("Loop of rotates anti-clockwise");
//		for(int i = 0; i<4; i++)
//		{
//			robot.rotate(90);
//		}
//		showCount(robot, 4);
//		System.out.println("Loop of rotates clockwise");
//		for(int i = 0; i<4; i++)
//		{
//			robot.rotate(-90,true);
//			while(robot.isMoving())Thread.yield();
//		}
//		showCount(robot, 5);
//		System.out.println("Arcs backwards and forwards, anticlockwise and clockwise");
//		robot.steer(-50,180,true);
//		while(robot.isMoving())Thread.yield();
//		robot.steer(-50,-180);
//		showCount(robot, 6);
//		robot.steer(50,180);
//		robot.steer(50, -180);
//		showCount(robot, 7);
//		System.out.println("Travel forwards");
//		robot.travel(10,true);
//		Delay.msDelay(500);
//		robot.stop();
//		System.out.println("Travel backwards");
//		robot.travel(-10);
		//System.out.println("Spin twice anti-clockwise");
		//robot.rotate(720);


		rbp.close();
	}

}
