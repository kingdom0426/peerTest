package cn.cnic.peer.connect;

import java.util.Scanner;

/**
 * ����߳�������������������������
 * @author DX
 *
 */
public class ExternalThread implements Runnable {

	public void run() {
		while (true) {
			System.out.print("������get�����ַ��");
			Scanner scan = new Scanner(System.in);
			String url = scan.next();
			//����ڱ���ϵͳ�д���
			if (isLocalExist(url)) {
		
			}
			//����ڱ���ϵͳ������
			else {
				TCPThread.urls.add(url);
			}
		}
	}
	
	//�жϴ�URL�Ƿ��ڱ���ϵͳ�д���
	private boolean isLocalExist(String url) {
		return false;
	}
}
