package cn.cnic.peer.entity;

public class Peer {

	private String peerID;
	private String udpIp;
	private int udpPort;
	public String getPeerID() {
		return peerID;
	}
	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}
	public String getUdpIp() {
		return udpIp;
	}
	public void setUdpIp(String udpIp) {
		this.udpIp = udpIp;
	}
	public int getUdpPort() {
		return udpPort;
	}
	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}
}
