package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpC {

	/**
	 * @param args
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static void main(String[] args) throws IllegalStateException,
			IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost=new HttpPost("http://127.0.0.1:8765/download");
//		HttpGet httpgets = new HttpGet("http://127.0.0.1:8765");
//		HttpResponse response = httpclient.execute(httpgets);
		HttpResponse response=httpclient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instreams = entity.getContent();
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					instreams));
			StringBuilder sb=new StringBuilder();
			String line = null;
			while ((line = bf.readLine()) != null) {
				sb.append(line + "\n");
			}
			System.out.println(sb.toString());
//			httpgets.abort();
			httpPost.abort();
		}
	}

}
