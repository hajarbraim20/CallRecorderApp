package com.rec.calls.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.rec.calls.R;

import com.rec.calls.utils.LogUtils;

import static com.rec.calls.utils.LogUtils.makeLogTag;

public class LayoutSplash extends AppCompatActivity {
	private static final String TAG = LogUtils.makeLogTag (LayoutSplash.class);

	@TargetApi (Build.VERSION_CODES.LOLLIPOP)
	public void setStatusBar(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow ();
			DisplayMetrics displayMetrics = new DisplayMetrics ();
			getWindowManager ().getDefaultDisplay ().getMetrics (displayMetrics);
			window.addFlags (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor (activity.getResources ().getColor (R.color.green_dark_to_dark));
			window.setNavigationBarColor (activity.getResources ().getColor (R.color.green_dark_to_dark));
		}
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setStatusBar(this);
		setContentView (R.layout.splash_layout);
		find_views_by_id ();
		init_variables ();
	}

	private void find_views_by_id () {
	}

	private void init_variables () {
		Handler handler = new Handler ();
		handler.postDelayed (new Runnable () {
			public void run () {
				LayoutSplash.this.startActivity (new Intent (LayoutSplash.this, MainActivity.class));
				finish ();
			}
		}, 900);
	}
}
