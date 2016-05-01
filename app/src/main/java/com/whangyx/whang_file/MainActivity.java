package com.whangyx.whang_file;



import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabWidget;


@SuppressWarnings("deprecation")
public class MainActivity extends Activity{

    TabHost mTabHost = null;
    TabWidget mTabWidget = null;
    Button btnVisiable, btnStrip;
    
 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();//说明文档说这个是在findViewById之后必须写的一个函数,写一下就行了
        mTabWidget = mTabHost.getTabWidget();//在Tabhost获取一个tabwidget
        mTabHost.addTab(mTabHost.newTabSpec("tab1").setContent(  //TabHost.TabSpec又是一个类 (indicator ,content ,tag)
                R.id.LinearLayout001).setIndicator("传输"));
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setContent(
                R.id.LinearLayout002).setIndicator("管理"));
//        mTabHost.addTab(mTabHost.newTabSpec("tab3").setContent(
//                R.id.LinearLayout003).setIndicator("Tab3"));

        // mTabHost.setCurrentTab(1);
        btnVisiable = (Button) findViewById(R.id.btnVisiable);
        btnStrip = (Button) findViewById(R.id.btnStrip);
        Button btnfile = (Button)findViewById(R.id.btnfilemanager);
        Button btnrev =(Button)findViewById(R.id.btnreceived);
 //       btnVisiable.setOnClickListener(this);
    //    btnStrip.setOnClickListener(this);
//        setTitle("共有" + mTabWidget.getTabCount() + "个tab");
        final Intent intent =new Intent(this,FileManager.class);
        final Intent intent2 =new Intent (this,FilesViewActivity.class);
        btnfile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				startActivity(intent);
				
			}
		});
        
        btnVisiable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				startActivity(intent2);
				
			}
		});
        
          
        
    }



	public void onClick(View v) {
		// TODO 自动生成的方法存根
//		switch (v.getId()) {
//        case R.id.btnfilemanager:
//        	Intent intent = new Intent(this,FileManager.class);
//        	startActivity(intent);
//          if (mTabWidget.getVisibility() != android.view.View.VISIBLE)
//              mTabWidget.setVisibility(android.view.View.VISIBLE);
//          else
//              mTabWidget.setVisibility(android.view.View.INVISIBLE);
//          break;
//
//      case R.id.btnStrip:
//          mTabWidget.setStripEnabled(!mTabWidget.isStripEnabled());
//          mTabWidget.setRightStripDrawable(android.R.color.transparent);
//          break;
//      default:
//          break;
		
//	}

	
	}
}
     
  

    
    
    
    
    
    
    
    
    
    
    
 //   @Override
 //   public void onClick(View v) {
        // TODO Auto-generated method stub
//        switch (v.getId()) {
//            case R.id.btnVisiable:
//                if (mTabWidget.getVisibility() != android.view.View.VISIBLE)
//                    mTabWidget.setVisibility(android.view.View.VISIBLE);
//                else
//                    mTabWidget.setVisibility(android.view.View.INVISIBLE);
//                break;
//
//            case R.id.btnStrip:
//                mTabWidget.setStripEnabled(!mTabWidget.isStripEnabled());
//                mTabWidget.setRightStripDrawable(android.R.color.transparent);
//                break;
//            default:
//                break;
//        }
 //   }
