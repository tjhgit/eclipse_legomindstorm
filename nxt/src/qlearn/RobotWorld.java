package qlearn;

import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.RConsole;
import lejos.util.Delay;

public class RobotWorld implements RLWorld {
		private UltrasonicSensor sonar;
		private NXTRegulatedMotor m1;
		private NXTRegulatedMotor m2;
		
		private TouchSensor touch1;
	    private TouchSensor touch2;
	    
		private int [] degpersec;
		private int [] startangle;
		private float [] anglestep;
		private float startdistance;
	// Actions.
		final int M1CL = 0;
		final int M2CL = 1;
		final int M1CCL = 2; 
		final int M2CCL = 3;

		// dimension: { x, y, actions }
		final int[] dimSize = { 10, 10, 4 };

		public RobotWorld(SensorPort S, MotorPort M1, MotorPort M2, SensorPort T1, SensorPort T2) {
			sonar = new UltrasonicSensor(S);
			
			
			touch1 = new TouchSensor(T1);
		    touch2 = new TouchSensor(T2);
		    
			m1 = new NXTRegulatedMotor(M1);
			m2 = new NXTRegulatedMotor(M2);
			
			degpersec = new int[2];
			degpersec[0] = 20;
			degpersec[1] = 20;
			
			m1.setSpeed(degpersec[0]);
			m2.setSpeed(degpersec[1]);
			
			int [] tmpangle = new int[2];
			tmpangle[0]= m1.getTachoCount();
			tmpangle[1]= m2.getTachoCount();
			
			// move to init positions
			if(!touch1.isPressed()) {
				m1.backward();
				while(!touch1.isPressed()) Thread.yield();
				m1.stop();
			}

			if(!touch2.isPressed()) {
				m2.forward();
				while(!touch2.isPressed()) Thread.yield();
				m2.stop();
			}

			startangle = new int[2];
			startangle[0]= m1.getTachoCount();
			startangle[1]= m2.getTachoCount();
			
			// one action increment in angle
			anglestep = new float[2];
			anglestep[0] = 30; 
			anglestep[1] = 30;
			
			RConsole.println("RobotWorld constr: moved backward ("+tmpangle[0]+"/"+tmpangle[1]+") -> ("+startangle[0]+"/"+startangle[1]+")");
			RConsole.println("RobotWorld constr: anglestep ("+anglestep[0]+"/"+anglestep[1]+")");
			
			startdistance = getDistance();
		
		}
		
		float getDistance() {
			int navg = 10;
			float dist = sonar.getRange();
			for(int i=0;i<navg-1;i++)
				dist+=sonar.getRange();
			return dist/navg;
		}
		
		public int[] getDimension() {
			return dimSize;
		}

		public int[] getNextState( int[] state, int action ) {

			int[] newstate = new int[state.length] ;
			
			// apply actions to motors and the retrieve new states
			if( action == M1CL )  {
				m1.forward();
				// move relative one step
				Delay.msDelay((int) ((anglestep[0]/(double) degpersec[0])*1000.));
			} else if( action == M2CL ) {
				m2.forward();
				Delay.msDelay((int) ((anglestep[1]/(double) degpersec[1])*1000.));
			}else if( action == M1CCL ){
				m1.backward();
				Delay.msDelay((int) ((anglestep[0]/(double) degpersec[0])*1000.));
			}else if( action == M2CCL ){
				m2.backward();
				Delay.msDelay((int) ((anglestep[1]/(double) degpersec[1])*1000.));
			}
			m1.stop();
			m2.stop();
			
			int tmp1 = (int) ((m1.getTachoCount() - startangle[0])/anglestep[0]);
			newstate[0] = Math.min(Math.max(0, tmp1),dimSize[0]-1); // bound to 0 to dimSize[0]-1
			
			int tmp2 = (int) ((m2.getTachoCount() - startangle[1])/anglestep[1]);
			newstate[1] = Math.min(Math.max(0, tmp2),dimSize[1]-1); // bound to 0 to dimSize[0]-1
			
			
			RConsole.println("getNextState(): w action "+action+" moved ("+state[0]+"/"+state[1]+")-> ("+tmp1+"/"+tmp2+")");
			RConsole.println("getNextState(): w action "+action+" moved ("+state[0]+"/"+state[1]+")-> ("+newstate[0]+"/"+newstate[1]+")");

			return newstate;


		}    

		public boolean validAction( int[] state, int action ) {

			// motor2 at one end of angle range
			if( state[1] == 0 && action == M2CCL )
				return false;
			// motor2 at the other end of angle range
			else if( state[1] == 9 && action == M2CL )
				return false;
			// motor1 at the one end of angle range
			else if( state[0] == 0 && action == M1CCL ) 
				return false;
			// motor1 at the other end of angle range
			else if( state[0] == 9 && action == M1CL )
				return false;
			else return true;
		}

		public boolean endState( int[] state ) {
			return false; // there exists no end state
		}

		public double getReward( int[] state, int action ) {
			float tmp = startdistance; // previous run startdistance
			startdistance = getDistance();
			float diff = startdistance - tmp;
//			float diff = getDistance() -tmp;
			
			if(diff==0)
				diff=0.01f;
			return (-diff);
		}

		public int [] resetState1( int[] state ) {
			
			state = new int[dimSize.length-1];
			for( int j = 0 ; j < dimSize.length-1 ; j++ )
				state[j] = (int) ( Math.random() * dimSize[j] );
			
			
			// move absolute: motor 1 to random position
			int moveangle = (int) (m1.getTachoCount() - (state[0]*anglestep[0]+startangle[0]));	
			if(moveangle<0) {
				m1.forward();
				Delay.msDelay((int) ((Math.abs(moveangle)/(double) degpersec[0])*1000.));
			} else if(moveangle<0) {
				m1.backward();
				Delay.msDelay((int) ((Math.abs(moveangle)/(double) degpersec[0])*1000.));				
			}
			m1.stop();
			RConsole.println("resetState(): SOLL state: "+state[0]+" m1 move to angle "+moveangle);

			
			// move absolute: motor 2 to random position
			moveangle = (int) (m2.getTachoCount() - (state[1]*anglestep[1]+startangle[1]));			
			if(moveangle<0) {
				m2.forward();
				Delay.msDelay((int) ((Math.abs(moveangle)/(double) degpersec[1])*1000.));
			} else if(moveangle<0) {
				m2.backward();
				Delay.msDelay((int) ((Math.abs(moveangle)/(double) degpersec[1])*1000.));				
			}			
			m2.stop();
			RConsole.println("resetState(): SOLL state: "+state[1]+" m2 move to angle "+moveangle);			
			
			int[] newstate = new int[state.length] ;
			newstate[0] = (int) ((m1.getTachoCount() - startangle[0])/anglestep[0]);
			newstate[1] = (int) ((m2.getTachoCount() - startangle[1])/anglestep[1]);
	
			RConsole.println("resetState(): IST state: ("+newstate[0]+"/"+newstate[1]+")");
			
			startdistance = getDistance();
			
			return state;
			
		}
		

		public double getInitValues() {
			return 0;
		}

		@Override
		public void resetState(int[] state) {
			// TODO Auto-generated method stub
			
		}
}
