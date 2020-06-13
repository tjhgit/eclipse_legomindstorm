package ev3rstorm;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.EncoderMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;


public class ev3rstorm {
	static RegulatedMotor m_rechts = new EV3LargeRegulatedMotor(MotorPort.B);
	static RegulatedMotor m_links = new EV3LargeRegulatedMotor(MotorPort.C);
//	// unregulated motor for shooting
	static EncoderMotor m_waffe = new UnregulatedMotor(MotorPort.A);
	static IRSensor sensor;
//	
	
	public static void mission1_message() {
		GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
		g.drawString("Mission1 Demo", 5, 0, 0);
		// Quit GUI button:
		g.setFont(Font.getSmallFont()); // can also get specific size using Font.getFont()
		int y_quit = 100;
		int width_quit = 45;
		int height_quit = width_quit/2;
		int arc_diam = 6;
		g.drawString("QUIT", 9, y_quit+7, 0);
		g.drawLine(0, y_quit, 45, y_quit); // top line
		g.drawLine(0, y_quit, 0, y_quit+height_quit-arc_diam/2); // left line
		g.drawLine(width_quit, y_quit, width_quit, y_quit+height_quit/2); // right line
		g.drawLine(0+arc_diam/2, y_quit+height_quit, width_quit-10, y_quit+height_quit); // bottom line
		g.drawLine(width_quit-10, y_quit+height_quit, width_quit, y_quit+height_quit/2); // diagonal
		g.drawArc(0, y_quit+height_quit-arc_diam, arc_diam, arc_diam, 180, 90);
		// Enter GUI button:
		g.fillRect(width_quit+10, y_quit, height_quit, height_quit);
		g.drawString("GO", width_quit+15, y_quit+7, 0,true);
		Button.waitForAnyPress();
		if(Button.ESCAPE.isDown()) System.exit(0);
		g.clear(); 
	}
	
	static void schuss() {
		ev3rstorm.m_waffe.setPower(100);
		ev3rstorm.m_waffe.forward();
		Delay.msDelay(1000);
		ev3rstorm.m_waffe.stop();
	}
	
	public static void geradeaus() {
		ev3rstorm.m_rechts.setSpeed(360);
		ev3rstorm.m_links.setSpeed(360);
		ev3rstorm.m_rechts.forward();
		ev3rstorm.m_links.forward();
		Delay.msDelay(4000*730/360);
		ev3rstorm.m_rechts.stop();
		ev3rstorm.m_links.stop();		
	}
	
	public static void drehe180()  {
	// regulated motor else
		ev3rstorm.m_links.stop();
		ev3rstorm.m_rechts.setSpeed(360);
		ev3rstorm.m_links.setSpeed(360);
		ev3rstorm.m_rechts.backward();
		//m_links.backward();
		Delay.msDelay(2000*720/360); // auf geschwindigkeit 720 geeicht
		ev3rstorm.m_rechts.stop();
		//m_links.stop();
	}
	
//	// get a port instance
//	Port port = LocalEV3.get().getPort("S2");
//
//	// Get an instance of the Ultrasonic EV3 sensor
//	SensorModes sensor = new EV3UltrasonicSensor(port);
//
//	// get an instance of this sensor in measurement mode
//	SampleProvider distance= sensor.getMode("Distance");
//
//	// initialize an array of floats for fetching samples. 
//	// Ask the SampleProvider how long the array should be
//	float[] sample = new float[distance.sampleSize()];
//
//	// fetch a sample
//	while(true) 
//	  distance.fetchSample(sample, 0);
	
//	// stack a filter on the sensor that gives the running average of the last 5 samples
//	SampleProvider average = new MeanFilter(distance, 5);
//
//	// initialise an array of floats for fetching samples
//	float[] sample = new float[average.sampleSize()];
//
//	// fetch a sample
//	average.fetchSample(sample, 0);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		mission1_message();
		
		geradeaus();
		drehe180();
		geradeaus();
		drehe180();
		schuss();
		schuss();
		schuss();

		// jetzt mal was Spannenderes
		sensor = new IRSensor();
		sensor.setDaemon(true);
		sensor.start();
		
		Behavior b1 = new DriveForward();
		Behavior b2 = new DetectWall();
		Behavior[] behaviorList = { b1, b2 };
		Arbitrator arbitrator = new Arbitrator(behaviorList);
		Button.LEDPattern(6);
		arbitrator.start();
	}
}

class IRSensor extends Thread {
	EV3IRSensor ir = new EV3IRSensor(SensorPort.S4);
	SampleProvider sp = ir.getDistanceMode();
	//public int control = 0;
	public int distance = 255;

	IRSensor() {
	}

	public void run() {
		while (true) {
			float[] sample = new float[sp.sampleSize()];
			//control = ir.getRemoteCommand(0);
			sp.fetchSample(sample, 0);
			distance = (int) sample[0];
			//System.out.println("Control: " + control + " Distance: " + distance);
			System.out.println(" Distance: " + distance);
		}
	}
}

class DriveForward implements Behavior {
	private boolean _suppressed = false;

	public boolean takeControl() {
		if (Button.readButtons() != 0) {
			_suppressed = true;
			ev3rstorm.m_links.stop();
			ev3rstorm.m_rechts.stop();
			Button.LEDPattern(6);
			Button.discardEvents();
			System.out.println("Button pressed");
			if ((Button.waitForAnyPress() & Button.ID_ESCAPE) != 0) {
				Button.LEDPattern(0);
				System.exit(1);
			}
			System.out.println("Button pressed 2");
			Button.waitForAnyEvent();
			System.out.println("Button released");
		}
		return true; // this behavior always wants control.
	}

	public void suppress() {
		_suppressed = true;// standard practice for suppress methods
	}

	public void action() {
		_suppressed = false;
		// ev3rstorm.m_links.forward();
		// ev3rstorm.m_rechts.forward();
		while (!_suppressed) {
			ev3rstorm.m_links.forward();
			ev3rstorm.m_rechts.forward();
		}
		Thread.yield(); // don't exit till suppressed
	}
}

class DetectWall implements Behavior {
	public DetectWall() {
		// touch = new TouchSensor(SensorPort.S1);
		// sonar = new UltrasonicSensor(SensorPort.S3);
	}

	private boolean checkDistance() {
		int dist = ev3rstorm.sensor.distance;
		if (dist < 20) {
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
//		ev3rstorm.m_links.backward();
//		ev3rstorm.m_rechts.backward();
//		Delay.msDelay(1000);
//		ev3rstorm.m_rechts.stop();
//		ev3rstorm.m_links.stop();
		ev3rstorm.drehe180();
		ev3rstorm.m_links.forward();
		ev3rstorm.m_rechts.forward();
		
	}
}
