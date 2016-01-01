package thomas.nxt.gelway;

import lejos.nxt.I2CSensor;
import lejos.nxt.SensorPort;

public class RawI2CSensor extends I2CSensor {

    RawI2CSensor(SensorPort p)
    {
        super(p);
    }

        @Override
    public void setAddress(int addr)
    {
        super.address = addr;
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

}
