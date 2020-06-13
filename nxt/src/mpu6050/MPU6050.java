package mpu6050;


import lejos.nxt.Button;
import lejos.nxt.I2CSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.util.Delay;

/**
 * Display the type and address of any I2C sensors attached to the NXT
 * @author andy
 */
public class MPU6050 {
	
	// https://github.com/jrowberg/i2cdevlib/tree/master/Arduino/MPU6050
	public static final int MPU6050_RA_PWR_MGMT_1 = 0x6B;
	public static final int MPU6050_RA_CONFIG = 0x1A;
	public static final int MPU6050_RA_GYRO_CONFIG = 0x1B;
	public static final int MPU6050_RA_GYRO_XOUT_H = 0x43;
	public static final int MPU6050_RA_GYRO_YOUT_H = 0x45;
	public static final int MPU6050_RA_GYRO_ZOUT_H = 0x47;
	public static final int MPU6050_RA_ACCEL_XOUT_H = 0x3B;
	public static final int MPU6050_RA_ACCEL_YOUT_H = 0x3D;
	public static final int MPU6050_RA_ACCEL_ZOUT_H = 0x3F;
	
	public static final int MPU6050_RA_SMPLRT_DIV = 0x19;
	
	public static final int MPU6050_RA_WHO_AM_I = 0x75;
	public static final int MPU6050_RA_ACCEL_CONFIG = 0x1C;
	
	public static final int MPU6050_WHO_AM_I_LENGTH = 6;
	public static final int MPU6050_WHO_AM_I_BIT = 6;
	
	public static final int MPU6050_PWR1_DEVICE_RESET_BIT = 7;
	public static final int MPU6050_PWR1_SLEEP_BIT = 6;
	public static final int MPU6050_PWR1_CLKSEL_BIT =  2;
	public static final int MPU6050_PWR1_CLKSEL_LENGTH = 3;
	public static final int MPU6050_GCONFIG_FS_SEL_BIT  =  4;
	public static final int MPU6050_GCONFIG_FS_SEL_LENGTH =  2;
	public static final int MPU6050_ACONFIG_AFS_SEL_BIT = 4;
	public static final int MPU6050_ACONFIG_AFS_SEL_LENGTH = 2;
	public static final byte MPU6050_CLOCK_PLL_XGYRO = 0x01;
	
	public static final int MPU6050_CFG_DLPF_CFG_BIT = 2;
	public static final int MPU6050_CFG_DLPF_CFG_LENGTH =3;
	
	
	public static final byte MPU6050_GYRO_FS_250 = 0x00;
	public static final byte MPU6050_GYRO_FS_500 = 0x01;
	public static final byte MPU6050_GYRO_FS_1000 = 0x02;
	public static final byte MPU6050_GYRO_FS_2000 = 0x03;
	
	public static final byte MPU6050_ACCEL_FS_2 = 0x00;
	public static final byte MPU6050_ACCEL_FS_4 = 0x01;
	public static final byte MPU6050_ACCEL_FS_8 = 0x02;
	public static final byte MPU6050_ACCEL_FS_16 = 0x03;
	
	
    public static double ACCELEROMETER_SENSITIVITY = 8192.0;
    public static double GYROSCOPE_SENSITIVITY = 65.536;
    //public static double dt =  0.01;							// 10 ms sample rate!    
	
    static class RawI2CSensor extends I2CSensor {

        RawI2CSensor(SensorPort p)
        {
            super(p);
        }

        @Override
        public void setAddress(int addr)
        {
            super.address = addr;
        }

        public void setClockSource(byte source) {
        	sendBits(MPU6050_RA_PWR_MGMT_1, MPU6050_PWR1_CLKSEL_BIT, MPU6050_PWR1_CLKSEL_LENGTH, source);
        }
        
        public void setFullScaleGyroRange(byte range) {
            sendBits(MPU6050_RA_GYRO_CONFIG, MPU6050_GCONFIG_FS_SEL_BIT, MPU6050_GCONFIG_FS_SEL_LENGTH, range);
        }
        
        void setFullScaleAccelRange(byte range) {
            sendBits(MPU6050_RA_ACCEL_CONFIG, MPU6050_ACONFIG_AFS_SEL_BIT, MPU6050_ACONFIG_AFS_SEL_LENGTH, range);
        }
        void setSleepEnabled(boolean enabled) {
            sendBit(MPU6050_RA_PWR_MGMT_1, MPU6050_PWR1_SLEEP_BIT, enabled?1:0);
        }
        
        public boolean testConnection() {
        	return getDeviceID() == 0x34;
        }
        
        public byte getDeviceID() {
    		byte[] buffer=new byte[1];
            //super.getData(MPU6050_RA_WHO_AM_I, buffer, MPU6050_WHO_AM_I_BIT, MPU6050_WHO_AM_I_LENGTH);
    		getBits(MPU6050_RA_WHO_AM_I,MPU6050_WHO_AM_I_BIT, MPU6050_WHO_AM_I_LENGTH,buffer);
    		//LCD.drawInt(buffer[0],5,5); Delay.msDelay(2000);
            return buffer[0];
        }
        
        public int getBit(int register, int bitNum, int buf) {
		    byte [] buf1 = new byte[1];
		    int count = super.getData(register, buf1,1);
		    buf =  buf1[0] & (1 << bitNum);
		    return count;
        }
        
        public int getBits(int register, int bitStart, int len, byte [] buf) {
            int count;
            byte [] buf1 = new byte[1];
            if ((count = super.getData(register, buf1, 1)) == 0) {          	
                int mask = ((1 << len) - 1) << (bitStart - len + 1);
                buf1[0] &= mask;
                buf1[0] >>= (bitStart - len + 1);
                buf[0] = buf1[0];
            }
            return count;
        }
        
        public int sendBit(int register, int bitNum, int data){
        	byte [] buf = new byte[1];
        	super.getData(register, buf, 1);
        	int buf1 = (data != 0) ? (buf[0] | (1 << bitNum)) : (buf[0] & ~(1 << bitNum));
        	return super.sendData(register,(byte) buf1);
        }
        
        public int sendBits(int regAddr, int bitStart, int length, byte data) {

            byte [] b = new byte[1];
            if (super.getData(regAddr, b,1) == 0) {
                int mask = ((1 << length) - 1) << (bitStart - length + 1);
                data <<= (bitStart - length + 1); // shift data into correct position
                data &= mask; // zero all non-important bits in data
                b[0] &= ~(mask); // zero all important bits in existing byte
                b[0] |= data; // combine data with existing byte
                return super.sendData(regAddr, b[0]);
            } else {
                return -1;
            }
        }
        
        
        public int getDLPFMode() {
        	byte [] buffer = new byte[1];
            int count = getBits(MPU6050_RA_CONFIG, MPU6050_CFG_DLPF_CFG_BIT, MPU6050_CFG_DLPF_CFG_LENGTH, buffer);
            if(count == 0) {
            	return buffer[0];
            } else {
            	return -1;
            }
    
        }
        
        public void setDLPFMode(byte mode) {
            sendBits(MPU6050_RA_CONFIG, MPU6050_CFG_DLPF_CFG_BIT, MPU6050_CFG_DLPF_CFG_LENGTH, mode);
        }
        
        public int getRate() {
        	byte [] buffer = new byte[1];
            int count = super.getData(MPU6050_RA_SMPLRT_DIV, buffer,1);
            if(count == 0) {
            return buffer[0];
            } else {
            	return -1;
            }
        }
        public void setRate(byte rate) {
            super.sendData(MPU6050_RA_SMPLRT_DIV, rate);
        }
         
        void ComplementaryFilter(int [] accData, int [] gyrData, float [] pitch, float [] roll, long timer)
        {       	
            double pitchAcc, rollAcc;               
         
            // Integrate the gyroscope data -> int(angularSpeed) = angle
            double dt = (double) (System.nanoTime()-timer) * 1e-9;
            pitch[0] += ((float)gyrData[0] / GYROSCOPE_SENSITIVITY) * dt; // Angle around the X-axis
            roll[0] -= ((float)gyrData[1] / GYROSCOPE_SENSITIVITY) * dt;    // Angle around the Y-axis
         
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
        
        void calcAngle(int [] gyrData, float [] pitch, long timer) {
        	
        	double gyroXrate = (double)gyrData[0] / GYROSCOPE_SENSITIVITY;
        	pitch[0] += gyroXrate*(double) (System.nanoTime()-timer) * 1e-9;
        	//Delay.msDelay(10);
        }
        
        // get rotation and acceleration in one read
        public void getMotion6(int [] accData, int [] gyrData) {
        	byte [] buffer = new byte[14];
            super.getData(MPU6050_RA_ACCEL_XOUT_H, buffer,14);
            accData[0] = (((int)buffer[0]) << 8) | buffer[1];
            accData[1] = (((int)buffer[2]) << 8) | buffer[3];
            accData[2] = (((int)buffer[4]) << 8) | buffer[5];
            gyrData[0] = (((int)buffer[8]) << 8) | buffer[9];
            gyrData[1] = (((int)buffer[10]) << 8) | buffer[11];
            gyrData[2] = (((int)buffer[12]) << 8) | buffer[13];
        }
        
        
        // get rotation
        void getRotation(int[] x, int[] y, int[] z) {
        	byte[] buffer = new byte[6];
            super.getData(MPU6050_RA_GYRO_XOUT_H, buffer,6);
            x[0] = (((int) buffer[0] ) << 8) | buffer[1];
            y[0] = (((int) buffer[2] ) << 8) | buffer[3];
            z[0] = (((int) buffer[4] ) << 8) | buffer[5];
        }

        int getRotationX() {
        	byte[] buffer = new byte[2];
            super.getData( MPU6050_RA_GYRO_XOUT_H, buffer,2);
            //return (((int)buffer[0]) << 8) | buffer[1];
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
            //return (short) ((buffer[1]) << 8) | (buffer[0] & 0xFF);
        }
        int getRotationY() {
        	byte[] buffer = new byte[2];
            super.getData( MPU6050_RA_GYRO_YOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
            //return (short) ((buffer[1]) << 8) | (buffer[0] & 0xFF);
        }  
        
        
        //return (short)((b[off] & 0xFF) | (b[off + 1] << 8));
        
        int getRotationZ() {
        	byte[] buffer = new byte[2];
            super.getData( MPU6050_RA_GYRO_ZOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }  
        
        // get acceleration
        void getAcceleration(int[] x, int[] y, int[] z) {
        	byte[] buffer = new byte[6];
            super.getData(MPU6050_RA_ACCEL_XOUT_H, buffer,6);
            x[0] = (((int) buffer[0] ) << 8) | buffer[1];
            y[0] = (((int) buffer[2] ) << 8) | buffer[3];
            z[0] = (((int) buffer[4] ) << 8) | buffer[5];
        }

        int getAccelerationX() {
        	byte[] buffer = new byte[2];
            super.getData( MPU6050_RA_ACCEL_XOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }
        int getAccelerationY() {
        	byte[] buffer = new byte[2];
            super.getData( MPU6050_RA_ACCEL_YOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }  
        int getAccelerationZ() {
        	byte[] buffer = new byte[2];
            super.getData( MPU6050_RA_ACCEL_ZOUT_H, buffer,2);
            return (short) ((buffer[0]) << 8) | (buffer[1] & (byte) 0xFF);
        }         
        
        
    }


	public static void main(String[] args) throws Exception {
		RawI2CSensor[] sensors = {
				new RawI2CSensor(SensorPort.S3)};
		
		int waittime = 1000;
		LCD.drawString("setting address",0,0);
		Thread.sleep(waittime);
//		sensors[0].setAddress(208);
		sensors[0].setAddress(0x68<<1);
//		sensors[0].setAddress(0b11010010);
		
		LCD.drawString(sensors[0].testConnection()?"success":"fail",5,1);
		//LCD.drawInt(sensors[0].getDeviceID(),5,1);
		Thread.sleep(waittime);
			
//		byte[] buf1 = new byte[1];
//		int ret = sensors[0].getData(MPU6050_RA_WHO_AM_I,buf1,1);
//		LCD.drawInt(buf1[0],5,1);
//		Thread.sleep(waittime);
//		
//		// 1000 0000 -> reset bit high
//		LCD.drawString("send data1",0,0);
//		ret = sensors[0].sendBit(MPU6050_RA_PWR_MGMT_1,MPU6050_PWR1_DEVICE_RESET_BIT,1);
//		LCD.drawInt(ret,5,2);
//		Thread.sleep(30);
//		
//		// 0000 0011
//		ret = sensors[0].sendBit(MPU6050_RA_PWR_MGMT_1,MPU6050_PWR1_SLEEP_BIT,0);
//		ret = sensors[0].sendData(MPU6050_RA_CONFIG,(byte) 0x00);
//		// 0001 1000
//		ret = sensors[0].sendData(MPU6050_RA_GYRO_CONFIG,(byte) 0x18);

		
	    sensors[0].setClockSource(MPU6050_CLOCK_PLL_XGYRO);
	    sensors[0].setFullScaleGyroRange(MPU6050_GYRO_FS_250); // most sensitive
//	    sensors[0].setFullScaleGyroRange(MPU6050_GYRO_FS_2000);
	    sensors[0].setFullScaleAccelRange(MPU6050_ACCEL_FS_2);// most sensitive
	    //sensors[0].setFullScaleAccelRange(MPU6050_ACCEL_FS_16);
	    
	    sensors[0].setSleepEnabled(false); // thanks to Jack Elston for pointing this one out!
	    
	    
	    // this piece of code does not change much
	    sensors[0].setRate((byte) 7); // Set the sample rate to 1000Hz - 8kHz/(7+1) = 1000Hz
	    sensors[0].setDLPFMode((byte) 0); // Gyroscope Output Rate = 8kHz when the DLPF is disabled
	    LCD.drawString("Rate= " + sensors[0].getRate() + "       ", 0, 1);
	    LCD.drawString("DLPF= " + sensors[0].getDLPFMode() + " ", 0, 2);
	    
//		LCD.drawString("sx:", 2, 2);
//		LCD.drawString("sy:", 2, 3);
//		LCD.drawString("ax:", 2, 4);
//		LCD.drawString("ay:", 2, 5);
		
//		int angleX = 0;
//		int angleY = 0;
		
	    Delay.msDelay(100); // Wait for sensor to stabilize
	    
		int [] accData = new int[3];
		int [] gyrData = new int[3];
		float [] pitch = new float[1];
		float [] roll = new float[1];
		
		long timer = System.nanoTime();
		while(!Button.ESCAPE.isDown()){
			
//			//sensors[0].getData(MPU6050_RA_GYRO_XOUT_H,data,6);
//			//int angle_speed = (((data[0]<<8) | data[1])) - angle_speed0;
//			int angle_speedX = sensors[0].getRotationX()/50;
//			int angle_speedY = sensors[0].getRotationY()/50;
////			LCD.drawInt(angle_speedX,6,6,2);
////			LCD.drawInt(angle_speedY,6,6,3);
////			
////			int angle_accX = sensors[0].getAccelerationX();
////			int angle_accY = sensors[0].getAccelerationY();
////			LCD.drawInt(angle_accX,6,6,4);
////			LCD.drawInt(angle_accY,6,6,5);
//			
//			
//	          //calculate angle
//	          angleX += angle_speedX;
//	          angleY += angle_speedY;
//	          LCD.drawString("AngleX = " + angleX + " ", 0, 3);
//	          LCD.drawString("AngleY = " + angleY + " ", 0, 4);
////			Thread.sleep(100);
	          
	          
	          sensors[0].getMotion6(accData, gyrData);
	          sensors[0].ComplementaryFilter(accData, gyrData, pitch, roll,timer);
	          timer = System.nanoTime();
	          LCD.drawString("pitch = " + (int) pitch[0] + " ", 0, 3);
	          LCD.drawString("roll = " + (int) roll[0] + " ", 0, 4);	       
//	          sensors[0].calcAngle(gyrData, pitch,timer);
//	          //LCD.drawString("time = " +(System.nanoTime()-timer)*1e-9 + " ", 0, 3);
//	          timer = System.nanoTime();
//	          LCD.drawString("pitch = " + pitch[0] + " ", 0, 3);
	          
	          
	          
		}
		
//		String sensorType = "";
//		sensorType = sensors[0].getProductID();
//		LCD.drawString(sensorType, 5,6);
//		Button.ESCAPE.waitForPress();
		
	}

}
