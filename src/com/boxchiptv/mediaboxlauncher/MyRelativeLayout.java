/*-------------------------------------------------------------------------
    
-------------------------------------------------------------------------*/
package com.boxchiptv.mediaboxlauncher;

import android.content.Context;
import android.content.Intent;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.AttributeSet;

import java.lang.Character;

public class MyRelativeLayout extends RelativeLayout
{
	private final static String TAG = "MyRelativeLayout";

	private Context mContext = null;
	private static Rect imgRect;
	private float scalePara = 1.1f;
	private float shortcutScalePara = 1.1f;
	private float framePara = 1.08f;
	private int animDuration = 110;
	private final int MODE_HOME_RECT = 0;
	private final int MODE_HOME_SHORTCUT = 1;
	private final int MODE_CHILD_SHORTCUT = 2;

	public MyRelativeLayout(Context context)
	{
		super(context);
	}

	public MyRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
	}

	public MyRelativeLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		Log.d(TAG, "the component get a toouch at ========================== 0> ");
		// Log.d(TAG, "mContext	 = "+ mContext.toString());
		// Log.d(TAG, "this   		 = "+ this.toString());
		// Log.d(TAG, "viewParent   ="+this.getParent().toString());

		getCurrentShortcutHead();
		setNumberOfScreen();
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			// Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ touch ="+ this +
			// " startX=" + event.getX());
			Launcher.startX = -1f;

			setSurface();

			if(this.getChildAt(0) instanceof ImageView)
			{
				ImageView img = (ImageView) this.getChildAt(0);
				// Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ img ="+ img +
				// "img.getDrawable()="+ img.getDrawable());
				if(img != null && img.getDrawable() != null
						&& img.getDrawable().getConstantState().equals(mContext.getResources().getDrawable(R.drawable.item_img_add).getConstantState()))
				{
					// Log.d(TAG,
					// "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ touch add");
					Launcher.isAddButtonBeTouched = true;
					Launcher.pressedAddButton = this;
					if (Launcher.isShowHomePage)
						CustomAppsActivity.current_shortcutHead = CustomAppsActivity.HOME_SHORTCUT_HEAD;
				}
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_UP)
		{
			// Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ACTION_UP");
			return false;
		}
		return true;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
	{
		//Log.d(" MyRelativeLayout", "gainFocus=" + gainFocus + "   direction="
		//+ direction + "  previouslyFocusedRect=" + previouslyFocusedRect);
		//Log.d(" MyRelativeLayout", "child 0="+ getChildAt(0) + " id ="
		//+getChildAt(0).getId());
		//Log.d(" MyRelativeLayout", "child 1="+ getChildAt(1));
		//Log.d(TAG,"onFocusChanged");
		//getCurrentShortcutHead();
          
		if(gainFocus == true && !Launcher.isInTouchMode && !Launcher.dontDrawFocus)
		{
			setNumberOfScreen();
			// Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ getfocus ="+
			// this);
			// Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ getParent="+
			// this.getParent());

			if(Launcher.prevFocusedView != null && (isParentSame(this, Launcher.prevFocusedView) || Launcher.isShowHomePage))
			{
				// Log.d(TAG,
				// "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ isShowHomePage ="+
				// Launcher.isShowHomePage + " isParentSame="+isParentSame(this,
				// Launcher.prevFocusedView));

				if(!Launcher.dontRunAnim && !Launcher.IntoCustomActivity)
				{
					// Log.d(TAG,
					// "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ dontRunAnim ="+
					// Launcher.dontRunAnim +
					// " IntoCustomActivity="+Launcher.IntoCustomActivity);
					Launcher.frameView.setVisibility(View.INVISIBLE);
					Launcher.layoutScaleShadow.setVisibility(View.INVISIBLE);

					Rect preRect = new Rect();
					Launcher.prevFocusedView.getGlobalVisibleRect(preRect);

					setShadowEffect();
					startFrameAnim(preRect);
				}
				else if(!(Launcher.IntoCustomActivity && Launcher.isShowHomePage && Launcher.ifChangedShortcut))
				{
					Launcher.dontRunAnim = false;
					setSurface();
				}

			}
			else
			{
				// Log.d(TAG,
				// "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ isShowHomePage ="+
				// Launcher.isShowHomePage +
				// " dontRunAnim="+Launcher.dontRunAnim +
				// " IntoCustomActivity=" + Launcher.IntoCustomActivity);
				if(Launcher.isShowHomePage || Launcher.dontRunAnim || Launcher.IntoCustomActivity)
				{
					Launcher.IntoCustomActivity = false;
					setSurface();
				}
			}
		}
		else if(!Launcher.isInTouchMode)
		{
			Launcher.prevFocusedView = this;
			if(!Launcher.dontRunAnim)
			{
				ScaleAnimation anim = new ScaleAnimation(1.1f, 1f, 1.1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				anim.setZAdjustment(Animation.ZORDER_TOP);
				anim.setDuration(animDuration);
				if(!(this.getParent() instanceof MyGridLayout))
				{
					this.bringToFront();
					((View) this.getParent()).bringToFront();
					Launcher.viewHomePage.bringToFront();
					// Launcher.layoutScaleShadow.bringToFront();
					// Launcher.frameView.bringToFront();
					// Launcher.trans_frameView.bringToFront();
				}
				this.startAnimation(anim);
			}
		}
	}

	private void getCurrentShortcutHead()
	{
		// View parent = (View) this.getParent();
		View thisview = this;
		// Log.d(TAG, "Launcher.videoShortcutView=" +
		// Launcher.videoShortcutView.toString());
		// Log.d(TAG, "Launcher.onlineTVShortcutView=" +
		// Launcher.onlineTVShortcutView.toString());
		// Log.d(TAG, "Launcher.marketShortcutView=" +
		// Launcher.marketShortcutView.toString());
		// Log.d("TAG", "Launcher.videoShortcutView.parent=" +
		// Launcher.videoShortcutView.getParent().toString());
		// Log.d("TAG", "Launcher.onlineTVShortcutView.parent=" +
		// Launcher.onlineTVShortcutView.getParent().toString());
		// Log.d("TAG", "Launcher.marketShortcutView.parent=" +
		// Launcher.marketShortcutView.getParent().toString());
		// Launcher.findViewById(R.id.layout_focus_unit);

		if(thisview.getId() == R.id.layout_video)// (View) Launcher.videoShortcutView)
		{
			CustomAppsActivity.current_shortcutHead = CustomAppsActivity.VIDEO_SHORTCUT_HEAD;
		}
		else if(thisview.getId() == R.id.layout_onlinetv)// (View) Launcher.onlineTVShortcutView)
		{
			CustomAppsActivity.current_shortcutHead = CustomAppsActivity.TV_ONLINE_SHORTCUT_HEAD;
		}
		else if(thisview.getId() == R.id.layout_web)
		{
			CustomAppsActivity.current_shortcutHead = CustomAppsActivity.MARKET_SHORTCUT_HEAD;
		}
		else if(thisview.getId() == R.id.layout_setting)
		{
			CustomAppsActivity.current_shortcutHead = CustomAppsActivity.SYSTEM_SETTING;
		}
		else if(thisview.getId() == R.id.layout_local)
		{
			CustomAppsActivity.current_shortcutHead = CustomAppsActivity.LOCAL_MEDIA;
		}
		else if(thisview.getId() == R.id.layout_app)
		{
			CustomAppsActivity.current_shortcutHead = CustomAppsActivity.MY_APPLICATIONS;
		}
		else
		// if(Launcher.isShowHomePage)// ==R.id.layout_homepage)
		{
			//CustomAppsActivity.current_shortcutHead = CustomAppsActivity.INVALID_HEAD;//CustomAppsActivity.HOME_SHORTCUT_HEAD;
		}

		// Log.d(TAG, "thisview.getId()=" + thisview.getId() +" " +
		// Launcher.isShowHomePage);
		// Log.d("getCurrentShortcutHead", "parent=" +
		// thisview.getParent().toString());
		// Log.d(TAG,
		// "View.inflate(mContext, R.layout.homegrid_item, null).getId()=" +
		// View.inflate(mContext, R.layout.homegrid_item, null).getId());
		 Log.d(TAG, "current_shortcutHead=" + CustomAppsActivity.current_shortcutHead);
	}

	private void setNumberOfScreen()
	{
		if(this.getParent() instanceof MyGridLayout)
		{
			MyGridLayout parent = (MyGridLayout) this.getParent();
			if(parent == Launcher.videoShortcutView)
			{
				Launcher.tx_video_count.setText(Integer.toString(Launcher.videoShortcutView.indexOfChild(this) + 1));
			}
			else if(parent == Launcher.appShortcutView)
			{
				Launcher.tx_app_count.setText(Integer.toString(Launcher.appShortcutView.indexOfChild(this) + 1));
			}
		}
	}

	public class TransAnimationListener implements AnimationListener
	{
		private Animation scaleAnim;
		private ViewGroup mView;

		public TransAnimationListener(Context context, ViewGroup view, Animation anim)
		{
			scaleAnim = anim;
			mView = view;
		}

		@Override
		public void onAnimationStart(Animation animation)
		{
		}

		@Override
		public void onAnimationEnd(Animation animation)
		{
			scaleAnim.reset();
			if(!Launcher.animIsRun)
			{
				Launcher.layoutScaleShadow.setVisibility(View.VISIBLE);
				Launcher.frameView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{
		}
	}

	public class ScaleAnimationListener implements AnimationListener
	{
		@Override
		public void onAnimationStart(Animation animation)
		{
		}

		@Override
		public void onAnimationEnd(Animation animation)
		{
			if(!Launcher.animIsRun)
			{
				Launcher.layoutScaleShadow.setVisibility(View.VISIBLE);
				Launcher.frameView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{
		}
	}

	private void startFrameAnim(Rect preRect)
	{
		imgRect = new Rect();
		this.getGlobalVisibleRect(imgRect);
		setTransFramePosition(preRect);

		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, imgRect.left - preRect.left, 0.0f, imgRect.top - preRect.top);
		translateAnimation.setDuration(animDuration);
		ScaleAnimation scaleAnimation = new ScaleAnimation(1f, (float) (imgRect.right - imgRect.left) / (float) (preRect.right - preRect.left), 1f,
				(float) (imgRect.bottom - imgRect.top) / (float) (preRect.bottom - preRect.top));
		// Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );
		scaleAnimation.setDuration(animDuration);
		animationSet.addAnimation(scaleAnimation);
		animationSet.addAnimation(translateAnimation);
		translateAnimation.setAnimationListener(new TransAnimationListener(mContext, this, scaleAnimation));

		ScaleAnimation shadowAnim = new ScaleAnimation(0.9f, 1f, 0.9f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		shadowAnim.setDuration(animDuration);
		// shadowAnim.setAnimationListener(new ScaleAnimationListener());

		Launcher.layoutScaleShadow.startAnimation(shadowAnim);
		Launcher.trans_frameView.startAnimation(animationSet);
	}

	public void setSurface()
	{
		setShadowEffect();
		if(!Launcher.animIsRun)
		{
			Launcher.frameView.setVisibility(View.VISIBLE);
			Launcher.layoutScaleShadow.setVisibility(View.VISIBLE);
		}
	}

	public void setShadowEffect()
	{
		float bgScalePara;
		Rect layoutRect;
		Bitmap scaleBitmap;
		Bitmap shadowBitmap;
		ViewGroup mView = this;
		ImageView scaleImage;
		TextView scaleText;
		int screen_mode;
		String text = null;

		Launcher.trans_frameView.bringToFront();
		Launcher.layoutScaleShadow.bringToFront();
		Launcher.frameView.bringToFront();

		imgRect = new Rect();
		mView.getGlobalVisibleRect(imgRect);
		setFramePosition(imgRect);

		screen_mode = getScreenMode(mView);
		scaleImage = (ImageView) Launcher.layoutScaleShadow.findViewById(R.id.img_focus_unit);
		scaleText = (TextView) Launcher.layoutScaleShadow.findViewById(R.id.tx_focus_unit);

		if(screen_mode == MODE_HOME_SHORTCUT)
		{
			bgScalePara = shortcutScalePara;
		}
		else
		{
			bgScalePara = scalePara;
		}

		ImageView img = (ImageView) (mView.getChildAt(0));
		img.buildDrawingCache();
		Bitmap bmp = img.getDrawingCache();
		if(bmp == null)
		{
			Launcher.cantGetDrawingCache = true;
			return;
		}
		else
		{
			Launcher.cantGetDrawingCache = false;
		}

		scaleBitmap = zoomBitmap(bmp, (int) (imgRect.width() * bgScalePara), (int) (imgRect.height() * bgScalePara));
		img.destroyDrawingCache();

		if(mView.getChildAt(1) instanceof TextView)
		{
			text = (String) ((TextView) mView.getChildAt(1)).getText();
		}

		shadowBitmap = BitmapFactory.decodeResource(mContext.getResources(), getShadow(mView.getChildAt(0), screen_mode));
		int layout_width = (shadowBitmap.getWidth() - imgRect.width()) / 2;
		int layout_height = (shadowBitmap.getHeight() - imgRect.height()) / 2;
		layoutRect = new Rect(imgRect.left - layout_width, imgRect.top - layout_height, imgRect.right + layout_width, imgRect.bottom + layout_height);

		scaleImage.setImageBitmap(scaleBitmap);

		if(text != null)
		{
			// setTextWidth(scaleText, scaleBitmap.getWidth());
			setTextMarginAndSize(scaleText, screen_mode);
			scaleText.setText(text);
		}
		else
		{
			scaleText.setText(null);
		}
		Launcher.layoutScaleShadow.setBackgroundResource(getShadow(mView.getChildAt(0), screen_mode));
		setViewPosition(Launcher.layoutScaleShadow, layoutRect);
	}

	private void setTextWidth(TextView text, int width)
	{
		android.view.ViewGroup.LayoutParams para;
		para = text.getLayoutParams();
		para.width = width;
		text.setLayoutParams(para);
	}

	private void setTextMarginAndSize(TextView text, int screen_mode)
	{
		android.widget.RelativeLayout.LayoutParams para;
		para = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		para.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		para.addRule(RelativeLayout.CENTER_HORIZONTAL);
		if(screen_mode == MODE_HOME_RECT)
		{
			para.setMargins(0, 0, 0, 95);
			text.setLayoutParams(para);
			text.setTextSize(33);
		}
		else
		{
			para.setMargins(50, 0, 50, 55);
			text.setLayoutParams(para);
			text.setTextSize(30);
		}
	}

	private void setTransFramePosition(Rect rect)
	{
		android.widget.AbsoluteLayout.LayoutParams lp = new android.widget.AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0,
				0);
		int rectWidth = rect.right - rect.left;
		int rectHeight = rect.bottom - rect.top;

		lp.width = (int) (rectWidth * framePara);
		lp.height = (int) (rectHeight * framePara);
		lp.x = rect.left + (int) ((rectWidth - lp.width) / 2);
		lp.y = rect.top + (int) ((rectHeight - lp.height) / 2);
		Launcher.trans_frameView.setLayoutParams(lp);
	}

	private void setFramePosition(Rect rect)
	{
		android.widget.AbsoluteLayout.LayoutParams lp = new android.widget.AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0,
				0);
		int rectWidth = rect.right - rect.left;
		int rectHeight = rect.bottom - rect.top;

		lp.width = (int) (rectWidth * framePara);
		lp.height = (int) (rectHeight * framePara);
		lp.x = rect.left + (int) ((rectWidth - lp.width) / 2);
		lp.y = rect.top + (int) ((rectHeight - lp.height) / 2);
		Launcher.frameView.setLayoutParams(lp);
	}

	private void setViewPosition(View view, Rect rect)
	{
		android.widget.AbsoluteLayout.LayoutParams lp = new android.widget.AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0,
				0);

		lp.width = rect.width();
		lp.height = rect.height();
		lp.x = rect.left;
		lp.y = rect.top;
		view.setLayoutParams(lp);
	}

	private int getScreenMode(ViewGroup view)
	{
		View img = view.getChildAt(0);
		View tx = view.getChildAt(1);
		String path = img.getResources().getResourceName(img.getId());
		String vName = path.substring(path.indexOf("/") + 1);

		if(vName.equals("img_onlinetv") || vName.equals("img_video") || vName.equals("img_setting") || vName.equals("img_app") || vName.equals("img_web")
				|| vName.equals("img_local"))
		{
			return MODE_HOME_RECT;
		}
		else if(tx != null)
		{
			return MODE_CHILD_SHORTCUT;
		}
		else
		{
			return MODE_HOME_SHORTCUT;
		}
	}

	private boolean isParentSame(View view1, View view2)
	{
		if(((ViewGroup) view1.getParent()).indexOfChild(view2) == -1)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	private int getShadow(View img, int mode)
	{
		String path = img.getResources().getResourceName(img.getId());
		String vName = path.substring(path.indexOf("/") + 1);
		// Log.d(" MyRelativeLayout", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ child 1="+
		// vName);

		if(vName.equals("img_onlinetv"))
		{
			return R.drawable.shadow_onlinetv;
		}
		else if(vName.equals("img_video"))
		{
			return R.drawable.shadow_video;
		}
		else if(vName.equals("img_setting"))
		{
			return R.drawable.shadow_setting;
		}
		else if(vName.equals("img_app"))
		{
			return R.drawable.shadow_app;
		}
		else if(vName.equals("img_web"))
		{
			return R.drawable.shadow_web;
		}
		else if(vName.equals("img_local"))
		{
			return R.drawable.shadow_setting;
		}
		else if(mode == MODE_CHILD_SHORTCUT)
		{
			return R.drawable.shadow_child_shortcut;
		}
		else
		{
			return R.drawable.shadow_shortcut;
		}
	}

	public int getStringLength(String s)
	{
		int length = 0;
		for (int i = 0; i < s.length(); i++)
		{
			int ascii = Character.codePointAt(s, i);
			if(ascii >= 0 && ascii <= 255)
				length++;
			else
				length += 2;

		}
		return length;

	}

	public Bitmap zoomBitmap(Bitmap bitmap, int w, int h)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}
}
