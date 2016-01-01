package redballtracer;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.video.Video;
import lejos.robotics.EncoderMotor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

// The client is the EV3


public class RedBallTraceClient {
	private static final int WIDTH = 160;
	private static final int HEIGHT = 120;
	private static final String SERVER = "10.0.1.2"; // via bluetooth using pand
	private static final int PORT = 55555;

	private static RegulatedMotor m_links = new EV3LargeRegulatedMotor(MotorPort.A);
	private static RegulatedMotor m_rechts = new EV3LargeRegulatedMotor(MotorPort.D);

	
    private Socket sock;
    private BufferedOutputStream bos;
    private DataOutputStream bos_data;
    private DataInputStream bis_data;
    private byte[] frame;
    private static Video video;
    
	public RedBallTraceClient() { 
		try {
			EV3 ev3 = (EV3) BrickFinder.getLocal();
			video = ev3.getVideo();

			video.open(WIDTH, HEIGHT);

			frame = video.createFrame();

			sock = new Socket(SERVER, PORT);
			bos = new BufferedOutputStream(sock.getOutputStream()); // for byte transfer of video
			bos_data = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			bis_data = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
			
			
			LCD.drawString("Connected to server", 0,1);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
    public void close() {
        try {            
    		bos.close();
    		bos_data.close();
    		bis_data.close();
    		sock.close();
    		video.close();   
    		
        } catch (Exception e1) {
        	e1.printStackTrace();
        }
    }
    
	private void sendResponse(int response, int resp_len) throws IOException {
		bos_data.writeInt(response);
		bos_data.writeInt(resp_len); // no length
		bos_data.flush();			
	}
	
	public static void main(String[] args) throws IOException  {
		
		RedBallTraceClient client = new RedBallTraceClient();
		
		int message;	
		while(Button.ESCAPE.isUp()) {
			try {
				//while((message = bis_data.readInt() )!=-1 || Button.ESCAPE.isUp());
				do {
					message = client.bis_data.readInt();
					LCD.drawInt(message, 2, 5);
				}while(message ==-1 && Button.ESCAPE.isUp());				
				client.bis_data.readInt();
				
				if(message == RedBallTraceCommands.PICTURE){					
					try {                	
						video.grabFrame(client.frame);

						LCD.drawString("video grabbed", 0, 2);
						
						client.sendResponse(RedBallTraceResponses.ALLOK,WIDTH * HEIGHT*2);
					
						client.bos.write(client.frame);
						client.bos.flush();
						LCD.drawString("video sent", 0, 2);
						
					} catch (IOException e) {
						LCD.drawString("CAM grab Error", 6,2);
					}
				}
				else if(message == RedBallTraceCommands.STRAIGHT){
					m_rechts.setSpeed(30);
					m_links.setSpeed(30);
					

					double howmuch = client.bis_data.readDouble();
					LCD.drawInt((int) howmuch, 0,3);
					m_rechts.rotate((int)howmuch, true);
					m_links.rotate((int)howmuch,true);
					while(m_rechts.isMoving());
					
					client.sendResponse(RedBallTraceResponses.ALLOK,0);
				
				}
				else if(message == RedBallTraceCommands.ROTATE){
					m_rechts.setSpeed(360);
					
					double howmuch = client.bis_data.readDouble();
					LCD.drawInt((int) howmuch, 0,3);
					if(howmuch>0)
						m_rechts.backward();
					else
						m_rechts.forward();
					Delay.msDelay((int) Math.abs(howmuch)); 
					m_rechts.stop();
					
					client.sendResponse(RedBallTraceResponses.ALLOK,0);
						
				}
				else {
					LCD.drawString("Waiting for valid message", 0,3);
					continue; // wait until a parseable command is reached
				}          
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		client.close();

	}

}
