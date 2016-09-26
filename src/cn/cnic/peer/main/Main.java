package cn.cnic.peer.main;

import cn.cnic.peer.connect.ExternalThread;
import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.connect.UDPThread;

/**
 * ���������
 * @author DX
 *
 */
public class Main {
	public static void main(String[] args) {
		String peerID = "duxin";
		new Main().startClient(peerID);
	}
	
	/**
	 * ����ʼ
	 */
	public void startClient(String peerID) {
		
		//����tcp����tracker��������trackerͨ��
		TCPThread tcp = new TCPThread(peerID);
		Thread t1 = new Thread(tcp);
		t1.start();
		
		//����udp�̣߳�����peer������ͨ��
		UDPThread udp = new UDPThread(peerID);
		Thread t3 = new Thread(udp);
		t3.start();
		
		//������������̣߳����ڼ�������������������
		ExternalThread ext = new ExternalThread();
		Thread t2 = new Thread(ext);
		t2.start();
	}
}
