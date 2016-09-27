package cn.cnic.peer.connect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.http.HttpStatus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 这个线程用来监听播放器发来的请求
 * @author DX
 *
 */
public class ExternalThread implements Runnable {

	/**
	 * 启动一个http server，来监听播放器发来的get请求
	 */
	public void run() {
		System.out.println("已启动监控线程，用于监控播放器发来的http请求");
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
            //针对请求的处理部分     
            //返回请求响应时，遵循HTTP协议  
            String responseString = "<font color='#ff0000'>Hello! This a HttpServer!</font>"; 
            //设置响应头  
            httpExchange.sendResponseHeaders(HttpStatus.SC_OK, responseString.length());    
            OutputStream os = httpExchange.getResponseBody();    
            os.write(responseString.getBytes());    
            os.close();
        }
    }
}
