package cn.cnic.peer.main;

import cn.cnic.peer.connect.ExternalThread;
import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.connect.UDPThread;

/**
 * 程序入口类
 * @author DX
 *
 */
public class Main {
	public static void main(String[] args) {
		String peerID = "duxin";
		new Main().startClient(peerID);
	}
	
	/**
	 * 程序开始
	 */
	public void startClient(String peerID) {
		
		//启动tcp连接tracker，用于与tracker通信
		TCPThread tcp = new TCPThread(peerID);
		Thread t1 = new Thread(tcp);
		t1.start();
		
		//启动udp线程，用于peer间数据通信
		UDPThread udp = new UDPThread(peerID);
		Thread t3 = new Thread(udp);
		t3.start();
		
		//启动任务监听线程，用于监听播放器发来的请求
		ExternalThread ext = new ExternalThread();
		Thread t2 = new Thread(ext);
		t2.start();
	}
}
