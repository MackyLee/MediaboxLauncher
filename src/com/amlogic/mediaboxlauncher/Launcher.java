package com.amlogic.mediaboxlauncher;

import android.os.Environment;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.ComponentName;
import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View.OnFocusChangeListener;
import android.view.SurfaceHolder;  
import android.view.Display; 
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.BaseAdapter;
import android.widget.AdapterView;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
    
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Collections;
import android.app.SystemWriteManager;

public class Launcher extends Activity{
	
	private final static String TAG="MediaBoxLauncher";

    private GridView lv_status;
    private final String SD_PATH = "/storage/external_storage/sdcard1";
	private final String USB_PATH ="/storage/external_storage";
    private final String net_change_action = "android.net.conn.CONNECTIVITY_CHANGE";
	private final String wifi_signal_action = "android.net.wifi.RSSI_CHANGED";
    private final String weather_request_action = "android.amlogic.launcher.REQUEST_WEATHER";
    private final String weather_receive_action = "android.amlogic.settings.WEATHER_INFO";
    private static int time_count = 0;
    private final int time_freq = 120;
    private final int SCREEN_HEIGHT = 719;

    public static View prevFocusedView;
    public static RelativeLayout layoutScaleShadow;
    public static View trans_frameView;
    public static View frameView;
    public static View viewHomePage = null;
    public static MyViewFlipper viewMenu = null;
    public static View pressedAddButton = null;
    
    public static boolean isShowHomePage;
    public static boolean dontRunAnim;
    public static boolean dontDrawFocus;
    public static boolean ifChangedShortcut;
    public static boolean IntoCustomActivity;
    public static boolean IntoApps;
    public static boolean isAddButtonBeTouched;
    public static boolean isInTouchMode;
    public static boolean animIsRun;
    public static boolean cantGetDrawingCache;
    public static int accessBoundaryCount = 0;
    public static int preDec;
    public static int HOME_SHORTCUT_COUNT = 9;
    public static View saveHomeFocusView = null;
	public static MyGridLayout homeShortcutView = null;
    public static MyGridLayout videoShortcutView = null;
    public static MyGridLayout recommendShortcutView = null;
    public static MyGridLayout appShortcutView = null;
    public static MyGridLayout musicShortcutView = null;
    public static MyGridLayout localShortcutView = null;
    public static TextView tx_video_count = null;
    public static TextView tx_recommend_count = null;   
    public static TextView tx_app_count = null;   
    public static TextView tx_music_count = null; 
    public static TextView tx_local_count = null;
    private TextView tx_video_allcount = null;
    private TextView tx_recommend_allcount = null;
    private TextView tx_app_allcount = null;
    private TextView tx_music_allcount = null;
    private TextView tx_local_allcount = null;

    public static Bitmap screenShot;
    public static Bitmap screenShot_keep;
    
    private String[] list_homeShortcut;
    private String[] list_videoShortcut;
    private String[] list_recommendShortcut;
    private String[] list_musicShortcut;
    private String[] list_localShortcut;
    
    private boolean is24hFormart = false;
    private int popWindow_top = -1;
    private int popWindow_bottom = -1;
    public static float startX;
    private static boolean updateAllShortcut;
    private static boolean checkOobe = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "------onCreate");

        if (DesUtils.isAmlogicChip() == false){
            finish();
        }
        
        if(checkOobe){
            checkOobe = false ;
			enableOOBE();
        }

        initStaticVariable();
        initChildViews();   
        //displayShortcuts();
        displayStatus();  
        displayDate();
        setRectOnKeyListener();
        sendWeatherBroadcast();
    }

    
    private void enableOOBE() {
        if(isNeedStartOobe()){
            Intent i = new Intent();
            ComponentName name = new ComponentName("com.mbx.settingsmbox", "com.mbx.settingsmbox.OobeActivity");
            i.setComponent(name);
            startActivity(i);
        }
	}

    private boolean isNeedStartOobe(){       		
        SystemWriteManager sw = (SystemWriteManager)getSystemService("system_write");
        String prop  = sw.getPropertyString("persist.sys.oobe.start", "true");
        return "true".equals(prop);
    }

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "------onResume");

        if(SystemProperties.getBoolean("ro.platform.has.mbxuimode", false)){
            if(SystemProperties.getBoolean("ubootenv.var.has.accelerometer", true)
                            && SystemProperties.getBoolean("sys.keeplauncher.landcape", false))
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
         }

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addDataScheme("file");
		registerReceiver(mediaReceiver, filter);

		filter = new IntentFilter();
		filter.addAction(net_change_action);
		filter.addAction(wifi_signal_action);
		filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
		filter.addAction(Intent.ACTION_TIME_TICK);	
        filter.addAction(weather_receive_action);
		registerReceiver(netReceiver, filter);

        filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
	    filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
	    filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
	    registerReceiver(appReceiver, filter);
        
        if (isInTouchMode || (IntoCustomActivity && isShowHomePage)){
            Launcher.dontRunAnim = true;
            layoutScaleShadow.setVisibility(View.INVISIBLE);
            frameView.setVisibility(View.INVISIBLE);
        }
 
        displayShortcuts();
        displayStatus();  
        displayDate();

       if (isShowHomePage){
            IntoCustomActivity = false;
       }

       if (cantGetDrawingCache){
           resetShadow();
       }
    }
    @Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "------onPause");
        prevFocusedView = null;
	}

    protected void onDestroy(){
        unregisterReceiver(mediaReceiver);
		unregisterReceiver(netReceiver);
	    unregisterReceiver(appReceiver);
	    super.onDestroy();
	}

    @Override
    public boolean onTouchEvent (MotionEvent event){ 
         if (event.getAction() == MotionEvent.ACTION_DOWN){
            startX = event.getX();
         } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ touch ="+ this);
            if (pressedAddButton != null && isAddButtonBeTouched){
                Rect rect = new Rect();
                pressedAddButton.getGlobalVisibleRect(rect);

                popWindow_top = rect.top - 10;
                popWindow_bottom = rect.bottom + 10;
                new Thread( new Runnable() {     
                    public void run() {
            		    mHandler.sendEmptyMessage(1);
            	    }            
    		    }).start();
                Intent intent = new Intent();
                intent.putExtra("top", popWindow_top);
                intent.putExtra("bottom", popWindow_bottom);
                intent.putExtra("left", rect.left);
                intent.putExtra("right", rect.right);
    			intent.setClass(this, CustomAppsActivity.class);
    			startActivity(intent);
                IntoCustomActivity = true;
                isAddButtonBeTouched = false;
            }
            else if (!isShowHomePage){
                Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@ getX = " +event.getX() + " startX = " + startX);
                if (event.getX() + 20 < startX && startX != -1f) {           
                    viewMenu.setInAnimation(this, R.anim.push_right_in);
                    viewMenu.setOutAnimation(this, R.anim.push_right_out);
                    viewMenu.showNext();
                } else if (event.getX() -20 > startX && startX != -1f) {
                    viewMenu.setInAnimation(this, R.anim.push_left_in);
                    viewMenu.setOutAnimation(this,  R.anim.push_left_out);
                    viewMenu.showPrevious();
                }
            }          
        }       
        return true;       
    }

	public boolean onKeyDown(int keyCode, KeyEvent event) {  
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@ KEYCODE_BACK");
            if (!isShowHomePage){
                viewHomePage.setVisibility(View.VISIBLE);
                viewMenu.setVisibility(View.GONE);
                isShowHomePage = true;
                IntoCustomActivity = false;
                if (saveHomeFocusView != null  && !isInTouchMode){
                    prevFocusedView = null;
                    dontRunAnim = true;
                    saveHomeFocusView.clearFocus();
                    dontRunAnim = true;
                    saveHomeFocusView.requestFocus();
                } 
            }
    	    return true;
    	} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER){
    	    ViewGroup view = (ViewGroup)getCurrentFocus();
            if (view.getChildAt(0) instanceof ImageView){
                ImageView img = (ImageView)view.getChildAt(0);    
                if(img != null &&
                        img.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.item_img_add).getConstantState())){ 
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);

                    popWindow_top = rect.top - 10;
                    popWindow_bottom = rect.bottom + 10;
                    new Thread( new Runnable() {     
            			public void run() {
            				    mHandler.sendEmptyMessage(1);
            			}            
    		        }).start();
                    Intent intent = new Intent();
                    intent.putExtra("top", popWindow_top);
                    intent.putExtra("bottom", popWindow_bottom);
                    intent.putExtra("left", rect.left);
                    intent.putExtra("right", rect.right);
    			    intent.setClass(this, CustomAppsActivity.class);
    			    startActivity(intent);
                    IntoCustomActivity = true;
                    if (saveHomeFocusView != null){
                        saveHomeFocusView.clearFocus();
                    }
                }
            }
    	}
		return super.onKeyDown(keyCode, event);
	}

  	private void displayStatus() {
		WifiManager mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int wifi_rssi = mWifiInfo.getRssi();
		int wifi_level = WifiManager.calculateSignalLevel(
                        wifi_rssi, 5);
	
	 
		 LocalAdapter ad = new LocalAdapter(Launcher.this,
						 getStatusData(wifi_level, isEthernetOn()),
						 R.layout.homelist_item, 			 
									 new String[] {"item_type", "item_name", "item_sel"},
									 new int[] {R.id.item_type, 0, 0});
		 lv_status.setAdapter(ad);
	}
    
    private void displayDate() {

		is24hFormart = DateFormat.is24HourFormat(this); 

        TextView  time = (TextView)findViewById(R.id.tx_time);
		TextView  date = (TextView)findViewById(R.id.tx_date);
        time.setText(getTime());
        time.setTypeface(Typeface.DEFAULT_BOLD);	
		date.setText(getDate());
	}
    private void initStaticVariable(){    
        isShowHomePage = true;
        dontRunAnim = false;
        dontDrawFocus = false;
        ifChangedShortcut = true;
        IntoCustomActivity = false;
        IntoApps = true;
        isAddButtonBeTouched = false;
        isInTouchMode = false;
        animIsRun = false;
        updateAllShortcut = true;
        animIsRun = false;
        cantGetDrawingCache = false;
    }
    
    private void initChildViews(){
        lv_status = (GridView)findViewById(R.id.list_status);
        layoutScaleShadow = (RelativeLayout)findViewById(R.id.layout_focus_unit);
        frameView = findViewById(R.id.img_frame); 
        trans_frameView = findViewById(R.id.img_trans_frame); 
  
        viewHomePage = findViewById(R.id.layout_homepage);    
        viewMenu = (MyViewFlipper)findViewById(R.id.menu_flipper);
        
        homeShortcutView = (MyGridLayout)findViewById(R.id.gv_shortcut);
        videoShortcutView = (MyGridLayout)findViewById(R.id.gv_shortcut_video);
        recommendShortcutView = (MyGridLayout)findViewById(R.id.gv_shortcut_recommend);
        appShortcutView = (MyGridLayout)findViewById(R.id.gv_shortcut_app);
        musicShortcutView = (MyGridLayout)findViewById(R.id.gv_shortcut_music);
        localShortcutView = (MyGridLayout)findViewById(R.id.gv_shortcut_local);

        tx_video_count = (TextView)findViewById(R.id.tx_video_count);
        tx_video_allcount = (TextView)findViewById(R.id.tx_video_allcount); 
        tx_recommend_count = (TextView)findViewById(R.id.tx_recommend_count);
        tx_recommend_allcount = (TextView)findViewById(R.id.tx_recommend_allcount);
        tx_app_count = (TextView)findViewById(R.id.tx_app_count);
        tx_app_allcount = (TextView)findViewById(R.id.tx_app_allcount);
        tx_music_count = (TextView)findViewById(R.id.tx_music_count);
        tx_music_allcount = (TextView)findViewById(R.id.tx_music_allcount);
        tx_local_count = (TextView)findViewById(R.id.tx_local_count);
        tx_local_allcount = (TextView)findViewById(R.id.tx_local_allcount);

        /*new Thread( new Runnable() {     
            public void run() {
                try{
                    Thread.sleep(500);
                } catch (Exception e) {
    			    Log.d(TAG,""+e);
    		    }
            	//Message msg = new Message();
                //msg.what = 2;
                mHandler.sendEmptyMessage(2);
            }            
        }).start();
        */


    }

    private void displayShortcuts() {       
        if (ifChangedShortcut == true){            
            loadApplications();
            ifChangedShortcut = false;      
      
            if (!isShowHomePage){
                //sendKeyCode(KeyEvent.KEYCODE_0);
                new Thread( new Runnable() {     
                    public void run() {
                        ViewGroup findGridLayout = null;
                        while(findGridLayout == null){
                            findGridLayout = ((ViewGroup)((ViewGroup)((ViewGroup)viewMenu.getCurrentView()).getChildAt(4)).getChildAt(0));
                        }
                        mHandler.sendEmptyMessage(3);
                    }            
                }).start();  
            } else  if(IntoCustomActivity){
                new Thread( new Runnable() {     
                    public void run() {
                         try{
                            Thread.sleep(200);
                        } catch (Exception e) {
            			    Log.d(TAG,""+e);
            		    }
                        mHandler.sendEmptyMessage(4);
                    }            
                }).start(); 
            }
        }
	}


    private void updateStatus() {
        ((BaseAdapter) lv_status.getAdapter()).notifyDataSetChanged();
	}

  	public  List<Map<String, Object>> getStatusData(int wifi_level, boolean is_ethernet_on) {	 
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
		Map<String, Object> map = new HashMap<String, Object>();
		
		switch (wifi_level) {
			case 0:
			 	map.put("item_type", R.drawable.wifi1);				
				break;
			case 1:
				map.put("item_type", R.drawable.wifi2);;
				break;
			case 2:
			 	map.put("item_type", R.drawable.wifi3);
				break;
			case 3:
			 	map.put("item_type", R.drawable.wifi4);
				break;
			case 4:
			 	map.put("item_type", R.drawable.wifi5);
				break;
			default:
				break;
		}
		list.add(map);

		 if(isSdcardExists() == true){
		 	map = new HashMap<String, Object>();
			map.put("item_type", R.drawable.img_status_sdcard);
			list.add(map);
		}

		 if(isUsbExists() == true){
		 	 map = new HashMap<String, Object>();
			 map.put("item_type", R.drawable.img_status_usb);
			 list.add(map);
		 }

		if(is_ethernet_on == true){
			map = new HashMap<String, Object>();
			map.put("item_type", R.drawable.img_status_ethernet);
			list.add(map);
		}
		
		return list;
	}	 

	public boolean isUsbExists(){
		File dir = new File(USB_PATH);  
		if (dir.exists() && dir.isDirectory()) {
			if (dir.listFiles() != null) {
				if (dir.listFiles().length > 0) {
					for (File file : dir.listFiles()) {
						String path = file.getAbsolutePath();
						if (path.startsWith(USB_PATH+"/sd")&&!path.equals(SD_PATH)) {
					//	if (path.startsWith("/mnt/sd[a-z]")){
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public  boolean isSdcardExists(){
		if(Environment.getExternalStorage2State().startsWith(Environment.MEDIA_MOUNTED)) {
			File dir = new File(SD_PATH);  
			if (dir.exists() && dir.isDirectory()) {
				return true;
			}
		}
		return false;
	}

	private boolean isEthernetOn(){
		ConnectivityManager connectivity = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
	
		if (info.isConnected()){
			return true;
		} else {
			return false;
		}
	}

    public  String getTime(){
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

        String time = "";

		if (hour >= 10) {
            time +=  Integer.toString(hour); 
		}else {
            time += "0" + Integer.toString(hour);
	    }
        time += ":";
        
        if (minute >= 10) {
            time +=  Integer.toString(minute); 
		}else {
            time += "0" +  Integer.toString(minute);
	    }

		return time;
    }

    private String getDate(){
		final Calendar c = Calendar.getInstance(); 
        int int_Month = c.get(Calendar.MONTH); 
        String mDay = Integer.toString(c.get(Calendar.DAY_OF_MONTH)); 
		int int_Week = c.get(Calendar.DAY_OF_WEEK) -1; 
		String str_week =  this.getResources().getStringArray(R.array.week)[int_Week];
        String mMonth =  this.getResources().getStringArray(R.array.month)[int_Month];

        String date;
		if (Locale.getDefault().getLanguage().equals("zh")){
            date = str_week + ", " + mMonth + " " + mDay + this.getResources().getString(R.string.str_day);
        }else {
		    date = str_week + ", " + mMonth + " " + mDay;
        }

		//Log.d(TAG, "@@@@@@@@@@@@@@@@@@@ "+ date  + "week = " +int_Week);
		return date;
	}


	 private void loadCustomApps(String path){	
        File mFile = new File(path);
        File default_file = new File(CustomAppsActivity.DEFAULT_SHORTCUR_PATH);
        
		if(!mFile.exists()) {
		    mFile = default_file;
            getShortcutFromDefault(CustomAppsActivity.DEFAULT_SHORTCUR_PATH, CustomAppsActivity.SHORTCUT_PATH);
		}
        
		try {
            BufferedReader br = new BufferedReader(new FileReader(mFile));
            String str = null;
            while( (str=br.readLine()) != null ){
                if (str.startsWith(CustomAppsActivity.HOME_SHORTCUT_HEAD)){                  
                    str = str.replaceAll(CustomAppsActivity.HOME_SHORTCUT_HEAD, "");
			        list_homeShortcut = str.split(";");
                } else if (str.startsWith(CustomAppsActivity.VIDEO_SHORTCUT_HEAD)){
                    str = str.replaceAll(CustomAppsActivity.VIDEO_SHORTCUT_HEAD, "");
			        list_videoShortcut = str.split(";");
                }  else if (str.startsWith(CustomAppsActivity.RECOMMEND_SHORTCUT_HEAD)){
                    str = str.replaceAll(CustomAppsActivity.RECOMMEND_SHORTCUT_HEAD, "");
			        list_recommendShortcut = str.split(";");
                }  else if (str.startsWith(CustomAppsActivity.MUSIC_SHORTCUT_HEAD)){
                    str = str.replaceAll(CustomAppsActivity.MUSIC_SHORTCUT_HEAD, "");
			        list_musicShortcut = str.split(";");
                }  else if (str.startsWith(CustomAppsActivity.LOCAL_SHORTCUT_HEAD)){
                    str = str.replaceAll(CustomAppsActivity.LOCAL_SHORTCUT_HEAD, "");
			        list_localShortcut = str.split(";");
                } 
            }

		}
		catch (Exception e) {
			Log.d(TAG,""+e);
		}
	}
     
    public void getShortcutFromDefault(String srcPath, String desPath){     
        File srcFile = new File(srcPath);
        File desFile = new File(desPath);
        if(!srcFile.exists()) {
		    return;
		}
        if(!desFile.exists()) {
			try {
				desFile.createNewFile();
			}
			catch (Exception e) {
				Log.e(TAG, e.getMessage().toString());
			}
		}
        
		try {
            BufferedReader br = new BufferedReader(new FileReader(srcFile));
            String str = null;
            List list = new ArrayList();
            
            while( (str=br.readLine()) != null ){
                list.add(str);
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(desFile));
            for( int i = 0;i < list.size(); i++ ){ 
                 bw.write(list.get(i).toString());
                 bw.newLine();
            }
            bw.flush();
            bw.close();
        }
		catch (Exception e) {
			Log.d(TAG, "   " + e);
		}
    }

    public void copyFile(String oldPath, String newPath) {   
       try {   
           int bytesum = 0;   
           int byteread = 0;   
           File oldfile = new File(oldPath); 
           Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@ copy file");
           if (!oldfile.exists()) {
               InputStream inStream = new FileInputStream(oldPath); 
               FileOutputStream fs = new FileOutputStream(newPath);   
               byte[] buffer = new byte[1444];   
               int length;   
               while ( (byteread = inStream.read(buffer)) != -1) {   
                   bytesum += byteread;
                   System.out.println(bytesum);   
                   fs.write(buffer, 0, byteread);   
               }   
               inStream.close();   
           }   
       }   
       catch (Exception e) {    
           e.printStackTrace();   
       }   
  }   

    private List<Map<String, Object>> loadShortcutList(PackageManager manager, final List<ResolveInfo> apps, String[] list_custom_apps) {
        Map<String, Object> map = null;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            
        if(list_custom_apps != null){     
		    for (int i = 0; i < list_custom_apps.length; i++){
                if (apps != null) {			
                    final int count = apps.size();
                    for (int j = 0; j < count; j++) {
                        ApplicationInfo application = new ApplicationInfo();
                        ResolveInfo info = apps.get(j);
        		
                        application.title = info.loadLabel(manager);
                        application.setActivity(new ComponentName(
                                info.activityInfo.applicationInfo.packageName,
                                info.activityInfo.name),
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        application.icon = info.activityInfo.loadIcon(manager);
                        if (application.componentName.getPackageName().equals(list_custom_apps[i])){
    					    map = new HashMap<String, Object>(); 
        					map.put("item_name", application.title.toString());
        					map.put("file_path", application.intent); 		   
        					map.put("item_type", application.icon);
                            map.put("item_symbol", application.componentName);  
                            list.add(map);
                            break;
        				} 
                    }
                }
			}
		}  

        return list;
    }

    private Map<String, Object> getAddMap(){
		Map<String, Object> map = new HashMap<String, Object>();
        map.put("item_name", this.getResources().getString(R.string.str_add));
	    map.put("file_path", null); 		   
		map.put("item_type", R.drawable.item_img_add);

        return map;
    }

	private void loadApplications() {
        List<Map<String, Object>> HomeShortCutList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> videoShortCutList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> recommendShortCutList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> appShortCutList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> musicShortCutList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> localShortCutList = new ArrayList<Map<String, Object>>();
        
        PackageManager manager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
        
        HomeShortCutList.clear();
        videoShortCutList.clear();
        recommendShortCutList.clear();
        appShortCutList.clear();
        musicShortCutList.clear();
        localShortCutList.clear();
        
		loadCustomApps(CustomAppsActivity.SHORTCUT_PATH);

        if (updateAllShortcut == true) {
            HomeShortCutList = loadShortcutList(manager, apps, list_homeShortcut);
            videoShortCutList = loadShortcutList(manager, apps, list_videoShortcut);
            recommendShortCutList = loadShortcutList(manager, apps, list_recommendShortcut);
            musicShortCutList = loadShortcutList(manager, apps, list_musicShortcut);
            localShortCutList = loadShortcutList(manager, apps, list_localShortcut);

            if (apps != null) {			
                final int count = apps.size();
                for (int i = 0; i < count; i++) {
                    ApplicationInfo application = new ApplicationInfo();
                    ResolveInfo info = apps.get(i);
    		
                    application.title = info.loadLabel(manager);
                    application.setActivity(new ComponentName(
                            info.activityInfo.applicationInfo.packageName,
                            info.activityInfo.name),
                            Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    application.icon = info.activityInfo.loadIcon(manager);

                    Map<String, Object> map = new HashMap<String, Object>(); 
                    map.put("item_name", application.title.toString());
    				map.put("file_path", application.intent); 		   
    				map.put("item_type", application.icon);
                    map.put("item_symbol", application.componentName); 
                  // Log.d(TAG, ""+ application.componentName.getPackageName());
                    appShortCutList.add(map);  
                }
            }

    		Map<String, Object> map = getAddMap();
            HomeShortCutList.add(map);
            videoShortCutList.add(map);
            musicShortCutList.add(map);
            localShortCutList.add(map);

            homeShortcutView.setLayoutView(HomeShortCutList, 0);
            videoShortcutView.setLayoutView(videoShortCutList, 1);
            recommendShortcutView.setLayoutView(recommendShortCutList, 1);
            appShortcutView.setLayoutView(appShortCutList, 1);
            musicShortcutView.setLayoutView(musicShortCutList, 1);
            localShortcutView.setLayoutView(localShortCutList, 1);  
            tx_video_allcount.setText("/" + Integer.toString(videoShortCutList.size()));
            tx_recommend_allcount.setText("/" + Integer.toString(recommendShortCutList.size()));
            tx_app_allcount.setText("/" + Integer.toString(appShortCutList.size()));
            tx_music_allcount.setText("/" + Integer.toString(musicShortCutList.size()));
            tx_local_allcount.setText("/" + Integer.toString(localShortCutList.size()));

            updateAllShortcut = false;
        } else if (CustomAppsActivity.current_shortcutHead.equals(CustomAppsActivity.VIDEO_SHORTCUT_HEAD)){
            videoShortCutList = loadShortcutList(manager, apps, list_videoShortcut);
            Map<String, Object> map = getAddMap();
            videoShortCutList.add(map);
            videoShortcutView.setLayoutView(videoShortCutList, 1);
            tx_video_allcount.setText("/" + Integer.toString(videoShortCutList.size()));
        } else if (CustomAppsActivity.current_shortcutHead.equals(CustomAppsActivity.RECOMMEND_SHORTCUT_HEAD)){
            recommendShortCutList = loadShortcutList(manager, apps, list_recommendShortcut);
            recommendShortcutView.setLayoutView(recommendShortCutList, 1);
            tx_recommend_allcount.setText("/" + Integer.toString(recommendShortCutList.size()));
        } else if (CustomAppsActivity.current_shortcutHead.equals(CustomAppsActivity.MUSIC_SHORTCUT_HEAD)){
            musicShortCutList = loadShortcutList(manager, apps, list_musicShortcut);
            Map<String, Object> map = getAddMap();
            musicShortCutList.add(map);
            musicShortcutView.setLayoutView(musicShortCutList, 1);
            tx_music_allcount.setText("/" + Integer.toString(musicShortCutList.size()));
        } else if (CustomAppsActivity.current_shortcutHead.equals(CustomAppsActivity.LOCAL_SHORTCUT_HEAD)){
            localShortCutList = loadShortcutList(manager, apps, list_localShortcut);
            Map<String, Object> map = getAddMap();
            localShortCutList.add(map);
            localShortcutView.setLayoutView(localShortCutList, 1);
            tx_local_allcount.setText("/" + Integer.toString(localShortCutList.size()));
        } else{
            HomeShortCutList = loadShortcutList(manager, apps, list_homeShortcut);
            Map<String, Object> map = getAddMap();
            HomeShortCutList.add(map);
            homeShortcutView.setLayoutView(HomeShortCutList, 0);
        }
    }

    private void setRectOnKeyListener(){
        findViewById(R.id.layout_video).setOnKeyListener(new MyOnKeyListener(this, null));
        findViewById(R.id.layout_recommend).setOnKeyListener(new MyOnKeyListener(this, null));
        findViewById(R.id.layout_setting).setOnKeyListener(new MyOnKeyListener(this, null));
        findViewById(R.id.layout_app).setOnKeyListener(new MyOnKeyListener(this, null));
        findViewById(R.id.layout_music).setOnKeyListener(new MyOnKeyListener(this, null));
        findViewById(R.id.layout_local).setOnKeyListener(new MyOnKeyListener(this, null));

        findViewById(R.id.layout_video).setOnTouchListener(new MyOnTouchListener(this, null));
        findViewById(R.id.layout_recommend).setOnTouchListener(new MyOnTouchListener(this, null));
        findViewById(R.id.layout_setting).setOnTouchListener(new MyOnTouchListener(this, null));
        findViewById(R.id.layout_app).setOnTouchListener(new MyOnTouchListener(this, null));
        findViewById(R.id.layout_music).setOnTouchListener(new MyOnTouchListener(this, null));
        findViewById(R.id.layout_local).setOnTouchListener(new MyOnTouchListener(this, null));
    }
    
	public static void playClickMusic() {	
		/* if (isSystemSoundOn == true) {
			sp_button.stop(music_prio_button);
			sp_button.play(music_prio_button, 1, 1, 0, 0, 1);
		} */		
	}

    public void setPopWindow(int top, int bottom){     
        View view = this.getWindow().getDecorView();  
        Display display = this.getWindowManager().getDefaultDisplay();  
        view.layout(0, 0, 1279, SCREEN_HEIGHT);  
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
        view.destroyDrawingCache();
        Log.d(TAG, "@@@@@@@@@@@@@@@@@@ window height="+ display.getHeight());

        if (bottom > SCREEN_HEIGHT/2){  
            if (top+3-CustomAppsActivity.CONTENT_HEIGHT > 0){
                screenShot = Bitmap.createBitmap(bmp, 0, 0,bmp.getWidth(), top);
                screenShot_keep = Bitmap.createBitmap(bmp, 0, CustomAppsActivity.CONTENT_HEIGHT,
                                                    bmp.getWidth(), top +3-CustomAppsActivity.CONTENT_HEIGHT);
            } else {
                screenShot = Bitmap.createBitmap(bmp, 0, 0,bmp.getWidth(), CustomAppsActivity.CONTENT_HEIGHT);
                screenShot_keep = null;
            }
        } else { 
            screenShot = Bitmap.createBitmap(bmp, 0, bottom,bmp.getWidth(), SCREEN_HEIGHT-bottom);
            screenShot_keep = Bitmap.createBitmap(bmp, 0, bottom, 
                                                    bmp.getWidth(), SCREEN_HEIGHT-(bottom+CustomAppsActivity.CONTENT_HEIGHT));
        }
    }

    private void sendWeatherBroadcast(){
        Intent intent =new Intent();
        intent.setAction(weather_request_action);
        sendBroadcast(intent);
        Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@      send weather broadcast: "+weather_request_action);
    }

    private void setWeatherView(String str_weather){
        if (str_weather == null || str_weather.length() == 0){
            return;
        }
        
        String[] list_data = str_weather.split(",");
        ImageView img_weather = (ImageView)findViewById(R.id.img_weather);
        if (list_data.length >= 3 && list_data[2] != null)
            img_weather.setImageResource(parseIcon(list_data[2]));

        String str_temp = list_data[1] + " ";
        TextView tx_temp = (TextView)findViewById(R.id.tx_temp);
        tx_temp.setTypeface(Typeface.DEFAULT_BOLD);
        if (list_data.length >= 3 && str_temp.length() >= 1)
            tx_temp.setText(str_temp); 
   
        String str_city = list_data[0];
        TextView tx_city = (TextView)findViewById(R.id.tx_city);
        if (list_data.length >= 3 && str_city.length() >= 1)
            tx_city.setText(str_city); 
    }
    
    private int parseIcon(String strIcon)
	{
		if (strIcon == null)
			return -1;
		if ("0.gif".equals(strIcon))
			return R.drawable.sunny03;
		if ("1.gif".equals(strIcon))
			return R.drawable.cloudy03;
		if ("2.gif".equals(strIcon))
			return R.drawable.shade03;
		if ("3.gif".equals(strIcon))
			return R.drawable.shower01;
		if ("4.gif".equals(strIcon))
			return R.drawable.thunder_shower03;
		if ("5.gif".equals(strIcon))
			return R.drawable.rain_and_hail;
		if ("6.gif".equals(strIcon))
			return R.drawable.rain_and_snow;
		if ("7.gif".equals(strIcon))
			return R.drawable.s_rain03;
		if ("8.gif".equals(strIcon))
			return R.drawable.m_rain03;
		if ("9.gif".equals(strIcon))
			return R.drawable.l_rain03;
		if ("10.gif".equals(strIcon))
			return R.drawable.h_rain03;
		if ("11.gif".equals(strIcon))
			return R.drawable.hh_rain03;
		if ("12.gif".equals(strIcon))
			return R.drawable.hhh_rain03;
		if ("13.gif".equals(strIcon))
			return R.drawable.snow_shower03;
		if ("14.gif".equals(strIcon))
			return R.drawable.s_snow03;
		if ("15.gif".equals(strIcon))
			return R.drawable.m_snow03;
		if ("16.gif".equals(strIcon))
			return R.drawable.l_snow03;
		if ("17.gif".equals(strIcon))
			return R.drawable.h_snow03;
		if ("18.gif".equals(strIcon))
			return R.drawable.fog03;
		if ("19.gif".equals(strIcon))
			return R.drawable.ics_rain;
		if ("20.gif".equals(strIcon))
			return R.drawable.sand_storm02;
		if ("21.gif".equals(strIcon))
			return R.drawable.m_rain03;
		if ("22.gif".equals(strIcon))
			return R.drawable.l_rain03;
		if ("23.gif".equals(strIcon))
			return R.drawable.h_rain03;
		if ("24.gif".equals(strIcon))
			return R.drawable.hh_rain03;
		if ("25.gif".equals(strIcon))
			return R.drawable.hhh_rain03;
		if ("26.gif".equals(strIcon))
			return R.drawable.m_snow03;
		if ("27.gif".equals(strIcon))
			return R.drawable.l_snow03;
		if ("28.gif".equals(strIcon))
			return R.drawable.h_snow03;
		if ("29.gif".equals(strIcon))
			return R.drawable.smoke03;
		if ("30.gif".equals(strIcon))
			return R.drawable.sand_blowing03;
		if ("31.gif".equals(strIcon))
			return R.drawable.sand_storm03;
		return 0;
	}

    public static int  parseItemIcon(String packageName){
        if (packageName.equals("com.fb.FileBrower")){
            return R.drawable.icon_filebrowser;
        } else if (packageName.equals("com.amlogic.OOBE")){
            return R.drawable.icon_oobe;
        } else if (packageName.equals("com.android.browser")){
            return R.drawable.icon_browser;
        } else if (packageName.equals("com.gsoft.appinstall")){
            return R.drawable.icon_appinstaller;
        } else if (packageName.equals("com.farcore.videoplayer")){
            return R.drawable.icon_videoplayer;
        } else if (packageName.equals("com.aml.settings")){
            return R.drawable.icon_amlsetting;
        } else if (packageName.equals("com.amlogic.mediacenter")){
            return R.drawable.icon_mediacenter;
        } else if (packageName.equals("com.amlapp.update.otaupgrade")){
            return R.drawable.icon_backupandupgrade;
        } else if (packageName.equals("com.android.gallery3d")){
            return R.drawable.icon_pictureplayer;
        } else if (packageName.equals("com.amlogic.netfilebrowser")){
            return R.drawable.icon_networkneiborhood;
        } else if (packageName.equals("st.com.xiami")){
            return R.drawable.icon_xiami;
        } else if (packageName.equals("com.android.providers.downloads.ui")){
            return R.drawable.icon_download;
        } else if (packageName.equals("app.android.applicationxc")){
            return R.drawable.icon_xiaocong;
        } else if (packageName.equals("com.example.airplay")){
            return R.drawable.icon_airplay;
        } else if (packageName.equals("com.amlogic.miracast")){
            return R.drawable.icon_miracast;
        } else if (packageName.equals("com.amlogic.PPPoE")){
            return R.drawable.icon_pppoe;
        } else if (packageName.equals("com.android.service.remotecontrol")){
            return R.drawable.icon_remotecontrol;
        } else if (packageName.equals("com.mbx.settingsmbox")){
            return R.drawable.icon_setting;
        } else if (packageName.equals("com.android.music")){
            return R.drawable.icon_music;
        }  
        return -1;
    }

    private void sendKeyCode(final int keyCode){  
        new Thread () {  
            public void run() {  
                try {  
                    Instrumentation inst = new Instrumentation();  
                    inst.sendKeyDownUpSync(keyCode);  
                } catch (Exception e) {  
                    Log.e("Exception when sendPointerSync", e.toString());  
                }  
            }  
        }.start();  
    }  
    
    private void resetShadow(){
        new Thread( new Runnable() {     
            public void run() {
                try{
                    Thread.sleep(500);
                } catch (Exception e) {
    			    Log.d(TAG,""+e);
    		    }
            	//Message msg = new Message();
                //msg.what = 2;
                mHandler.sendEmptyMessage(2);
            }            
        }).start();  
   }

    private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case 1:
					setPopWindow(popWindow_top, popWindow_bottom);
					break;
                case 2:
					MyRelativeLayout view = (MyRelativeLayout)getCurrentFocus();
                    view.setSurface();
					break;
                case 3:
                    ViewGroup findGridLayout = ((ViewGroup)((ViewGroup)((ViewGroup)viewMenu.getCurrentView()).getChildAt(4)).getChildAt(0));
                    int count = findGridLayout.getChildCount();
                    Launcher.dontRunAnim = true;
                    findGridLayout.getChildAt(count-1).requestFocus();
                    Launcher.dontRunAnim = false;
					break;
                case 4:
                    int i = homeShortcutView.getChildCount();
                    Launcher.dontRunAnim = true;
                    homeShortcutView.getChildAt(i-1).requestFocus();
                    Launcher.dontRunAnim = false;
                    layoutScaleShadow.setVisibility(View.VISIBLE);
                    frameView.setVisibility(View.VISIBLE);
					break;
				default:	
                    break;      
			}
		}
	};
    
	private BroadcastReceiver mediaReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
	
				//Log.d(TAG, " mediaReceiver		  action = " + action);
				if(action == null)
					return;
			
				if(Intent.ACTION_MEDIA_EJECT.equals(action)
						|| Intent.ACTION_MEDIA_UNMOUNTED.equals(action) || Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
					displayStatus();
					updateStatus(); 	
				}
			}
	};
	
	private BroadcastReceiver netReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if(action == null)
				return;

			//Log.d(TAG, "netReceiver         action = " + action);

			if (action.equals(Intent.ACTION_TIME_TICK)){
				displayDate();	
                
                time_count++;
				if(time_count >= time_freq){
                    sendWeatherBroadcast();
					time_count = 0;
				}
			} else if (action.equals(weather_receive_action)) {
			    String weatherInfo = intent.getExtras().getString("weather_today");
                Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@ receive " + action + " weather:" + weatherInfo);
                setWeatherView(weatherInfo);
            } 
            else {
				displayStatus();
				updateStatus();
			}
		}
	};

    private BroadcastReceiver appReceiver = new BroadcastReceiver(){       
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
           
                final String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                	   	
                final String packageName = intent.getData().getSchemeSpecificPart();
                final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                if (packageName == null || packageName.length() == 0) {
                    // they sent us a bad intent
                    return;
                }

                updateAllShortcut = true;
                loadApplications();  
	        }        
    	}	
	};
}
