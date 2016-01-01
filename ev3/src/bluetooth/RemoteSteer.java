package bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
 
import lejos.hardware.Bluetooth;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.remote.nxt.NXTConnection;
import lejos.robotics.navigation.DifferentialPilot;
 
public class RemoteSteer {
 
  public static void main(String[] args) throws Exception {
    DifferentialPilot robot = new DifferentialPilot(2.1f,6f,Motor.A, Motor.C,true);
    String connected = "Connected";
        String waiting = "Waiting";
    LCD.drawString(waiting,0,0);
    LCD.refresh();
 
    NXTConnection btc = Bluetooth.getNXTCommConnector().waitForConnection(0, NXTConnection.PACKET);
         
    LCD.clear();
    LCD.drawString(connected,0,0);
    LCD.refresh(); 
 
    DataInputStream dis = new DataInputStream(btc.openInputStream());
    DataOutputStream dos = new DataOutputStream(btc.openOutputStream());
 
    while (true) {
       
      int x = dis.readByte();
      int y = dis.readByte() & 0xFF;
      int z = dis.readByte() & 0xFF;
       
      dos.writeByte((byte) 0xFF); // Ack
      dos.flush();
           
      LCD.drawInt(x,4, 0,1);
      LCD.drawInt(y,4, 0,2);
      LCD.drawInt(z,4, 0,3);
       
      int speed = (180 - y) * 20;
       
      LCD.drawInt(speed, 5, 0, 4);
      LCD.refresh();
       
      int ax = (x < 0 ? -x : x);
       
      if (speed > 0) {
        robot.setTravelSpeed(speed);
        System.out.println("forward");
        if (ax < 5) robot.forward();
        else robot.steer(-x);
      } else {
        speed = - speed;
        robot.setTravelSpeed(speed);
        System.out.println("Backward");
        robot.backward();
      }
    }
  }
}


/*// The NXT part
import java.io.*;
import lejos.nxt.*;
import lejos.nxt.addon.*;
import lejos.nxt.comm.*;
 
public class TiltController {
 
  *//**
   * Wii like controller for the NXT.
   *
   * Needs two NXTs: one for the controller and one to control.
   *//*
  public static void main(String[] args) throws Exception {
    AccelMindSensor controller = new AccelMindSensor(SensorPort.S1);
    int x, y, z;
    String name = &quot;EV3&quot;;
 
    LCD.drawString(&quot;Connecting ...&quot;, 0, 0);
    LCD.refresh();
 
    BTConnection btc = Bluetooth.connect(name, NXTConnection.PACKET);
 
    LCD.clear();
    LCD.drawString(&quot;Connected&quot;, 0, 0);
    LCD.refresh();
 
    DataInputStream dis = btc.openDataInputStream();
    DataOutputStream dos = btc.openDataOutputStream();
 
    while (true) {
      x = controller.getXTilt();
      y = controller.getYTilt();
      z = controller.getZTilt();
 
      LCD.drawInt(x, 3, 0, 1);
      LCD.drawInt(y, 3, 0, 2);
      LCD.drawInt(z, 3, 0, 3);
 
      dos.writeByte(x);
      dos.writeByte(y);
      dos.writeByte(z);
      dos.flush();
      byte ack = dis.readByte();
      LCD.drawInt(ack, 3, 0, 4);
    }
  }
}*/
