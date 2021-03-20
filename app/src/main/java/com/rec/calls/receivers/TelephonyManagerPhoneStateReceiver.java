package com.rec.calls.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.rec.calls.R;
import com.rec.calls.envrenement.EnvironmentApplication;
import com.rec.calls.services.CallRecorderService;

import com.rec.calls.utils.LogUtils;

import static com.rec.calls.utils.LogUtils.LOGE;
import static com.rec.calls.utils.LogUtils.LOGI;
import static com.rec.calls.utils.LogUtils.LOGW;


public class TelephonyManagerPhoneStateReceiver extends BroadcastReceiver {
	private static final String TAG = TelephonyManagerPhoneStateReceiver.class.getSimpleName ();
	private static boolean sIsIncoming = false;
	private static boolean sIsOutgoing = false;

	@Override
	public void onReceive (Context context, Intent intent) {
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
		if (!intentAction.equals (TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			LogUtils.LOGW (TAG, "Receiver receive: Intent action mismatch");
			return;
		}
		onReceiveOk (context, intent);
	}

	private void onReceiveOk (@NonNull Context context, @NonNull Intent intent) {
		TelephonyManager telephonyManager = null;
		try {
			telephonyManager = (TelephonyManager) context.getSystemService (Context.TELEPHONY_SERVICE);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		String phoneStateExtraState = null;
		try {
			phoneStateExtraState = intent.getStringExtra (TelephonyManager.EXTRA_STATE);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (phoneStateExtraState != null) {
			if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_IDLE)) {
				LogUtils.LOGI (TAG, "Phone state: Idle");
				if (telephonyManager != null) {
					if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_IDLE) {
						onCallStateChange (context, intent, TelephonyManager.CALL_STATE_IDLE);
					}
				} else {
					onCallStateChange (context, intent, TelephonyManager.CALL_STATE_IDLE);
				}
			}
			if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_RINGING)) {
				LogUtils.LOGI (TAG, "Phone state: Ringing");
				if (telephonyManager != null) {
					if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_RINGING) {
						onCallStateChange (context, intent, TelephonyManager.CALL_STATE_RINGING);
					}
				} else {
					onCallStateChange (context, intent, TelephonyManager.CALL_STATE_RINGING);
				}
			}
			if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				LogUtils.LOGI (TAG, "Phone state: Offhook");
				if (telephonyManager != null) {
					if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_OFFHOOK) {
						onCallStateChange (context, intent, TelephonyManager.CALL_STATE_OFFHOOK);
					}
				} else {
					onCallStateChange (context, intent, TelephonyManager.CALL_STATE_OFFHOOK);
				}
			}
		}
	}

	private void onCallStateChange (@NonNull Context context, @NonNull Intent intent, int callState) {
		SharedPreferences sharedPreferences = null;
		try {
			sharedPreferences = context.getSharedPreferences (context.getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (sharedPreferences != null) {
			switch (callState) {
				case TelephonyManager.CALL_STATE_IDLE:
					if (CallRecorderService.sIsServiceRunning) {
						stopRecorder (context, intent);
					}
					if (sIsIncoming) {
						sIsIncoming = false;
					}
					if (sIsOutgoing) {
						sIsOutgoing = false;
					}
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					if (!sIsOutgoing) {
						sIsIncoming = true;
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (!sIsIncoming) {
						sIsOutgoing = true;
					}
					if (!CallRecorderService.sIsServiceRunning) {
						if (sIsIncoming) {
							LogUtils.LOGI (TAG, "Call type: Incoming");
							if (sharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_INCOMING_CALLS, true)) {
								startRecorder (context, intent);
							}
							sIsIncoming = false;
						}
						if (sIsOutgoing) {
							LogUtils.LOGI (TAG, "Call type: Outgoing");
							if (sharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, true)) {
								startRecorder (context, intent);
							}
							sIsOutgoing = false;
						}
					}
					break;
			}
		}
	}

	private void startRecorder (@NonNull Context context, @NonNull Intent intent) {
		if (CallRecorderService.sIsServiceRunning) {
			return;
		}
		intent.setClass (context, CallRecorderService.class);
		intent.putExtra (EnvironmentApplication.INTENT_ACTION_INCOMING_CALL, sIsIncoming);
		intent.putExtra (EnvironmentApplication.INTENT_ACTION_OUTGOING_CALL, sIsOutgoing);
		try {
			context.startService (intent);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
	}

	private void stopRecorder (@NonNull Context context, @NonNull Intent intent) {
		if (!CallRecorderService.sIsServiceRunning) {
			return;
		}
		intent.setClass (context, CallRecorderService.class);
		try {
			context.stopService (intent);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
	}
}
