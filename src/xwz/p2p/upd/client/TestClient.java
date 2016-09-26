/**
 * MainClient.java Nov 23, 2009
 * 
 * Copyright 2009 xwz, Inc. All rights reserved.
 */
package xwz.p2p.upd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import xwz.p2p.upd.util.MyProtocol;
import xwz.p2p.upd.util.StringUtil;

/**
 * @author xwz
 * @version 1.0, Nov 23, 2009 11:24:47 PM
 */
public class TestClient {

	public static String mes = null;
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Client------------");

		Scanner scanner = new Scanner(System.in);

		System.out.print("Server IP:");
		String ip = scanner.next();// ������ip��ַ

		System.out.print("Server port:");
		int port = scanner.nextInt();// �������˿�

		// UDP
		DatagramSocket ds = new DatagramSocket();

		// ���������߳�
		String heart = "heart";
		DatagramPacket hp = new DatagramPacket(heart.getBytes(), heart.length(), InetAddress.getByName(ip), port);
		new Thread(new HeartThread(ds, hp)).start();
		
		System.out.print("target IP:");
		String targetIP = scanner.next();
		
		System.out.print("target Port:");
		String targetPort = scanner.next();
		
		String text = "test connection";
		DatagramPacket t = new DatagramPacket(text.getBytes(), text.length(), InetAddress.getByName(targetIP), Integer.parseInt(targetPort));
		ds.send(t);
		ds.send(t);
		ds.send(t);
		ds.send(t);
		ds.send(t);
		ds.send(t);
		ds.send(t);
		ds.send(t);
		ds.send(t);
		ds.send(t);
		
		ExternalThread ex = new ExternalThread(scanner);
		Thread th = new Thread(ex);
		th.start();

		// ѭ������
		byte[] buf = new byte[1024];
		DatagramPacket rp = new DatagramPacket(buf, 1024);
		boolean isEnd = false;
		while (!isEnd) {
			ds.receive(rp);
			// ȡ����Ϣ
			String content = new String(rp.getData(), 0, rp.getLength());
			String rip = rp.getAddress().getHostAddress();
			int rport = rp.getPort();
			// ������յ�������
			System.out.println(rip + ":" + rport + " >>>> " + content);
			if(mes!=null) {
				System.out.println(ip+":"+port);
				DatagramPacket dp = new DatagramPacket(mes.getBytes(), mes.length(), InetAddress.getByName(rip), rport);
				ds.send(dp);
				mes = null;
			}
		}
		ds.close();
	}

	
}
