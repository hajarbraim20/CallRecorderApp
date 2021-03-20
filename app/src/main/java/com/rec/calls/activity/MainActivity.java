package com.rec.calls.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.rec.calls.R;
import com.rec.calls.items.FavouriteItem;
import com.rec.calls.items.IncomingItem;
import com.rec.calls.items.OutgoingItem;
import com.rec.calls.items.FragmentItem;
import com.rec.calls.lib_layout.LibLayouts;
import com.rec.calls.envrenement.EnvironmentApplication;
import com.rec.calls.fragments_for_layouts.FavouriteFragment;
import com.rec.calls.fragments_for_layouts.IncomingFragment;
import com.rec.calls.fragments_for_layouts.OutgoingFragment;
import com.rec.calls.services.MainService;
import com.rec.calls.utils.AppUtil;
import com.rec.calls.utils.RequestIgnoreBatteryOptimizationsUtil;
import com.rec.calls.utils.ResourceUtil;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

import com.rec.calls.utils.LogUtils;

import static com.rec.calls.utils.LogUtils.LOGD;
import static com.rec.calls.utils.LogUtils.LOGE;
import static com.rec.calls.utils.LogUtils.LOGI;
import static com.rec.calls.utils.LogUtils.LOGW;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName ();
	private TabLayout mTabLayout = null;
	private LibLayouts libLayout;
	private TabLayout.OnTabSelectedListener mOnTabSelectedListener = null;
	private SharedPreferences mSharedPreferences = null;
	private boolean mRecordCalls = true;
	private boolean mRecordIncomingCalls = mRecordCalls;
	private boolean mRecordOutgoingCalls = mRecordCalls;
	private SwitchCompat scRecordCalls;
	private ImageView mSetting;
	private ImageView mClose;
	private ImageView mSearch;
	private ConstraintLayout clMain;
	private ConstraintLayout clSearch;
	private SearchView mSearchView = null;
	private OutgoingFragment outgoingFragment;
	private IncomingFragment incomingFragment;
	private FavouriteFragment favouriteFragment;

	public void setStatusBar(Activity activity) {
		Window window = activity.getWindow ();
		DisplayMetrics displayMetrics = new DisplayMetrics ();
		getWindowManager ().getDefaultDisplay ().getMetrics (displayMetrics);
		window.addFlags (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor (activity.getResources ().getColor (R.color.green_dark_to_dark));
		window.setNavigationBarColor (activity.getResources ().getColor (R.color.green_dark_to_dark));
	}



	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		LogUtils.LOGD (TAG, "Activity create");
		setStatusBar(this);

		setContentView (R.layout.activity_main);
		libLayout = findViewById (R.id.lib_layout);
		Toolbar toolbar = findViewById (R.id.toolbar_for_layouts);
		clMain = toolbar.findViewById (R.id.constraint_layout_layouts);
		clSearch = toolbar.findViewById (R.id.constraint_layout_search_view);
		mClose = toolbar.findViewById (R.id.img_close);
		mSearch = toolbar.findViewById (R.id.img_search);
		mSearchView = toolbar.findViewById (R.id.card_search_view);
		mSearch.setOnClickListener (view -> {
			clMain.setVisibility (View.GONE);
			clSearch.setVisibility (View.VISIBLE);
		});
		mClose.setOnClickListener (view -> {
			clSearch.setVisibility (View.GONE);
			clMain.setVisibility (View.VISIBLE);
		});
		toolbar.findViewById (R.id.img_back).setVisibility (View.GONE);
		TextView title = toolbar.findViewById (R.id.title_toolbar);
		title.setPadding (0, 0, 0, 0);
		mSetting = toolbar.findViewById (R.id.img_settings);
		mSetting.setOnClickListener (this);
		ArrayList<FragmentItem.ITabLayoutIconFragmentPagerAdapter> tabLayoutIconFragmentPagerAdapterArrayList = new ArrayList<> ();
		outgoingFragment = new OutgoingFragment();
		incomingFragment = new IncomingFragment();
		favouriteFragment = new FavouriteFragment();
		tabLayoutIconFragmentPagerAdapterArrayList.add (outgoingFragment);
		tabLayoutIconFragmentPagerAdapterArrayList.add (incomingFragment);
		tabLayoutIconFragmentPagerAdapterArrayList.add (favouriteFragment);
		FragmentItem fragmentItem = new FragmentItem(getSupportFragmentManager (), null, tabLayoutIconFragmentPagerAdapterArrayList,this);
		ViewPager viewPager = findViewById (R.id.view_pager_layouts);
		viewPager.setAdapter (fragmentItem);
		viewPager.addOnPageChangeListener (new ViewPager.OnPageChangeListener () {
			@Override
			public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {
				if (clMain.getVisibility () == View.GONE) {
					clSearch.setVisibility (View.GONE);
					clMain.setVisibility (View.VISIBLE);
				}
			}



			@Override
			public void onPageSelected (int position) {
				Filter filter = null;
				switch (position) {
					case 0:
						filter = ((OutgoingItem) Objects.requireNonNull (outgoingFragment.mRecyclerView.getAdapter ())).getFilter ();
						break;
					case 1:
						filter = ((IncomingItem) Objects.requireNonNull (incomingFragment.mRecyclerView.getAdapter ())).getFilter ();
						break;
					case 2:
						filter = ((FavouriteItem) Objects.requireNonNull (favouriteFragment.mRecyclerView.getAdapter ())).getFilter ();
						break;
				}
				if (mSearchView != null) {
					mSearchView.setQuery (null, true);
					Filter finalFilter = filter;
					mSearchView.setOnQueryTextListener (new SearchView.OnQueryTextListener () {
						@Override
						public boolean onQueryTextSubmit (String s) {
							if (finalFilter != null) {
								finalFilter.filter (s);
								mSearchView.clearFocus ();
								return true;
							}
							return false;
						}

						@Override
						public boolean onQueryTextChange (String s) {
							if (finalFilter != null) {
								finalFilter.filter (s);
								return true;
							}
							return false;
						}
					});
				}
			}

			@Override
			public void onPageScrollStateChanged (int state) {
			}
		});
		mTabLayout = findViewById (R.id.tabs_layouts);
		mTabLayout.setupWithViewPager (viewPager);
		ColorFilter tabIconColorFilter = new PorterDuffColorFilter (ResourceUtil.getColor (this, R.color.title_tab), PorterDuff.Mode.SRC_IN);
		ColorFilter tabSelectedIconColorFilter = new PorterDuffColorFilter (ResourceUtil.getColor (this, R.color.title_tab), PorterDuff.Mode.SRC_IN);
		for (int i = 0 ; i < mTabLayout.getTabCount () ; i++) {
			TabLayout.Tab tab = null;
			try {
				tab = mTabLayout.getTabAt (i);
			} catch (Exception e) {
				LogUtils.LOGE (TAG, e.getMessage ());
				LogUtils.LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (tab != null) {
				try {
					tab.setIcon (tabLayoutIconFragmentPagerAdapterArrayList.get (i).getIcon ());
					Drawable icon = tab.getIcon ();
					if (icon != null) {
						if (tab.getPosition () == 0) {
							icon.setColorFilter (tabSelectedIconColorFilter);
						} else {
							icon.setColorFilter (tabIconColorFilter);
						}
					}
				} catch (Exception e) {
					LogUtils.LOGE (TAG, e.getMessage ());
					LogUtils.LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
		}
		mOnTabSelectedListener = new TabLayout.ViewPagerOnTabSelectedListener (viewPager) {
			@Override
			public void onTabSelected (TabLayout.Tab tab) {
				if (tab == null) {
					return;
				}
				super.onTabSelected (tab);
				LogUtils.LOGD (TAG, "Tab select");
				if (tab.getText () != null) {
					LogUtils.LOGI (TAG, "Tab select: " + tab.getText ());
				}
				if (tab.getIcon () != null) {
					tab.getIcon ().setColorFilter (tabSelectedIconColorFilter);
				}
			}

			@Override
			public void onTabUnselected (TabLayout.Tab tab) {
				if (tab == null) {
					return;
				}
				super.onTabUnselected (tab);
				LogUtils.LOGD (TAG, "Tab unselect");
				if (tab.getText () != null) {
					LogUtils.LOGI (TAG, "Tab unselect: " + tab.getText ());
				}
				if (tab.getIcon () != null) {
					tab.getIcon ().setColorFilter (tabIconColorFilter);
				}
			}

			@Override
			public void onTabReselected (TabLayout.Tab tab) {
				if (tab == null) {
					return;
				}
				super.onTabReselected (tab);
				LogUtils.LOGD (TAG, "Tab reselect");
				if (tab.getText () != null) {
					LogUtils.LOGI (TAG, "Tab reselect: " + tab.getText ());
				}
			}
		};
		mTabLayout.addOnTabSelectedListener (mOnTabSelectedListener);
		try {
			mSharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences != null) {
			if (!mSharedPreferences.contains (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS)) {
				SharedPreferences.Editor editor = mSharedPreferences.edit ();
				editor.putBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, true);
				editor.apply ();
			}
			if (!mSharedPreferences.contains (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS)) {
				SharedPreferences.Editor editor = mSharedPreferences.edit ();
				editor.putBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, true);
				editor.apply ();
			}
			boolean recordIncomingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			boolean recordOutgoingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
			if (recordIncomingCalls || recordOutgoingCalls) {
				if (!MainService.sIsServiceRunning) {
					AppUtil.startMainService (this);
				}
			}
		}
		scRecordCalls = findViewById (R.id.switch_compat_calls);
		scRecordCalls.setChecked (mRecordCalls);
		scRecordCalls.setOnCheckedChangeListener ((compoundButton, b) -> {
			mRecordCalls = b;
			mRecordIncomingCalls = mRecordCalls;
			mRecordOutgoingCalls = mRecordCalls;
			if (mSharedPreferences != null) {
				SharedPreferences.Editor editor = mSharedPreferences.edit ();
				editor.putBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
				editor.putBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
				editor.apply ();
			}
			if (mRecordIncomingCalls && !MainService.sIsServiceRunning) {
				AppUtil.startMainService (this);
			}
			if (mRecordOutgoingCalls && !MainService.sIsServiceRunning) {
				AppUtil.startMainService (this);
			}
		});
	}

	@Override
	protected void onResume () {
		super.onResume ();
		LogUtils.LOGE (TAG, "Main Resume");
		if (mSharedPreferences != null) {
			mRecordIncomingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		} else {
			try {
				mSharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
			} catch (Exception e) {
				LogUtils.LOGE (TAG, e.getMessage ());
				LogUtils.LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			mRecordIncomingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		}
		mRecordCalls = mRecordIncomingCalls;
		scRecordCalls.setChecked (mRecordCalls);
	}

	@Override
	protected void onStart () {
		super.onStart ();
		LogUtils.LOGD (TAG, "Activity start");
		libLayout.startBlur ();
		libLayout.lockView ();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ArrayList<String> runtimePermissionsArrayList = new ArrayList<> ();
			runtimePermissionsArrayList.add (Manifest.permission.INTERNET);
			runtimePermissionsArrayList.add (Manifest.permission.READ_PHONE_STATE);
			runtimePermissionsArrayList.add (Manifest.permission.CALL_PHONE);
			runtimePermissionsArrayList.add (Manifest.permission.RECORD_AUDIO);
			runtimePermissionsArrayList.add (Manifest.permission.VIBRATE);
			runtimePermissionsArrayList.add (Manifest.permission.RECEIVE_BOOT_COMPLETED);
			runtimePermissionsArrayList.add (Manifest.permission.READ_CONTACTS);
			runtimePermissionsArrayList.add (Manifest.permission.MODIFY_AUDIO_SETTINGS);
			runtimePermissionsArrayList.add (Manifest.permission.WAKE_LOCK);
			runtimePermissionsArrayList.add (Manifest.permission.READ_EXTERNAL_STORAGE);
			runtimePermissionsArrayList.add (Manifest.permission.WRITE_EXTERNAL_STORAGE);
			runtimePermissionsArrayList.add (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				runtimePermissionsArrayList.add (Manifest.permission.FOREGROUND_SERVICE);
			}
			if (!runtimePermissionsArrayList.isEmpty ()) {
				ArrayList<String> requestRuntimePermissionsArrayList = new ArrayList<> ();
				for (String requestRuntimePermission : runtimePermissionsArrayList) {
					if (checkSelfPermission (requestRuntimePermission) != PackageManager.PERMISSION_GRANTED) {
						requestRuntimePermissionsArrayList.add (requestRuntimePermission);
					}
				}
				if (!requestRuntimePermissionsArrayList.isEmpty ()) {
					requestPermissions (requestRuntimePermissionsArrayList.toArray (new String[ 0 ]), 1);
				}
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
				PowerManager powerManager = null;
				try {
					powerManager = (PowerManager) getSystemService (Context.POWER_SERVICE);
				} catch (Exception e) {
					LogUtils.LOGE (TAG, e.getMessage ());
					LogUtils.LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
				if (powerManager != null) {
					if (powerManager.isIgnoringBatteryOptimizations (getPackageName ())) {
						LogUtils.LOGI (TAG, "2. Request ignore battery optimizations (\"1.\" alternative; with package URI) - Entire application: Enabled");
					} else {
						LogUtils.LOGW (TAG, "2. Request ignore battery optimizations (\"1.\" alternative; with package URI) - Entire application: Not enabled");
						Intent intent = RequestIgnoreBatteryOptimizationsUtil.getRequestIgnoreBatteryOptimizationsIntent (this);
						if (intent != null) {
							startActivityForResult (intent, 2);
						}
					}
				}
			}
		}
	}

	@Override
	protected void onDestroy () {
		super.onDestroy ();
		LogUtils.LOGD (TAG, "Activity destroy");
		if (mSharedPreferences != null) {
			mSharedPreferences = null;
		}
		if (mTabLayout != null) {
			if (mOnTabSelectedListener != null) {
				mTabLayout.removeOnTabSelectedListener (mOnTabSelectedListener);
				mOnTabSelectedListener = null;
			}
			mTabLayout = null;
		}
	}

	@Override
	protected void onPause () {
		super.onPause ();
	}

	@Override
	protected void onStop () {
		libLayout.pauseBlur ();
		super.onStop ();
	}

	@Override
	public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult (requestCode, permissions, grantResults);
		switch (requestCode) {
			case 1:
				if (grantResults.length > 0 && grantResults.length == permissions.length) {
					boolean allGranted = true;
					for (int grantResult : grantResults) {
						if (grantResult != PackageManager.PERMISSION_GRANTED) {
							allGranted = false;
						}
					}
					if (allGranted) {
						LogUtils.LOGI (TAG, "All requested permissions are granted");
					} else {
						LogUtils.LOGW (TAG, "Not all requested permissions are granted");
						AlertDialog.Builder builder = new AlertDialog.Builder (this);
						builder.setTitle (getString (R.string.txt_runtime_permissions_not_granted_title));
						builder.setMessage (getString (R.string.txt_runtime_permissions_not_granted_message));
						builder.setNeutralButton (android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss ());
						AlertDialog alertDialog = builder.create ();
						alertDialog.show ();
					}
				}
				break;
		}
	}

	@Override
	public void onClick (View view) {
		switch (view.getId ()) {
			case R.id.img_settings:
				startActivity (new Intent (this, LayoutSettings.class));
				break;
		}
	}
}
