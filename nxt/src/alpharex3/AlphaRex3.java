package alpharex3;

import lejos.nxt.*;
import lejos.util.*;

public class AlphaRex3 {

    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TouchSensor touch1 = new TouchSensor(SensorPort.S1);
	    NXTMotor m1 = new NXTMotor(MotorPort.B);		
	    TouchSensor touch2 = new TouchSensor(SensorPort.S2);
	    NXTMotor m2 = new NXTMotor(MotorPort.C);
	    NXTMotor m3 = new NXTMotor(MotorPort.A);
	    UltrasonicSensor us = new UltrasonicSensor(SensorPort.S4);
	    
		
		Delay.msDelay(500);
		while(us.getDistance()>25);
		
		
		bewegebissensor(m1,touch1,45,true);
		bewegebissensor(m2,touch2,45,true);
		bewegeumdrehungen(new NXTMotor [] {m2},0.15,45,true);
		
		bewegebissensor(new NXTMotor [] {m1,m2,m3},us,45,true);
		
		bewegebissensor(m1,touch1,45,true);
		bewegeumdrehungen(new NXTMotor [] {m1},0.6,45,true);
		bewegebissensor(m2,touch2,45,true);
		bewegeumdrehungen(new NXTMotor [] {m1},20,50,true);
		bewegeumdrehungen(new NXTMotor [] {m1,m2,m3},7,45,true);

	}


    
    static void bewegebissensor(NXTMotor m, TouchSensor t, int powerlevel, boolean forward) {
    	m.setPower(powerlevel);
    	if(forward)
    		m.forward();
    	else
    		m.backward();       
    	while (!t.isPressed())		    	   	
    		Thread.yield();  	
    	m.stop();    	   	
    }
    
    static void bewegebissensor(NXTMotor [] m, UltrasonicSensor us, int powerlevel, boolean forward) {
    	for(int ii=0;ii<m.length;ii++){
    		m[ii].setPower(powerlevel);    	
    		if(forward)
    			m[ii].forward();
    		else
    			m[ii].backward();
    	}      
    	while (us.getDistance()>25)		    	   	
    		Thread.yield();
    	
    	for(int ii=0;ii<m.length;ii++){
    		m[ii].stop();
    	}  	   	
    }   
    static void bewegeumdrehungen(NXTMotor [] m, double turns, int powerlevel, boolean forward){
    	int currtacho=m[0].getTachoCount();
    	
    	for(int ii=0;ii<m.length;ii++){
    		m[ii].setPower(powerlevel);    	
    		if(forward)
    			m[ii].forward();
    		else
    			m[ii].backward();
    	}
    	
    	while(Math.abs(m[0].getTachoCount()-currtacho)<turns*360)
    		Thread.yield();
    	
    	for(int ii=0;ii<m.length;ii++){
    		m[ii].stop();
    	}
    }
}
