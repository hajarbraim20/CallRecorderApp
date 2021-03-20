package com.rec.calls.fragments_for_layouts;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rec.calls.R;
import com.rec.calls.items.OutgoingItem;
import com.rec.calls.items.FragmentItem;
import com.rec.calls.envrenement.EnvironmentApplication;
import com.rec.calls.models.CallObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.rec.calls.utils.LogUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.rec.calls.utils.LogUtils.LOGD;
import static com.rec.calls.utils.LogUtils.LOGE;


public class OutgoingFragment extends Fragment implements FragmentItem.ITabLayoutIconFragmentPagerAdapter {
	private static final String TAG = OutgoingFragment.class.getSimpleName ();

	public RecyclerView mRecyclerView = null;
	private Realm mRealm = null;
	private RealmResults<CallObject> mOutgoingCallObjectRealmResults = null;
	private SharedPreferences mSharedPreferences = null;
	private boolean mRecordOutgoingCalls = true;
	private ScrollView mScrollView = null;
	private LinearLayout mMainLinearLayout = null;
	private Context context;


	public OutgoingFragment() {
	}

	private Context getContextNonNull () {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return getContext ();
		} else {
			return getContext ();
		}
	}

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		LogUtils.LOGD (TAG, "Fragment create");
		try {
			mRealm = Realm.getDefaultInstance ();
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mRealm != null && !mRealm.isClosed ()) {
			try {
				mOutgoingCallObjectRealmResults = mRealm.where (CallObject.class)
						.greaterThan ("mEndTimestamp", 0L)
						.sort ("mBeginTimestamp", Sort.DESCENDING)
						.beginGroup ()
						.equalTo ("type", "outgoing")
						.endGroup ()
						.findAll ();
			} catch (Exception e) {
				LogUtils.LOGE (TAG, e.getMessage ());
				LogUtils.LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (mOutgoingCallObjectRealmResults != null) {
				mOutgoingCallObjectRealmResults.addChangeListener (outgoingCallObjectRealmResults -> {
					if (mRecyclerView != null) {
						List<CallObject> outgoingCallObjectList = null;
						if (mRealm != null) {
							outgoingCallObjectList = mRealm.copyFromRealm (outgoingCallObjectRealmResults);
						}
						if (outgoingCallObjectList == null) {
							outgoingCallObjectList = new ArrayList<> (outgoingCallObjectRealmResults);
						}
						setAdapter (populateAdapter (mRecyclerView.getContext (), outgoingCallObjectList));
					}
					updateLayouts ();
				});
			}
		}
		try {
			mSharedPreferences = getContextNonNull ().getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences.contains (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS)) {
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		} else {
			SharedPreferences.Editor editor = mSharedPreferences.edit ();
			editor.putBoolean (EnvironmentApplication.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
			editor.apply ();
		}
	}

	@Override
	public void onResume () {
		super.onResume ();
		LogUtils.LOGD (TAG, "Fragment resume");
		if (mRealm != null && !mRealm.isClosed ()) {
			try {
				mRealm.refresh ();
			} catch (Exception e) {
				LogUtils.LOGE (TAG, e.getMessage ());
				LogUtils.LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
	}

	@Override
	public void onDestroy () {
		super.onDestroy ();
		LogUtils.LOGD (TAG, "Fragment destroy");
		if (mSharedPreferences != null) {
			mSharedPreferences = null;
		}
		if (mOutgoingCallObjectRealmResults != null) {
			mOutgoingCallObjectRealmResults.removeAllChangeListeners ();
			mOutgoingCallObjectRealmResults = null;
		}
		if (mRealm != null) {
			if (!mRealm.isClosed ()) {
				try {
					mRealm.close ();
				} catch (Exception e) {
					LogUtils.LOGE (TAG, e.getMessage ());
					LogUtils.LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			mRealm = null;
		}
	}

	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate (R.layout.fragment_outgoing_layout, container, false);
		mScrollView = view.findViewById (R.id.scroll_view_outgoing);
		mMainLinearLayout = view.findViewById (R.id.linear_layout_outgoing);
		mRecyclerView = view.findViewById (R.id.recycler_view_outgoing);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager (mRecyclerView.getContext ());
		linearLayoutManager.setOrientation (RecyclerView.VERTICAL);
		mRecyclerView.setHasFixedSize (true);
		mRecyclerView.setLayoutManager (linearLayoutManager);
		mRecyclerView.setItemAnimator (new DefaultItemAnimator ());
		List<CallObject> outgoingCallObjectList = null;
		if (mRealm != null) {
			outgoingCallObjectList = mRealm.copyFromRealm (mOutgoingCallObjectRealmResults);
		}
		if (outgoingCallObjectList == null) {
			outgoingCallObjectList = new ArrayList<> (mOutgoingCallObjectRealmResults);
		}
		setAdapter (populateAdapter (mRecyclerView.getContext (), outgoingCallObjectList));
		return view;
	}

	@Override
	public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated (view, savedInstanceState);
		updateLayouts ();
	}

	@Override
	public Fragment getItem () {
		return this;
	}


	@Override
	public int getIcon () {
		return R.drawable.img_outgoing;
	}

	private OutgoingItem populateAdapter (@NonNull Context context, @NonNull List<CallObject> outgoingCallObjectList) {
		Calendar calendar = Calendar.getInstance ();
		int todayDayOfYear = calendar.get (Calendar.DAY_OF_YEAR), yesterdayDayOfYear = todayDayOfYear - 1;
		boolean hasToday = false, hasYesterday = false;
		List<CallObject> list = new ArrayList<> ();
		if (!outgoingCallObjectList.isEmpty ()) {
			calendar.setTime (new Date (outgoingCallObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
				hasToday = true;
			}
			if (hasToday) {
				list.add (new CallObject(true, context.getString (R.string.txt_today)));
				for (Iterator<CallObject> iterator = outgoingCallObjectList.iterator (); iterator.hasNext () ; ) {
					CallObject outgoingCallObject = iterator.next ();
					calendar.setTime (new Date (outgoingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
						iterator.remove ();
						list.add (outgoingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!outgoingCallObjectList.isEmpty ()) {
			calendar.setTime (new Date (outgoingCallObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
				hasYesterday = true;
			}
			if (hasYesterday) {
				list.add (new CallObject(true, context.getString (R.string.txt_yesterday)));
				for (Iterator<CallObject> iterator = outgoingCallObjectList.iterator (); iterator.hasNext () ; ) {
					CallObject outgoingCallObject = iterator.next ();
					calendar.setTime (new Date (outgoingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
						iterator.remove ();
						list.add (outgoingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!outgoingCallObjectList.isEmpty ()) {
			list.add (new CallObject(true, context.getString (R.string.txt_older)));
			list.addAll (outgoingCallObjectList);
		}
		try {
			if (ActivityCompat.checkSelfPermission (getContextNonNull (), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
				return new OutgoingItem(context, list, true);
			}
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		return new OutgoingItem(context, list);
	}

	private void setAdapter (@NonNull OutgoingItem outgoingItem) {
		if (mRecyclerView != null) {
			mRecyclerView.setAdapter (outgoingItem);
			mRecyclerView.setItemViewCacheSize (outgoingItem.getItemCount ());
		}
	}

	private void updateLayouts () {
		if (mRecyclerView != null && mRecyclerView.getAdapter () != null && mRecyclerView.getAdapter ().getItemCount () > 0) {
			if (mScrollView != null && mScrollView.getVisibility () != View.GONE) {
				mScrollView.setVisibility (View.GONE);
			}
			if (mMainLinearLayout != null && mMainLinearLayout.getVisibility () != View.VISIBLE) {
				mMainLinearLayout.setVisibility (View.VISIBLE);
			}
		} else {
			if (mMainLinearLayout != null && mMainLinearLayout.getVisibility () != View.GONE) {
				mMainLinearLayout.setVisibility (View.GONE);
			}
			if (mScrollView != null && mScrollView.getVisibility () != View.VISIBLE) {
				mScrollView.setVisibility (View.VISIBLE);
			}
		}
	}
}
