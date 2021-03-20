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
import com.rec.calls.items.IncomingItem;
import com.rec.calls.items.FragmentItem;
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


public class IncomingFragment extends Fragment implements FragmentItem.ITabLayoutIconFragmentPagerAdapter {
	private static final String TAG = IncomingFragment.class.getSimpleName ();

	public RecyclerView mRecyclerView = null;
	private Realm mRealm = null;
	private RealmResults<CallObject> mIncomingCallObjectRealmResults = null;
	private SharedPreferences mSharedPreferences = null;
	private ScrollView mScrollView = null;
	private LinearLayout mMainLinearLayout = null;


	public IncomingFragment() {
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
				mIncomingCallObjectRealmResults = mRealm.where (CallObject.class)
						.greaterThan ("mEndTimestamp", 0L)
						.sort ("mBeginTimestamp", Sort.DESCENDING)
						.beginGroup ()
						.equalTo ("type", "incoming")
						.endGroup ()
						.findAll ();
			} catch (Exception e) {
				LogUtils.LOGE (TAG, e.getMessage ());
				LogUtils.LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (mIncomingCallObjectRealmResults != null) {
				mIncomingCallObjectRealmResults.addChangeListener (incomingCallObjectRealmResults -> {
					if (mRecyclerView != null) {
						List<CallObject> incomingCallObjectList = null;
						if (mRealm != null) {
							incomingCallObjectList = mRealm.copyFromRealm (incomingCallObjectRealmResults);
						}
						if (incomingCallObjectList == null) {
							incomingCallObjectList = new ArrayList<> (incomingCallObjectRealmResults);
						}
						setAdapter (populateAdapter (mRecyclerView.getContext (), incomingCallObjectList));
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
		if (mIncomingCallObjectRealmResults != null) {
			mIncomingCallObjectRealmResults.removeAllChangeListeners ();
			mIncomingCallObjectRealmResults = null;
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
		View view = inflater.inflate (R.layout.fragment_incoming_layout, container, false);
		mScrollView = view.findViewById (R.id.scroll_view_incoming);
		mMainLinearLayout = view.findViewById (R.id.linear_layout_incoming);
		mRecyclerView = view.findViewById (R.id.recycler_view_incoming);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager (mRecyclerView.getContext ());
		linearLayoutManager.setOrientation (RecyclerView.VERTICAL);
		mRecyclerView.setHasFixedSize (true);
		mRecyclerView.setLayoutManager (linearLayoutManager);
		mRecyclerView.setItemAnimator (new DefaultItemAnimator ());
		List<CallObject> incomingCallObjectList = null;
		if (mRealm != null) {
			incomingCallObjectList = mRealm.copyFromRealm (mIncomingCallObjectRealmResults);
		}
		if (incomingCallObjectList == null) {
			incomingCallObjectList = new ArrayList<> (mIncomingCallObjectRealmResults);
		}
		setAdapter (populateAdapter (mRecyclerView.getContext (), incomingCallObjectList));
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
		return R.drawable.img_inoming;
	}

	private IncomingItem populateAdapter (@NonNull Context context, @NonNull List<CallObject> incomingCallObjectList) {
		Calendar calendar = Calendar.getInstance ();
		int todayDayOfYear = calendar.get (Calendar.DAY_OF_YEAR), yesterdayDayOfYear = todayDayOfYear - 1;
		boolean hasToday = false, hasYesterday = false;
		List<CallObject> list = new ArrayList<> ();
		if (!incomingCallObjectList.isEmpty ()) {
			calendar.setTime (new Date (incomingCallObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
				hasToday = true;
			}
			if (hasToday) {
				list.add (new CallObject(true, context.getString (R.string.txt_today)));
				for (Iterator<CallObject> iterator = incomingCallObjectList.iterator (); iterator.hasNext () ; ) {
					CallObject incomingCallObject = iterator.next ();
					calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
						iterator.remove ();
						list.add (incomingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!incomingCallObjectList.isEmpty ()) {
			calendar.setTime (new Date (incomingCallObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
				hasYesterday = true;
			}
			if (hasYesterday) {
				list.add (new CallObject(true, context.getString (R.string.txt_yesterday)));
				for (Iterator<CallObject> iterator = incomingCallObjectList.iterator (); iterator.hasNext () ; ) {
					CallObject incomingCallObject = iterator.next ();
					calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
						iterator.remove ();
						list.add (incomingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!incomingCallObjectList.isEmpty ()) {
			list.add (new CallObject(true, context.getString (R.string.txt_older)));
			list.addAll (incomingCallObjectList);
		}
		try {
			if (ActivityCompat.checkSelfPermission (getContextNonNull (), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
				return new IncomingItem(context, list, true);
			}
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		return new IncomingItem(context, list);
	}

	private void setAdapter (@NonNull IncomingItem incomingItem) {
		if (mRecyclerView != null) {
			mRecyclerView.setAdapter (incomingItem);
			mRecyclerView.setItemViewCacheSize (incomingItem.getItemCount ());
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
