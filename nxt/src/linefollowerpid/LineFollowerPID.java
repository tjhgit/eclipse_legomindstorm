package linefollowerpid;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.util.PIDController;
import lejos.util.Delay;


public class LineFollowerPID {

    PIDController pid;
    ColorSensor light;
    int lightValue;
    int turn;
    int targetHigh;
    int targetLow;
    int targetTmp;
    int offset;
    static final float KP = 20.0f; // 40
    static final float KI = 0.0005f;  //1e-3
    static final float KD = 0.0f; //40 
    static final int baseSpeed = 700; //900 is max
	
    public void drive() {
        light = new ColorSensor(SensorPort.S4);
		
        LCD.drawString("Calibrate High", 0, 0);
        Button.waitForAnyPress();
        targetHigh = light.getLightValue();
        LCD.drawInt(targetHigh,4,0,1);
        Delay.msDelay(1000);
   	   
        LCD.drawString("Calibrate Low ", 0, 0);
        Button.waitForAnyPress();
        targetLow = light.getLightValue();
        LCD.drawInt(targetLow,4,0,1);
        Delay.msDelay(1000);
        
        offset = (targetHigh + targetLow)/2;
   	    
        pid = new PIDController(offset, 10);
        pid.setPIDParam(PIDController.PID_KP, KP);
        pid.setPIDParam(PIDController.PID_KI, KI);
        pid.setPIDParam(PIDController.PID_KD, KD);
        pid.setPIDParam(PIDController.PID_LIMITHIGH, Motor.A.getMaxSpeed()-baseSpeed);
        pid.setPIDParam(PIDController.PID_LIMITLOW, -baseSpeed);
   	    
        LCD.drawString("Set on Midpoint ", 0, 0);
        Button.waitForAnyPress();
        targetTmp = light.getLightValue();
        LCD.drawInt(targetTmp,4,0,1);
        Delay.msDelay(1000);
        
        Motor.A.setSpeed(baseSpeed);
        Motor.C.setSpeed(baseSpeed);
        Motor.A.forward();
        Motor.C.forward();
        
		
        while (!Button.ESCAPE.isDown()) {
            lightValue = light.getLightValue();
            turn = pid.doPID(lightValue);
//            LCD.drawInt(turn,4,0,1);
            Motor.A.setSpeed(baseSpeed - turn);
            Motor.C.setSpeed(baseSpeed + turn);
            Motor.A.forward();
            Motor.C.forward();
//           Delay.msDelay(1000);
        }
    }
	
    public static void main(String[] args) {
    	LineFollowerPID pid = new LineFollowerPID();
        pid.drive();
    }
}