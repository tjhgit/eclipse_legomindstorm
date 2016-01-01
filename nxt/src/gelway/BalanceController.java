package thomas.nxt.gelway;

import lejos.nxt.*;
import lejos.nxt.addon.EOPD;
import lejos.util.Datalogger;
/**
 * This class contains the parameters needed to keep the GELway balanced. It contains a PID
 * controller which reads in the angles and the angle velocities from the gyro sensor and
 * the left and right motor, and uses these (weighted) values to calculate the motor output
 * required to keep the GELway balanced.
 * 
 * @author Steven Witzand
 * @version March 2009
 */
public class BalanceController extends Thread
{
   // The PID control parameters
   private final double Kp = 1.2;
   private final double Ki = 0.25;
   private final double Kd = 0.1;
   //private final int eopdThresh = 1022;
   double num = 0.0;
   int startLog = 0;
   static double damp = 0.1;
   private static final int stationary = 0; // left
   private static final int forwards = 1; // up
   private static final int backwards = 2; // down
   static boolean upright = true;
   // Teseing error contributions.
//   private final double K_psi = 44.257035; // Gyro angle weight
//   private final double K_phi = 0.806876; // Motor angle weight
//   private final double K_psidot = 0.620882; // Gyro angle velocity weight
//   private final double K_phidot = 0.039711;// Motor angle velocity weight
   // Original Balance numbers
    private final double K_psi = 34.189581; // Gyro angle weight
    private final double K_phi = 0.835082; // Motor angle weight
    private final double K_psidot = 0.646772; // Gyro angle velocity weight
    private final double K_phidot = 0.028141; // Motor angle velocity weight
   private static CtrlParam ctrl;
   public boolean offsetDone = false;
   /**
    * BalanceController constructor.
    * 
    * @param ctrl The motor control parameters.
    */
   public BalanceController(CtrlParam ctrl)
   {
      this.ctrl = ctrl;
      setDaemon(true);
   }

   /**
    * The BalanceController thread which constantly runs to keep the GELway upright
    */
   public void run()
   {
	   
      MotorController motors = new MotorController(new NXTMotor(MotorPort.A),new NXTMotor(MotorPort.B));
      MPU6050 gyro = new MPU6050(SensorPort.S2);
      TouchSensor touch1 = new TouchSensor(SensorPort.S3);		
	  TouchSensor touch2 = new TouchSensor(SensorPort.S4);
	    
     // EOPD eopd = new EOPD(SensorPort.S2);
      //eopd.setModeLong();
      double int_error = 0.0;
      double prev_error = 0.0;
      int cLoop = 0;
      while (true) {
    	  motors.calcInterval(cLoop++);
         // Start balancing provided GELway is upright and the EOPD sensor can sense the
         // ground
        // while (eopd.readRawValue() < eopdThresh && upright) {
    	  while(!touch1.isPressed() && !touch2.isPressed() && upright) {
            ctrl.setUpright(true);
            runDriveState();
            double Psi = gyro.getAngle();
            double PsiDot = gyro.getAngleVelocity();
            // ctrl.tiltAngle() is used to drive the robot forwards and backwards
            double Phi = motors.getAngle() - ctrl.tiltAngle();
            double PhiDot = motors.getAngleVelocity();
            // Proportional Error
            double error = Psi * K_psi + Phi * K_phi + PsiDot * K_psidot + PhiDot
                  * K_phidot;
            // Integral Error
            int_error += error;
            // Derivative Error
            double deriv_error = error - prev_error;
            prev_error = error;
            // Power sent to the motors
            double pw = (error * Kp + deriv_error * Kd + int_error * Ki) * 1;
            
        	LCD.drawString("G angle= " + Psi + "       ", 0, 2);
        	LCD.drawString("G dotang= " + PsiDot + "       ", 0, 3);
        	LCD.drawString("M angle= " + Phi + "       ", 0, 4);
        	LCD.drawString("M dotang= " + PhiDot + "       ", 0, 5);       	
        	LCD.drawString("power= " + pw + "       ", 0, 6);

//           motors.setPower(10, 10);
//            motors.setPower(pw + ctrl.leftMotorOffset(), pw + ctrl.rightMotorOffset());
            // Delay used to stop Gyro being read to quickly. May need to be increase or
            // decreased depending on leJOS version.
            //delay(6);
         }
         startLog = 0;
         motors.stop();
         upright = false;
         ctrl.setUpright(false);
         //while (eopd.readRawValue() > eopdThresh) {}
         while(touch1.isPressed() || touch2.isPressed()) {}
         // Restart the robot after the third beep. Reset balance parameters
         //if (eopd.readRawValue() < eopdThresh) {
         if(!touch1.isPressed() && !touch2.isPressed()) {
            for (int i = 0; i < 3; i++) {
               //if (eopd.readRawValue() > eopdThresh) break;
            	if(touch1.isPressed() || touch2.isPressed()) break;
               Sound.setVolume(50 + i * 25);
               Sound.beep();
               delay(700);
            }
            gyro.resetGyro();
            motors.resetMotors();
            ctrl.resetTiltAngle();
            int_error = 0.0;
            prev_error = 0.0;
            ctrl.setDriveState(stationary);
            upright = true;
         } else { motors.stop(); }
      }
   }

   public static void delay(int time)
   {
      try {Thread.sleep(time);} catch (Exception e) {}
   }

   /**
    * Returns the current upright state of the GELway. TRUE = Upright
    * 
    * @return upright current upright state of the GELway
    */
   public static boolean getUpright() { return upright; }

   /**
    * Run drive state is used to keep the GELway in a constant state of forwards or
    * backwards motion. It can be set to keep the GELway stationary as well.
    */
   public static void runDriveState()
   {
      if (ctrl.getDriveState() == forwards)
         ctrl.setTiltAngle(3 - 3 * Math.exp(-ctrl.getDamp()));
      else if (ctrl.getDriveState() == backwards)
         ctrl.setTiltAngle(-5 + 3 * Math.exp(-ctrl.getDamp()));
      else
         ctrl.setTiltAngle(0);
      ctrl.setDamp(0.1);
   }
}