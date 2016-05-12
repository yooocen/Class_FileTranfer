package com.whangyx.whang_file;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.google.zxing.common.StringUtils;
import com.xys.libzxing.zxing.activity.CaptureActivity;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    //网络状态
    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;
    //进度条
    private ProgressBar process; //= (ProgressBar)findViewById(R.id.process);你不能这样写的,xml在onCreate那里设置的,你都还没设置
    //传输用
    private EditText info;

    private Button QR;
    private Button tranfer;
    private Handler handler;
    private SocketManager socketManager;
    String ipAddress = null;//ip地址
    int port = 0;//端口号
//    static int m = 0 ;

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
        //进度条
        process = (ProgressBar)findViewById(R.id.process);
        openNcloseWifi();
        //以下是分栏显示的
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();//说明文档说这个是在findViewById之后必须写的一个函数,写一下就行了
        mTabWidget = mTabHost.getTabWidget();//在Tabhost获取一个tabwidget
        mTabHost.addTab(mTabHost.newTabSpec("tab1").setContent(  //TabHost.TabSpec又是一个类 (indicator ,content ,tag)
                R.id.LinearLayout001).setIndicator("传输"));
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setContent(
                R.id.LinearLayout002).setIndicator("管理"));

        //*********************************************以下是文件传输的:********************************************
        info = (EditText) findViewById(R.id.info);//获取传输时的信息
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 0:
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");//SimpleDateFormat 是一个以国别敏感的方式格式化和分析数据的具体类
                        info.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());//[日期]正在发送至
                        if(msg.obj.toString().contains("接收完成")) {
                            process.incrementProgressBy(100);
                        }
                        else process.incrementProgressBy(20);
                        if(msg.obj.toString().contains("发送完成"))
                            process.incrementProgressBy(100);
                        else process.incrementProgressBy(20);
                        break;
                    case 1:
                        getport = msg.obj.toString();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        socketManager = new SocketManager(handler);//套接字管理者,形参是handler


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
                String port1 = getport;//getport;
//                Log.i("tag",port1);
                String information= ipAddress1+"\n"+port1;

                qrCodeBitmap = EncodingUtils.createQRCode(information, 250, 250, null);
//                wifiImage.setImageBitmap(qrCodeBitmap);//在这里画图
                showAlertDialog(qrCodeBitmap);


            }
        });
        //****************************************以下是热点连接的*****************************************************
        open=(Button)findViewById(R.id.open);//打开热点
        close = (Button)findViewById(R.id.close);//关闭热点
        wifiImage = (ImageView) this.findViewById(R.id.wifiImage);

        wifiHotspot = new WifiHotspot(this);
        wifiHotspot.netConfig = new WifiConfiguration();

        connect = (Button)findViewById(R.id.connect);

        if(wifiHotspot.isWifiApEnabled()){

            wifiHotspot.netConfig = wifiHotspot.getWifiApConfiguration();

            wifiSSID = wifiHotspot.netConfig.SSID;
            wifiPwd = wifiHotspot.netConfig.preSharedKey;
            Log.e("DEBUG","检测到"+wifiSSID+" "+wifiPwd);
            String transport = wifiSSID + "\n" + wifiPwd;
            qrCodeBitmap = EncodingUtils.createQRCode(transport, 500, 500, null);
//            wifiImage.setImageBitmap(qrCodeBitmap);//!!!!!!!!!!!!!!!!!!!!!!!!!
        }

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
                    showAlertDialog(qrCodeBitmap);
//                    wifiImage.setImageBitmap(qrCodeBitmap);//在这里画图
                }
                else if (wifiHotspot.isWifiApEnabled()){
//                    Toast.makeText(MainActivity.this,"Wifi热点已开启",Toast.LENGTH_LONG).show();
                    showAlertDialog(qrCodeBitmap);
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                getNetworkType();
                GetIpAddress();

                wifiHotspot.closeWifiAp();
//                wifiImage.setImageBitmap(null);
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
            String firstChar, secondChar;
            splitResult = scanResult.split("\n");//将扫描到的信息通过换行符分开

            firstChar = splitResult[0];
            secondChar = splitResult[1];
            if (firstChar.contains(".")) {
                ipAddress = firstChar;//通过二维码扫描获取ip地址
                port = Integer.parseInt(secondChar);//!!!!
                Log.e("tag", "ip地址：" + ipAddress + "\n" + "监听端口：" + port);
            } else {
                String wifiSSID = firstChar;
                String wifiPwd = secondChar;
                Log.e("tag", "账号：" + wifiSSID + "\n" + "密码：" + wifiPwd);
                connectToWifi = new ConnectToWifi(MainActivity.this);

                connectToWifi.openWifi();

                connectToWifi.addNetwork(connectToWifi.CreateWifiInfo(wifiSSID, wifiPwd, 3));
                Log.i("tag", "呦呦呦!");
            }

        }
        //点击发送按钮之后的反馈
        if (resultCode == 6 && requestCode == 1) {  //发送按键transfer
            final ArrayList<String> fileName = data.getStringArrayListExtra("fileName");//文件名
            final ArrayList<String> safeFileName = data.getStringArrayListExtra("safeFileName");
            final String ipAddress0 = ipAddress;//ip地址
            final int port0 = port;//端口号
            Message.obtain(handler, 0, "正在发送至" + ipAddress + ":" + port).sendToTarget();//sendToTarget方法,把这条消息发到被getTarget()指定的handler, obtain设置目标值和对象成员,生成这条消息
            process.incrementProgressBy(20);
            Thread sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    socketManager.SendFile(fileName, safeFileName, ipAddress0, port0);//sendFile方法
                }
            });
            sendThread.start();
        }
    }

        //下面是显示二维码的对话框
    private void showAlertDialog(Bitmap bitmap) {
        Log.i("hhh",qrCodeBitmap.toString().trim());

        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,alertdialog.xml文件中定义view内容
        window.setContentView(R.layout.qrdialog);
        // 为确认按钮添加事件,执行退出应用操作
        ImageView wifiImage = (ImageView) window.findViewById(R.id.wifiImg);//注意这里一定要加window.findViewById
        Log.i("hhh",wifiImage.toString().trim());
        wifiImage.setImageBitmap(bitmap);

        Button tv_queding = (Button) window.findViewById(R.id.tv_content1);
        tv_queding.setText("  确 定   ");
        tv_queding.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {

                dlg.cancel();
            }
        });
        dlg.setCancelable(false);
    }


    public String GetIpAddress() {   //获取连接到的wifi分配给手机的ip地址
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        Log.i("tag",i+"");
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF)+ "." +
                ((i >> 24 ) & 0xFF );

    }
    //获取网络状态函数  0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public int getNetworkType() {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if(!extraInfo.isEmpty()){
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NETTYPE_CMNET;
                } else {
                    netType = NETTYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        Log.e("tag",new String().valueOf(netType));//在华为上不显示是因为没网的时候在第一个if就返回了,并没有执行这一句!
        return netType;
    }

    public void openNcloseWifi(){
        WifiManager wifiManager =(WifiManager)MainActivity.this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

}

     

