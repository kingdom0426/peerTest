package file;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receive {

	public static DatagramSocket dataSocket;
	public static final int PORT = 1234;
	public static byte[] receiveByte;
	public static DatagramPacket dataPacket;

	public static void main(String[] args) throws IOException {
		dataSocket = new DatagramSocket(PORT);
		File f = new File("C:/video/1.3gp");
		if(!f.exists()) {
			f.createNewFile();
		}
		DataOutputStream fileOut = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(f)));
		int i = 0;
		while (i == 0) {// 无数据，则循环
			receiveByte = new byte[1024];
			dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
			dataSocket.receive(dataPacket);
			i = dataPacket.getLength();
			// 接收数据
			if (i > 0) {
				// 指定接收到数据的长度，可使接收数据正常显示，开始时很容易忽略这一点
				fileOut.write(receiveByte, 0, i);
				fileOut.flush();
				i = 0;// 循环接收
			}
		}
		fileOut.close();
	}
}
