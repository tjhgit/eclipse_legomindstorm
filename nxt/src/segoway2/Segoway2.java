package segoway2;

// use this program in connection with BTSend1.java in Project BTControl
import java.io.DataInputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.robotics.*;
import lejos.util.Delay;


class TiltDriver extends Thread {
	BTConnection connection;
	DataInputStream dataIn;
//	DataOutputStream dataOut;
	
	enum Command {
		LEAN;
	}
	
	public void connect() {
		LCD.clear();
		LCD.drawString("Waiting", 0, 0);
		connection = Bluetooth.waitForConnection(); // this method is very patient. 
		LCD.clear();
		LCD.drawString("Connected", 0, 0);
		dataIn = connection.openDataInputStream();
//		dataOut = connection.openDataOutputStream();
		Sound.beepSequence();
	}
	
	private void readData() {
		int code;
		try {
			code = dataIn.readInt();
			Command command = Command.values()[code];
			LCD.clear();
			LCD.drawInt(code,0,1);
			Sound.playTone(800 + 100 * code, 200);
			if (command == Command.LEAN) {
			    int angle = dataIn.readInt();
			    Motor.A.rotate(angle);
			} 
			Sound.pause(100);
		} catch (IOException e) {
			System.out.println("Read exception "+e);
		}
	}		
	
	public void run(){
		connect();
		while (!Button.ESCAPE.isDown())
			readData();
	}
}

public class Segoway2 {
	static private void startBeeps(int n, int tone) {
		// Play warning beep sequence to indicate balance about to start
		for (int c=n; c>0;c--) {
			Sound.playTone(tone,10);
			try { Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}
	public static void main(String[] args) {
		Motor.A.stop();
		startBeeps(3,440);
		
		ColorSensor cs = new ColorSensor(SensorPort.S3);
		cs.setFloodlight(Color.RED);
				
		int target = 0;
		for(int i=0;i<5;i++) {
			target += cs.getNormalizedLightValue();
			Delay.msDelay(500);
		}
		target /=5;	
		
		LCD.drawInt(target,4, 4, 2);
		
		startBeeps(1,540);
		
		new TiltDriver().start();
		
        EncoderMotor m1 = new NXTMotor(MotorPort.B);
        EncoderMotor m2 = new NXTMotor(MotorPort.C);
        
        float factor = 10.f;
        float kp  = 25/factor; // 30
        float ki = 1/factor; // 2
        float kd = 10/factor;
        
        int integral = 0;
        int prev_error = 0;
        int abs_power = 0;
        
        do{
        	int value = cs.getNormalizedLightValue();
        	if(value > 0){
        		int error = value-target;
	        	integral = integral + error;
	        	int derivative = error - prev_error;
	        	prev_error = error;
	        	int power = (int) (kd*derivative + ki*integral + kp*error);
	        	abs_power = Math.abs(power);
	        	
	        	LCD.drawInt(value,4, 4, 3);
	        	LCD.drawInt(power,4, 4, 4);
	        		            	
	        	m1.setPower(abs_power);
	        	m2.setPower(abs_power);
	        	if(power>0) {
	        		m1.forward();
	        		m2.forward();
	        	} else {
	        		m1.backward();
	        		m2.backward();
	        	}
	        	
        	}
		} while (abs_power<100 && !Button.ESCAPE.isDown());
//        } while (!Button.ESCAPE.isDown());
	}
}
