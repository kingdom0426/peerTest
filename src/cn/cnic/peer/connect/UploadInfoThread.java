package cn.cnic.peer.connect;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import net.sf.json.JSONObject;
import cn.cnic.peer.cons.Constant;

public class UploadInfoThread implements Runnable {
	private String peerID;
	
	public UploadInfoThread(String peerID) {
		this.peerID = peerID;
	}

	public void run() {
		while(true) {
			doSubmitWork();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//�����Ե�ִ���ϴ���Ϣ����
	public void doSubmitWork() {
		boolean isEnd = false;
		if(isEnd) {
			submitCPUUseRate("1", "1", "1", "1", "1");
			submitNATSuccessRate("1", "1", "1", "1");
			submitPeakRate("1", "1", "1", "1", "1");
			submitPeerCnt(1, "1", "1", "1");
			submitPieceDelay("1", "1", "1");
			submitServiceDelay("1", "1", "1");
			submitVolume("1", "1", "1", "1", "1");
			submitCPUUseInfo();
		}
	}
	

	/**
	 * Peer�ڵ��ȡ����ʱ���ӵ�Peer�ڵ���
	 * @param peerCnt��ʾ���ص�ǰ���������ӵ�Peer��
	 * @param peerID
	 * @param contentHash
	 * @param timeStart��ʼͳ�Ƶ�ʱ��
	 * @param timeEnd����ͳ�Ƶ�ʱ��
	 */
	private void submitPeerCnt(int peerCnt, String contentHash, String timeStart, String timeEnd) {
		JSONObject json = new JSONObject();
		json.put(Constant.TYPE, Constant.TYPE_PEER_CNT);
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.CONTENT_HASH, contentHash);
		json.put(Constant.PEER_CNT, peerCnt + "");
		json.put(Constant.TIME_START, timeStart);
		json.put(Constant.TIME_END, timeEnd);
		send(json, Constant.TRACKER_IP + "/api/peer/taskpeercnt");
	}
	
	/**
	 * Peer�ڵ�ý����������NAT��Խ�ɹ���
	 * @param peerID
	 * @param contentHash
	 * @param NATSuccessRate��ʾ���ص�ǰ���������ӵ�NAT Peer�ɹ���
	 * @param timeStart��ʼͳ�Ƶ�ʱ��
	 * @param timeEnd����ͳ�Ƶ�ʱ��
	 */
	private void submitNATSuccessRate(String contentHash, String NATSuccessRate, String timeStart, String timeEnd) {
		JSONObject json = new JSONObject();
		json.put(Constant.TYPE, Constant.TYPE_NAT_SUCCESS_RATE);
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.NAT_SUCCESS_RATE, NATSuccessRate);
		json.put(Constant.TIME_START, timeStart);
		json.put(Constant.TIME_END, timeEnd);
		send(json, Constant.TRACKER_IP + "/api/peer/natsuccrate");
	}
	
	/**
	 * Peer�ڵ����ϵͳ����ʱ��
	 * @param peerID
	 * @param contentHash
	 * @param serviceDelay��Peer�ڵ㷢���ļ��������󵽽��յ��������ķ�����Ӧ��Peer����ڵ��б���Ϣ��һ�����е�ʱ��
	 * @param logTime��¼����ʱ��
	 */
	private void submitServiceDelay(String contentHash, String serviceDelay, String logTime) {
		JSONObject json = new JSONObject();
		json.put(Constant.TYPE, Constant.TYPE_SERVICE_DELAY);
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.CONTENT_HASH, contentHash);
		json.put(Constant.SERVICE_DELAY, serviceDelay);
		json.put(Constant.LOG_TIME, logTime);
		send(json, Constant.TRACKER_IP + "/api/peer/schedulingdelay");
	}
	
	/**
	 * ��Ƭ����ʱ��
	 * @param peerID
	 * @param contentHash
	 * @param pieceDelay��Peer�ڵ㷢���ļ���ȡ�������󵽵���ȡ��һ����Ƶ��Ƭ��������Ҫ��ʱ��
	 * @param logTime��¼����ʱ��
	 */
	private void submitPieceDelay(String contentHash, String pieceDelay, String logTime) {
		JSONObject json = new JSONObject();
		json.put(Constant.TYPE, Constant.TYPE_PIECE_DELAY);
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.CONTENT_HASH, contentHash);
		json.put(Constant.PIECE_DELAY, pieceDelay);
		json.put(Constant.LOG_TIME, logTime);
		send(json, Constant.TRACKER_IP + "/api/peer/slicedelay");
	}
	
	/**
	 * Peer�ڵ㱱�����С���������
	 * @param peerID
	 * @param contentHash
	 * @param uploadVolume��ʾ��Content��������ͳ��ֵ
	 * @param downloadVolume��ʾ��Content��������ͳ��ֵ
	 * @param timeStart��ʼͳ�Ƶ�ʱ��
	 * @param timeEnd����ͳ�Ƶ�ʱ��
	 */
	private void submitVolume(String contentHash, String uploadVolume, String downloadVolume, String timeStart, String timeEnd) {
		JSONObject json = new JSONObject();
		json.put(Constant.TYPE, Constant.TYPE_UPLOAD_VOLUME);
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.CONTENT_HASH, contentHash);
		json.put(Constant.UPLOAD_VOLUME, uploadVolume);
		json.put(Constant.DOWNLOAD_VOLUME, downloadVolume);
		json.put(Constant.TIME_START, timeStart);
		json.put(Constant.TIME_END, timeEnd);
		send(json, Constant.TRACKER_IP + "/api/peer/volume");
	}
	
	/**
	 * Peer�ڵ㱱�����С����д����ֵ
	 * @param peerID
	 * @param contentHash
	 * @param uploadPeakRate��ʾ���з�ֵ����
	 * @param downloadPeakRate��ʾ���з�ֵ����
	 * @param timeStart��ʼͳ�Ƶ�ʱ��
	 * @param timeEnd����ͳ�Ƶ�ʱ��
	 */
	private void submitPeakRate(String contentHash, String uploadPeakRate, String downloadPeakRate, String timeStart, String timeEnd) {
		JSONObject json = new JSONObject();
		json.put(Constant.TYPE, Constant.TYPE_UPLOAD_PEAK_RATE);
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.CONTENT_HASH, contentHash);
		json.put(Constant.UPLOAD_PEAK_RATE, uploadPeakRate);
		json.put(Constant.DOWNLOAD_PEAK_RATE, downloadPeakRate);
		json.put(Constant.TIME_START, timeStart);
		json.put(Constant.TIME_END, timeEnd);
		send(json, Constant.TRACKER_IP + "/api/peer/peakrate");
	}
	
	/**
	 * Peer�ڵ�CPU���ڴ�ռ���ʡ����ش洢���������ô洢����
	 * @param peerID
	 * @param CPUUseRate CPUʹ����
	 * @param MEMUseRate�ڴ�ʹ����
	 * @param totalStorage�ܴ洢��
	 * @param availableStorage���ô洢����
	 * @param logTime��¼����ʱ��
	 */
	private void submitCPUUseRate(String CPUUseRate, String MEMUseRate, String totalStorage, String availableStorage, String logTime) {
		JSONObject json = new JSONObject();
		json.put(Constant.TYPE, Constant.TYPE_CPU_USE_RATE);
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.CPU_USE_RATE, CPUUseRate);
		json.put(Constant.MEM_USE_RATE, MEMUseRate);
		json.put(Constant.TOTAL_STORAGE, totalStorage);
		json.put(Constant.AVAILABLE_STORAGE, availableStorage);
		json.put(Constant.LOG_TIME, logTime);
		send(json, Constant.TRACKER_IP + "/api/device/disk");
	}
	
	/**
	 * Peer�ڵ�CPU���ڴ�ռ����
	 */
	private void submitCPUUseInfo() {
		
	}
	
	private void send(JSONObject json, String url) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			StringEntity entity = new StringEntity(json.toString());
			entity.setContentType("text/json");
			entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(entity);
			client.execute(post);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
