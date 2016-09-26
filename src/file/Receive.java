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
		while (i == 0) {// �����ݣ���ѭ��
			receiveByte = new byte[1024];
			dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
			dataSocket.receive(dataPacket);
			i = dataPacket.getLength();
			// ��������
			if (i > 0) {
				// ָ�����յ����ݵĳ��ȣ���ʹ��������������ʾ����ʼʱ�����׺�����һ��
				fileOut.write(receiveByte, 0, i);
				fileOut.flush();
				i = 0;// ѭ������
			}
		}
		fileOut.close();
	}
}
