package test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
public class UDPClient extends UDPAgent {
/**
* @param args
*/
public static void main(String[] args) throws Exception {
   new UDPClient("www.javadoc.cn", 2008, -1).start();
}
String serverName;
int serverPort;
SocketAddress server;
public UDPClient(String host, int port, int localPort) {
   super(localPort);
   this.server = new InetSocketAddress(host, port);
}
public void start() throws Exception {
   println("start");
   init();
   register();
   new Thread(this).start();// recive thread
   receive();
}
public void onReceive(DatagramPacket rec) {
   try {
    report(rec);
    if (rec.getSocketAddress().equals(server)) {
     doCommand(new String(rec.getData(), rec.getOffset(), rec
       .getLength()));
    }
   } catch (Exception e) {
    e.printStackTrace();
   }
}
public void report(DatagramPacket rec) throws Exception {
   String s = rec.getSocketAddress()
     + new String(rec.getData(), rec.getOffset(), rec.getLength());
   byte[] buf = s.getBytes();
   ds.send(new DatagramPacket(buf, buf.length, server));
}
public void register() throws Exception {
   String msg = "register " + getLocalAddress() + " " + ds.getLocalPort();
   doSend(server, msg.getBytes());
}
public String getLocalAddress() throws Exception {
   InetAddress addr = InetAddress.getLocalHost();
   return addr.getHostAddress();
}
}