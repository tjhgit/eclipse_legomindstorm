package segoway1;


import lejos.nxt.*;
import lejos.robotics.*;
import lejos.util.Delay;
import lejos.util.PIDController;

public class Segoway1 {
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
		
//		LightSensor cs = new LightSensor(SensorPort.S3);
		
		int target = 0;
		for(int i=0;i<5;i++) {
			target += cs.getNormalizedLightValue();
			Delay.msDelay(500);
		}
		target /=5;	
		
		LCD.drawInt(target,4, 4, 0);
		
		startBeeps(1,540);
		
        EncoderMotor m1 = new NXTMotor(MotorPort.B);
        EncoderMotor m2 = new NXTMotor(MotorPort.C);
 
        float factor = 10f;  
        float dt = 1.2f;
        PIDController pid = new PIDController(target, 0);
        pid.setPIDParam(PIDController.PID_KP, 25/factor); // 60 50
        pid.setPIDParam(PIDController.PID_KI, 1/factor/dt);
        pid.setPIDParam(PIDController.PID_KD, 10/factor*dt);
        
        int abs_power = 0;
        do{
        	int value = cs.getNormalizedLightValue();
        	if(value > 0){
        		int power = pid.doPID(value);
	        	abs_power = Math.abs(power);
	        	
//	        	LCD.drawInt(value,4, 4, 1);
//	        	LCD.drawInt(power,4, 4, 2);
	        		            	
	        	m1.setPower(abs_power);
	        	m2.setPower(abs_power);
	        	if(power<0) {
	        		m1.forward();
	        		m2.forward();
	        	} else {
	        		m1.backward();
	        		m2.backward();
	        	}
	        	
        	}
		} while (abs_power<1000 && !Button.ESCAPE.isDown());
	}

}







//
//import lejos.nxt.*;
//import lejos.util.Delay;
//import lejos.util.PIDController;
//
//public class Segoway1 {
//	
//	 static private void startBeeps(int n, int tone) {
//
////         System.out.println("Balance in");
//
//         // Play warning beep sequence to indicate balance about to start
//         for (int c=n; c>0;c--) {
////                 System.out.print(c + " ");
//                 Sound.playTone(tone,30);
//                 try { Thread.sleep(1000);
//                 } catch (InterruptedException e) {}
//         }
////         System.out.println("GO");
////         System.out.println();
// }
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		Motor.A.stop();
//		
//		
//		ColorSensor cs = new ColorSensor(SensorPort.S3);
//		
//		startBeeps(3,440);
//		int target = 0;
//		do {
////			target = cs.getNormalizedLightValue();
//			target = cs.getLightValue();
//			LCD.drawInt(target,4, 4, 0);
//		} while (target<40 && !Button.ESCAPE.isDown());
//		
////		startBeeps(3,540);
////		int target1 = 0;
////		do {
//////			target = cs.getNormalizedLightValue();
////			target1 = cs.getLightValue();
////			LCD.drawInt(target1,4, 4, 1);
////		} while (target1<40 && !Button.ESCAPE.isDown());
////		
////		target = (int) (0.5*(target+target1));
////		
//		double dt = 1*1e-3;
//        PIDController pid = new PIDController(target, (int) (dt*1e3));
//        pid.setPIDParam(PIDController.PID_KP, 3f); // 60 50
//        pid.setPIDParam(PIDController.PID_KI, (float) (10f/dt)); // 1e-2 1e-3
//        pid.setPIDParam(PIDController.PID_KD, (float) (62000/dt));
//        pid.setPIDParam(PIDController.PID_RAMP_POWER, 1.0f);
////        pid.setPIDParam(PIDController.PID_LIMITHIGH, Motor.A.getMaxSpeed());
////        pid.setPIDParam(PIDController.PID_LIMITLOW, -Motor.A.getMaxSpeed());
////        pid.setPIDParam(PIDController.PID_I_LIMITHIGH, (int) (Motor.A.getMaxSpeed()/(PIDController.PID_KI*dt)));
//
//        pid.setPIDParam(PIDController.PID_LIMITHIGH, 100);
//        pid.setPIDParam(PIDController.PID_LIMITLOW, -100);
//        pid.setPIDParam(PIDController.PID_I_LIMITHIGH, (int) (0.1*100/(PIDController.PID_KI*dt)));
////        startBeeps(1,540);		
//		
////        double ratio = Motor.B.getMaxSpeed()/PIDController.PID_LIMITHIGH;
//        NXTMotor m1 = new NXTMotor(MotorPort.B);
//        NXTMotor m2 = new NXTMotor(MotorPort.C);
//        
//        int correction = 0;
//        int abs_corr = 0;
//        // the balancing loop
//        do {
//        	int value = cs.getLightValue();
//        	if(value>0){
//	        	correction = pid.doPID(value);
//	        	abs_corr = Math.abs(correction);
//	        	int velocity = (int) (abs_corr);
//	        	
////	        	LCD.drawInt(value,4, 4, 0);
////	        	LCD.drawInt(correction,4, 4, 2);
////	        	LCD.drawInt(velocity ,4, 4, 3);
//	        	
////	        	Motor.B.setSpeed(velocity);
////	        	Motor.C.setSpeed(velocity);
//	        	m1.setPower(velocity);
//	        	m2.setPower(velocity);
//	        	
//	        	if(correction<0) {
//	        		Motor.B.forward();
//	        		Motor.C.forward();
//	        		m1.forward();
//	        		m2.forward();
//	        	} else {
//	        		Motor.B.backward();
//	        		Motor.C.backward();
//	        		m1.backward();
//	        		m2.backward();
//	        	}
//        	}
//        	
//        } while (!Button.ESCAPE.isDown());
////        	}
////        } while(!Button.ESCAPE.isDown());
////        Delay.msDelay(2000);
//	}
//
//}
