package cn.cnic.peer.connect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.http.HttpStatus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * ����߳�������������������������
 * @author DX
 *
 */
public class ExternalThread implements Runnable {

	/**
	 * ����һ��http server��������������������get����
	 */
	public void run() {
		System.out.println("����������̣߳����ڼ�ز�����������http����");
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8765), 0);
			server.createContext("/download",new MyResponseHandler());
			server.setExecutor(null);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class MyResponseHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) throws IOException {
            //�������Ĵ�����     
            //����������Ӧʱ����ѭHTTPЭ��  
            String responseString = "<font color='#ff0000'>Hello! This a HttpServer!</font>"; 
            //������Ӧͷ  
            httpExchange.sendResponseHeaders(HttpStatus.SC_OK, responseString.length());    
            OutputStream os = httpExchange.getResponseBody();    
            os.write(responseString.getBytes());    
            os.close();
        }
    }
}
