package file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Send {

	public static DatagramSocket dataSocket;
	public static final int PORT = 1234;
	public static byte[] sendDataByte;
	public static DatagramPacket dataPacket;

	public static void main(String[] args) throws IOException,
			InterruptedException {
		String filePath = "I:/1.3gp";
		DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
		dataSocket = new DatagramSocket(PORT + 1);
		sendDataByte = new byte[1024];
		int read = 0;
		long a = new Date().getTime();
		while (true) {
			if (fis != null) {
				read = fis.read(sendDataByte);
			}
			if (read == -1) {
				System.out.print("用时：");
				System.out.println(new Date().getTime() - a);
				break;
			}
			dataPacket = new DatagramPacket(sendDataByte, sendDataByte.length,
					(Inet4Address) Inet4Address.getByName("60.205.187.36"), PORT);
			dataSocket.send(dataPacket);
			TimeUnit.MICROSECONDS.sleep(1);// 限制传输速度
		}
		fis.close();
	}
}
