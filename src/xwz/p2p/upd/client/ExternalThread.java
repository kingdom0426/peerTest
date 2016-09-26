package xwz.p2p.upd.client;

import java.util.Scanner;

public class ExternalThread implements Runnable {
	private Scanner scanner;
	
	public ExternalThread(Scanner scanner) {
		this.scanner = scanner;
	}

	public void run() {
		while (true) {
			System.out.print("请输入要发送的信息：");
			TestClient.mes = scanner.next();
		}
	}
}
