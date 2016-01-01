package ev3rstorm;

import lejos.hardware.Button;
import lejos.hardware.device.DeviceIdentifier;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.utility.Delay;

public class ev3rstormIRctrl {

	protected GraphicsLCD bild = LocalEV3.get().getGraphicsLCD();
	private UnregulatedMotor m_rechts = new UnregulatedMotor(MotorPort.B);
	private UnregulatedMotor m_links = new UnregulatedMotor(MotorPort.C);
	private UnregulatedMotor m_waffe = new UnregulatedMotor(MotorPort.A);
	private EV3IRSensor ir;
	// Will autodetect port with IR sensor
	Port [] sensorPorts = {SensorPort.S1, SensorPort.S2,SensorPort.S3, SensorPort.S4}; 

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ev3rstormIRctrl robot=new ev3rstormIRctrl();
		robot.autodetectIRSensor();
		robot.monitorIR();
	}

	public void monitorIR() {
		boolean keep_looping = true;
		int previous_command = 0;
		int channel = 0;
		UnregulatedMotor currentLeft = m_links;
		UnregulatedMotor currentRight = m_rechts;
		UnregulatedMotor currentWaffe = m_waffe;
		while(keep_looping) {
			Delay.msDelay(25);
			// Get the IR commands
			byte [] cmds = new byte[4];
			ir.getRemoteCommands(cmds, 0, cmds.length);
			// Figure out which channel is active:
			int command = 0;
			for(int i=0;i<4;i++) {
				if(cmds[i] > 0) {
					channel = i;
					command = cmds[i];
				}
			}

			LCD.drawString("Kanal: " + channel, 0, 4);
			LCD.drawString("Kommando: " + command, 0, 5);
			LCD.refresh();


			//			// Make motor and GUI changes according to active channel:
			if(channel == 0) {
				currentLeft = m_links;
				currentRight = m_rechts;				
			}
			else if(channel == 1) {
				currentLeft = m_waffe;
			}
			//			else if(channel == 2) {
			//							
			//			}
			//			else if(channel == 3) {
			//							
			//			}
			if(command != previous_command) {
				if(command==0|command==3|command==4|command==10) {// left buttons not pressed
					currentLeft.setPower(0);
					currentWaffe.setPower(0);
				}
				if(command==0|command==1|command==2|command==11) {// right buttons not pressed
					currentRight.setPower(0);
					currentWaffe.setPower(0);
				}
				if(command==9) {
					currentWaffe.setPower(100);
					currentWaffe.forward();
				}
				if(command==0|command==3|command==4|command==10) {// left buttons not pressed
					currentLeft.setPower(0);
				}
				if(command==0|command==1|command==2|command==11) {// right buttons not pressed
					currentRight.setPower(0);
				}
				if(command==1|command==5|command==6) { // upper-left
					currentLeft.forward();
					currentLeft.setPower(100);
				}
				if(command==2|command==7|command==8) { // lower-left
					currentLeft.backward();
					currentLeft.setPower(100);
				}
				if(command==3|command==5|command==7) { // upper-right
					currentRight.forward();
					currentRight.setPower(100);
				}
				if(command==4|command==6|command==8) { // lower-right
					currentRight.backward();
					currentRight.setPower(100);
				}
				previous_command = command;
				bild.refresh();
			}
			if (Button.ESCAPE.isDown()) keep_looping = false;
		}
		ir.close();
		m_rechts.close();
		m_links.close();
		m_waffe.close();
	}
	public void autodetectIRSensor() {
		int irPort;
		do {
			irPort = detectIRSensorPort();
			if(irPort >= 0) {
				LCD.drawString("Detected port " + (irPort+1), 0, 4);
				LCD.refresh();
				ir = new EV3IRSensor(sensorPorts[irPort]);
			}
			else {
				LCD.drawString("Plug IR sensor", 0, 0);
				LCD.drawString("into any port", 0, 1);
				LCD.drawString("ENTER+DOWN Quits", 0, 3);
				LCD.refresh();
			}
		} while(irPort < 0);
		bild.clear(); // Clears previous messages
	}
	private int detectIRSensorPort() {
		int detectedPort = -99;
		// run device detection in parallel to reduce detection time
		DeviceIdentifier []ids = new DeviceIdentifier[4];
		for(int i=0; i<4; i++)
			ids[i] = new DeviceIdentifier(sensorPorts[i]);
		for(int i=0;i<4;i++) {
			if (ids[i].getDeviceSignature(false).contains("IR-PROX"))
				detectedPort = i;
			ids[i].close();
		}
		return detectedPort;
	} 

}
