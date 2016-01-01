package balancebot;

import lejos.nxt.*;

public class BalanceBot {
    ColorSensor light = new ColorSensor(SensorPort.S1);
    int offset;
    int prev_error;
    float int_error;
    
    static int KP;
    static int KI; 
    static int KD; 
    static int SCALE;
    static boolean upBoolLeft = true;
    static boolean upBoolRight = true;
    
    public void getBalancePos() {
        while (!Button.LEFT.isDown()) {
            offset = light.getNormalizedLightValue();
            LCD.clear();
            LCD.drawInt(offset, 4, 2, 2);
            LCD.drawInt(KP, 4, 0, 0);
            LCD.refresh();
        }
    }

    public void pidControl() {
        KP = 59;
        KI = 4;
        KD = 33;
        SCALE = 15;
        LCD.clear();
        while (!Button.ESCAPE.isDown() ) {
            if (Button.LEFT.isDown() && upBoolLeft) {
                KP = KP - 1;
                LCD.drawInt(KP, 4, 0, 0);
                upBoolLeft= false;
            }
            if (Button.RIGHT.isDown() && upBoolRight) {
                KP = KP + 1;
                LCD.drawInt(KP, 4, 0, 0);
                upBoolRight = false;
            } 
            if (Button.LEFT.isUp()) upBoolLeft = true;
            if (Button.RIGHT.isUp()) upBoolRight = true;
        
            int normVal = light.getNormalizedLightValue();
            int error = normVal - offset;
            if (error < 0) error = (int)(error * 1.8F);
            int_error = ((int_error + error) * 2)/3;
            int deriv_error = error - prev_error;
            prev_error = error;
            int pid_val = (int)(KP * error + KI * int_error + KD * deriv_error)/SCALE;
            if (pid_val > 100) pid_val = 100;
            if (pid_val < -100) pid_val = -100;
            int power = Math.abs(pid_val);
            power = 55 + (power * 45)/100;
            Motor.A.setSpeed(power*6);
            Motor.C.setSpeed(power*6);
            if (pid_val > 0) {
                Motor.A.forward();
                Motor.C.forward();
            } else {
                Motor.A.backward();
                Motor.C.backward();
            }
        }
    }

    public static void main(String[] args) {
        BalanceBot bot = new BalanceBot();
        bot.getBalancePos();
        bot.pidControl();
    }
}
