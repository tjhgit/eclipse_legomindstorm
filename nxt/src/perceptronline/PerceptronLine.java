package thomas.nxt.perceptronline;

import java.io.File;

import lejos.nxt.Button;
import lejos.nxt.ColorSensor;

import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.util.Delay;

public class PerceptronLine {

	double[] weights;
	int N=0;//N=number of inputs/weights
	double alpha=0.0;//alpha = learning rate
	double threshold=0.0;//threshold = threshold value
	double error_threshold=0.0;//threshold = threshold value    
	double[][] weight_history;
	int HISTORYSIZE = 5, historyctr=0;
	int currentError = 0;
	int currentSetError = 5;
	static boolean DIAG =false;
	static int MAXRUNS = 10000;


	// Perceptron(3 arguments) - constructor 
	public PerceptronLine(int N, double alpha, double threshold){
		this.N=N;
		this.alpha=alpha;
		this.threshold=threshold;
		weights = new double[N];
		for (int i=0;i<N;i++){
			weights[i]=Math.random() - 0.5;
		}
		weight_history=new double[HISTORYSIZE][N];
	}

	 //Perceptron(no arguments) - constructor for a blank perceptron
	
	public PerceptronLine(){
		this.N=0;
		this.alpha=0;
		this.threshold=0;
		weights = new double[0];
		weight_history=new double[HISTORYSIZE][0];
	}


	 //Perceptron(4 arguments) - constructor 
	public PerceptronLine(int N, double alpha, double threshold,double[] weights){
		this.N=N;
		this.alpha=alpha;
		this.threshold=threshold;
		this.weights=weights;
		weight_history=new double[HISTORYSIZE][N];
	}

	 //Y()- gives output from the perceptron, 1 if it will fire, 0 if it won't
	public int Y(int[] input){
		return Step(X(input));
	}

	 //Step() - returns a 1 if passed value x is above threshold and 0 otherwise
	public int Step(double x){
		if (x > threshold)
			return 1;
		else
			return -1; //0
	}

	// X() - returns the sum of all input and weight pairs
	private double X(int[] input){
		double returnvalue=0;
		for (int i=0;i<N;i++){
			returnvalue += ((double)input[i])*weights[i];
		}
		return returnvalue;
	}


	 //trainOne() - trains the Perceptron on one set of inputs and their desired
	// output.
	public void trainOne(int[] input, int output_actual, int output_desired){
		double[] newweights = new double[N];
		for (int i=0; i<N; i++){
			newweights[i]=weights[i] + 
					alpha*(output_desired- output_actual)*input[i];
		}
		currentError =  output_desired - output_actual;
		currentSetError += Math.abs(currentError);
		setWeights(newweights);

	}
    
	// writeConfiguration() - prints out the configuration of this perceptron to
	 //Standard Output.
	public void writeConfiguration(){   
		System.out.print(N + " ");
		System.out.print(alpha + " ");
		System.out.print(threshold + " ");
		for (int i=0;i<N;i++)
			System.out.print(weights[i] + " ");
		System.out.print("\n"); 
	}


	 //setWeights() - replace the weight matrix of the Perceptron
	private void setWeights(double[] weights){
		if (weights.length == N){
			weight_history[(historyctr++) % HISTORYSIZE] = this.weights;
			this.weights = weights;         
		}
		else
			System.err.println("ERROR:invalid Weight list used for update");
	}


	 //setThreshold() - set the Threshold value of this perceptron
	public void setThreshold(double threshold){
		this.threshold=threshold;
	}
	 //setLearningRate() - set the learning rate of this Perceptron
	public void setLearningRate(double alpha){
		this.alpha=alpha;
	}
	 //getError() - returns the sum of all errors in last epoch
	public int getError(){
		return currentSetError;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File sGreen = new File("Green.wav");
		File sPlease = new File("Please.wav");
//		File sYes = new File("Yes.wav");
//		File sBlue = new File("Blue.wav");
//		File sRed = new File("Red.wav");
		File sYellow = new File("Yellow.wav");
			
						
		ColorSensor light = new ColorSensor(SensorPort.S1);
		TouchSensor touchYES = new TouchSensor(SensorPort.S2);		
	    TouchSensor touchNO = new TouchSensor(SensorPort.S3);

		int nn = 4;
		PerceptronLine prcl = new PerceptronLine(nn, 0.5, 0.0); // 4 weights
		int [] xvec = new int[nn];
		xvec[0]=1;


		while(Button.ESCAPE.isUp()) {
			//xvec[1]=light.getLightValue();
			int dur = Sound.playSample(sPlease,100);
			Delay.msDelay(dur);
			while(Button.ENTER.isUp())
				Delay.msDelay(100);
			
			xvec[1] = light.getRawColor().getRed();
			xvec[2] = light.getRawColor().getGreen();
			xvec[3] = light.getRawColor().getBlue();
			
			int output = prcl.Y(xvec); // +1 or -1
			if(output>0) {
				dur = Sound.playSample(sYellow,100);
				Delay.msDelay(dur);
			} else {
				dur = Sound.playSample(sGreen,100);
				Delay.msDelay(dur);
			}
			int desired = output;
			
			while(true) {
				if(touchNO.isPressed()) {
					desired = -output;
					break;
				} else if(touchYES.isPressed()) {
					desired = output;
					break;
				}
				Delay.msDelay(100);
			}
			
//			long time1 = System.nanoTime();			
//			while(System.nanoTime()-time1<2e6) {
//				if(Button.ENTER.isDown()) {
//					desired = -output; // robot chose the wrong action
//					break;
//				}
//			}
			prcl.trainOne(xvec, output, desired);
		}

	}

}


