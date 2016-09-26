package http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.http.HttpStatus;

import com.sun.net.httpserver.*;

public class HttpSer {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8765), 0);
		server.createContext("/download",new MyResponseHandler());
		server.setExecutor(null); // creates a default executor
		server.start();

	}
	public static class MyResponseHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) throws IOException {
            //�������Ĵ�������     
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