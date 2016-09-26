package udp;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {

	public static void main(String[] args) {
		try {
			DatagramSocket ds = new DatagramSocket(1111);
			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf,buf.length);
			while (true) {
				ds.receive(dp);
				String data = new String(dp.getData(), 0, dp.getLength());
				System.out.println(dp.getAddress().getHostName() + ":"
						+ dp.getPort() + "发来消息：" + data);
				DatagramPacket dp2;
				if (data.startsWith("conn")) {
					System.out.println("已打洞");
				} else if (data.startsWith("hello")) {
					String info = "hello im back";
					String[] mes = data.split("\\|");
					dp2 = new DatagramPacket(info.getBytes(), info.length(),
							InetAddress.getByName(mes[1]),
							Integer.parseInt(mes[2]));
					ds.send(dp2);
				} else {
					String[] mes = data.split("\\|");
					String info = "hello|" + dp.getAddress().getHostName()
							+ "|" + dp.getPort();
					dp2 = new DatagramPacket(info.getBytes(), info.length(),
							InetAddress.getByName(mes[0]),
							Integer.parseInt(mes[1]));
					ds.send(dp2);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
