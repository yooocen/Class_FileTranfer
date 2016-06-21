package com.whangyx.whang_file;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by chen on 16-5-12.
 *
 * feild:  boolean endFlag
 *         Socket s
 *
 * Method: connect()
 *         isConnected()
 *         getSocket()
 */
public class TcpConnect {

    private Boolean endFlag = false;
    public Socket s;

    public boolean connect(int port) {

        try {

            ServerSocket ss = new ServerSocket(port);
            while (!endFlag) {
                // 等待客户端连接
                s = ss.accept();//连接上以后会生成一个新的ServerSocket
                if (s.isConnected()) {
                    endFlag = true;
                }
                //s.close();

            }

            System.out.print("ServerSocket is turning down");
            ss.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return endFlag;
    }

    public Boolean isConnected()
    {
        return endFlag;
    }

    public Socket getSocket()
    {
        return s;
    }
}
