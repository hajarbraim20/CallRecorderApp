package com.rec.calls.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.rec.calls.R;
import com.rec.calls.envrenement.EnvironmentApplication;
import com.rec.calls.services.MainService;
import com.rec.calls.utils.AppUtil;

import static com.rec.calls.utils.LogUtils.LOGD;
import static com.rec.calls.utils.LogUtils.LOGE;

public class LayoutSettings extends AppCompatActivity {
	private static final String TAG = LayoutSettings.class.getSimpleName();
	private Toolbar toolbar;
	private boolean mRecordCalls;
	private boolean mRecordIncomingCalls = mRecordCalls;
	private boolean mRecordOutgoingCalls = mRecordCalls;
	private SwitchCompat scRecordCalls;
	private SharedPreferences mSharedPreferences = null;

	private ImageView mBack;


	public void setStatusBar(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow();
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(activity.getResources().getColor(R.color.green_dark_to_dark));
			window.setNavigationBarColor(activity.getResources().getColor(R.color.green_dark_to_dark));
		}
	}


	public void restartApp () {
		Intent i = new Intent(getApplicationContext(),MainActivity.class);
		startActivity(i);
		finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LOGD(TAG, "Activity create");
		setStatusBar(this);
		setContentView(R.layout.settings_layout);
		toolbar = findViewById(R.id.toolbar_for_layouts);
		toolbar.findViewById(R.id.img_settings).setVisibility(View.GONE);
		toolbar.findViewById(R.id.img_search).setVisibility(View.GONE);
		toolbar.findViewById(R.id.switch_compat_calls).setVisibility(View.GONE);

		TextView title = toolbar.findViewById (R.id.title_toolbar);
		title.setText (getString(R.string.txt_settings));
		mBack = toolbar.findViewById (R.id.img_back);
		mBack.setOnClickListener (view -> finish ());
		getSupportFragmentManager ()
				.beginTransaction ()
				.replace (R.id.frame_settings, new SettingsFragment ())
				.commit ();
		try {
			mSharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences != null) {
			mRecordIncomingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		}
		mRecordCalls = mRecordIncomingCalls;
		scRecordCalls = findViewById (R.id.switch_compat_settings);
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

	public void btn_dark(View view){
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
		startActivity(new Intent(getApplicationContext(),MainActivity.class));
		finish();
	}

	public void btn_light(View view){
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		startActivity(new Intent(getApplicationContext(),MainActivity.class));
		finish();

	}

	public void btn_privicy(View view){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/developecallrecorder")));
		finish();


	}

	public static class SettingsFragment extends PreferenceFragmentCompat {
		private Context getContextNonNull () {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				return getContext ();
			} else {
				return getContext ();
			}
		}

		@Override
		public void onCreatePreferences (Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource (R.xml.preferences_xml, rootKey);
			Preference changeConsentInformationPreference = findPreference (EnvironmentApplication.FM_SP_CHANGE_CONSENT_INFORMATION);
		}
	}
}