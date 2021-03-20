package com.rec.calls.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.rec.calls.R;
import com.rec.calls.envrenement.EnvironmentApplication;
import com.rec.calls.services.MainService;
import com.rec.calls.utils.AppUtil;

import com.rec.calls.utils.LogUtils;

import static com.rec.calls.utils.LogUtils.LOGD;
import static com.rec.calls.utils.LogUtils.LOGE;
import static com.rec.calls.utils.LogUtils.LOGW;


public class ActionShutdownReceiver extends BroadcastReceiver {
	private static final String TAG = ActionShutdownReceiver.class.getSimpleName ();

	@Override
	public void onReceive (Context context, Intent intent) {
		LogUtils.LOGD (TAG, "Receiver receive");
		if (context == null || intent == null) {
			if (context == null) {
				LogUtils.LOGW (TAG, "Receiver receive: Context lack");
			}
			if (intent == null) {
				LogUtils.LOGW (TAG, "Receiver receive: Intent lack");
			}
			return;
		}
		String intentAction = null;
		try {
			intentAction = intent.getAction ();
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (intentAction == null) {
			LogUtils.LOGW (TAG, "Receiver receive: Intent action lack");
			return;
		}
		if (!intentAction.equals (Intent.ACTION_SHUTDOWN)) {
			LogUtils.LOGW (TAG, "Receiver receive: Intent action mismatch");
			return;
		}
		LogUtils.LOGD (TAG, "Receiver receive: OK");
		onReceiveOk (context, intent);
	}

	private void onReceiveOk (@NonNull Context context, @NonNull Intent intent) {
		SharedPreferences sharedPreferences = null;
		try {
			sharedPreferences = context.getSharedPreferences (context.getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (sharedPreferences != null) {
			if (!sharedPreferences.contains (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS)) {
				SharedPreferences.Editor editor = sharedPreferences.edit ();
				editor.putBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, true);
				editor.apply ();
			}
			if (!sharedPreferences.contains (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS)) {
				SharedPreferences.Editor editor = sharedPreferences.edit ();
				editor.putBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, true);
				editor.apply ();
			}
			boolean recordIncomingCalls = sharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, true);
			boolean recordOutgoingCalls = sharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, true);
			if (recordIncomingCalls || recordOutgoingCalls) {
				if (MainService.sIsServiceRunning) {
					AppUtil.stopMainService (context);
				}
			}
		}
	}
}
