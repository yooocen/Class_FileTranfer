package com.whangyx.whang_file;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;

public class ReceiveFile {

	private BigInteger fileSize;
	private BigInteger totalSize;
	private BigInteger fileNameSize;
	private String fileName = null;
	private Button btn;
	private ProgressBar pb;
	private TextView textView;
	private long bytesReceived = 0;
	private EditText et;
	private Socket socket;
	private DataInputStream in;
	private boolean threadFlag = false;
	private boolean receiveDone = false;
	private TextView recTips;
	private byte[] zeroByte = new byte[4];


	public boolean start() {
		receive();
		return true;
	}

	/*
	 * public boolean connect() {
	 * 
	 * try { SocketAddress socketAddress = new InetSocketAddress(ip, port);
	 * socket = new Socket(); socket.connect(socketAddress); if
	 * (socket.isConnected()) { Toast.makeText(ContextUtil.getInstance(),
	 * "与主机连接成功！", Toast.LENGTH_SHORT).show(); return true; } else {
	 * 
	 * return false; }
	 * 
	 * } catch (IOException e) { Log.v("receive error",
	 * e.getLocalizedMessage()); // e.printStackTrace();
	 * Toast.makeText(ContextUtil.getInstance(), "与主机连接失败",
	 * Toast.LENGTH_SHORT).show(); return false; } }
	 */
	public void setSocket(Socket temp) {
		socket = new Socket();
		socket = temp;

	}

	private void receive() {
//
		try {
			makeDir();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		new Thread()
		{		
			@Override
			public void run() {
				while(true)
					try {
						in = new DataInputStream(socket.getInputStream());

//						if(in.available() >0)
//						{
//							int i = 0;
//							Log.v("step", "!!!!!!!!!!");
//							threadFlag = true;
						while (in.available() > 0) {
							//threadFlag =false;
//								Log.v("count", String.valueOf(i++));
							download();

							if (in.available() == 0) {
								Log.v("step", "succeed!!");
								//in.close();
								return;
							}
						}

						//}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // 获取socket上的信息
		}
		}.start();
	}

	private void download() {

		try {
//			if(in.available() ==0)
//			{
//				Log.v("receive", "break");
//				threadFlag =false;
//				return;
//			}
			byte[] bytes = new byte[8]; // 服务器上已定义了发送一段8字节的信息，所以定义8字节
			//第一段
			in.read(bytes);// 获取文件总大小（包括fileNameSize,fileSize）
			totalSize = new BigInteger(bytes);
			Log.v("receive", totalSize.toString());
//			if (totalSize.intValue() == 0) {
//				Log.v("receive", "break");
//				threadFlag = false;
//				return;
//			}
			//第二段
			in.read(bytes);// 获取文件名大小
			fileNameSize = new BigInteger(bytes);
			Log.v("receive", fileNameSize.toString());
			//第三段
			byte[] extraBytes = new byte[4]; // 读取一段多余字节。读了不用
			in.read(extraBytes);

			bytes = new byte[fileNameSize.intValue() - 8 ]; // 定义名字所含字节大小。服务器所用的是utf-8.
			//第四段
			in.read(bytes);
			fileName = new String(bytes, "utf-8");
			Log.v("receive", "1.4");
			File file = new File("/mnt/sdcard/WifiFile/" + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			Log.v("receive", "1.5");
			bytes = new byte[1 * 1024];

			bytesReceived = 8 + 8 + fileNameSize.intValue();//文件名大小8字节 文件大小8字节,这两个是说具体有多大,第三个就是具体文件名的大小
			//文件的流的大小:bytesReceived
			Log.v("receive", "2");
			int len = -1;
			
			if((totalSize.intValue() - bytesReceived) < 1024)//0-1MB
			{
				if ((totalSize.intValue() - bytesReceived)>0) {
					bytes = new byte[(int) (totalSize.intValue() - bytesReceived)];
					in.read(bytes);//读取输入流的文件
					fos.write(bytes);//写入本机输入流
					bytesReceived += (totalSize.intValue() - bytesReceived);
				}
			}
			else
			{
				while ((len = in.read(bytes)) != -1
						&& bytesReceived < totalSize.intValue()) {
					fos.write(bytes, 0, len);
					bytesReceived += len;

					if ((totalSize.intValue() - bytesReceived)<1024) {
						bytes = new byte[(int) (totalSize.intValue() - bytesReceived)];
						in.read(bytes);
						fos.write(bytes);
						bytesReceived += (totalSize.intValue() - bytesReceived);
						break;
					}
				}
			}

			totalSize = new BigInteger(zeroByte);
			Log.v("receive", "receive succeed!");

		//	in.reset();
			fos.close();
			// download();
		} catch (Exception e) {
			//Log.v("download error", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public String getFileName()
	{
		return fileName;
	}
	private void makeDir() throws IOException {
		String Path =FileUtil.setMkdir(ContextUtil.getInstance());
		Log.i("tag",Path);
	}

}
