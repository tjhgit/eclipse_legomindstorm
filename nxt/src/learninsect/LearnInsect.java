//package thomas.nxt.learninsect;
//
//import lejos.nxt.Motor;
//
//public class LearnInsect {
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
////		//SELF LEARNING ROBOT WITH NEURAL NETWORK BRAIN
////
////		//In order to understand what this program is doing
////		//and why, it might be a good idea if you read the
////		//page on neural networks first. I have attempted to
////		//keep this source as readable as possible. That
////		//means the code itself is not optimized. If you need
////		//a larger brain you must by the way use arrays.
////
////		//Declare all variables as public so you can use them
////		//everywhere (needed because the matrix operations
////		//are in subrutines
//		int m1, m2, m3, m4, m5, m6, m7, m8, m9;
//		int m10, m11, m12, m13, m14, m15, m16;
//		int nm1, nm2, nm3, nm4, nm5, nm6, nm7, nm8, nm9;
//		int nm10, nm11, nm12, nm13, nm14, nm15, nm16;
//		int v1, v2, v3, v4, av1, av2, av3, av4;
//		static int MaxSpeed;
//		
//		Motor.A.setSpeed(40);
//		Motor.B.setSpeed(40);
//		Motor.A.forward();
//		Motor.B.forward();
//		
//		
//		
//		
//		public void ResetBrain() {
//
////		//Reset the brain to the starting values in case the
////		//robot learned the wrong behaviour
//		m1 = 1
//		m2 = 1
//		m3 = -1
//		m4 = -1
//		m5 = 1
//		m6 = 1
//		m7 = -1
//		m8 = -1
//		m9 = -1
//		m10 = -1
//		m11 = 1
//		m12 = 1
//		m13 = -1
//		m14 = -1
//		m15 = 1
//		m16 = 1
//
////		//And show it
//		DisplayMatrix()
//
//	}
//
//		void LearnInsect() {
//
////		
////		//Definition of the empty neural network,
////		//where m stands for matrix
////		// ( m1  m2  m3  m4  )
////		// ( m5  m6  m7  m8  )
////		// ( m9  m10 m11 m12 )
////		// ( m13 m14 m15 m16 )
//		m1 = 0
//		m2 = 0
//		m3 = 0
//		m4 = 0
//		m5 = 0
//		m6 = 0
//		m7 = 0
//		m8 = 0
//		m9 = 0
//		m10 = 0
//		m11 = 0
//		m12 = 0
//		m13 = 0
//		m14 = 0
//		m15 = 0
//		m16 = 0
//
////		//We know we want to drive forward if none of the
////		//bumpers is hit, so this can be added to the neural
////		//brain. The vector format (where v stands for vector):
////		//  left bumper     ( v1 )
////		//  right bumper    ( v2 )
////		//  left motor      ( v3 )
////		//  right motor     ( v4 )
////		//and -1 means motor off / sensor not activated and
////		//+1 means motor on / sensor activated.
//		v1 = -1
//		v2 = -1
//		v3 = 1
//		v4 = 1
//		VectorTimesVector();
//		MatrixAddMatrix();
//		DisplayMatrix();
//		
//		}
//
//		//Check the bumper status
//		CheckBumpers(v1, v2)
//		    
//		//Determine the "number" of switches activated
//		NumAct = v1 + v2
//
//		//Check the neural brain for this condition. In order
//		//to do this multiply the memory matrix with this
//		//input vector (v1,v2,?,?) where av is answer vector
//		av1 = m1 * v1 + m2 * v2;
//		av2 = m5 * v1 + m6 * v2;
//		av3 = m9 * v1 + m10 * v2;
//		av4 = m13 * v1 + m14 * v2;
//
//		//Normalise this vector by replacing anything
//		//larger than zero by +1 and anything smaller
//		//that zero by -1
//		if(av1 > 0) av1 = 1;
//		if(av1 < 0) av1 = -1;
//		if(av2 > 0) av2 = 1;
//		if(av2 < 0) av2 = -1;
//		if(av3 > 0) av3 = 1;
//		if(av3 < 0) av3 = -1;
//		if(av4 > 0) av4 = 1;
//		if(av4 < 0) av4 = -1;
//		    
//		//Check wether this sensor status was known to the
//		//neural netwerk by checking wether the inputs in
//		//the answer vector are equal to the inputs in the
//		//question vector. This is a bit tricky, since
//		//sometimes it will yield a false //known// answer
//		//by accident (Compare this with a child that _thinks_
//		//it knows something, but you need to correct that).
//		//In order to accomodate errors like this a special
//		//routine could be included, I haven//t done that.
//		 if( (av1 <> v1) || (av2 <> v2)) {
//
//		    //Advise user a solution could not be found
//		    boolean success = false;
//		    do {
//		        
//		        //Generate a random solution for the two
//		        //motors
//		        double sol1 = Math.random();
//		        double sol2 = Math.random();
//		        if(sol1 > 0.5) v3 = 1; else v3 = -1;
//		        if(sol2 > 0.5) v4 = 1; else v4 = -1;
//
//		        //Try this solution
//		        if(v3 = 1){
//		            Motor.A.forward();
//		        } else {
//		            Motor.A.stop();
//		        }
//		        if(v4 = 1){
//		            Motor.B.forward();
//		        }
//		        else {
//		            Motor.B.stop();
//		        }
//		     //   Form1.Spirit1.Drive motor0, motor1
//		    
////		        //Now figure out wether this is succesfull by
////		        //waiting for a given time so see if the
////		        //bumber status has improved (eg less switches
////		        //activated). The timeloop routine fails at
////		        //midnight rollover by the way, couldn//t be
////		        //bothered to make a real one.
//		        long StartTime = System.currentTimeMillis();
//		        do {
//		       //     //Check the bumper status
//		            CheckBumpers(nv1, nv2)
//		            
////		            //Give windows some time to refresh the
////		            //screen otherwise you won//t see anything
////		       
//		            
//		            if( nv1 + nv2 < NumAct){
//		            
//		             //   //This is succesvol, add to the
//		              //  //neural brain
//		            	VectorTimesVector();
//		            	MatrixAddMatrix();
//		            	DisplayMatrix();
//		                success = true;
//		            }
//		            
////		            //Repeat until 2 seconds have passed (should
////		            //be enough time for the robot to get itself
////		            //out of trouble, if not modify this time
////		            //period) or until as solution has been
////		            //found.
//		        }while( (Timer - StartTime > 2) || (Succes = True));
//		 
////		        //repeat the loop (the random search for a correct
////		        //answer) until one is found
//		        }while ( ssucces = false);
//		    else {
//		    
//		    ////Show the current memory contents
//		    DisplayMatrix();
//		    
//		    ////Execute solution
//		    if(av3 = 1) {
//		     	Motor.A.forward();
//		    } else {
//		        Motor.A.stop();
//		    }
//		    if(av4 = 1) {
//		     	Motor.B.forward();
//		    } else {
//		        Motor.B.stop();
//		    }
//
////		    Form1.Spirit1.Drive motor0, motor1
//		    }	    
//	
//
//	
//		 }
//		void VectorTimesVector() {
//		//Determine the resulting matrix if this vector
//		//is multiplied by itself. The formula of course is
//		//obvious (where nm stands for new matrix):
//		nm1 = v1 * v1
//		nm2 = v1 * v2
//		nm3 = v1 * v3
//		nm4 = v1 * v4
//		nm5 = v2 * v1
//		nm6 = v2 * v2
//		nm7 = v2 * v3
//		nm8 = v2 * v4
//		nm9 = v3 * v1
//		nm10 = v3 * v2
//		nm11 = v3 * v3
//		nm12 = v3 * v4
//		nm13 = v4 * v1
//		nm14 = v4 * v2
//		nm15 = v4 * v3
//		nm16 = v4 * v4
//		}
//
//		void MatrixAddMatrix() {
////		//To add this new knowledge to the neural network this
////		//matrix has to be added to the original one
//		m1 = m1 + nm1
//		m2 = m2 + nm2
//		m3 = m3 + nm3
//		m4 = m4 + nm4
//		m5 = m5 + nm5
//		m6 = m6 + nm6
//		m7 = m7 + nm7
//		m8 = m8 + nm8
//		m9 = m9 + nm9
//		m10 = m10 + nm10
//		m11 = m11 + nm11
//		m12 = m12 + nm12
//		m13 = m13 + nm13
//		m14 = m14 + nm14
//		m15 = m15 + nm15
//		m16 = m16 + nm16
//		}
//
//		void DisplayMatrix() {
////		//Display the memory matrix so you can see what happened
//		Form1.Label1.Caption = m1
//		Form1.Label2.Caption = m2
//		Form1.Label3.Caption = m3
//		Form1.Label4.Caption = m4
//		Form1.Label5.Caption = m5
//		Form1.Label6.Caption = m6
//		Form1.Label7.Caption = m7
//		Form1.Label8.Caption = m8
//		Form1.Label9.Caption = m9
//		Form1.Label10.Caption = m10
//		Form1.Label11.Caption = m11
//		Form1.Label12.Caption = m12
//		Form1.Label13.Caption = m13
//		Form1.Label14.Caption = m14
//		Form1.Label15.Caption = m15
//		Form1.Label16.Caption = m16
//		}
//
//		Public Sub CHECKBUMPERS(vv1, vv2)
////		//check left bumper, show status on screen, and if
////		//v1 is not equal to 1 make it -1
//		vv1 = Form1.Spirit1.Poll(9, 0)
//		If vv1 = 1 Then
//		    Form1.Shape1.FillColor = &HFF&
//		    Form1.Shape2.FillColor = &HC0C0C0
//		Else
//		    vv1 = -1
//		    Form1.Shape2.FillColor = &HFF00&
//		    Form1.Shape1.FillColor = &HC0C0C0
//		End If
//		    
//		//check right bumper, show status on screen, and if
//		//v1 is not equal to 1 make it -1
//		vv2 = Form1.Spirit1.Poll(9, 1)
//		If vv2 = 1 Then
//		    Form1.Shape3.FillColor = &HFF&
//		    Form1.Shape4.FillColor = &HC0C0C0
//		Else
//		    vv2 = -1
//		    Form1.Shape4.FillColor = &HFF00&
//		    Form1.Shape3.FillColor = &HC0C0C0
//		End If
//
//		End Sub
//
//
//	}
//
//}
