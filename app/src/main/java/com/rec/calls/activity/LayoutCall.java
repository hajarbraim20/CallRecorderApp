package com.rec.calls.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rec.calls.R;
import com.rec.calls.models.CallObject;
import com.rec.calls.utils.ResourceUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.Sort;

import static com.rec.calls.utils.LogUtils.LOGD;
import static com.rec.calls.utils.LogUtils.LOGE;



public class LayoutCall extends AppCompatActivity {
	private static final String TAG = LayoutCall.class.getSimpleName ();
	private boolean mIsIncoming = false;
	private boolean mIsOutgoing = false;
	private Realm mRealm = null;
	private CallObject mIncomingCallObject = null;
	private CallObject mOutgoingCallObject = null;
	private MediaPlayer mMediaPlayer = null;
	private ImageView playImageButton;

	public void setStatusBarColor(Activity activity) {
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
		LOGD (TAG, "Activity create");
		setStatusBarColor(this);
		setContentView (R.layout.all_call_layout);
		Toolbar toolbar = findViewById (R.id.toolbar_for_layouts);
		toolbar.findViewById (R.id.img_settings).setVisibility (View.GONE);
		toolbar.findViewById (R.id.img_search).setVisibility (View.GONE);
		toolbar.findViewById (R.id.switch_compat_calls).setVisibility (View.GONE);
		TextView title = toolbar.findViewById (R.id.title_toolbar);
		toolbar.findViewById (R.id.img_back).setOnClickListener (view -> finish ());
		playImageButton = findViewById (R.id.img_play_call);
		Intent intent = getIntent ();
		long beginTimestamp = 0L, endTimestamp = 0L;
		if (intent != null) {
			if (intent.hasExtra ("mType") && Objects.equals (intent.getStringExtra ("mType"), "incoming")) {
				mIsIncoming = true;
			}
			if (intent.hasExtra ("mType") && Objects.equals (intent.getStringExtra ("mType"), "outgoing")) {
				mIsOutgoing = true;
			}
			if (mIsIncoming || mIsOutgoing) {
				if (intent.hasExtra ("mBeginTimestamp")) {
					beginTimestamp = intent.getLongExtra ("mBeginTimestamp", 0L);
				}
				if (intent.hasExtra ("mEndTimestamp")) {
					endTimestamp = intent.getLongExtra ("mEndTimestamp", 0L);
				}
			}
		}
		if (beginTimestamp == 0L || endTimestamp == 0L) {
			getMissingDataDialog ().show ();
			return;
		}
		try {
			mRealm = Realm.getDefaultInstance ();
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mRealm != null && !mRealm.isClosed ()) {
			if (mIsIncoming) {
				mIncomingCallObject = mRealm.where (CallObject.class)
						.equalTo ("mBeginTimestamp", beginTimestamp)
						.equalTo ("mEndTimestamp", endTimestamp)
						.sort ("mBeginTimestamp", Sort.DESCENDING)
						.beginGroup ()
						.equalTo ("type", "incoming")
						.endGroup ()
						.findFirst ();
			} else if (mIsOutgoing) {
				mOutgoingCallObject = mRealm.where (CallObject.class)
						.equalTo ("mBeginTimestamp", beginTimestamp)
						.equalTo ("mEndTimestamp", endTimestamp)
						.sort ("mBeginTimestamp", Sort.DESCENDING)
						.beginGroup ()
						.equalTo ("type", "outgoing")
						.endGroup ()
						.findFirst ();
			}
		}
		mIsIncoming = mIsIncoming && mIncomingCallObject != null;
		mIsOutgoing = mIsOutgoing && mOutgoingCallObject != null;
		if (!mIsIncoming && !mIsOutgoing) {
			getMissingDataDialog ().show ();
			return;
		}
		if (intent.hasExtra ("mCorrespondentName")) {
			String correspondentName = intent.getStringExtra ("mCorrespondentName");
			title.setText (correspondentName);
			if (mIsIncoming) {
				mIncomingCallObject.setCorrespondentName (correspondentName);
			}
			if (mIsOutgoing) {
				mOutgoingCallObject.setCorrespondentName (correspondentName);
			}
		}
		if (mIsIncoming) {
			String phoneNumber = mIncomingCallObject.getPhoneNumber ();
			if (mIncomingCallObject.getCorrespondentName () == null) {
				if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
					title.setText (phoneNumber);
				} else {
					title.setText (getString (R.string.txt_unknown_number));
				}
			}
		} else if (mIsOutgoing) {
			String phoneNumber = mOutgoingCallObject.getPhoneNumber ();
			if (mOutgoingCallObject.getCorrespondentName () == null) {
				if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
					title.setText (phoneNumber);
				} else {
					title.setText (getString (R.string.txt_unknown_number));
				}
			}
		}
		TextView typeTextView = findViewById (R.id.txt_type_of_call);
		ImageView typeImageView = findViewById (R.id.img_type_of_call);
		String beginTimeDate = null, endTimeDate = null;
		if (mIsIncoming) {
			String phoneNumber = mIncomingCallObject.getPhoneNumber ();
			Bitmap imageBitmap = null;
			if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
				((TextView) findViewById (R.id.int_number_phone)).setText (phoneNumber);
				try {
					if (ActivityCompat.checkSelfPermission (this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
						Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode (phoneNumber));
						Cursor cursor = getContentResolver ().query (uri, new String[] {ContactsContract.PhoneLookup._ID}, null, null, null);
						if (cursor != null) {
							if (cursor.moveToFirst ()) {
								String id = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup._ID));
								if (id != null && !id.trim ().isEmpty ()) {
									InputStream inputStream = null;
									try {
										inputStream = ContactsContract.Contacts.openContactPhotoInputStream (getContentResolver (), ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, Long.valueOf (id)));
									} catch (Exception e) {
										LOGE (TAG, e.getMessage ());
										LOGE (TAG, e.toString ());
										e.printStackTrace ();
									}
									if (inputStream != null) {
										Bitmap bitmap = null;
										try {
											bitmap = BitmapFactory.decodeStream (inputStream);
										} catch (Exception e) {
											LOGE (TAG, e.getMessage ());
											LOGE (TAG, e.toString ());
											e.printStackTrace ();
										}
										if (bitmap != null) {
											imageBitmap = ResourceUtil.getBitmapClippedCircle (bitmap);
										}
									}
								}
							}
							cursor.close ();
						}
					}
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			} else {
				((TextView) findViewById (R.id.int_number_phone)).setText (getString (R.string.txt_unknown_number));
			}
			typeTextView.setText (getString (R.string.txt_incoming_call));
			if (imageBitmap != null) {
				typeImageView.setImageBitmap (imageBitmap);
			} else {
				typeImageView.setImageDrawable (ResourceUtil.getDrawable (this, R.drawable.img_inoming));
				typeImageView.setColorFilter(getResources().getColor(R.color.red));
			}
			if (!DateFormat.is24HourFormat (this)) {
				try {
					beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mIncomingCallObject.getBeginTimestamp ()));
					endTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mIncomingCallObject.getEndTimestamp ()));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			} else {
				try {
					beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mIncomingCallObject.getBeginTimestamp ()));
					endTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mIncomingCallObject.getEndTimestamp ()));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			String durationString = null;
			Date beginDate = new Date (mIncomingCallObject.getBeginTimestamp ());
			Date endDate = new Date (mIncomingCallObject.getEndTimestamp ());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				try {
					Duration duration = Duration.between (beginDate.toInstant (), endDate.toInstant ());
					long minutes = TimeUnit.SECONDS.toMinutes (duration.getSeconds ());
					durationString = String.format (Locale.getDefault (), "%d min, %d sec",
							minutes,
							duration.getSeconds () - TimeUnit.MINUTES.toSeconds (minutes));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			} else {
				long durationMs = endDate.getTime () - beginDate.getTime ();
				try {
					long minutes = TimeUnit.MILLISECONDS.toMinutes (durationMs);
					durationString = String.format (Locale.getDefault (), "%d min, %d sec",
							minutes,
							TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (minutes));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			durationString = durationString != null && !durationString.isEmpty () ? durationString : "N/A";
			((TextView) findViewById (R.id.int_duration)).setText (durationString);
		} else if (mIsOutgoing) {
			String phoneNumber = mOutgoingCallObject.getPhoneNumber ();
			Bitmap imageBitmap = null;
			if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
				((TextView) findViewById (R.id.int_number_phone)).setText (phoneNumber);
				try {
					if (ActivityCompat.checkSelfPermission (this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
						Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode (phoneNumber));
						Cursor cursor = getContentResolver ().query (uri, new String[] {ContactsContract.PhoneLookup._ID}, null, null, null);
						if (cursor != null) {
							if (cursor.moveToFirst ()) {
								String id = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup._ID));
								if (id != null && !id.trim ().isEmpty ()) {
									InputStream inputStream = null;
									try {
										inputStream = ContactsContract.Contacts.openContactPhotoInputStream (getContentResolver (), ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, Long.valueOf (id)));
									} catch (Exception e) {
										LOGE (TAG, e.getMessage ());
										LOGE (TAG, e.toString ());
										e.printStackTrace ();
									}
									if (inputStream != null) {
										Bitmap bitmap = null;
										try {
											bitmap = BitmapFactory.decodeStream (inputStream);
										} catch (Exception e) {
											LOGE (TAG, e.getMessage ());
											LOGE (TAG, e.toString ());
											e.printStackTrace ();
										}
										if (bitmap != null) {
											imageBitmap = ResourceUtil.getBitmapClippedCircle (bitmap);
										}
									}
								}
							}
							cursor.close ();
						}
					}
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			} else {
				((TextView) findViewById (R.id.int_number_phone)).setText (getString (R.string.txt_unknown_number));
			}
			typeTextView.setText (getString (R.string.txt_outgoing_call));
			if (imageBitmap != null) {
				typeImageView.setImageBitmap (imageBitmap);
			} else {
				typeImageView.setImageDrawable (ResourceUtil.getDrawable (this, R.drawable.img_outgoing));
				typeImageView.setColorFilter(getResources().getColor(R.color.blue));

			}
			if (!DateFormat.is24HourFormat (this)) {
				try {
					beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getBeginTimestamp ()));
					endTimeDate = new SimpleDateFormat ("dd-MM-yyyy hh:mm a", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getEndTimestamp ()));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			} else {
				try {
					beginTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getBeginTimestamp ()));
					endTimeDate = new SimpleDateFormat ("dd-MM-yyyy HH:mm", Locale.getDefault ()).format (new Date (mOutgoingCallObject.getEndTimestamp ()));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			String durationString = null;
			Date beginDate = new Date (mOutgoingCallObject.getBeginTimestamp ());
			Date endDate = new Date (mOutgoingCallObject.getEndTimestamp ());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				try {
					Duration duration = Duration.between (beginDate.toInstant (), endDate.toInstant ());
					long minutes = TimeUnit.SECONDS.toMinutes (duration.getSeconds ());
					durationString = String.format (Locale.getDefault (), "%d min, %d sec",
							minutes,
							duration.getSeconds () - TimeUnit.MINUTES.toSeconds (minutes));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			} else {
				long durationMs = endDate.getTime () - beginDate.getTime ();
				try {
					long minutes = TimeUnit.MILLISECONDS.toMinutes (durationMs);
					durationString = String.format (Locale.getDefault (), "%d min, %d sec",
							minutes,
							TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (minutes));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			durationString = durationString != null && !durationString.isEmpty () ? durationString : "N/A";
			((TextView) findViewById (R.id.int_duration)).setText (durationString);
		}
		TextView beginTimeDateTextView = findViewById (R.id.int_begin_date_time);
		beginTimeDateTextView.setText (beginTimeDate != null && !beginTimeDate.trim ().isEmpty () ? beginTimeDate : "N/A");
		TextView endTimeDateTextView = findViewById (R.id.int_end_date_time);
		endTimeDateTextView.setText (endTimeDate != null && !endTimeDate.trim ().isEmpty () ? endTimeDate : "N/A");
		float mainMargin = getResources ().getDimension (R.dimen._16sdp);
		File file = null;
		try {
			if (mIsIncoming) {
				file = new File (mIncomingCallObject.getOutputFile ());
			} else if (mIsOutgoing) {
				file = new File (mOutgoingCallObject.getOutputFile ());
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		String path = file != null ? file.getPath () : null;
		boolean exists = false, isFile = false;
		if (file != null) {
			exists = file.exists ();
			isFile = file.isFile ();
		}
		if (path != null && !path.trim ().isEmpty ()) {
			if (exists && isFile) {
				SeekBar playSeekBar = findViewById (R.id.seekbar_call);
				playSeekBar.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () {
					@Override
					public void onProgressChanged (SeekBar seekBar, int i, boolean b) {
						if (b) {
							if (mMediaPlayer != null) {
								mMediaPlayer.seekTo (i);
							}
							playSeekBar.setProgress (i);
						}
					}

					@Override
					public void onStartTrackingTouch (SeekBar seekBar) {
					}

					@Override
					public void onStopTrackingTouch (SeekBar seekBar) {
					}
				});
				TextView playTimeElapsedTextView = findViewById (R.id.int_el_start_time);
				TextView playTimeRemainingTextView = findViewById (R.id.int_re_finish_time);
				playImageButton.setOnClickListener (view -> {
						if (mMediaPlayer != null) {
							if (mMediaPlayer.isPlaying ()) {
								mMediaPlayer.pause ();
								playImageButton.setImageResource (R.drawable.img_play);
							} else {
								mMediaPlayer.start ();
								playImageButton.setImageResource (R.drawable.img_pause);
							}
						} else {
							playImageButton.setImageResource (R.drawable.img_play);
						}

				});
				SeekBar volumeSeekBar = findViewById (R.id.seekbar_volume);
				volumeSeekBar.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () {
					@Override
					public void onProgressChanged (SeekBar seekBar, int i, boolean b) {
						if (b) {
							if (mMediaPlayer != null) {
								mMediaPlayer.setVolume (i / 100f, i / 100f);
							}
							volumeSeekBar.setProgress (i);
						}
					}

					@Override
					public void onStartTrackingTouch (SeekBar seekBar) {
					}

					@Override
					public void onStopTrackingTouch (SeekBar seekBar) {
					}
				});
				try {
					mMediaPlayer = MediaPlayer.create (this, Uri.parse (path));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
				if (mMediaPlayer != null) {
					mMediaPlayer.setOnCompletionListener (mediaPlayer -> {
						if (mediaPlayer != null) {
							if (mediaPlayer.isPlaying ()) {
								mediaPlayer.pause ();
								playImageButton.setImageResource (R.drawable.img_play);
							} else {
								mediaPlayer.start ();
								playImageButton.setImageResource (R.drawable.img_pause);
							}
						} else {
							playImageButton.setImageResource (R.drawable.img_play);
						}
					});
					mMediaPlayer.setOnInfoListener ((mp, what, extra) -> false);
					mMediaPlayer.setOnErrorListener ((mp, what, extra) -> false);
					mMediaPlayer.seekTo (0);
					mMediaPlayer.setVolume (0.5f, 0.5f);
					playSeekBar.setMax (mMediaPlayer.getDuration ());
					Handler handler = new Handler ();
					runOnUiThread (new Runnable () {
						@Override
						public void run () {
							if (mMediaPlayer != null) {
								int currentPosition = mMediaPlayer.getCurrentPosition ();
								playSeekBar.setProgress (currentPosition);
								String elapsedTime;
								int minElapsed = currentPosition / 1000 / 60;
								int secElapsed = currentPosition / 1000 % 60;
								elapsedTime = minElapsed + ":";
								if (secElapsed < 10) {
									elapsedTime += "0";
								}
								elapsedTime += secElapsed;
								playTimeElapsedTextView.setText (elapsedTime);
								String remainingTime;
								int minRemaining = (playSeekBar.getMax () - currentPosition) / 1000 / 60;
								int secRemaining = (playSeekBar.getMax () - currentPosition) / 1000 % 60;
								remainingTime = minRemaining + ":";
								if (secRemaining < 10) {
									remainingTime += "0";
								}
								remainingTime += secRemaining;
								playTimeRemainingTextView.setText (remainingTime);
							}
							handler.postDelayed (this, 1);
						}
					});
				}
			}
		}
	}

	@Override
	protected void onDestroy () {
		super.onDestroy ();
		LOGD (TAG, "Activity destroy");
		if (mMediaPlayer != null) {
			try {
				mMediaPlayer.stop ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			try {
				mMediaPlayer.reset ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			try {
				mMediaPlayer.release ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			mMediaPlayer = null;
		}
		if (mIncomingCallObject != null) {
			mIncomingCallObject = null;
		}
		if (mOutgoingCallObject != null) {
			mOutgoingCallObject = null;
		}
		if (mRealm != null) {
			if (!mRealm.isClosed ()) {
				try {
					mRealm.close ();
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			mRealm = null;
		}
		if (mIsIncoming) {
			mIsIncoming = false;
		}
		if (mIsOutgoing) {
			mIsOutgoing = false;
		}
	}

	private Dialog getMissingDataDialog () {
		return new AlertDialog.Builder (this)
				.setTitle (getString(R.string.txt_cannot_get_data))
				.setMessage (getString(R.string.txt_getting_call_recording))
				.setNeutralButton (android.R.string.ok, (dialogInterface, i) -> {
					dialogInterface.dismiss ();
					finish ();
				})
				.setCancelable (false)
				.create ();
	}


}

