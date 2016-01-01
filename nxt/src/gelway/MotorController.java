package thomas.nxt.gelway;
/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
import lejos.nxt.*;
import lejos.robotics.EncoderMotor;

/**
 * A class used to handle controlling the motors. Has methods to set the motors speed and
 * get the motors angle and velocity. Based off original programmers of Marvin Bent Bisballe
 * Nyeng, Kasper Sohn and Johnny Rieper
 * 
 * @author Steven Jan Witzand
 * @version August 2009
 */
class MotorController
{

   private NXTMotor leftMotor;
   private NXTMotor rightMotor;
   // Sinusoidal parameters used to smooth motors
   private double sin_x = 0.0;
   private final double sin_speed = 0.1;
   private final double sin_amp = 20.0;
	// Motor globals
	private double motorPos = 0;
	private long mrcSum = 0, mrcSumPrev;
	private long motorDiff;
	private long mrcDeltaP3 = 0;
	private long mrcDeltaP2 = 0;
	private long mrcDeltaP1 = 0;
	private double tInterval;
	private long tCalcStart;
	
   /**
    * MotorController constructor.
    * 
    * @param leftMotor
    *           The GELways left motor.
    * @param rightMotor
    *           The GELways right motor.
    */
   public MotorController(NXTMotor leftMotor, NXTMotor rightMotor)
   {
      this.leftMotor = leftMotor;
      
      this.leftMotor.resetTachoCount();

      this.rightMotor = rightMotor;
      this.rightMotor.resetTachoCount();
   }

   /**
    * Method is used to set the power level to the motors required to keep it upright. A
    * dampened sinusoidal curve is applied to the motors to reduce the rotation of the
    * motors over time from moving forwards and backwards constantly.
    * 
    * @param leftPower
    *           A double used to set the power of the left motor. Maximum value depends on
    *           battery level but is approximately 815. A negative value results in motors
    *           reversing.
    * @param rightPower
    *           A double used to set the power of the right motor. Maximum value depends on
    *           battery level but is approximately 815. A negative value results in motors
    *           reversing.
    */
   public void setPower(double leftPower, double rightPower)
   {
//      sin_x += sin_speed;
//      int pwl = (int) (leftPower + Math.sin(sin_x) * sin_amp);
//      int pwr = (int) (rightPower - Math.sin(sin_x) * sin_amp);
	   int pwl = (int) leftPower;
	   int pwr = (int) rightPower;
	   
      leftMotor.setPower(Math.abs(pwl));
      if (pwl > 0) {
         leftMotor.backward();
      } else if (pwl < 0) {
         leftMotor.forward();
      } else {
         leftMotor.stop();
      }

      rightMotor.setPower(Math.abs(pwr));
      if (pwr > 0) {
         rightMotor.backward();
      } else if (pwr < 0) {
         rightMotor.forward();
      } else {
         rightMotor.stop();
      }
   }

   /**
    * getAngle returns the average motor angle of the left and right motors
    * 
    * @return A double of the average motor angle of the left and right motors in degrees.
    */
   public double getAngle()
   {
      return ((double) leftMotor.getTachoCount() + 
            (double) rightMotor.getTachoCount()) / 2.0;
   }

   /**
    * getAngle returns the average motor velocity of the left and right motors
    * 
    * @return a double of the average motor velocity of the left and right motors in
    *         degrees.
    */
	public void calcInterval(long cLoop) {
		if (cLoop == 0) {
			// First time through, set an initial tInterval time and
			// record start time
			tInterval = 0.0055;
			tCalcStart = System.currentTimeMillis();
		} else {
			// Take average of number of times through the loop and
			// use for interval time.
			tInterval = (System.currentTimeMillis() - tCalcStart)/(cLoop*1000.0);
		}
	}
	
   public double getAngleVelocity()
   {
//      return ((double) leftMotor.getActualSpeed() + 
//            (double) rightMotor.getActualSpeed()) / 2.0;
  	long mrcLeft, mrcRight, mrcDelta;

	// Keep track of motor position and speed
	mrcLeft = leftMotor.getTachoCount();
	mrcRight = rightMotor.getTachoCount();

	// Maintain previous mrcSum so that delta can be calculated and get
	// new mrcSum and Diff values
	mrcSumPrev = mrcSum;
	mrcSum = mrcLeft + mrcRight;
	motorDiff = mrcLeft - mrcRight;

	// mrcDetla is the change int sum of the motor encoders, update
	// motorPos based on this detla
	mrcDelta = mrcSum - mrcSumPrev;
	motorPos += mrcDelta;

	// motorSpeed is based on the average of the last four delta's.
	double motorSpeed = (mrcDelta+mrcDeltaP1+mrcDeltaP2+mrcDeltaP3)/(4*tInterval);

	// Shift the latest mrcDelta into the previous three saved delta values
	mrcDeltaP3 = mrcDeltaP2;
	mrcDeltaP2 = mrcDeltaP1;
	mrcDeltaP1 = mrcDelta;
	return motorSpeed;
	
   }

   /**
    * reset the motors tacho count
    */
   public void resetMotors()
   {
      leftMotor.resetTachoCount();
      rightMotor.resetTachoCount();
   }

   /**
    * stop both motors from rotating
    */
   public void stop()
   {
      leftMotor.stop();
      rightMotor.stop();
   }
}