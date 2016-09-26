package xwz.p2p.upd.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Test {

	public static void main(String[] args) {
		try {
			String text = "ddd";
			DatagramSocket ds = new DatagramSocket();
			DatagramPacket dp = new DatagramPacket(text.getBytes(), text.length(), InetAddress.getByName("139.224.19.129"), 3333);
			ds.send(dp);
			ds.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
