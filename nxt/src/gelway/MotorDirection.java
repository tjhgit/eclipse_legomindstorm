package thomas.nxt.gelway;
/**
 * A class which handles controlling the GELway directional movements. Works by altering
 * parameters in the CtrlParam class which are called by the BalanceController thread.
 * 
 * @author Steven Jan Witzand
 * @version August 2009
 */
public class MotorDirection
{
   private CtrlParam ctrl;
   private final double turnPower = 200; // Speed at which the GELway rotates
   private final double tiltPower = 3; // Speed of moving forwards and backwards

   /**
    * MotorDirection constructor.
    * 
    * @param ctrl
    *           The motor control parameters.
    */
   public MotorDirection(CtrlParam ctrl)
   {
      this.ctrl = ctrl;
   }

   /**
    * Moves the GELway forward for a specified period of time. Works by incrementally
    * increasing the motor tilt angle offset.
    * 
    * @param period
    *           An integer number specifying the length to move the GELway forward.
    */
   public void forward(int period)
   {
      ctrl.setLeftMotorOffset(0);
      ctrl.setRightMotorOffset(0);
      for (int time = 0; time < period; time += 10) {
         try {
            Thread.sleep(10);
         } catch (InterruptedException e) {
         }
         ctrl.setTiltAngle(tiltPower);
      }
   }

   /**
    * Moves the GELway backward for a specified period of time. Works by incrementally
    * decreasing the motor tilt angle offset.
    * 
    * @param period
    *           An integer number specifying the length to move the GELway backward.
    */
   public void backward(int period)
   {
      ctrl.setLeftMotorOffset(0);
      ctrl.setRightMotorOffset(0);

      for (int time = 0; time < period; time += 10) {
         try {
            Thread.sleep(10);
         } catch (InterruptedException e) {
         }
         ctrl.setTiltAngle(-1.5 * tiltPower);
      }
   }

   /**
    * Moves the GELway in a direction for a specified period of time and power. Works by
    * incrementally increasing the motor tilt angle offset.
    * 
    * @param period
    *           An integer number specifying the length to move the GELway forward.
    */
   public void move(int period, double tiltDir)
   {
      ctrl.setLeftMotorOffset(0);
      ctrl.setRightMotorOffset(0);
      for (int time = 0; time < period; time += 10) {
         try {
            Thread.sleep(10);
         } catch (InterruptedException e) {
         }
         ctrl.setTiltAngle(tiltDir);
      }
   }

   /**
    * Rotate the GELway left indefinitely until a forward or backward movement is given.
    * 
    * @param period
    *           An integer to delay other commands being sent to the GELway
    */
   public void left(int period)
   {
      ctrl.setLeftMotorOffset(-turnPower);
      ctrl.setRightMotorOffset(turnPower);
      delay(period);
   }

   /**
    * Rotate the GELway right indefinitely until a forward or backward movement is given.
    * 
    * @param period
    *           An integer to delay other commands being sent to the GELway
    */
   public void right(int period)
   {
      ctrl.setLeftMotorOffset(turnPower);
      ctrl.setRightMotorOffset(-turnPower);
      delay(period);
   }

   /**
    * Instruct GELway to stop, and stay stopped for a given period of time.
    * 
    * @param period
    *           An integer containing the number of millisecond to pause before returning.
    */
   public void stop(int period)
   {
      ctrl.setLeftMotorOffset(0);
      ctrl.setRightMotorOffset(0);
      delay(period);
   }

   /**
    * Make the programming sleep for a specified period of time.
    * 
    * @param time
    *           An integer containing the number of milliseconds to sleep.
    */
   public void delay(int time)
   {
      try {
         Thread.sleep(time);
      } catch (Exception e) {}
   }
}
