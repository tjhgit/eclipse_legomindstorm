package qlearn;

import lejos.nxt.Button;
import lejos.nxt.MotorPort;
import lejos.nxt.SensorPort;
import lejos.nxt.comm.RConsole;
import lejos.util.Delay;

public class Qlearn {

	static public RLearner learner;
	public static int epochswaiting = 0, epochsdone = 0, totaldone = 0;
	static long delay;
	int UPDATE_EPOCHS = 100;

	public static boolean newInfo;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//GridWorld world = new GridWorld();
		RConsole.open();
	    
		RLWorld world = new RobotWorld(SensorPort.S3,MotorPort.A,MotorPort.B,SensorPort.S1,SensorPort.S4);
		RConsole.println("RLWorld world object created.");
		// create RLearner
		
		learner = new RLearner(world);	
		delay = 100;
		
		Qlearn.setEpisodes(1); // since there is no end state
		
		while(Button.ESCAPE.isUp()) {
			if (epochswaiting > 0) {
				RConsole.println("Running "+epochswaiting+" epochs");
				learner.running = true;
				while(epochswaiting > 0) {
					epochswaiting--;
					epochsdone++;
					learner.runEpoch();

					//if (epochswaiting % UPDATE_EPOCHS == 0) 
					//SwingUtilities.invokeLater(a);
				}
				totaldone += epochsdone;
				epochsdone = 0;
				learner.running = false;

				newInfo = true;

				// inform applet we're finished
				//SwingUtilities.invokeLater(a);
			}

			//sleep(delay);
			Delay.msDelay(delay);
		}
		
	    RConsole.println("\n done ");
	    RConsole.close();
		Delay.msDelay(1000);
	}

	public static void setEpisodes(int episodes) { 
		RConsole.println("Setting "+episodes+" episodes");
		Qlearn.epochswaiting += episodes;
	}
	public void stopLearner() {
		System.out.println("Stopping learner.");
		newInfo = false;
		epochswaiting = 0;
		totaldone += epochsdone;
		epochsdone = 0;

		// inform applet we're finished
		//SwingUtilities.invokeLater(a);

		learner.running = false;
	}

	public synchronized RLPolicy resetLearner() {
		totaldone = 0;
		epochsdone = 0;
		epochswaiting = 0;

		return learner.newPolicy();		
	}

}


