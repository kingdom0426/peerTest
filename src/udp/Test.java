package udp;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import net.sf.json.JSONObject;


public class Test {

	public static void main(String[] args) {
		new Test().start();
	}
	
	public void start() {
		try {
			DatagramSocket ds = new DatagramSocket();
			Scanner scan = new Scanner(System.in);
			System.out.print("server IP:");
			String serverIP = scan.next();
			System.out.print("server Port:");
			String serverPort = scan.next();
			System.out.print("target IP:");
			String targetIP = scan.next();
			System.out.print("server Port:");
			String targetPort = scan.next();
			
			String mes = targetIP+"|"+targetPort;
			DatagramPacket dp = new DatagramPacket(mes.getBytes(), mes.length(), InetAddress.getByName(serverIP), Integer.parseInt(serverPort));
			ds.send(dp);
			String testinfo = "conn";
			DatagramPacket dpd = new DatagramPacket(testinfo.getBytes(), testinfo.length(), InetAddress.getByName(targetIP), Integer.parseInt(targetPort));
			ds.send(dpd);
			byte[] buf = new byte[1024];
	        DatagramPacket dp2 = new DatagramPacket(buf,buf.length,InetAddress.getByName(serverIP),Integer.parseInt(serverPort));
			ds.receive(dp2);
			String data = new String(dp2.getData(), 0, dp2.getLength());
			System.out.println(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	class inputThread implements Runnable {
		private DatagramSocket ds;
		private Scanner scan;
		private String ip;
		private String port;
		
		public inputThread(DatagramSocket ds, Scanner scan, String ip, String port) {
			this.ds = ds;
			this.scan = scan;
			this.ip = ip;
			this.port = port;
		}

		public void run() {
			try {
				while(true) {
					System.out.print("input:");
					String senddata = scan.next();
					ds.send(new DatagramPacket(senddata.getBytes(), senddata.length(), InetAddress.getByName(ip), Integer.parseInt(port)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
