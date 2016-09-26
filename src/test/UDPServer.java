package test;

public class UDPServer extends UDPAgent {
public static void main(String[] args) throws Exception {
   new UDPServer(1111).start();
}
public UDPServer(int port) {
   super(port);
}
}