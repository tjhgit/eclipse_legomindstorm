package thomas.nxt.gelway;

/**
 * This class is utilised to set and get common parameters between the Balance and
 * Behavioural threads. It is used to offset the motors power levels and the tilt offset of
 * the robot.
 * 
 * @author Steven Witzand
 * @version August 2009
 */
class CtrlParam
{
   private double offsetLeft = 0.0;
   private double offsetRight = 0.0;
   private double tiltAngle = 0.0;
   private int driveState = 0;
   private double damp = 0.0;
   private boolean upright = false;

   /**
    * Set the GELways LEFT motor offset.
    * 
    * @param offset
    *           a double used to set the left motor offset in degrees per second.
    */
   public synchronized void setLeftMotorOffset(double offset)
   {
      this.offsetLeft = offset;
   }

   /**
    * Set the GELways RIGHT motor offset.
    * 
    * @param offset
    *           a double used to set the right motor offset in degrees per second.
    */
   public synchronized void setRightMotorOffset(double offset)
   {
      this.offsetRight = offset;
   }

   /**
    * Set the GELways LEFT motor offset.
    * 
    * @return A double of the left motor offset in degrees per second.
    */
   public synchronized double leftMotorOffset()
   {
      return this.offsetLeft;
   }

   /**
    * Set the GELways RIGHT motor offset.
    * 
    * @return A double of the right motor offset in degrees per second.
    */
   public synchronized double rightMotorOffset()
   {
      return this.offsetRight;
   }

   /**
    * Set the tilt motor angle offset. The method works incrementally since it is the nature
    * off the PID controller to remove the offset.
    * 
    * @param angle
    *           A double of the tilt angle offset in degrees.
    */
   public synchronized void setTiltAngle(double angle)
   {
      this.tiltAngle += angle;
   }

   /**
    * Get the tilt motor angle offset.
    * 
    * @return A double of the tilt angle offset in degrees.
    */
   public synchronized double tiltAngle()
   {
      return this.tiltAngle;
   }

   /**
    * Resets the tilt motor angle back to zero.
    */
   public void resetTiltAngle()
   {
      this.tiltAngle = 0.0;
   }

   /**
    * Set the current drive state of the GELway. 0 = Forwards, 1 = Backwards, 2 = Stationary
    * 
    * @param state
    *           current drive state of the GELway
    */
   public void setDriveState(int state)
   {
      this.driveState = state;
   }
   /**
    * Returns the current drive state of the GELway
    * 
    * @return current drive state of the GELway
    */
   public synchronized int getDriveState()
   {
      return this.driveState;
   }
   /**
    * Returns the current upright state of the GELway
    * 
    * @return current upright state of the GELway
    */
   public void setUpright(boolean state)
   {
      this.upright = state;
   }
   public synchronized boolean getUpright()
   {
      return this.upright;
   }
   /**
    * Testing methods used to slow the initial accelleration of the GELway
    */
   public void setDamp(double weight)
   {
      this.damp += weight;
   }
   public void resetDamp()
   {
      this.damp = 0.0;
   }
   public synchronized double getDamp()
   {
      return this.damp;
   }
}