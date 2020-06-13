package btsend1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.JTextField;

import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;
//import lejos.util.Delay;


class BTSend1 {
	private DataOutputStream dataOut;
	private NXTConnector connector;
	
	// should be the same as used by Segoway2.java
	enum Command {
	  LEAN;
	}
	
	boolean connect(String name, String address) {
		System.out.println(" connecting to " + name + " " + address);
		connector = new NXTConnector();
		boolean connected = connector.connectTo(name, address, NXTCommFactory.BLUETOOTH);
		System.out.println(" connect result " + connected);
		if (!connected) {
			return connected;
		}
		dataOut = new DataOutputStream(connector.getOutputStream());
		return connected;
	}
	  
	void send(Command c, int... data) {
		try {
			dataOut.writeInt(c.ordinal());  // convert the enum to an integer
			for (int d : data) {
				dataOut.writeInt(d);
			}
			dataOut.flush();
		} catch (IOException e) {
			System.out.println(" send throws exception  " + e);
		}
	}
	  
	public static void main(String[] args) {
		String name = "NXT";
		String address = "0016530B0F99";
		System.out.println("Connecting to " + name);
		final BTSend1 bts = new BTSend1();
		boolean connected;
		
		if (!bts.connect(name, address)) {
			System.out.println("Connection Failed");
			connected = false;
		} else {
			System.out.println("Connected to " + name);
			connected = true;
		}
		
		if(connected) {
			Frame f = new Frame("Segoway Controller");
			
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
		                    System.out.println("Leaning forward");
		        			bts.send(Command.LEAN, 1);
		                    break;
		                case KeyEvent.VK_DOWN:
		        			System.out.println("Leaning backward");
		        			bts.send(Command.LEAN,-1); 
		                    break;
		             }	            
		        }
		        public void keyReleased(KeyEvent e) {}
		        public void keyTyped(KeyEvent e) {}
		        
		    });
		    
		    
	        f.setSize(300, 200);
	        f.setVisible(true);
	        textField.requestFocusInWindow();

		}

	}

}
