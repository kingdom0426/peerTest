package cn.cnic.peer.connect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.entity.Peer;
import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.entity.Segment;

public class UDPThread implements Runnable {
	
	private String peerID;
	public static List<Segment> segments = new ArrayList<Segment>();
	
	/**
	 * 下面这两个map用来判断对端peerlist是不是都已返回结果
	 * 如果已都返回（total == current），就可进行视频拼接，然后向各peer发送片段请求，并在map中删除此contentHash
	 */
	public static Map<String, Integer> mapTotal = new HashMap<String, Integer>();
	public static Map<String, Integer> mapCurrent = new HashMap<String, Integer>();
	
	//这个map用来记录contentHash的所有片段
	private Map<String, List<Piece>> mapPiece = new HashMap<String, List<Piece>>();
	
	public UDPThread(String peerID) {
		this.peerID = peerID;
	}
	
	public void run() {
		System.out.println("已启动UDP线程，用于PEER之间进行通信");
		try {
			DatagramSocket ds = new DatagramSocket(Constant.TRACKER_UDP_PORT);

			// 启动心跳线程
			new Thread(new HeartThread(ds, peerID)).start();

			// 循环接收
			byte[] buf = new byte[1024];
			DatagramPacket rp = new DatagramPacket(buf, 1024);
			boolean isEnd = false;
			while (!isEnd) {
				ds.receive(rp);
				// 取出信息
				String content = new String(rp.getData(), 0, rp.getLength());
				System.out.println("-------------" + content);
				JSONObject json = JSONObject.fromObject(content);
				System.out.println(json.toString());
				String contentHash = json.getString(Constant.CONTENT_HASH);
				String action = json.getString(Constant.ACTION);
				
				//Tracker返回Peer的UDP心跳响应，内容包含Peer的公网IP地址和端口，Peer可以依据此信息判断自己的网络类型（如是否为NAT，即PubIP不等于LocalIP，暂时不适用）
				if(action.equals(Constant.ACTION_HEARTBEAT_RESPONSE)) {
					
				} 
				
				//UDP穿透响应，收到来自tracker的穿透响应后，peer向对端peer进行打洞，在此进行三次打洞
				else if(action.equals(Constant.ACTION_NAT_TRAVERSAL_ORDER)) {
					String targetPeerIP = json.getString(Constant.TARGET_PEER_IP);
					String targetPeerPort = json.getString(Constant.TARGET_PEER_PORT);
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
				} 
				
				//收到握手请求后，返回本地所拥有的报文片段，并进行握手响应
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_REQUEST)) {
//					List<Piece> pieces = DB.getPiecesByContentHash(contentHash);
//					submitP2PhandShakeResponse(ds, contentHash, pieces, ds.getInetAddress().getHostName(), ds.getPort());
				} 
				
				//收到握手响应后，判断是否已全部返回，如果是，则对视频进行拼接
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_RESPONSE)) {
					JSONArray pieces = JSONArray.fromObject(json.get(Constant.PIECES));
					if(!mapPiece.containsKey(contentHash)) {
						mapPiece.put(contentHash, new ArrayList<Piece>());
					}
					for(int i = 0; i < pieces.size(); i++) {
						mapPiece.get(contentHash).add((Piece)pieces.get(i));
					}
					int total = mapTotal.get(contentHash);
					int current = mapCurrent.get(contentHash);
					//全部返回
					if(total == current) {
						//文件总大小（单位：kb）
						int size = 10000;
						//进行视频拼接，得到需要请求的视频片段
						List<Piece> resultPieces = generateFinalPieces(mapPiece.get(contentHash), size, ds, contentHash);
						
						//拼接完成后将记录从mapPiece中删除
						mapPiece.remove(contentHash);
						
						//依次发送数据请求
						for(Piece p : resultPieces) {
							submitP2PPieceRequest(ds, contentHash, p.getOffset(), p.getLength(), p.getPeer().getUdpIp(), p.getPeer().getUdpPort());
						}
						
						//请求发送完毕后，将此任务从两个map中移走
						mapTotal.remove(contentHash);
						mapCurrent.remove(contentHash);
					}
				} 
				
				//收到报文请求后，向请求方发送报文数据
				else if(action.equals(Constant.ACTION_P2P_PIECE_REQUEST)) {
					JSONArray array = JSONArray.fromObject(json.get(Constant.PIECES));
					for(int i = 0; i < array.size(); i++) {
						DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream("filePath")));
						Piece p = (Piece)array.get(i);
						int count = p.getLength()/1000;
						for(int j = 0; j < count; j++) {
							byte[] data = new byte[1024];
							fis.read(data, 0, 1000);
							submitP2PPieceResponse(ds, contentHash, p.getOffset()+j*1000, 1000, ds.getInetAddress().getHostName(), ds.getPort(), data);
							
						}
						if(p.getLength()%1000 != 0) {
							int size = p.getLength()%1000;
							byte[] data = new byte[size];
							fis.read(data, 0, 1000);
							submitP2PPieceResponse(ds, contentHash, p.getLength()/1000*1000, size, ds.getInetAddress().getHostName(), ds.getPort(), data);
						}
						fis.close();
					}
				}
				
				//接收到对端peer传来的数据相应后
				else if(action.equals(Constant.ACTION_P2P_PIECE_RESPONSE)) {
					File f = new File("C:/video/1.3gp");
					DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
					fileOut.write(json.get(Constant.DATA).toString().getBytes(), (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
					fileOut.close();
				} 
				
				//其他
				else {
					
				}
				
				
				//如果有需要获取的数据，就向peer进行请求
				if(segments.size() > 0) {
					for(int i = 0; i < segments.size(); i++) {
						Segment s = segments.get(i);
						
						//依次遍历tracker返回的每一个peer
						for(int j = 0; j < s.getPeerList().size(); i ++) {
							Peer p = s.getPeerList().get(j);
							//peer向tracker发送协助穿透请求
							submitNATTraversalAssist(ds, p.getPeerID(), s.getContentHash());
							//新建一个线程，在线程中延迟两秒，再去发送握手请求（延迟两秒的目的是让对端的peer节点有时间去进行打洞）
							new Thread(new HandShakeThread(ds, s.getContentHash(), p.getUdpIp(), p.getUdpPort())).start();
						}
					}
				}
				Thread.sleep(Constant.RECEIVE_INTERVAL);
			}
			ds.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void submitNATTraversalAssist(DatagramSocket ds, String targetPeerID, String contentHash) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_NAT_TRAVERSAL_ASSIST);
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.TARGET_PEER_ID, targetPeerID);
			json.put(Constant.CONTENT_HASH, contentHash);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(Constant.TRACKER_IP), Constant.TRACKER_UDP_PORT);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void submitNATTraversalOrder(DatagramSocket ds, String targetPeerID, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_NAT_TRAVERSAL_ORDER);
			json.put(Constant.TARGET_PEER_ID, targetPeerID);
			json.put(Constant.TARGET_PEER_IP, targetPeerIP);
			json.put(Constant.TARGET_PEER_PORT, targetPeerPort);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void submitP2PhandShakeRequest(DatagramSocket ds, String contentHash, String targetPeerIP, int targetPeerPort) {
		try {
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
	
	public void submitP2PhandShakeResponse(DatagramSocket ds, String contentHash, List<Piece> pieces, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_HANDSHAKE_RESPONSE);
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.CONTENT_HASH, contentHash);
			json.put(Constant.PIECES, pieces);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void submitP2PPieceRequest(DatagramSocket ds, String contentHash, int requestOffset, int requestLength, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_PIECE_REQUEST);
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.CONTENT_HASH, contentHash);
			json.put(Constant.REQUEST_OFFSET, requestOffset);
			json.put(Constant.REQUEST_LENGTH, requestLength);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void submitP2PPieceResponse(DatagramSocket ds, String contentHash, int dataOffset, int dataLength, String targetPeerIP, int targetPeerPort, byte[] bytes) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_PIECE_RESPONSE);
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.CONTENT_HASH, contentHash);
			json.put(Constant.DATA_OFFSET, dataOffset);
			json.put(Constant.DATA_LENGTH, dataLength);
			json.put(Constant.DATA, bytes);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void makeHole(DatagramSocket ds, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put("info", "打洞");
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		List<Piece> pieces = new ArrayList<Piece>();
		String content = "123";
		Piece p = new Piece();
		
		p.setContentHash(content);
		p.setOffset(100);
		p.setLength(100);
		pieces.add(p);

		p.setContentHash(content);
		p.setOffset(300);
		p.setLength(100);
		pieces.add(p);

		p.setContentHash(content);
		p.setOffset(500);
		p.setLength(100);
		pieces.add(p);
		
		p.setContentHash(content);
		p.setOffset(700);
		p.setLength(100);
		pieces.add(p);
		
		System.out.println(pieces);
	}
	
	/**
	 * ###########视频拼接算法#############
	 * 1.找出最长的一段数据，采用
	 * 
	 * ###################################
	 * @param sourcePieces peer提供的数据片段
	 * @param size 文件大小
	 * @return 最终需要请求的数据片段
	 */
	public List<Piece> generateFinalPieces(List<Piece> sourcePieces, int size, DatagramSocket ds, String contentHash) {
		List<Piece> finalPieces = new ArrayList<Piece>();
		int offset = 0;
		int end = 0;
		
		while (end < size) {
			//去list中找offset开始的片段
			Piece p = getLargestPiece(offset, sourcePieces);
			//如果片段不存在，就去cdn中下载，下载的范围是：开始：offset，结束：list中所有大于当前offset的片段中，offset最小的那个
			if (p == null) {
				int originalOffset = offset;
				Piece nextPiece = getNearestPiece(offset, sourcePieces);
				if (nextPiece == null) {
					downloadFromCDN("url", offset, size);
					end = size;
				} else {
					downloadFromCDN("url", originalOffset, offset);
					end = offset;
				}
			} else {
				finalPieces.add(p);
				end = p.getLength() + offset;
			}
		}
		
		for(Piece p : finalPieces) {
			submitP2PPieceRequest(ds, contentHash, p.getOffset(), p.getLength(), p.getPeer().getUdpIp(), p.getPeer().getUdpPort());
		}
		return finalPieces;
	}
	
	/**
	 * 获取某起点开始的最大的Piece片段
	 * @param offset 开始位置
	 * @param pieces 候选piece
	 * @return
	 */
	public Piece getLargestPiece(int offset, List<Piece> pieces) {
		int size = 0;
		Piece resultPiece = null;
		for(Piece p : pieces) {
			int position = p.getOffset() + p.getLength();
			if(p.getOffset() <= offset && position > offset) {
				int tempSize = position - offset;
				if(tempSize > size) {
					size = tempSize;
					resultPiece = p;
				}
			}
		}
		resultPiece.setOffset(offset);
		offset = offset + size;
		return resultPiece;
	}
	
	
	public Piece getNearestPiece(int offset, List<Piece> pieces) {
		Piece resultPiece = null;
		int minOffset = 999999999;
		for(Piece p : pieces) {
			if(p.getOffset() > offset && minOffset > p.getOffset()) {
				resultPiece = p;
				minOffset = p.getOffset();
			}
		}
		if(resultPiece != null) {
			offset = resultPiece.getOffset();
		}
		return resultPiece;
	}
	
	public void downloadFromCDN(String url, int offset, int length) {
		
	}
}
