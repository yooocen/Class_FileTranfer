package com.whangyx.whang_file;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URLEncoder;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SendFile {

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
	private boolean sendFlag = false;
	private boolean receiveDone = false;
	private TextView recTips;
	private byte[] zeroByte = new byte[4];

	public void start(final String filePath) {
		//send(filePath);

		new Thread() {
			@Override
			public void run() {
				sendFlag = true;
				while (sendFlag) {
					send(filePath);
					sendFlag =false;

				}
			}
		}.start();
	}

	public void setSocket(Socket temp) {
		socket = new Socket();
		socket = temp;

	}

	private void send(String filePath) { //work here!!!!!!!!!!!!!broken pipe

		try {
			Log.v("send", filePath);
			Log.v("send", "send start");

			OutputStream out = socket.getOutputStream();
			File file = new File(filePath);

			String fileName = file.getName();
			Log.v("send fileName", fileName);

			byte[] b = new byte[4];
			Long totalSize = file.length() +Long.valueOf(fileName.length());
			//BigInteger big = new BigInteger(totalSize.toString());

			//b = totalSize.toString().getBytes();
			b = longToBytes(totalSize);
			out.write(b);//在out中写入b

			Log.v("send totalSize", Long.toString(totalSize));
			Log.v("send totalSize", Long.toHexString(totalSize));

			b = intToBytes(fileName.length());
			out.write(b);
			Log.v("send fileNameSize", Integer.toHexString(fileName.length()));

			byte[] extraBytes = new byte[4];
			//out.write(extraBytes);


			byte[] fileNameBytes = new byte[fileName.length()];
			fileNameBytes = fileName.getBytes();
			out.write(fileNameBytes,0,fileNameBytes.length);
			//byte b0 = '\n';
			//out.write(b0);


			RandomAccessFile fileOutStream = new RandomAccessFile(file, "r");  //访问保存数据记录的文件
			byte[] buffer = new byte[1024];
			int len = -1;
			while( (len = fileOutStream.read(buffer)) != -1)
			{
				out.write(buffer, 0, len);
				//if(i==10) break;
			}

			fileOutStream.close();
			//out.close();

			Log.v("send", "send succeed!");
		} catch (Exception e) {
//			Log.v("send error", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}


	public static byte[] intToBytes(int v){//int有两个字节或者4个字节
		byte[] writeBuffer = new byte[ 4 ];


		writeBuffer[0] = (byte)(v >>> 24);
		writeBuffer[1] = (byte)(v >>> 16);
		writeBuffer[2] = (byte)(v >>>  8);
		writeBuffer[3] = (byte)(v >>>  0);

		return writeBuffer;
	}

	public String getFileName(String pathandname) {

		int start = pathandname.lastIndexOf("/");
		int end = pathandname.lastIndexOf(".");
		if (start != -1 && end != -1) {
			return pathandname.substring(start + 1, end);
		} else {
			return null;
		}

	}

	public final byte[] longToBytes(long v) {
		byte[] writeBuffer = new byte[ 4 ];//4个字节


		writeBuffer[0] = (byte)(v >>> 24);//无符号右移24位,long有4个字节,一共32位
		writeBuffer[1] = (byte)(v >>> 16);
		writeBuffer[2] = (byte)(v >>>  8);
		writeBuffer[3] = (byte)(v >>>  0);

		return writeBuffer;
	}
}
