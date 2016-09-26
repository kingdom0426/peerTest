package cn.cnic.peer.connect;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.DownloadThread;
import cn.cnic.peer.entity.Peer;
import cn.cnic.peer.entity.Segment;

public class TCPThread implements Runnable {
	
	private Socket tcp;
	private BufferedWriter writer;
	private String peerID;
	
	//用于存储播放器请求
	public static List<String> urls = new ArrayList<String>();
	
	public TCPThread(String peerID) {
		try {
			tcp = new Socket(Constant.TRACKER_IP, Constant.TRACKER_TCP_PORT);
			writer = new BufferedWriter(new OutputStreamWriter(tcp.getOutputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.peerID = peerID;
	}

	public void run() {
		
		try {
			//启动上传信息线程，周期性上传数据
			new Thread(new UploadInfoThread(writer, peerID)).start();
			
			//不断获取tracker发来的数据
			boolean isEnd = false;
			while (!isEnd) {
				String data = receive(tcp);
				if(!data.equals("") && data != null) {
					JSONObject json = JSONObject.fromObject(data);
					if(json.containsKey(Constant.PEER_LIST)) {
						JSONArray array = json.getJSONArray(Constant.PEER_LIST);
						List<Peer> peerList = new ArrayList<Peer>();
						for(int i = 0; i < array.size(); i ++) {
							Peer peer = new Peer();
							JSONObject peerJson = JSONObject.fromObject(array.get(i));
							peer.setPeerID(peerJson.getString(Constant.PEER_ID));
							peer.setUdpIp(peerJson.getString(Constant.UDP_IP));
							peer.setUdpPort(peerJson.getInt(Constant.UDP_PORT));
							peerList.add(peer);
						}
						
						//如果tracker中存在，就按tracker的指示去下载
						if(peerList.size() > 0) {
							Segment seg = new Segment();
							seg.setContentHash(json.getString(Constant.CONTENT_HASH));
							seg.setUrlHash(json.getString(Constant.URL_HASH));
							seg.setPeerList(peerList);
							UDPThread.segments.add(seg);
							UDPThread.mapTotal.put(json.getString(Constant.CONTENT_HASH), peerList.size());
							UDPThread.mapCurrent.put(json.getString(Constant.CONTENT_HASH), 0);
						}
						
						//如果本地不存在，就从cdn中下载
						else {
							new DownloadThread(json.getString(Constant.URL_HASH), Constant.SAVE_PATH, 5);
						}
					}
				}
				
				if(urls.size() > 0) {
					for(int i = 0; i < urls.size(); i++) {
						//先创建一个空文件
						File f = new File("I:/dx/a.txt");
						if(!f.exists()) {
							f.createNewFile();
						}
						
						//发送URL请求
						queryPeerList(peerID, urls.get(i));
						
						//从列表中清除此记录
						urls.remove(i);
					}
				}
				
				Thread.sleep(Constant.RECEIVE_INTERVAL);
			}
			tcp.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer给Tracker发送的请求指令
	 * @param peerID Peer的特征串，每个Peer节点唯一，可以在初始随机生成，之后一直沿用（一般用SHA1哈希算法获取20字节值，即40字节可打印字符串）
	 * @param URLHash Peer所请求的下载任务URL哈希值
	 */
	private void queryPeerList(String peerID, String URLHash) {
		JSONObject json = new JSONObject();
		json.put(Constant.PEER_ID, peerID);
		json.put(Constant.URL_HASH, URLHash);
		send(json);
	}
	
	private void send(JSONObject json) {
		try {
			writer.write(json.toString());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String receive(Socket socket) {
		String data = "";
		try {
			DataInputStream input = new DataInputStream(socket.getInputStream());    
			byte[] buffer;
			buffer = new byte[input.available()];
			if(buffer.length != 0){
				input.read(buffer);
				data = new String(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}
