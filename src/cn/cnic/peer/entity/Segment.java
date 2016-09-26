package cn.cnic.peer.entity;

import java.util.List;

public class Segment {

	private String urlHash;
	private String contentHash;
	private List<Peer> peerList;
	public List<Peer> getPeerList() {
		return peerList;
	}
	public void setPeerList(List<Peer> peerList) {
		this.peerList = peerList;
	}
	public String getUrlHash() {
		return urlHash;
	}
	public void setUrlHash(String urlHash) {
		this.urlHash = urlHash;
	}
	public String getContentHash() {
		return contentHash;
	}
	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}
}
