package redballtracer;



import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingUtilities;
import org.opencv.core.Mat;


public class RedBallPlayerServer {
	//static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	// communication
	private static final int PORT = 55555;
	private ServerSocket ss;
	private Socket sock;
	private BufferedInputStream bis;
	private DataOutputStream bos_data;
	private DataInputStream bis_data;
	private static RedBallDetection rbd;

	// constructor
	RedBallPlayerServer() {
		try {
			ss = new ServerSocket(PORT);
			//creating socket and waiting for client connection
			sock = ss.accept();
			bis = new BufferedInputStream(sock.getInputStream()); // for video data
			bos_data = new DataOutputStream (new BufferedOutputStream(sock.getOutputStream())); // for reply on commands
			bis_data = new DataInputStream (new BufferedInputStream(sock.getInputStream())); // for commands
			System.out.println("Started server!");
		} catch (Exception e) {
			System.err.println("Failed to start server socket: " + e);
			System.exit(1);
		}


	}

	public void close() {
		try {
			if (bis != null) bis.close();
			if (sock != null) sock.close();
			if (ss != null) ss.close();
		} catch (Exception e1) {
			System.err.println("Exception closing window: " + e1);
		}
	}

	// communication - image transfer via socket connection
	private int parseResponse(String str1) throws IOException, EOFException {  	
		int temp;  			
		do {		
			temp = bis_data.readInt();
			System.out.println("Response from client ["+ str1 + "]"+temp);
		} while(temp!=RedBallTraceResponses.ALLOK);
		return bis_data.readInt();		
	}


	private void sendBallPosition(Mat centers) throws IOException {
		bos_data.writeInt(RedBallTraceResponses.ALLOK);

		if(centers.empty()) {
			bos_data.writeInt(0);
		} else {
			bos_data.writeInt(3);
			bos_data.writeFloat((float)centers.get(0,0)[0]); 
			bos_data.writeFloat((float)centers.get(0,1)[0]);
			bos_data.writeFloat((float)centers.get(0,2)[0]);
		}
		bos_data.flush();
		System.out.println("Position of ball sent to client");
	}    

	private byte [] receiveImage() throws IOException {
		synchronized (RedBallPlayerServer.rbd) {    
			byte [] buffer = null;
			try {
				// waiting for client to send image 	
				int bytesToRead = parseResponse("picture");

				System.out.println("Picture bytes: " + bytesToRead);

				buffer = new byte[bytesToRead];  			
				int offset = 0;
				while (offset < bytesToRead) {
					offset += bis.read(buffer, offset, bytesToRead - offset);
				}	
			} catch (EOFException e) {
				System.out.println("client connection broken");
				close();
				System.exit(0);
//			} catch (Exception e) {
//				e.printStackTrace();
			}
			return buffer;
		}	
	}	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		final RedBallPlayerServer svr = new RedBallPlayerServer();
		rbd = new RedBallDetection();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				rbd.createAndShowGUI();	
			}
		});
		
					
		BufferedImage image1 = null;
//		// TEST CODE 1
//		try {
//			
//		    image1 = ImageIO.read(new File("/home/t13147/Dokumente/data/redball98.png"));
//		} catch (IOException e) {
//		}
//		Mat centers = rbd.getRedBallPosition(image1);

		int img_count = 0;
		while(System.in.available() == 0) { // wait until enter pressed
		
			System.out.println("Aquiring picture...");
			
			image1 = rbd.getImageFromYUYV(svr.receiveImage());

			
			rbd.refreshFrameandSave(img_count++);
			
			//svr.panel.repaint(1);
			Mat centers = rbd.getRedBallPosition(image1);
			// send position to client
			try {
				svr.sendBallPosition(centers);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		svr.close();

	}

}
