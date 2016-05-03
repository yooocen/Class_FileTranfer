package com.whangyx.whang_file;



import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.xys.libzxing.zxing.activity.CaptureActivity;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

import java.util.ArrayList;


@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    //传输用
    private Button QR;
    private Button tranfer;
    private Handler handler;
    private SocketManager socketManager;
    String ipAddress = null;//ip地址
    int port = 0;//端口号

    String getport;
    //分栏用
    TabHost mTabHost = null;
    TabWidget mTabWidget = null;
    //热点用
    Bitmap qrCodeBitmap;  //二维码的图片
    String wifiSSID,wifiPwd;   //热点的名字和密码
    private WifiHotspot wifiHotspot;
    private ConnectToWifi connectToWifi;
    private Button open,close,connect;
    private ImageView wifiImage;
    private String[] splitResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);
        //以下是分栏显示的
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();//说明文档说这个是在findViewById之后必须写的一个函数,写一下就行了
        mTabWidget = mTabHost.getTabWidget();//在Tabhost获取一个tabwidget
        mTabHost.addTab(mTabHost.newTabSpec("tab1").setContent(  //TabHost.TabSpec又是一个类 (indicator ,content ,tag)
                R.id.LinearLayout001).setIndicator("传输"));
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setContent(
                R.id.LinearLayout002).setIndicator("管理"));

        //*********************************************以下是文件传输的:********************************************
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
                if(msg.what==1){
                   getport=msg.obj.toString() ;
                }
            }
        };

        tranfer = (Button) findViewById(R.id.transfer);
        QR =(Button) findViewById(R.id.QR);


        tranfer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilesViewActivity.class);

                startActivityForResult(intent,1);
            }
        });

        QR.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //首先获取本机的ip地址和端口port
                String ipAddress1 = GetIpAddress();
                String port1 = getport;
                String information= ipAddress1+"\n"+port1;
                qrCodeBitmap = EncodingUtils.createQRCode(information, 250, 250, null);
                wifiImage.setImageBitmap(qrCodeBitmap);//在这里画图


            }
        });
        //****************************************以下是热点连接的*****************************************************
        open=(Button)findViewById(R.id.open);//打开热点
        close = (Button)findViewById(R.id.close);//关闭热点
        wifiImage = (ImageView) this.findViewById(R.id.wifiImage);

        wifiHotspot = new WifiHotspot(this);
        wifiHotspot.netConfig = new WifiConfiguration();

        connect = (Button)findViewById(R.id.connect);

//        if(wifiHotspot.isWifiApEnabled()){
//
//            wifiHotspot.netConfig = wifiHotspot.getWifiApConfiguration();
//
//            wifiSSID = wifiHotspot.netConfig.SSID;
//            wifiPwd = wifiHotspot.netConfig.preSharedKey;
//            Log.e("DEBUG","检测到"+wifiSSID+" "+wifiPwd);
//            String transport = wifiSSID + "\n" + wifiPwd;
//            qrCodeBitmap = EncodingUtils.createQRCode(transport, 500, 500, null);
////            wifiImage.setImageBitmap(qrCodeBitmap);//!!!!!!!!!!!!!!!!!!!!!!!!!
//        }

        open.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断热点开关是否打开
                if(!wifiHotspot.isWifiApEnabled()){
                    wifiSSID = String.valueOf(Math.random()).substring(2,2+8);
                    wifiPwd = String.valueOf(Math.random()).substring(2,2+8);
                    String transport = wifiSSID + "\n" + wifiPwd;
                    wifiHotspot.startWifiAp(wifiSSID,wifiPwd);
                    qrCodeBitmap = EncodingUtils.createQRCode(transport, 250, 250, null);
//
                    wifiImage.setImageBitmap(qrCodeBitmap);//在这里画图

//                    AlertDialog.Builder bulilder = new AlertDialog.Builder(MainActivity.this);
//                    bulilder.setTitle("二维码")
//                            .setMessage("二维码显示")
////                            .setView(wifiImage)
////                            .setSingleChoiceItems("one",-1,qrCodeBitmap)
//                            .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    });
//                    AlertDialog dialog = bulilder.create();
//                    dialog.show();
//                    showAlertDialog();
                }
                else if (wifiHotspot.isWifiApEnabled()){
                    Toast.makeText(MainActivity.this,"Wifi热点已开启",Toast.LENGTH_LONG).show();
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                wifiHotspot.closeWifiAp();
                wifiImage.setImageBitmap(null);
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开扫描界面扫描条形码或二维码
                Intent openCameraIntent = new Intent(MainActivity.this,CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });
        //***********************************以下是文件管理的:******************************************************
        Button btnfile = (Button)findViewById(R.id.btnfilemanager);
        Button btnrev =(Button)findViewById(R.id.btnreceived);


        final Intent intent =new Intent(this,FileManager.class);
        btnfile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(intent);
			}
		});

    }

    @Override
    //扫二维码的页面回馈的信息
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //一下是连接热点用的(当然也有获取ip地址和端口)问题在这里!!!!!!!
        if (resultCode == RESULT_OK && requestCode == 0) {  //扫一扫那个按键
            Bundle bundle = data.getExtras();//获取扫描到的信息
            String scanResult = bundle.getString("result");
            splitResult = scanResult.split("\n");//将扫描到的信息通过换行符分开
            if(true) {
                String wifiSSID = splitResult[0];
                String wifiPwd = splitResult[1];
                Log.e("DEBUG", "账号：" + wifiSSID + "\n" + "密码：" + wifiPwd);
                connectToWifi = new ConnectToWifi(MainActivity.this);
                connectToWifi.openWifi();
                connectToWifi.addNetwork(connectToWifi.CreateWifiInfo(wifiSSID, wifiPwd, 3));
            }
            else {
                //注意这里会有bug的,一定要扫对图以后才能发送
                ipAddress = splitResult[0];//通过二维码扫描获取ip地址
                port = Integer.parseInt(splitResult[1]);//!!!!
                Log.e("DEBUG", "ip地址：" + ipAddress + "\n" + "监听端口：" + port);
            }
        }

            if (resultCode == 6 && requestCode == 1) {  //发送按键transfer
                final ArrayList<String> fileName = data.getStringArrayListExtra("fileName");//文件名
                final ArrayList<String> safeFileName = data.getStringArrayListExtra("safeFileName");
                final String ipAddress0 = ipAddress;//ip地址
                final int port0 = port;//端口号
                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketManager.SendFile(fileName, safeFileName, ipAddress0, port0);//sendFile方法
                    }
                });
                sendThread.start();
            }

        //下面是显示二维码的对话框
//    private void showAlertDialog() {
//
//        final AlertDialog dlg = new AlertDialog.Builder(this).create();
//        dlg.show();
//        Window window = dlg.getWindow();
//        // *** 主要就是在这里实现这种效果的.
//        // 设置窗口的内容页面,alertdialog.xml文件中定义view内容
//        window.setContentView(R.layout.qrdialog);
//        // 为确认按钮添加事件,执行退出应用操作
//        ImageView wifiImage = (ImageView) findViewById(R.id.wifiImage);
//        if(!wifiHotspot.isWifiApEnabled()) {
//            wifiSSID = String.valueOf(Math.random()).substring(2, 2 + 8);
//            wifiPwd = String.valueOf(Math.random()).substring(2, 2 + 8);
//            String transport = wifiSSID + "\n" + wifiPwd;
//            wifiHotspot.startWifiAp(wifiSSID, wifiPwd);
//            Bitmap qrCodeBitmap = EncodingUtils.createQRCode(transport, 250, 250, null);
//            wifiImage.setImageBitmap(qrCodeBitmap);
//        }
//
//
//      //  Log.i("hhh",qrCodeBitmap.toString().trim());
//        Button tv_queding = (Button) window.findViewById(R.id.tv_content1);
//        tv_queding.setText("  确 定   ");
//        tv_queding.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("SdCardPath")
//            public void onClick(View v) {
//
//                dlg.cancel();
//            }
//        });
//
//        dlg.setCancelable(false);
//    }
    }
    public String GetIpAddress() {   //获取连接到的wifi分配给手机的ip地址
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF)+ "." +
                ((i >> 24 ) & 0xFF );
    }

}

     

