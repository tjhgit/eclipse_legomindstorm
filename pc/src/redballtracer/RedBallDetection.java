package redballtracer;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class RedBallDetection {
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }


	// image
	private static final int WIDTH = 160;
	private static final int HEIGHT = 120;
	private BufferedImage image;	
	private Mat yuy2,bgr;


	// GUI
	private CameraPanel panel = new CameraPanel();
	private JFrame frame;


	// constructor
	RedBallDetection() { 
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		yuy2 = new Mat(HEIGHT,WIDTH, CvType.CV_8UC2);
		bgr = new Mat(HEIGHT,WIDTH, CvType.CV_8UC3);
	}


	private static BufferedImage toBufferedImage(Mat m){
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

	private static Mat BufferedImagetoMat(BufferedImage image) {
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, data);
		return mat;
	}

	private static Mat getNormalizedRGB(Mat rgb) {
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

	public static Mat bootstrap(Mat bgr) {
		Mat nbgr = getNormalizedRGB(bgr);
		Mat hsv=new Mat(); 
		Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV);
		//take the pixels that are inside the ranges in both colorspaces
		Mat mask_hsv = new Mat(); 
		Mat mask_nbgr = new Mat();
		Mat outputmask = new Mat();
		
		// THESE ARE THE PARAMETERS FOR YELLOW BALLS
		//H=[0,50], S= [0.20,0.68] and V= [0.35,1.0]
		Core.inRange(hsv, new Scalar(20./360.*255,0.5*255.,0.5*255.), new Scalar(60.0*255./360.,1.0*255.,1.0*255.0), mask_hsv);
		//r = [0.36,0.465], g = [0.28,0.363]
		Core.inRange(nbgr, new Scalar(0.0,0.3,0.4), new Scalar(0.2,0.5,0.6), mask_nbgr); // detect red

		//combine the masks
		Core.bitwise_and(mask_hsv, mask_nbgr, outputmask);
		
		
		//outputmask = mask_hsv;
		//outputmask = mask_nbgr;
		//System.out.println(outputmask.dump());

		
		return outputmask;
	}

	private Mat findCircles(Mat src, Mat mask, int [] params) {
		Mat src_gray = new Mat();
		Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_BGR2GRAY );
				
		Core.multiply(mask, new Scalar(1./255.), mask, 1.0);		
		//System.out.println(mask.dump());
		Core.multiply(src_gray, mask, src_gray);
		//System.out.println(src_gray.dump());

		return findCircles(src_gray,params);
	}
	
	// image recognition stuff
	private Mat findCircles(Mat src, int [] params) {
		Mat circles = new Mat();
		Mat src_gray = new Mat();
		//int[] params;
		if(src.type() !=  0) {
			//params = new int[]{1,80,20};
			//System.out.println(src.type());
			Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_BGR2GRAY );
			//System.out.println(src_gray.type());
		} else {
			//params = new int[]{2,30,20};
			src_gray = src;   		
		}
		//this.image = toBufferedImage(src_gray);
		//panel.repaint(1);
		//System.out.println(src_gray.dump());
		int smallestDim = (WIDTH<HEIGHT)?WIDTH:HEIGHT;
		Imgproc.HoughCircles( src_gray, circles, Imgproc.CV_HOUGH_GRADIENT , params[0], src_gray.rows()/8, params[1], params[2], 0, (int) (smallestDim/2.) );
		circles = circles.reshape(1); // 1 channel
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
		center = center.reshape(1); // 1 channel
		return center;
	}

	private void paintCircle(Mat centers) {		
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setPaint(Color.green);
		//g2d.fillOval((int) (WIDTH/2.),(int) (HEIGHT/2.), 5, 5);
		//g2d.fillOval((int) (circ[0]-circ1[0]),(int) (circ[1]-circ1[0]), (int) (circ1[0]*2.), (int) (circ1[0]*2.));
		int radius = (int) (centers.get(0,2)[0]/1.);
		g2d.fillOval((int) (centers.get(0,0)[0]-radius),(int) (centers.get(0,1)[0]-radius), (int) (radius*2.), (int) (radius*2.));
		g2d.dispose();
		panel.repaint(1);
	}


	// GUI stuff
	public void createAndShowGUI() {
		frame = new JFrame("Image View");

		frame.getContentPane().add(panel);
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//close();
			}
		});

		frame.pack();
		frame.setVisible(true);
	}


	class CameraPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// Ensure that we don't paint while the image is being refreshed
			synchronized(this) {
				//g.drawImage(image, 0, 0, null);
				g.drawImage(image, 0, 0, null);
			}
		}  
	}
	
	public BufferedImage getImageFromYUYV(byte [] buffer) {
		yuy2.put( 0, 0, buffer );
		Imgproc.cvtColor(yuy2,bgr,Imgproc.COLOR_YUV2BGR_YUYV);// convert YUY2 to BGR	
		image = toBufferedImage(bgr);
		return image;
	}
	
	public void refreshFrameandSave(int img_count) {		
		panel.repaint(1);		
		File outputfile = new File("/home/t13147/Dokumente/data/ev3/ball_"+ (img_count++)+".png");
		try {
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Mat getRedBallPosition(BufferedImage image) {
		// RED BALL RECOGNITION !!!
		bgr = BufferedImagetoMat(image);	
		//this.image = toBufferedImage(bgr);
		//panel.repaint(1);
		
		Mat outputmask = bootstrap(bgr);
		// uncomment this to optimize the bootstrap -> color filter
		//this.image = toBufferedImage(outputmask);
		//panel.repaint(1);
		
		int nredpix = Core.countNonZero(outputmask);
		System.out.println("nonzero pix:" + nredpix);

		Mat centers = new Mat();
		if(nredpix>0) {		
			
			if(false) {
				// OPTION1: find circle in grayscale image, cropped to area of color filter
				int erosion_size = 10; //5 
				Mat kernel = Imgproc.getStructuringElement( Imgproc.MORPH_RECT, new Size( 2*erosion_size + 1, 2*erosion_size+1 ), new Point( erosion_size, erosion_size ) );
				Imgproc.dilate(outputmask, outputmask, kernel); // make bright regions bigger
				centers = findCircles(bgr,outputmask,new int[] {2,20,30});
			} else {
				// OPTION2: Detect circle in mask
				int erosion_size = 2; //5 
				Mat kernel = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE, new Size( 2*erosion_size + 1, 2*erosion_size+1 ), new Point( erosion_size, erosion_size ) );
				Imgproc.dilate(outputmask, outputmask, kernel); // make bright regions bigger			
				erosion_size = 1; // 4
				kernel = Imgproc.getStructuringElement( Imgproc.MORPH_RECT, new Size( 2*erosion_size + 1, 2*erosion_size+1 ), new Point( erosion_size, erosion_size ) );
				Imgproc.erode(outputmask, outputmask, kernel);
				Imgproc.GaussianBlur(outputmask, outputmask,new Size (5,5),3,3);
				centers = findCircles(outputmask,new int[] {2,20,15});			
			}
			//Mat centers1;
			//centers1 = findOneCenter(outputmask);
			//System.out.println("centers avg:" + centers1.dump());
			//System.out.println(centers1.size());
						
			//System.out.println(centers.size());
			//System.out.println(centers1.get(0,1)[0]);

			System.out.println("centers hough bin:" + centers.dump());

			// overwrite hough bin centers with the result from findOneCenter
			if(!centers.empty()) {
				//centers.put(0, 0, centers1.get(0, 0)[0]);
				//centers.put(0, 1, centers1.get(0, 1)[0]);
				//System.out.println("centers hough bin corrected:" + centers.dump());
				paintCircle(centers);
				System.out.println(centers.cols());
				System.out.println(centers.rows());
				System.out.println(centers.get(0, 2)[0]);
			}
		}
		return centers;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		final RedBallDetection rbd = new RedBallDetection();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				rbd.createAndShowGUI();	
			}
		});

		try {
		    rbd.image = ImageIO.read(new File("/home/t13147/Dokumente/data/ev3/ball_33.png"));
		} catch (IOException e) {
		}
		Mat centers = rbd.getRedBallPosition(rbd.image);
		
	}

}
