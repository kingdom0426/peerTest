package cn.cnic.peer.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread implements Runnable {
	
	private String path;//����������Դ��·��
    private String targetFile;//ָ���������ļ��ı���λ��
    private int threadNum;//������Ҫʹ�ö������߳�������Դ
    private DownThread[] threads;//�������ص��̶߳���
    private int fileSize;//�������ص��ļ��ܴ�С
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
			fileSize=conn.getContentLength();//�õ��ļ���С
			conn.disconnect();
			int currentPartSize=fileSize/threadNum +1;
			RandomAccessFile file=new RandomAccessFile(targetFile, "rw");
			file.setLength(fileSize);
			file.close();
			for(int i=0;i<threadNum;i++){
				int startPos=i*currentPartSize;//����ÿ���߳����صĿ�ʼλ��
				//ÿ���߳�ʹ��һ��RandomAccessFile��������
				RandomAccessFile currentPart=new RandomAccessFile(targetFile, "rw");
				//��λ���̵߳�����λ��
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
		//������߳������ص��ֽ���
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
						//����startPos���ֽڣ��������߳�ֻ�����Լ�������ǲ���
						skipFully(inStream, this.startPos);
						byte[] buffer=new byte[1024];
						int hasRead=0;
						//��ȡ�������ݣ���д�뱾���ļ���
						while(length < currentPartSize &&(hasRead =inStream.read(buffer))>0){
							currentPart.write(buffer, 0, hasRead);
							//�ۼƸ��߳����ص��ܴ�С
							length +=hasRead;
						}
				        currentPart.close();
				        inStream.close();
			}catch(Exception e){
				e.printStackTrace();
			} 
			
		}
	}
	//����һ��ΪInputStream ����bytes�ֽڵķ�����ֱ��ʹ��skip�������ɿ���
	public static void skipFully(InputStream in,long bytes)throws IOException{
		long remainning =bytes;
		long len=0;
		while(remainning>0){
			len=in.skip(remainning);
			remainning -=len;
		}
	}
}
