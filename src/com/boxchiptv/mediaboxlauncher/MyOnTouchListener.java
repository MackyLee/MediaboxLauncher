package com.boxchiptv.mediaboxlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class MyOnTouchListener implements OnTouchListener
{
	private final static String TAG = "MyOnTouchListener";

	private int NUM_VIDEO = 0;
	private int NUM_RECOMMEND = 1;
	private int NUM_APP = 2;
	private Context mContext;
	private Object appPath;

	public MyOnTouchListener(Context context, Object path)
	{
		mContext = context;
		appPath = path;
	}

	public boolean onTouch(View view, MotionEvent event)
	{
		// TODO Auto-generated method stub
		Launcher.isInTouchMode = true;
		Log.d(TAG, "the component get a toouch at ========================== 1> ");

		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			ImageView img = (ImageView) ((ViewGroup) view).getChildAt(0);
			String path = img.getResources().getResourceName(img.getId());
			String vName = path.substring(path.indexOf("/") + 1);

			// Log.d(TAG, "mContext	 = "+ mContext.toString());
			// Log.d(TAG, "this   		 = "+ this.toString());
			 Log.d(TAG, "view         = "+ view.toString());
			 Log.d(TAG, "viewParent   ="+view.getParent().toString());

			if(vName.equals("img_setting"))
			{

				Intent intent = new Intent();
				intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
				mContext.startActivity(intent);

				/*
				 * if (Launcher.list_Setting == null) { Log.d(TAG,
				 * " list_Setting == null"); //return false; } String[] params =
				 * Launcher.list_Setting[0].split(","); if (params == null) {
				 * Log.d(TAG, " list_Setting params == null"); return false; }
				 * Log.d(TAG, " start:" + params[0] + " act:" + params[1]);
				 * Intent intent = new Intent(); intent.setClassName(params[0],
				 * params[1]); mContext.startActivity(intent);
				 */

			}
			else if(vName.equals("img_video"))
			{
				showMenuView(0, view);
			}
			else if(vName.equals("img_onlinetv"))
			{
				/*
				 * Log.d(TAG,
				 * "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@om.softwinner.TvdFileManager MEDIA_TYPE_MUSIC "
				 * );
				 * 
				 * Intent intent = new Intent();
				 * intent.setClassName("com.cloud.tv",
				 * "com.cloud.activity.SplashActivity");
				 * mContext.startActivity(intent);
				 */
				showMenuView(1, view);
			}
			else if(vName.equals("img_app"))
			{
				showMenuView(2, view);
			}
			else if(vName.equals("img_web"))
			{

				// Intent intent = new Intent();
				// intent.setClassName("com.softwinner.TvdFileManager"
				// ,"tv.tv9ikam.app.softwinner.TvdFileManager.MainUI");
				// intent.putExtra("media_type", "MEDIA_TYPE_MUSIC");
				showMenuView(3, view);

				/*
				 * Uri content_uri_browsers; Intent intent = new Intent();
				 * intent.setAction("android.intent.action.VIEW");
				 * content_uri_browsers = Uri.parse("www.google.com");
				 * intent.setData(content_uri_browsers);
				 * intent.setClassName("com.android.browser",
				 * "com.android.browser.BrowserActivity");
				 * mContext.startActivity(intent);
				 */
				Log.d(TAG, "vName.equals img_web");

			}
			else if(vName.equals("img_local"))
			{
				// showMenuView(NUM_LOCAL, view);
				Intent intent = new Intent();
				intent.setClassName("com.softwinner.TvdFileManager", "com.softwinner.TvdFileManager.MainUI");
				intent.putExtra("media_type", "MEDIA_TYPE_VIDEO");
				mContext.startActivity(intent);
				Log.d(TAG, "vName.equals img_local");
			}
			else
			{
				if(appPath != null)
				{
					mContext.startActivity((Intent) appPath);
					Log.d(TAG, appPath.toString());
				}
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			ImageView img = (ImageView) ((ViewGroup) view).getChildAt(0);
			String path = img.getResources().getResourceName(img.getId());
			String vName = path.substring(path.indexOf("/") + 1);

			if(vName.equals("img_video") || vName.equals("img_onlinetv") || vName.equals("img_app") || vName.equals("img_web") || vName.equals("img_local"))
			{
				return false;// view.onTouchEvent(event);
			}
		}

		return false;
	}

	private void showMenuView(int num, View view)
	{
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
		//Log.d(TAG, "Launcher.viewMenu="+Launcher.viewMenu.toString()+"MenuNumber="+num);
	}

	private float getPiovtX(Rect rect)
	{
		return (float) ((rect.left + rect.width() / 2));
	}

	private float getPiovtY(Rect rect)
	{
		return (float) ((rect.top + rect.height() / 2));
	}

}
