package com.rec.calls.utils;

import android.util.Log;

import static com.rec.calls.RecoderApp.LOGGING_ENABLED;
import static com.rec.calls.RecoderApp.LOG_PREFIX;
import static com.rec.calls.RecoderApp.LOG_PREFIX_LENGTH;
import static com.rec.calls.RecoderApp.MAX_LOG_TAG_LENGTH;


public class LogUtils {
	private static final String TAG = makeLogTag (LogUtils.class);

	private LogUtils () {
	}


	public static String makeLogTag (String str) {
		if (str.length () > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
			return LOG_PREFIX + str.substring (0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
		}
		return LOG_PREFIX + str;
	}


	public static String makeLogTag (Class cls) {
		return makeLogTag (cls.getSimpleName ());
	}


	public static void LOGD (final String tag, String message) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.DEBUG)) {
				Log.d (tag, message);
			}
		}
	}


	public static void LOGD (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.DEBUG)) {
				Log.d (tag, message, cause);
			}
		}
	}


	public static void LOGV (final String tag, String message) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.VERBOSE)) {
				Log.v (tag, message);
			}
		}
	}

	public static void LOGV (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.VERBOSE)) {
				Log.v (tag, message, cause);
			}
		}
	}


	public static void LOGI (final String tag, String message) {
		if (LOGGING_ENABLED) {
			Log.i (tag, message);
		}
	}


	public static void LOGI (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			Log.i (tag, message, cause);
		}
	}


	public static void LOGW (final String tag, String message) {
		if (LOGGING_ENABLED) {
			Log.w (tag, message);
		}
	}


	public static void LOGW (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			Log.w (tag, message, cause);
		}
	}


	public static void LOGE (final String tag, String message) {
		if (LOGGING_ENABLED) {
			Log.e (tag, message);
		}
	}


	public static void LOGE (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			Log.e (tag, message, cause);
		}
	}
}
