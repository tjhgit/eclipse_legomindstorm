package rmi;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;

public class RmiEx {
	private static RemoteEV3 ev3;
	private static RMIRegulatedMotor motor0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ev3 = new RemoteEV3("10.0.1.1");
			
			if(ev3==null) return;
			System.out.println("Connected !");
			
			if(motor0 == null) motor0 = ev3.createRegulatedMotor("B", 'L');
			motor0.setSpeed(50);
			motor0.rotate(3*360);
			motor0.close();
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
