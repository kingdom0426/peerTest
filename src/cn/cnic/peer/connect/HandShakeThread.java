package cn.cnic.peer.connect;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import net.sf.json.JSONObject;
import cn.cnic.peer.cons.Constant;

public class HandShakeThread implements Runnable {
	
	private DatagramSocket ds;
	private String contentHash;
	private String targetPeerIP;
	private int targetPeerPort;
	private String peerID;
	
	public HandShakeThread(DatagramSocket ds, String peerID, String targetPeerIP, int targetPeerPort) {
		this.ds = ds;
		this.peerID = peerID;
		this.targetPeerIP = targetPeerIP;
		this.targetPeerPort = targetPeerPort;
	}

	public void run() {
		try {
			Thread.sleep(2000);
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_HANDSHAKE_REQUEST);
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.CONTENT_HASH, contentHash);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
