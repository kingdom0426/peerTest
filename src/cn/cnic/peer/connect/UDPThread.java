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
	 * ����������map�����ж϶Զ�peerlist�ǲ��Ƕ��ѷ��ؽ��
	 * ����Ѷ����أ�total == current�����Ϳɽ�����Ƶƴ�ӣ�Ȼ�����peer����Ƭ�����󣬲���map��ɾ����contentHash
	 */
	public static Map<String, Integer> mapTotal = new HashMap<String, Integer>();
	public static Map<String, Integer> mapCurrent = new HashMap<String, Integer>();
	
	//���map������¼contentHash������Ƭ��
	private Map<String, List<Piece>> mapPiece = new HashMap<String, List<Piece>>();
	
	public UDPThread(String peerID) {
		this.peerID = peerID;
	}
	
	public void run() {
		System.out.println("������UDP�̣߳�����PEER֮�����ͨ��");
		try {
			DatagramSocket ds = new DatagramSocket(Constant.TRACKER_UDP_PORT);

			// ���������߳�
			new Thread(new HeartThread(ds, peerID)).start();

			// ѭ������
			byte[] buf = new byte[1024];
			DatagramPacket rp = new DatagramPacket(buf, 1024);
			boolean isEnd = false;
			while (!isEnd) {
				ds.receive(rp);
				// ȡ����Ϣ
				String content = new String(rp.getData(), 0, rp.getLength());
				System.out.println("-------------" + content);
				JSONObject json = JSONObject.fromObject(content);
				System.out.println(json.toString());
				String contentHash = json.getString(Constant.CONTENT_HASH);
				String action = json.getString(Constant.ACTION);
				
				//Tracker����Peer��UDP������Ӧ�����ݰ���Peer�Ĺ���IP��ַ�Ͷ˿ڣ�Peer�������ݴ���Ϣ�ж��Լ����������ͣ����Ƿ�ΪNAT����PubIP������LocalIP����ʱ�����ã�
				if(action.equals(Constant.ACTION_HEARTBEAT_RESPONSE)) {
					
				} 
				
				//UDP��͸��Ӧ���յ�����tracker�Ĵ�͸��Ӧ��peer��Զ�peer���д򶴣��ڴ˽������δ�
				else if(action.equals(Constant.ACTION_NAT_TRAVERSAL_ORDER)) {
					String targetPeerIP = json.getString(Constant.TARGET_PEER_IP);
					String targetPeerPort = json.getString(Constant.TARGET_PEER_PORT);
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
				} 
				
				//�յ���������󣬷��ر�����ӵ�еı���Ƭ�Σ�������������Ӧ
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_REQUEST)) {
//					List<Piece> pieces = DB.getPiecesByContentHash(contentHash);
//					submitP2PhandShakeResponse(ds, contentHash, pieces, ds.getInetAddress().getHostName(), ds.getPort());
				} 
				
				//�յ�������Ӧ���ж��Ƿ���ȫ�����أ�����ǣ������Ƶ����ƴ��
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
					//ȫ������
					if(total == current) {
						//�ļ��ܴ�С����λ��kb��
						int size = 10000;
						//������Ƶƴ�ӣ��õ���Ҫ�������ƵƬ��
						List<Piece> resultPieces = generateFinalPieces(mapPiece.get(contentHash), size, ds, contentHash);
						
						//ƴ����ɺ󽫼�¼��mapPiece��ɾ��
						mapPiece.remove(contentHash);
						
						//���η�����������
						for(Piece p : resultPieces) {
							submitP2PPieceRequest(ds, contentHash, p.getOffset(), p.getLength(), p.getPeer().getUdpIp(), p.getPeer().getUdpPort());
						}
						
						//��������Ϻ󣬽������������map������
						mapTotal.remove(contentHash);
						mapCurrent.remove(contentHash);
					}
				} 
				
				//�յ���������������󷽷��ͱ�������
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
				
				//���յ��Զ�peer������������Ӧ��
				else if(action.equals(Constant.ACTION_P2P_PIECE_RESPONSE)) {
					File f = new File("C:/video/1.3gp");
					DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
					fileOut.write(json.get(Constant.DATA).toString().getBytes(), (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
					fileOut.close();
				} 
				
				//����
				else {
					
				}
				
				
				//�������Ҫ��ȡ�����ݣ�����peer��������
				if(segments.size() > 0) {
					for(int i = 0; i < segments.size(); i++) {
						Segment s = segments.get(i);
						
						//���α���tracker���ص�ÿһ��peer
						for(int j = 0; j < s.getPeerList().size(); i ++) {
							Peer p = s.getPeerList().get(j);
							//peer��tracker����Э����͸����
							submitNATTraversalAssist(ds, p.getPeerID(), s.getContentHash());
							//�½�һ���̣߳����߳����ӳ����룬��ȥ�������������ӳ������Ŀ�����öԶ˵�peer�ڵ���ʱ��ȥ���д򶴣�
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
			json.put("info", "��");
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
	 * ###########��Ƶƴ���㷨#############
	 * 1.�ҳ����һ�����ݣ�����
	 * 
	 * ###################################
	 * @param sourcePieces peer�ṩ������Ƭ��
	 * @param size �ļ���С
	 * @return ������Ҫ���������Ƭ��
	 */
	public List<Piece> generateFinalPieces(List<Piece> sourcePieces, int size, DatagramSocket ds, String contentHash) {
		List<Piece> finalPieces = new ArrayList<Piece>();
		int offset = 0;
		int end = 0;
		
		while (end < size) {
			//ȥlist����offset��ʼ��Ƭ��
			Piece p = getLargestPiece(offset, sourcePieces);
			//���Ƭ�β����ڣ���ȥcdn�����أ����صķ�Χ�ǣ���ʼ��offset��������list�����д��ڵ�ǰoffset��Ƭ���У�offset��С���Ǹ�
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
	 * ��ȡĳ��㿪ʼ������PieceƬ��
	 * @param offset ��ʼλ��
	 * @param pieces ��ѡpiece
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
