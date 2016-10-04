package httpclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class Download {
	
	private final static String REMOTE_FILE_URL = "http://111.39.226.112:8114/VODS/1092287_142222153_0002222153_0000000000_0001143039.ts?Fsv_Sd=10&Fsv_filetype=2&Provider_id=gslb/program&Pcontent_id=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&FvOPid=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&Fsv_MBt=0&FvHlsIdx=3&UserID=&Fsv_otype=0&FvSeid=54e0e9c78502314b";
	
	private final static int BUFFER = 1024;

	public static void main(String[] args) {

	   HttpClient client = new HttpClient();
	   GetMethod httpGet = new GetMethod(REMOTE_FILE_URL);
		try {
			client.executeMethod(httpGet);
			
			InputStream in = httpGet.getResponseBodyAsStream();
		   
//			FileOutputStream out = new FileOutputStream(new File("E:/a.ts"));
//		   
//		    byte[] b = new byte[BUFFER];
//		    int len = 0;
//			while((len=in.read(b))!= -1){
//			    out.write(b,0,len);
//			}
			
			FileOutputStream out = new FileOutputStream(new File("E:/b.ts"));
			   
		    byte[] b = new byte[BUFFER];
		    int len = 0;
		    int i = 0;
		    for(int k = 0; k < 1000; k++) {
		    	len = in.read(b);
		    	if(k >=800) {
		    		out.write(b,0,len);
		    	}
		    }
			in.close();
			out.close();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			httpGet.releaseConnection();
		}
       	System.out.println("download, success!!");
       }
}
