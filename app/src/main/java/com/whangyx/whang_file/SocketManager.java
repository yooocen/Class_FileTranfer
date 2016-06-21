package com.whangyx.whang_file;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by chen on 16-5-3.
 */
public class SocketManager {
    private ServerSocket server;
    private Handler handler = null;
    public SocketManager(Handler handler){
        this.handler = handler;
        int port = 9999;
        while(port > 9000){
            try {
                server = new ServerSocket(port);
                break;
            } catch (Exception e) {
                port--;//假设上一个port不行的话,则减一再试一下
            }
        }
        SendMessage(1, port);//发送文件
        Thread receiveFileThread = new Thread(new Runnable(){
            @Override
            public void run() {  //接收文件的线程
                while(true){//接收文件
                    ReceiveFile();
                }
            }
        });
        receiveFileThread.start();
    }
    void SendMessage(int what, Object obj){
        if (handler != null){
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }
    //接收文件
    void ReceiveFile(){
        try{
            //接收文件名
            Socket name = server.accept();//
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            name.close();
            SendMessage(0, "正在接收:" + fileName);//0代表接收
            //接收文件数据
            Socket data = server.accept();
            InputStream dataStream = data.getInputStream();
            String savePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
            FileOutputStream file = new FileOutputStream(savePath, false);//在当前路径下建立这个文件
            byte[] buffer = new byte[1024];
            int size = -1;
            while ((size = dataStream.read(buffer)) != -1){//很常见的写法,从右往左读
                file.write(buffer, 0 ,size);
            }
            file.close();
            dataStream.close();
            data.close();
            SendMessage(0, fileName + " 接收完成");
        }catch(Exception e){
            SendMessage(0, "接收错误:\n" + e.getMessage());
        }
    }

    public void SendFile(ArrayList<String> fileName, ArrayList<String> path, String ipAddress, int port){
        try {
            for (int i = 0; i < fileName.size(); i++){//可能发送了多个文件
                Socket name = new Socket(ipAddress, port);
                OutputStream outputName = name.getOutputStream();
                OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
                BufferedWriter bwName = new BufferedWriter(outputWriter);
                bwName.write(fileName.get(i));
                bwName.close();
                outputWriter.close();
                outputName.close();
                name.close();
                SendMessage(0, "正在发送" + fileName.get(i));

                Socket data = new Socket(ipAddress, port);
                OutputStream outputData = data.getOutputStream();
                FileInputStream fileInput = new FileInputStream(path.get(i));
                int size = -1;
                byte[] buffer = new byte[1024];
                while((size = fileInput.read(buffer, 0, 1024)) != -1){
                    outputData.write(buffer, 0, size);
                }
                outputData.close();
                fileInput.close();
                data.close();
                SendMessage(0, fileName.get(i) + " 发送完成");
            }
            SendMessage(0, "所有文件发送完成");
        } catch (Exception e) {
            SendMessage(0, "发送错误:\n" + e.getMessage());
        }
    }
}
