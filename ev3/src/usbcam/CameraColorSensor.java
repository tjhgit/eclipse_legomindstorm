package usbcam;

import java.io.File;
import java.io.IOException;
 
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.EV3;
import lejos.hardware.video.Video;

public class CameraColorSensor {
	private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int NUM_PIXELS = WIDTH * HEIGHT;
    private static final File BLACK = new File("black.wav");
    private static final File WHITE = new File("white.wav");
    private static final File RED = new File("red.wav");
    private static final File GREEN = new File("green.wav");
     
    private static long lastPlay = 0;
 
    public static void main(String[] args) throws IOException  {
         
        EV3 ev3 = (EV3) BrickFinder.getLocal();
        Video video = ev3.getVideo();
        video.open(WIDTH, HEIGHT);
        byte[] frame = video.createFrame();
         
        while(Button.ESCAPE.isUp()) {
            video.grabFrame(frame);
            int tr = 0, tg = 0, tb = 0;
             
            for(int i=0;i<frame.length;i+=4) {
                int y1 = frame[i] & 0xFF;
                int y2 = frame[i+2] & 0xFF;
                int u = frame[i+1] & 0xFF;
                int v = frame[i+3] & 0xFF;
                int rgb1 = convertYUVtoARGB(y1,u,v);
                int rgb2 = convertYUVtoARGB(y2,u,v);
                 
                tr += ((rgb1 >> 16) & 0xff) + ((rgb2 >> 16) & 0xff);
                tg += ((rgb1 >> 8) & 0xff) + ((rgb2 >> 8) & 0xff);
                tb += (rgb1 & 0xff) + (rgb2 & 0xff);
            }
             
            float ar = tr/NUM_PIXELS, ag = tg/NUM_PIXELS, ab = tb/NUM_PIXELS;
            System.out.println((int) ar + " , " + (int) ag + " , " + (int) ab);
            if (ar < 10 && ag < 10 && ab < 10) play(BLACK);
            else if (ar > 250 && ag > 250 && ab > 250) play(WHITE);
            else if ( ar > 1.8*ag && ar > 2*ab) play(RED);
            else if ( ag > 1.8*ar && ag > ab) play(GREEN);
 
        }
        video.close();
    }
     
    private static void play(File file) {
        long now = System.currentTimeMillis();
         
        if (now - lastPlay > 2000) {
            System.out.println("Playing " + file.getName());
            Sound.playSample(file);
            lastPlay = now;
        }
    }
     
    private static int convertYUVtoARGB(int y, int u, int v) {
        int c = y - 16;
        int d = u - 128;
        int e = v - 128;
        int r = (298*c+409*e+128)/256;
        int g = (298*c-100*d-208*e+128)/256;
        int b = (298*c+516*d+128)/256;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (r<<16) | (g<<8) | b;
    }
}
