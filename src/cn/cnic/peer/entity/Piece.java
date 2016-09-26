package cn.cnic.peer.entity;

public class Piece {

	private Peer peer;
	private String contentHash;
	private int offset;
	private int length;
	public Peer getPeer() {
		return peer;
	}
	public void setPeer(Peer peer) {
		this.peer = peer;
	}
	public String getContentHash() {
		return contentHash;
	}
	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
}
