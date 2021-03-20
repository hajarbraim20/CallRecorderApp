package com.rec.calls;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.os.Build;

import com.rec.calls.envrenement.EnvironmentApplication;

import java.util.Objects;

import com.rec.calls.utils.LogUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

import static com.rec.calls.utils.LogUtils.LOGD;
import static com.rec.calls.utils.LogUtils.LOGE;
import static com.rec.calls.utils.LogUtils.makeLogTag;

public class RecoderApp extends Application {
	public static final String LOG_PREFIX = "_";
	public static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length ();

	public static final int MAX_LOG_TAG_LENGTH = 50;
	private static final String TAG = LogUtils.makeLogTag (RecoderApp.class);

	public static boolean LOGGING_ENABLED = false;
	private final RealmMigration mRealmMigration = (realm, oldVersion, newVersion) -> {
	};

	@Override
	public void onCreate () {
		EnvironmentApplication.sFilesDirMemory = getFilesDir ();
		EnvironmentApplication.sFilesDirPathMemory = getFilesDir ().getPath ();
		EnvironmentApplication.sCacheDirMemory = getCacheDir ();
		EnvironmentApplication.sCacheDirPathMemory = getCacheDir ().getPath ();
		try {
			EnvironmentApplication.sExternalFilesDirMemory = getExternalFilesDir (null);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		try {
			EnvironmentApplication.sExternalFilesDirPathMemory = Objects.requireNonNull (getExternalFilesDir (null)).getPath ();
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		EnvironmentApplication.sExternalCacheDirMemory = getExternalCacheDir ();
		EnvironmentApplication.sExternalCacheDirPathMemory = Objects.requireNonNull (getExternalCacheDir ()).getPath ();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			EnvironmentApplication.sProcessName = getProcessName ();
		}
		super.onCreate ();
		LogUtils.LOGD (TAG, "Application create");
		Realm.init (this);
		RealmConfiguration realmConfiguration = new RealmConfiguration.Builder ()
				.migration (mRealmMigration)
				.build ();
		LogUtils.LOGD (TAG, "Realm configuration schema version: " + realmConfiguration.getSchemaVersion ());
		Realm.setDefaultConfiguration (realmConfiguration);
	}

	@Override
	public void onTrimMemory (int level) {
		super.onTrimMemory (level);
		LogUtils.LOGD (TAG, "Application trim memory");
		switch (level) {
			case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
				LogUtils.LOGD (TAG, "Application trim memory: Running moderate");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
				LogUtils.LOGD (TAG, "Application trim memory: Running low");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
				LogUtils.LOGD (TAG, "Application trim memory: Running critical");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
				LogUtils.LOGD (TAG, "Application trim memory: UI hidden");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
				LogUtils.LOGD (TAG, "Application trim memory: Background");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
				LogUtils.LOGD (TAG, "Application trim memory: Moderate");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
				LogUtils.LOGD (TAG, "Application trim memory: Complete");
				if (EnvironmentApplication.sFilesDirMemory == null) {
					EnvironmentApplication.sFilesDirMemory = getFilesDir ();
				}
				if (EnvironmentApplication.sFilesDirPathMemory == null) {
					EnvironmentApplication.sFilesDirPathMemory = getFilesDir ().getPath ();
				}
				if (EnvironmentApplication.sCacheDirMemory == null) {
					EnvironmentApplication.sCacheDirMemory = getCacheDir ();
				}
				if (EnvironmentApplication.sCacheDirPathMemory == null) {
					EnvironmentApplication.sCacheDirPathMemory = getCacheDir ().getPath ();
				}
				if (EnvironmentApplication.sExternalFilesDirMemory == null) {
					try {
						EnvironmentApplication.sExternalFilesDirMemory = getExternalFilesDir (null);
					} catch (Exception e) {
						LogUtils.LOGE (TAG, e.getMessage ());
						LogUtils.LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
				if (EnvironmentApplication.sExternalFilesDirPathMemory == null) {
					try {
						EnvironmentApplication.sExternalFilesDirPathMemory = Objects.requireNonNull (getExternalFilesDir (null)).getPath ();
					} catch (Exception e) {
						LogUtils.LOGE (TAG, e.getMessage ());
						LogUtils.LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
				if (EnvironmentApplication.sExternalCacheDirMemory == null) {
					EnvironmentApplication.sExternalCacheDirMemory = getExternalCacheDir ();
				}
				if (EnvironmentApplication.sExternalCacheDirPathMemory == null) {
					EnvironmentApplication.sExternalCacheDirPathMemory = Objects.requireNonNull (getExternalCacheDir ()).getPath ();
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					if (EnvironmentApplication.sProcessName == null) {
						EnvironmentApplication.sProcessName = getProcessName ();
					}
				}
				break;
		}
	}
}
