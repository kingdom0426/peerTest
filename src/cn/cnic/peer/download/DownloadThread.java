package cn.cnic.peer.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread implements Runnable {
	
	private String path;//定义下载资源的路径
    private String targetFile;//指定所下载文件的保存位置
    private int threadNum;//定义需要使用多少条线程下载资源
    private DownThread[] threads;//定义下载的线程对象
    private int fileSize;//定义下载的文件总大小
	public DownloadThread(String path, String targetFile, int threadNum) {
		this.path = path;
		this.targetFile = targetFile;
		this.threadNum = threadNum;
		threads=new DownThread[threadNum];
		this.targetFile=targetFile;
		
	}

	public void run() {
		try {
			URL url=new URL(path);
			HttpURLConnection conn=(HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5*1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "image/gif,image/jpeg,image/pjpeg,image/pjpeg, "
					+ "application/x-shockwave-flash, application/xaml+xml, "
					+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
					+ "application/x-ms-application, application/vnd.ms-excel, "
					+ "application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Connection", "Keep-Alive");
			fileSize=conn.getContentLength();//得到文件大小
			conn.disconnect();
			int currentPartSize=fileSize/threadNum +1;
			RandomAccessFile file=new RandomAccessFile(targetFile, "rw");
			file.setLength(fileSize);
			file.close();
			for(int i=0;i<threadNum;i++){
				int startPos=i*currentPartSize;//计算每条线程下载的开始位置
				//每条线程使用一个RandomAccessFile进行下载
				RandomAccessFile currentPart=new RandomAccessFile(targetFile, "rw");
				//定位该线程的下载位置
				currentPart.seek(startPos);
				threads[i] =new DownThread(startPos,currentPartSize,currentPart);
				threads[i].start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class DownThread extends Thread{
		private int startPos;
		private int currentPartSize;
		private RandomAccessFile currentPart;
		//定义该线程已下载的字节数
		private int length;
		
		public DownThread(int startPos, int currentPartSize,
				RandomAccessFile currentPart) {
			
			this.startPos = startPos;
			this.currentPartSize = currentPartSize;
			this.currentPart = currentPart;
		}
		@Override
		public void run() {
			try {
				URL url=new URL(path);
				HttpURLConnection conn=(HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5*1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "image/gif,image/jpeg,image/pjpeg,image/pjpeg, "
						+ "application/x-shockwave-flash, application/xaml+xml, "
						+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
						+ "application/x-ms-application, application/vnd.ms-excel, "
						+ "application/vnd.ms-powerpoint, application/msword, */*");
						conn.setRequestProperty("Accept-Language", "zh-CN");
						conn.setRequestProperty("Charset", "UTF-8");
						InputStream inStream=conn.getInputStream();
						//跳过startPos个字节，表明该线程只下载自己负责的那部分
						skipFully(inStream, this.startPos);
						byte[] buffer=new byte[1024];
						int hasRead=0;
						//读取网络数据，并写入本地文件中
						while(length < currentPartSize &&(hasRead =inStream.read(buffer))>0){
							currentPart.write(buffer, 0, hasRead);
							//累计该线程下载的总大小
							length +=hasRead;
						}
				        currentPart.close();
				        inStream.close();
			}catch(Exception e){
				e.printStackTrace();
			} 
			
		}
	}
	//定义一个为InputStream 跳过bytes字节的方法（直接使用skip方法不可靠）
	public static void skipFully(InputStream in,long bytes)throws IOException{
		long remainning =bytes;
		long len=0;
		while(remainning>0){
			len=in.skip(remainning);
			remainning -=len;
		}
	}
}
