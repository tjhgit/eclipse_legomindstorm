package redballtracer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class RedBallTraceServer {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int PORT = 55555;
 
    private ServerSocket ss;
    private Socket sock;
    
    private BufferedInputStream bis;
    private DataOutputStream bos_data;
    private DataInputStream bis_data;
    
    private BufferedImage image_bgr;
    
    private CameraPanel panel = new CameraPanel();
    private JFrame frame;
    private Mat yuy2, bgr;
    
    // constructor
	public RedBallTraceServer() { 
        try {
            ss = new ServerSocket(PORT);
            sock = ss.accept();
            bis = new BufferedInputStream(sock.getInputStream()); // for video data
            bos_data = new DataOutputStream (new BufferedOutputStream(sock.getOutputStream())); // for reply on commands
            bis_data = new DataInputStream (new BufferedInputStream(sock.getInputStream())); // for commands
            
        } catch (Exception e) {
            System.err.println("Failed to connect: " + e);
            System.exit(1);
        }
 
        image_bgr = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
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
	
	class CameraPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// Ensure that we don't paint while the image is being refreshed
			synchronized(RedBallTraceServer.this) {
				//g.drawImage(image, 0, 0, null);
				g.drawImage(image_bgr, 0, 0, null);
			}
		}  
	}
	
	public void createAndShowGUI() {
		frame = new JFrame("EV3 Camera View");

		frame.getContentPane().add(panel);
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		frame.pack();
		frame.setVisible(true);
	}
	
    private BufferedImage toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
        return image;

    }
    
    private int parseResponse(String str1) throws IOException {  	
		int temp;  			
		do {
			temp = bis_data.readInt();
			System.out.println("Response from client ["+ str1 + "]"+temp);
		}while(temp!=RedBallTraceResponses.ALLOK);
		return bis_data.readInt();		
    }
    
    private void sendCommand(int command) throws IOException {
		bos_data.writeInt(command);
		bos_data.writeInt(0);
		bos_data.flush();   	
    }
    private void sendCommand(int command, double betrag) throws IOException {
		bos_data.writeInt(command);
		bos_data.writeInt(1);
		bos_data.writeDouble(betrag);
		bos_data.flush();   	
    }    
    
    private void receiveImage() {
    	yuy2 = new Mat(HEIGHT,WIDTH, CvType.CV_8UC2);
    	bgr = new Mat(HEIGHT,WIDTH, CvType.CV_8UC3);

    	synchronized (this) {    		
    		try {
    			int bytesToRead = parseResponse("picture");
    			
    			System.out.println("Picture bytes: " + bytesToRead);
    			
    			byte[] buffer = new byte[bytesToRead];  			
    			int offset = 0;
    			while (offset < bytesToRead) {
    				offset += bis.read(buffer, offset, bytesToRead - offset);
    			}
    			yuy2.put( 0, 0, buffer );
    			Imgproc.cvtColor(yuy2,bgr,Imgproc.COLOR_YUV2BGR_YUYV);// convert YUY2 to BGR	
    			image_bgr = toBufferedImage(bgr);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    	}
    	panel.repaint(1);
    }
	
    
    
    private Mat getNormalizedRGB(Mat rgb) {
		assert(rgb.type() == CvType.CV_8UC3);
		Mat rgb32f = new Mat(); 
		rgb.convertTo(rgb32f, CvType.CV_32FC3);
		
		List<Mat> split_rgb = new ArrayList<Mat>(); 
		
		Core.split(rgb32f, split_rgb);
		Mat sum_rgb = new Mat();
		//Mat sum_rgb = split_rgb.get(0) + split_rgb.get(1) + split_rgb.get(2);
		Core.add(split_rgb.get(0), split_rgb.get(1), sum_rgb);
		Core.add(sum_rgb, split_rgb.get(2), sum_rgb);
		
		Core.divide(split_rgb.get(0),sum_rgb,split_rgb.get(0));
		Core.divide(split_rgb.get(1),sum_rgb,split_rgb.get(1));
		Core.divide(split_rgb.get(2),sum_rgb,split_rgb.get(2));
		
		Core.merge(split_rgb,rgb32f);
		return rgb32f;
	}
    
    private static Mat multiplyScalar(Mat m, double i) {
        return m = m.mul(new Mat((int)m.size().height, (int)m.size().width, m.type(), new Scalar(i)));
    }
    
    private static Mat findCircles1(Mat src) {
    	List<Mat> centers = new ArrayList<Mat>();
    	
    	Mat binary = new Mat();
    	
    	if(src.type() !=  0) { // not a binary image already
    		Mat src_gray = new Mat(); 
    			//System.out.println(src.type());
    			Imgproc.GaussianBlur(src, src,new Size (11,11),3,3);
    			Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_BGR2GRAY );
    			Mat kernel = new Mat();
    			Imgproc.erode(src_gray, binary, kernel);
    			Imgproc.dilate(binary, binary, kernel);
    			Imgproc.Canny(binary, binary, 70, 3);
    			Imgproc.GaussianBlur(binary, binary,new Size (21,21),5,5);
    		//Imgproc.threshold(src_gray,binary,127,255,Imgproc.THRESH_BINARY);
    		//System.out.println(src_gray.type());
    	} else { 		
    		binary = src;   		
    	}
    	Mat hierarchy = new Mat();
    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	Imgproc.findContours(binary,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
    	 	
    	for (MatOfPoint cnt : contours) {
    		MatOfPoint2f approx = new MatOfPoint2f();
    		MatOfPoint2f cnt2f = new MatOfPoint2f( cnt.toArray() );
    		Imgproc.approxPolyDP(cnt2f,approx,0.01*Imgproc.arcLength(cnt2f,true),true);
    		
    		//System.out.println("size approx: "+approx.total());
    		if(approx.total() > 15) { // detected a circle
    			//System.out.println("contours: "+cnt.dump());
    			
    			Mat tmp = new Mat();
    			cnt.convertTo(tmp, CvType.CV_32F, 1.0); 
    			Mat center = new Mat();
    			Mat radius = new Mat();
    			Mat max = new Mat();
    			Mat min = new Mat();
    			List<Mat> tmplist = new ArrayList<Mat>();
    			//System.out.println(tmp.dump());
    			//System.out.println(tmp.type());
    			Core.reduce(tmp,center,0,Core.REDUCE_AVG);
    			Core.reduce(tmp,max,0,Core.REDUCE_MAX);
    			Core.reduce(tmp,min,0,Core.REDUCE_MIN);
    			Core.subtract(max, min, radius);
    			
    			radius = multiplyScalar(radius, 0.5); // why does this generate a zero in the 2nd Mat element??
    			
    			tmplist.add(center);
    			tmplist.add(radius);
    			//System.out.println(Arrays.toString(tmplist.toArray()));
    			Core.hconcat(tmplist, tmp);
    			centers.add(tmp);   			
    			//System.out.println("contours: "+centers.dump());
    		}
    	}
    	//System.out.println(Arrays.toString(centers.toArray()));
    	Mat centerMat = new Mat();
    	Core.hconcat(centers,centerMat);
    	//System.out.println("contours: "+centerMat.dump());
    	return centerMat;
    }
    
    private static Mat findCircles(Mat src) {
    	Mat circles = new Mat();
    	Mat src_gray = new Mat();
    	int[] params;
    	if(src.type() !=  0) {
    		params = new int[]{1,80,20};
    		//System.out.println(src.type());
    		Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_BGR2GRAY );
    		//System.out.println(src_gray.type());
    	} else {
    		params = new int[]{2,30,20};
    		src_gray = src;   		
    	}
    	//System.out.println(src_gray.dump());
    	int smallestDim = (WIDTH<HEIGHT)?WIDTH:HEIGHT;
    	Imgproc.HoughCircles( src_gray, circles, Imgproc.CV_HOUGH_GRADIENT , params[0], src_gray.rows()/8, params[1], params[2], (int) (smallestDim/10.) , (int) (smallestDim/2.) );
    	return circles;
    }
    
	private static Mat findOneCenter(Mat binMat) {
    	Mat wLocMat = Mat.zeros(binMat.size(), binMat.channels()); 
    	Core.findNonZero(binMat, wLocMat);  	
    	Mat samples32f = new Mat();
    	wLocMat.convertTo(samples32f, CvType.CV_32F, 1.0);  	
    	Mat center = new Mat();
    	Core.reduce(samples32f,center,0,Core.REDUCE_AVG);    	
 //   	System.out.println("centers:" + center.dump());  	
    	return center;
    }
	
    // works only for red ball
    private Mat getBallPosition() {
    	Mat centers;
    	
		centers = findCircles1(bgr);
		System.out.println("centers/radii contours bgr:"+centers.dump());
		
		// CONVERT red to BINARY mask
	    Mat hsv=new Mat(); 
	    Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV);
	    //get Normalized RGB (aka rgb)
	    Mat nbgr = getNormalizedRGB(bgr);         
	    //take the pixels that are inside the ranges in both colorspaces
	    Mat mask_hsv = new Mat(); 
	    Mat mask_nbgr = new Mat();
	    Mat outputmask = new Mat();
	    //H=[0,50], S= [0.20,0.68] and V= [0.35,1.0]
	    Core.inRange(hsv, new Scalar(0,0.56*255.0,0.35*255.0), new Scalar(10.0*255./360.,1.0*255.0,1.0*255.0), mask_hsv);
	    //r = [0.36,0.465], g = [0.28,0.363]
	    Core.inRange(nbgr, new Scalar(0.0,0.0,0.8), new Scalar(0.2,0.2,1.0), mask_nbgr); // detect red
	    //combine the masks
	    Core.bitwise_and(mask_hsv, mask_nbgr, outputmask);
	    
	    // get the center of the red mask
	    centers = findOneCenter(outputmask);
	    System.out.println("centers avg:" + centers.dump());
	    centers = findCircles(outputmask);
		System.out.println("centers hough bin:" + centers.dump());		
		centers = findCircles1(outputmask);
		System.out.println("centers/radii contours bin:"+centers.dump());
    	
 //   	System.out.println("centers:" + center.dump());  	
    	return centers;
	    
    }
    
	public static void main(String[] args) {
		final RedBallTraceServer svr = new RedBallTraceServer();
		
		// set-up GUI
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				svr.createAndShowGUI();
			}
		});
		
		try {
			System.out.println("Aquiring picture");
			svr.sendCommand(RedBallTraceCommands.PICTURE);
			svr.receiveImage();
			//svr.getBallPosition();
			
			System.out.println("Sending move command");		
			svr.sendCommand(RedBallTraceCommands.STRAIGHT, 300);
			svr.parseResponse("straight");
		
//			System.out.println("Sending move command");
//			svr.sendCommand(RedBallTraceCommands.ROTATE, 1000);
//			svr.parseResponse("rotate");
						
			System.out.println("Aquiring picture");
			svr.sendCommand(RedBallTraceCommands.PICTURE);
			svr.receiveImage();
			//svr.getBallPosition();
			
			File outputfile = new File("/home/thomas/Dokumente/data/redball.png");
		    ImageIO.write(svr.image_bgr, "png", outputfile);
			
			System.out.println("All done. Thanx");
			
		} catch (IOException e)  {
			e.printStackTrace();
		}
		
		svr.close();
		

	}

}
