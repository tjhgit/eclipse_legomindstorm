package rmicamrobot;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
//import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;

class RobotControl implements Runnable {
	private Thread t;
	private String threadName;

	private static RemoteEV3 ev3;
	private static RMIRegulatedMotor motor0;
	public RobotControl() {
		try {
			ev3 = new RemoteEV3("10.0.1.1");
			motor0 = ev3.createRegulatedMotor("B", 'L');		

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
	public void run() {
		try {
			System.out.println("Setting motor speed");
			motor0.setSpeed(50);
			motor0.rotate(3*360);
			motor0.close();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	public void start() {
		threadName = "RobotControl";
		System.out.println("Starting " +  threadName );
		if (t == null)
		{
			t = new Thread (this, threadName);
			t.start ();
		}
	}
}


class CameraFrame implements Runnable {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	private Thread t;
	private String threadName;


	private static final int WIDTH = 160;
	private static final int HEIGHT = 120;
	private static final int NUM_PIXELS = WIDTH * HEIGHT;
	private static final int BUFFER_SIZE = NUM_PIXELS * 2;
	private static final int PORT = 55555;

	private ServerSocket ss;
	private Socket sock;
	private byte[] buffer = new byte[BUFFER_SIZE];
	//private int[] buffer = new int[BUFFER_SIZE];
	private BufferedInputStream bis;
	private BufferedImage image, image_bgr;

	private Mat rgb, bgr, yuy2;

	
	private CameraPanel panel = new CameraPanel();
	private JFrame frame;




	public CameraFrame() { 
		try {
			ss = new ServerSocket(PORT);
			sock = ss.accept();
			bis = new BufferedInputStream(sock.getInputStream());
		} catch (Exception e) {
			System.err.println("Failed to connect: " + e);
			System.exit(1);
		}

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		//image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
		image_bgr = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		
		yuy2 = new Mat(HEIGHT,WIDTH, CvType.CV_8UC2);
		rgb = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
		bgr = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
		//yuy2_signed = new Mat(HEIGHT, WIDTH, CvType.CV_8SC2); // since JAVA only supports signed 8-bit :(
		//rgb_signed = new Mat(HEIGHT, WIDTH, CvType.CV_8SC4); // since JAVA only supports signed 8-bit :(
		//rgb = new Mat(HEIGHT, WIDTH, CvType.CV_32S);
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

	public void close() {
		try {
			if (bis != null) bis.close();
			if (sock != null) sock.close();
			if (ss != null) ss.close();
		} catch (Exception e1) {
			System.err.println("Exception closing window: " + e1);
		}
	}

/*	private int convertYUVtoARGB(int y, int u, int v) {
		int c = y - 16;
		int d = u - 128;
		int e = v - 128;
		int r = (298*c+409*e+128)/256;
		int g = (298*c-100*d-208*e+128)/256;
		int b = (298*c+516*d+128)/256;
		System.out.print(",r:"+r+"g:"+g+"b:"+b);
		r = r>255? 255 : r<0 ? 0 : r;
		g = g>255? 255 : g<0 ? 0 : g;
		b = b>255? 255 : b<0 ? 0 : b;
		return 0xff000000 | (r<<16) | (g<<8) | b;  // int32 = Alpha = FF (100% opaque) = 8 bits, Red = 8 bits, Green=8 bits, Blue=8 bits 
	}*/

	
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

    
    private static Mat multiplyScalar(Mat m, double i) {
        return m = m.mul(new Mat((int)m.size().height, (int)m.size().width, m.type(), new Scalar(i)));
    }
    
    private static Mat findCircles1(Mat src) {
    	List<Mat> centers = new ArrayList<Mat>();
    	
    	Mat binary = new Mat();
    	
    	if(src.type() !=  0) { // not a binary image already

    		Mat src_gray = new Mat();
    		if(true) {
    			//System.out.println(src.type());
    			Imgproc.GaussianBlur(src, src,new Size (11,11),3,3);
    			Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_BGR2GRAY );
    			Mat kernel = new Mat();
    			Imgproc.erode(src_gray, binary, kernel);
    			Imgproc.dilate(binary, binary, kernel);
    			Imgproc.Canny(binary, binary, 70, 3);
    			Imgproc.GaussianBlur(binary, binary,new Size (21,21),15,15);
    		} else {
    			// TODO : for each color channel and move to Hough detection algo
    			// http://stackoverflow.com/questions/9860667/writing-robust-color-and-size-invariant-circle-detection-with-opencv-based-on
    			Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_BGR2GRAY );
    			Imgproc.adaptiveThreshold(src_gray,binary,255.,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 55, 7.);
    			Mat kernel = new Mat();
    			Imgproc.erode(binary, binary, kernel);
    			Imgproc.dilate(binary, binary, kernel);
    		}
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
    
    private static Mat cluster(Mat binMat, int k) {  	
    	Mat wLocMat = Mat.zeros(binMat.size(), binMat.channels()); 
    	Core.findNonZero(binMat, wLocMat);	
//    	System.out.println("conv. bin. mask:" + wLocMat.dump());
//    	System.out.println("rows:" + wLocMat.rows());
 //   	System.out.println("cols:" + wLocMat.cols());   	
  //  	System.out.println("dims:" + wLocMat.dims()); 
    	Mat samples32f = new Mat();
    	wLocMat.convertTo(samples32f, CvType.CV_32F, 1.0);
    		
    	Mat labels = new Mat();
    	TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 10, 1);
    	Mat centers = new Mat();
    	Core.kmeans(samples32f, k, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centers);
    	    	   	
 //   	System.out.println("centers:" + centers.dump());
    	
    	//centers.reshape(3);
    	return centers;
    }
    
	public void run() {
		while(true) {
			synchronized (this) {
				try {
					int offset = 0;
					while (offset < BUFFER_SIZE) {
						offset += bis.read(buffer, offset, BUFFER_SIZE - offset);
					}
					
					// transform to matimage.
					yuy2.put( 0, 0, buffer );
//					yuy2_signed.convertTo(yuy2, CvType.CV_8UC2,1,128);
					
//					System.out.println("Channels: " + yuy2.channels());	
//					System.out.println("Size: " + yuy2.size());				
//					System.out.println("OpenCV Mat data:\n" + yuy2.dump());
//					System.out.println(Arrays.toString(buffer));

					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2BGRA_YUY2);
					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2RGBA_YUY2);
					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2RGBA_YUYV);
					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2BGRA_YUYV);
					
					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2RGBA_YVYU);
					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2BGRA_YVYU);
					
					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2RGB_YUYV); // YUYV same as YUY2
					
					Imgproc.cvtColor(yuy2,bgr,Imgproc.COLOR_YUV2BGR_YUYV);
					
					image_bgr = toBufferedImage(bgr);
					
					//Imgproc.cvtColor(yuy2,rgb,Imgproc.COLOR_YUV2GRAY_YUY2);
					
					
					
//				    System.out.println("Channels: " + rgb.channels());
//				    System.out.println("Rows: " + rgb.rows());	
//				    System.out.println("Cols: " + rgb.cols());		
//				    System.out.println("OpenCV Mat data:\n" + rgb.dump());
				    //System.exit(0);
				    
				    
				    // OBJECT RECOGNITION IN ACTION
					Mat centers = new Mat();
					centers = findCircles(bgr);
					System.out.println("centers hough bgr:" + centers.dump());
					
					centers = findCircles1(bgr);
					System.out.println("centers/radii contours bgr:"+centers.dump());
					
					
					
				    //get HSV
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
				  //System.out.println("OpenCV Mat data:\n" + outputmask.dump());
				    
				    //System.out.println("type:" + outputmask.type());
				    centers = cluster(outputmask,1);
				    System.out.println("centers kmeans:" + centers.dump());
				    centers = findOneCenter(outputmask);
				    System.out.println("centers avg:" + centers.dump());
				    centers = findCircles(outputmask);
					System.out.println("centers hough bin:" + centers.dump());
					centers = findCircles1(outputmask);
					System.out.println("centers/radii contours bin:"+centers.dump());
				    
				    
				    //image.getRaster().setDataElements(0, 0, WIDTH, HEIGHT, rgb);
				    
				    //rgb.convertTo(rgb_signed, CvType.CV_8SC4,1,-128);
//					byte [] b = new byte[rgb.channels()*rgb.cols()*rgb.rows()];
				    //rgb_signed.get(0,0,b); // get all the pixels
					//rgb.get(0,0,b);
				    //System.out.println(Arrays.toString(b));
				    
				    //ByteBuffer byteBuffer = ByteBuffer.allocate(b.length);				    
				    //IntBuffer intBuffer = byteBuffer.asIntBuffer(); // divide by 4			    
				    //byteBuffer.put(b);
				    
//				    byteBuffer.rewind();
//				    while (byteBuffer.hasRemaining()) {
//				    	System.out.print(","+byteBuffer.get());
//				    }
//				    System.out.println("\n next...");
//				    intBuffer.rewind();
//				    while (intBuffer.hasRemaining()) {
//				    	System.out.print(","+intBuffer.get());
//				    }				   
				    
				    //System.out.println("\n has array? "+intBuffer.hasArray());
				    
				    //intBuffer.rewind();
/*				    int[] rgbimage = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
				    intBuffer.get(rgbimage);*/

				    
				    //Arrays.setAll(rgbimage,i->v[i]+128); // JAVA8
				    
//				    rgbimage = intBuffer.array();
//				    System.out.println(Arrays.toString(rgbimage));
				    

				    
					//Use this instead of for loop below???
					
					
					
/*					//YUV422: 4 bytes per 2 pixels
					for(int i=0;i<BUFFER_SIZE;i+=4) {
						int y1 = buffer[i] & 0xFF;
						int y2 = buffer[i+2] & 0xFF;
						int u = buffer[i+1] & 0xFF;
						int v = buffer[i+3] & 0xFF;
						//System.out.print(",y:"+y1+"u:"+u+"y:"+y2+"v:"+v);
						
						int rgb1 = convertYUVtoARGB(y1,u,v);
						int rgb2 = convertYUVtoARGB(y2,u,v);
						
						image.setRGB((i % (WIDTH * 2)) / 2, i / (WIDTH * 2), rgb1);
						image.setRGB((i % (WIDTH * 2)) / 2 + 1, i / (WIDTH * 2), rgb2);						
					}*/
				} catch (Exception e) {
					break;
				}
			}
			panel.repaint(1);
		}
	}

	class CameraPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// Ensure that we don't paint while the image is being refreshed
			synchronized(CameraFrame.this) {
				//g.drawImage(image, 0, 0, null);
				g.drawImage(image_bgr, 0, 0, null);
			}
		}  
	}


	public void start () {   
		//final CameraFrame cameraFrame = new CameraFrame();
		threadName = "getCameraImg";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}

		});
		System.out.println("Starting " +  threadName );
		if (t == null)
		{
			t = new Thread (this, threadName);
			t.start ();
		}         
	}
}

public class TestThread {
	public static void main(String args[]) {

		CameraFrame R1 = new CameraFrame();
		R1.start();
		

	
		//RobotControl R2 = new RobotControl();
		//R2.start();

		Delay.msDelay(2000);
	}   
}


