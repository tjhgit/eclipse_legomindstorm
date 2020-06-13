package thomas.nxt.gelway;

import lejos.nxt.Button;

public class GELway {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CtrlParam ctrl1 = new CtrlParam();
		BalanceController bcthread = new BalanceController(ctrl1);
		bcthread.start();
		
		while(Button.ESCAPE.isUp()) {Thread.yield();};
	}

}
