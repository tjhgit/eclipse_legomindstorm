package thomas.nxt.gelway;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.util.Delay;

/**
 * Display the type and address of any I2C sensors attached to the NXT
 * @author andy
 */


public class MPU6050 {
	
	// https://github.com/jrowberg/i2cdevlib/tree/master/Arduino/MPU6050
	private static final int MPU6050_RA_PWR_MGMT_1 = 0x6B;
	private static final int MPU6050_RA_CONFIG = 0x1A;
	private static final int MPU6050_RA_GYRO_CONFIG = 0x1B;
	private static final int MPU6050_RA_GYRO_XOUT_H = 0x43;
	private static final int MPU6050_RA_GYRO_YOUT_H = 0x45;
	private static final int MPU6050_RA_GYRO_ZOUT_H = 0x47;
	private static final int MPU6050_RA_ACCEL_XOUT_H = 0x3B;
	private static final int MPU6050_RA_ACCEL_YOUT_H = 0x3D;
	private static final int MPU6050_RA_ACCEL_ZOUT_H = 0x3F;
	
	private static final int MPU6050_RA_SMPLRT_DIV = 0x19;
	
	private static final int MPU6050_RA_WHO_AM_I = 0x75;
	private static final int MPU6050_RA_ACCEL_CONFIG = 0x1C;
	
	private static final int MPU6050_WHO_AM_I_LENGTH = 6;
	private static final int MPU6050_WHO_AM_I_BIT = 6;
	
	private static final int MPU6050_PWR1_DEVICE_RESET_BIT = 7;
	private static final int MPU6050_PWR1_SLEEP_BIT = 6;
	private static final int MPU6050_PWR1_CLKSEL_BIT =  2;
	private static final int MPU6050_PWR1_CLKSEL_LENGTH = 3;
	private static final int MPU6050_GCONFIG_FS_SEL_BIT  =  4;
	private static final int MPU6050_GCONFIG_FS_SEL_LENGTH =  2;
	private static final int MPU6050_ACONFIG_AFS_SEL_BIT = 4;
	private static final int MPU6050_ACONFIG_AFS_SEL_LENGTH = 2;
	private static final byte MPU6050_CLOCK_PLL_XGYRO = 0x01;
	
	private static final int MPU6050_CFG_DLPF_CFG_BIT = 2;
	private static final int MPU6050_CFG_DLPF_CFG_LENGTH =3;
	
	private static final int MPU6050_DLPF_BW_256 = 0x00;
	private static final int MPU6050_DLPF_BW_188 = 0x01;
	private static final int MPU6050_DLPF_BW_98 = 0x02;
	private static final int MPU6050_DLPF_BW_42 = 0x03;
	private static final int MPU6050_DLPF_BW_20 = 0x04;
	private static final int MPU6050_DLPF_BW_10 = 0x05;
	private static final int MPU6050_DLPF_BW_5 = 0x06;
	
	
	
	private static final byte MPU6050_GYRO_FS_250 = 0x00;
	private static final byte MPU6050_GYRO_FS_500 = 0x01;
	private static final byte MPU6050_GYRO_FS_1000 = 0x02;
	private static final byte MPU6050_GYRO_FS_2000 = 0x03;
	
	private static final byte MPU6050_ACCEL_FS_2 = 0x00;
	private static final byte MPU6050_ACCEL_FS_4 = 0x01;
	private static final byte MPU6050_ACCEL_FS_8 = 0x02;
	private static final byte MPU6050_ACCEL_FS_16 = 0x03;
	
	
    private static double ACCELEROMETER_SENSITIVITY = 8192.0;
    private static double GYROSCOPE_SENSITIVITY = 65.536;
    //private static double dt =  0.01;							// 10 ms sample rate!    
	
    
    private double angle = 0.0;
    private int lastGetAngleTime = 0;
    private double lastOffset = 0;
    private final double a = 0.999999;// Weight of older offset values.
    
    private RawI2CSensor I2C;
    
    MPU6050(SensorPort sens) {
    	I2C = new RawI2CSensor(sens);
		
		int waittime = 100;
		LCD.drawString("setting address",0,0);
		Delay.msDelay(waittime);
		I2C.setAddress(0x68<<1);

		
		LCD.drawString(testConnection()?"success":"fail",5,1);
		Delay.msDelay(waittime);
			
	    setClockSource(MPU6050_CLOCK_PLL_XGYRO);
	    setFullScaleGyroRange(MPU6050_GYRO_FS_250); // most sensitive
	    setFullScaleAccelRange(MPU6050_ACCEL_FS_2);// most sensitive
	    
	    setSleepEnabled(false); // thanks to Jack Elston for pointing this one out!
	    
	    //setRate((byte) 7); //7 Set the sample rate to 1000Hz - 8kHz/(7+1) = 1000Hz
	    setRate((byte)4); // 200 Hz
	    //setDLPFMode((byte) 0); // Gyroscope Output Rate = 8kHz when the DLPF is disabled
	    setDLPFMode((byte) MPU6050_DLPF_BW_42);
	    LCD.drawString("Rate= " + getRate() + "       ", 0, 1);
	    LCD.drawString("DLPF= " + getDLPFMode() + " ", 0, 2);
	    
	    resetGyro();
	    
	    Delay.msDelay(100); // Wait for sensor to stabilize
    }
    
    
    /**
     * Reset the gyro angle
     */
    public void resetGyro()
    {
       angle = 0.0;
       lastGetAngleTime = 0;
       calcOffset();
    }
    public void calcOffset()
    {
       lastOffset = 0;
       double offsetTotal = 0;
       LCD.drawString("Calibrating Gyro", 0, 2);
       int npts=500;
       for (int i = 0; i < npts; i++) {
          offsetTotal += (double) (getRotationX() / GYROSCOPE_SENSITIVITY);
          try {
             Thread.sleep(4);
          } catch (InterruptedException e) {
          }
       }
//       while (!Button.ENTER.isDown()) {
          lastOffset = Math.ceil(offsetTotal / npts) + 1;
//          LCD.drawString("Calibration Done", 0, 4);
//          LCD.drawString("offset: " + lastOffset, 2, 5);
//          LCD.drawString("Press Enter", 1, 6);
//       }
       try {
          Thread.sleep(500);
       } catch (InterruptedException e) {
       }
    }
    
    private double getAngleOffset()
    {
       double offset = lastOffset * a + (1.0 - a) * (getRotationX() / GYROSCOPE_SENSITIVITY);
       lastOffset = offset;
       return offset;
    }
    
    public double getAngleVelocity()
    {
       double offset = getAngleOffset();
       
       return (double) (getRotationX() / GYROSCOPE_SENSITIVITY) - offset;
       
       //return (double) port.readValue() - offset;
    }
    
    // calculate the angle by direct integration
//    public double getAngle() {
//    	  int now = (int) System.currentTimeMillis();
//          int delta_t = now - lastGetAngleTime;
//
//          // Make sure we only add to the sum when there has actually
//          // been a previous call (delta_t == now if its the first call).
//          if (delta_t != now) {
//        	  angle += getAngleVelocity() * ((double) delta_t / 1000.0);
//          }
//          lastGetAngleTime = now;
//
//          return angle;
//    }
    
    // calculate the angle using complementarity filter
    public double getAngle() {
		int [] accData = new int[3];
		int [] gyrData = new int[3];
		float [] pitch = new float[1];
		float [] roll = new float[1];
		
		pitch[0]=(float) angle;
		//Delay.msDelay((long) (10 - (System.nanoTime()-timer)/1e6));
        getMotion6(accData, gyrData);
        ComplementaryFilter(accData, gyrData, pitch, roll);
        angle = pitch[0];
        return angle;
    }
    
    private void ComplementaryFilter(int [] accData, int [] gyrData, float [] pitch, float [] roll) {       	
        double pitchAcc, rollAcc;               
  	  int now = (int) System.currentTimeMillis();
      int dt = (now - lastGetAngleTime);
      
      if (dt != now) {
        // Integrate the gyroscope data -> int(angularSpeed) = angle
        //double dt = (double) (System.nanoTime()-timer) * 1e-9;
        pitch[0] += ((float)gyrData[0] / GYROSCOPE_SENSITIVITY) * dt/1000.; // Angle around the X-axis
        roll[0] -= ((float)gyrData[1] / GYROSCOPE_SENSITIVITY) * dt/1000.;    // Angle around the Y-axis
     
        // Compensate for drift with accelerometer data if !bullshit
        // Sensitivity = -2 to 2 G at 16Bit -> 2G = 32768 && 0.5G = 8192
        int forceMagnitudeApprox = Math.abs(accData[0]) + Math.abs(accData[1]) + Math.abs(accData[2]);
        if (forceMagnitudeApprox > 8192 && forceMagnitudeApprox < 32768)
        {
    	// Turning around the X axis results in a vector on the Y-axis
            pitchAcc = Math.atan2((double)accData[1], (double)accData[2]) * 180 / Math.PI;
            pitch[0] = (float) (pitch[0] * 0.98 + pitchAcc * 0.02);
     
    	// Turning around the Y axis results in a vector on the X-axis
            rollAcc = Math.atan2((double)accData[0], (double)accData[2]) * 180 / Math.PI;
            roll[0] = (float) (roll[0] * 0.98 + rollAcc * 0.02);
        }
      }
        lastGetAngleTime = now;
    } 
                    
        private void setClockSource(byte source) {
        	I2C.sendBits(MPU6050_RA_PWR_MGMT_1, MPU6050_PWR1_CLKSEL_BIT, MPU6050_PWR1_CLKSEL_LENGTH, source);
        }
        
        private void setFullScaleGyroRange(byte range) {
        	I2C.sendBits(MPU6050_RA_GYRO_CONFIG, MPU6050_GCONFIG_FS_SEL_BIT, MPU6050_GCONFIG_FS_SEL_LENGTH, range);
        }
        
        private void setFullScaleAccelRange(byte range) {
        	I2C.sendBits(MPU6050_RA_ACCEL_CONFIG, MPU6050_ACONFIG_AFS_SEL_BIT, MPU6050_ACONFIG_AFS_SEL_LENGTH, range);
        }
        private void setSleepEnabled(boolean enabled) {
        	I2C.sendBit(MPU6050_RA_PWR_MGMT_1, MPU6050_PWR1_SLEEP_BIT, enabled?1:0);
        }
        
        private boolean testConnection() {
        	return getDeviceID() == 0x34;
        }
        
        private byte getDeviceID() {
    		byte[] buffer=new byte[1];
            //super.getData(MPU6050_RA_WHO_AM_I, buffer, MPU6050_WHO_AM_I_BIT, MPU6050_WHO_AM_I_LENGTH);
    		I2C.getBits(MPU6050_RA_WHO_AM_I,MPU6050_WHO_AM_I_BIT, MPU6050_WHO_AM_I_LENGTH,buffer);
    		//LCD.drawInt(buffer[0],5,5); Delay.msDelay(2000);
            return buffer[0];
        }
        
        
        
        
        private int getDLPFMode() {
        	byte [] buffer = new byte[1];
            int count = I2C.getBits(MPU6050_RA_CONFIG, MPU6050_CFG_DLPF_CFG_BIT, MPU6050_CFG_DLPF_CFG_LENGTH, buffer);
            if(count == 0) {
            	return buffer[0];
            } else {
            	return -1;
            }
    
        }
        
        private void setDLPFMode(byte mode) {
        	I2C.sendBits(MPU6050_RA_CONFIG, MPU6050_CFG_DLPF_CFG_BIT, MPU6050_CFG_DLPF_CFG_LENGTH, mode);
        }
        
        private int getRate() {
        	byte [] buffer = new byte[1];
            int count = I2C.getData(MPU6050_RA_SMPLRT_DIV, buffer,1);
            if(count == 0) {
            return buffer[0];
            } else {
            	return -1;
            }
        }
        private void setRate(byte rate) {
        	I2C.sendData(MPU6050_RA_SMPLRT_DIV, rate);
        }
         
  
        
    
        
        // get rotation and acceleration in one read
        private void getMotion6(int [] accData, int [] gyrData) {
        	byte [] buffer = new byte[14];
        	I2C.getData(MPU6050_RA_ACCEL_XOUT_H, buffer,14);
            accData[0] = (((int)buffer[0]) << 8) | buffer[1];
            accData[1] = (((int)buffer[2]) << 8) | buffer[3];
            accData[2] = (((int)buffer[4]) << 8) | buffer[5];
            gyrData[0] = (((int)buffer[8]) << 8) | buffer[9];
            gyrData[1] = (((int)buffer[10]) << 8) | buffer[11];
            gyrData[2] = (((int)buffer[12]) << 8) | buffer[13];
        }
        
        
        // get rotation
        private void getRotation(int[] x, int[] y, int[] z) {
        	byte[] buffer = new byte[6];
        	I2C.getData(MPU6050_RA_GYRO_XOUT_H, buffer,6);
            x[0] = (((int) buffer[0] ) << 8) | buffer[1];
            y[0] = (((int) buffer[2] ) << 8) | buffer[3];
            z[0] = (((int) buffer[4] ) << 8) | buffer[5];
        }

        private int getRotationX() {
        	byte[] buffer = new byte[2];
        	I2C.getData( MPU6050_RA_GYRO_XOUT_H, buffer,2);
            //return (((int)buffer[0]) << 8) | buffer[1];
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
            //return (short) ((buffer[1]) << 8) | (buffer[0] & 0xFF);
        }
        private int getRotationY() {
        	byte[] buffer = new byte[2];
        	I2C.getData( MPU6050_RA_GYRO_YOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
            //return (short) ((buffer[1]) << 8) | (buffer[0] & 0xFF);
        }  
        
        
        //return (short)((b[off] & 0xFF) | (b[off + 1] << 8));
        
        private int getRotationZ() {
        	byte[] buffer = new byte[2];
        	I2C.getData( MPU6050_RA_GYRO_ZOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }  
        
        // get acceleration
        private void getAcceleration(int[] x, int[] y, int[] z) {
        	byte[] buffer = new byte[6];
        	I2C.getData(MPU6050_RA_ACCEL_XOUT_H, buffer,6);
            x[0] = (((int) buffer[0] ) << 8) | buffer[1];
            y[0] = (((int) buffer[2] ) << 8) | buffer[3];
            z[0] = (((int) buffer[4] ) << 8) | buffer[5];
        }

        private int getAccelerationX() {
        	byte[] buffer = new byte[2];
        	I2C.getData( MPU6050_RA_ACCEL_XOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }
        private int getAccelerationY() {
        	byte[] buffer = new byte[2];
        	I2C.getData( MPU6050_RA_ACCEL_YOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }  
        private int getAccelerationZ() {
        	byte[] buffer = new byte[2];
        	I2C.getData( MPU6050_RA_ACCEL_ZOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }         
        
        
}

