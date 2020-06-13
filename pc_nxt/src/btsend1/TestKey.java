package btsend1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.JTextField;

public class TestKey {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		char tmp = (char) System.in.read();
//		System.out.println("Key " +tmp+ " pressed");
		
//		DataInputStream in=new DataInputStream(System.in);
//		System.out.println("Enter a character");
//		byte b = in.readByte();
//		char ch=(char)b;
//		System.out.println("Char : " +ch );
		
		Frame f = new Frame("Key Listener Test");
		
	    JTextField textField = new JTextField();
	    
	    f.add(textField, BorderLayout.SOUTH);
        f.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        } );
        
	    textField.addKeyListener(new KeyListener() {
	        public void keyPressed(KeyEvent e) {
	            System.out.println(e.getKeyChar() + " pressed");
	            int keyCode = e.getKeyCode();
	            switch( keyCode ) { 
	                case KeyEvent.VK_UP:
	                    // handle up 
	                    break;
	                case KeyEvent.VK_DOWN:
	                    // handle down 
	                    break;
	                case KeyEvent.VK_LEFT:
	                	System.out.println("Left Cursor pressed");
	                    break;
	                case KeyEvent.VK_RIGHT :
	                	System.out.println("Right Cursor pressed");
	                    // handle right
	                    break;
	             }	            
	        }
	        public void keyReleased(KeyEvent e) {
	            System.out.println(e.getKeyChar() + " released");
	        }
	        public void keyTyped(KeyEvent e) {
	            System.out.println(e.getKeyChar() + " typed");
	        }
	        
	    });
	    
	    
        f.setSize(300, 200);
        f.setVisible(true);
        textField.requestFocusInWindow();
	    
	}

}
