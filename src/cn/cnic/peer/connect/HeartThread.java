package cn.cnic.peer.connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sf.json.JSONObject;

import cn.cnic.peer.cons.Constant;

public class HeartThread implements Runnable {
	private DatagramSocket ds;
	private DatagramPacket p;

	public HeartThread(DatagramSocket ds, String peerID) {
		this.ds = ds;
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_HEARTBEAT_RESPONSE);
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.LOCAL_UDP_IP, "localhost");
			json.put(Constant.LOCAL_UDP_PORT, "1234");
			String data = json.toString();
			p = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByName(Constant.TRACKER_IP), Constant.TRACKER_UDP_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			try {
				ds.send(p);
				Thread.sleep(Constant.HEART_BEAT_INTERVAL);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
