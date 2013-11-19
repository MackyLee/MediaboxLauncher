package com.boxchiptv.mediaboxlauncher;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.KeyEvent;
import android.view.FocusFinder;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.util.Log;

import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.graphics.Bitmap;  
import android.graphics.Rect;
import android.graphics.Canvas; 
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;

public class MyOnTouchListener implements OnTouchListener{
    private final static String TAG = "MyOnTouchListener";
        
    private int NUM_VIDEO = 0;
   // private int NUM_RECOMMEND = 1;
    private int NUM_APP = 1;
 //   private int NUM_MUSIC = 2;
 //   private int NUM_LOCAL = 3;
        
    private Context mContext;
    private Object appPath;
    
    public MyOnTouchListener(Context context, Object path){
         mContext = context;
         appPath = path;
    }
    public boolean onTouch (View view, MotionEvent event)  {
        // TODO Auto-generated method stub
      Launcher.isInTouchMode = true;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            ImageView img = (ImageView)((ViewGroup)view).getChildAt(0);
            String path  = img.getResources().getResourceName(img.getId()); 
            String vName = path.substring(path.indexOf("/")+1);

          Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ACTION_UP");

             Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@ viewname=" + vName);
            if (vName.equals("img_setting")){
                /*Intent intent = new Intent();
                intent .setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));                  
								mContext.startActivity(intent);
									*/if(Launcher.list_Setting == null)
                			{
                				Log.d(TAG, " list_Setting == null");
                				return false;
                			}                			
                		String[] params = Launcher.list_Setting[0].split(",");
                		if(params == null)
                		{
                			  Log.d(TAG, " list_Setting params == null");
                				return false;
                		}               	
                		Log.d(TAG, " start:" + params[0] + " act:" + params[1]);
                		Intent intent = new Intent();
                    intent.setClassName(params[0], params[1]);
                     mContext.startActivity(intent);
                     
            } else if (vName.equals("img_video")){
                showMenuView(NUM_VIDEO, view);
            }
            else if (vName.equals("img_recommend"))
            {
            	Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@om.softwinner.TvdFileManager MEDIA_TYPE_MUSIC ");
             //   showMenuView(NUM_RECOMMEND, view);
             	Intent intent = new Intent();
              intent.setClassName("com.cloud.tv", "com.cloud.activity.SplashActivity");
              mContext.startActivity(intent);
            }
            else if (vName.equals("img_app")){
                showMenuView(NUM_APP, view);
           }else if (vName.equals("img_music"))
           {
           				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@om.softwinner.TvdFileManager MEDIA_TYPE_MUSIC ");
                 		Intent intent = new Intent();
                     intent.setClassName("com.softwinner.TvdFileManager","com.softwinner.TvdFileManager.MainUI");
                    intent.putExtra("media_type",  "MEDIA_TYPE_MUSIC");                    
                     mContext.startActivity(intent);
                 
                }else if (vName.equals("img_local"))
                {
                	Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@om.softwinner.TvdFileManager MEDIA_TYPE_VIDEO ");
                //    showMenuView(NUM_LOCAL, view);
                		Intent intent = new Intent();
                    intent.setClassName("com.softwinner.TvdFileManager","com.softwinner.TvdFileManager.MainUI");
                    intent.putExtra("media_type",  "MEDIA_TYPE_VIDEO");                    
                     mContext.startActivity(intent);
                 
            }else {
                if (appPath != null){                                      
                    mContext.startActivity((Intent)appPath);
                } 
            }                 
        }
        else if (event.getAction() == MotionEvent.ACTION_DOWN){
            ImageView img = (ImageView)((ViewGroup)view).getChildAt(0);
            String path  = img.getResources().getResourceName(img.getId()); 
            String vName = path.substring(path.indexOf("/")+1);   

            if (vName.equals("img_video") || vName.equals("img_recommend") || vName.equals("img_app") ||
                     vName.equals("img_music") ||  vName.equals("img_local")){
                return false;//view.onTouchEvent(event);
            }
        }

        return false;
    }   

    private void showMenuView(int num, View view){
         Launcher.saveHomeFocusView = view;
         Launcher.isShowHomePage = false;
         Launcher.layoutScaleShadow.setVisibility(View.INVISIBLE);
         Launcher.frameView.setVisibility(View.INVISIBLE);
          
         Rect rect = new Rect();
         view.getGlobalVisibleRect(rect);
         ScaleAnimation scaleAnimationIn = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, getPiovtX(rect), getPiovtY(rect));
         ScaleAnimation scaleAnimationOut = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, getPiovtX(rect), getPiovtY(rect));
         scaleAnimationIn.setDuration(400);
         scaleAnimationOut.setDuration(400);
         Launcher.viewMenu.setInAnimation(scaleAnimationIn);
         Launcher.viewMenu.setOutAnimation(scaleAnimationOut);
         
         Launcher.viewHomePage.setVisibility(View.GONE);
         Launcher.viewMenu.setVisibility(View.VISIBLE);
         Launcher.viewMenu.setDisplayedChild(num);
         Launcher.viewMenu.setFocusableInTouchMode(true);
    }

    private float getPiovtX(Rect rect){
        return (float)((rect.left + rect.width() / 2));
    }

    private float getPiovtY(Rect rect){
        return (float)((rect.top + rect.height() / 2));
    }
    
    
}



