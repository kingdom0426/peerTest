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
	
	//周期性地执行上传信息任务
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
	 * Peer节点获取内容时连接的Peer节点数
	 * @param peerCnt表示下载当前任务所连接的Peer数
	 * @param peerID
	 * @param contentHash
	 * @param timeStart开始统计的时间
	 * @param timeEnd结束统计的时间
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
	 * Peer节点媒体数据连接NAT穿越成功率
	 * @param peerID
	 * @param contentHash
	 * @param NATSuccessRate表示下载当前任务所连接的NAT Peer成功率
	 * @param timeStart开始统计的时间
	 * @param timeEnd结束统计的时间
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
	 * Peer节点调度系统服务时延
	 * @param peerID
	 * @param contentHash
	 * @param serviceDelay从Peer节点发起文件调度请求到接收到调度中心返回相应的Peer服务节点列表信息这一过程中的时延
	 * @param logTime记录生成时间
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
	 * 切片请求时延
	 * @param peerID
	 * @param contentHash
	 * @param pieceDelay从Peer节点发起文件获取调度请求到到获取到一个视频切片数据所需要的时间
	 * @param logTime记录生成时间
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
	 * Peer节点北向上行、下行流量
	 * @param peerID
	 * @param contentHash
	 * @param uploadVolume表示该Content上行流量统计值
	 * @param downloadVolume表示该Content下行流量统计值
	 * @param timeStart开始统计的时间
	 * @param timeEnd结束统计的时间
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
	 * Peer节点北向上行、下行带宽峰值
	 * @param peerID
	 * @param contentHash
	 * @param uploadPeakRate表示上行峰值速率
	 * @param downloadPeakRate表示下行峰值速率
	 * @param timeStart开始统计的时间
	 * @param timeEnd结束统计的时间
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
	 * Peer节点CPU、内存占用率、本地存储总量、可用存储总量
	 * @param peerID
	 * @param CPUUseRate CPU使用率
	 * @param MEMUseRate内存使用率
	 * @param totalStorage总存储量
	 * @param availableStorage可用存储总量
	 * @param logTime记录生成时间
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
	 * Peer节点CPU、内存占用率
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
