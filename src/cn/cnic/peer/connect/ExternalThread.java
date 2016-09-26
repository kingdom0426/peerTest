package cn.cnic.peer.connect;

import java.util.Scanner;

/**
 * 这个线程用来监听播放器发来的请求
 * @author DX
 *
 */
public class ExternalThread implements Runnable {

	public void run() {
		while (true) {
			System.out.print("请输入get请求地址：");
			Scanner scan = new Scanner(System.in);
			String url = scan.next();
			//如果在本地系统中存在
			if (isLocalExist(url)) {
		
			}
			//如果在本地系统不存在
			else {
				TCPThread.urls.add(url);
			}
		}
	}
	
	//判断此URL是否在本地系统中存在
	private boolean isLocalExist(String url) {
		return false;
	}
}
