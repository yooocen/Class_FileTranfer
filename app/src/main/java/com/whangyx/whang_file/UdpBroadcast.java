package com.whangyx.whang_file;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by chen on 16-5-12.
 */
public class UdpBroadcast {
    private static final int udpPort = 15000;
    private boolean udpStart = true;

    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {

                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {

                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress()) {
                        Log.e("udp", inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("udp", ex.toString());
        }
        return null;
    }

    public class BroadCastUdp extends Thread {
        private String dataString;
        private DatagramSocket udpSocket;

        public BroadCastUdp(String dataString) {//构造函数
            this.dataString = dataString;
        }

        @Override
        public void run() {
            DatagramPacket dPacket = null;

            try {
                udpSocket = new DatagramSocket(udpPort);

                byte[] buffer = new byte[1024];
                dPacket = new DatagramPacket(buffer,40);//报文总长为40字节
                byte[] data = dataString.getBytes();
                dPacket.setData(data);
                dPacket.setLength(data.length);
                dPacket.setPort(udpPort);

                InetAddress broadcastAddr;

                broadcastAddr = InetAddress.getByName("255.255.255.255");
                dPacket.setAddress(broadcastAddr);
            } catch (Exception e) {
                Log.e("udp", e.toString());
            }
            while (udpStart) {
                try {
                    udpSocket.send(dPacket);
                    sleep(10);
                } catch (Exception e) {
                    Log.e("udp", e.toString());
                }
            }

            udpSocket.close();
        }
    }

    public void start()
    {
        String ip = getLocalIPAddress();
        BroadCastUdp broad = new BroadCastUdp(ip);
        broad.start();
    }

    public void stop()
    {
        udpStart =false;
    }
}
