package com.rec.calls.fragments_for_layouts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import com.rec.calls.items.FavouriteItem;
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


public class FavouriteFragment extends Fragment implements FragmentItem.ITabLayoutIconFragmentPagerAdapter {
	private static final String TAG = FavouriteFragment.class.getSimpleName ();

	public RecyclerView mRecyclerView = null;
	private Realm mRealm = null;
	private RealmResults<CallObject> mFavouritCallObjectRealmResults = null;
	private ScrollView mScrollView = null;
	private LinearLayout mMainLinearLayout = null;

	private Context getContextNonNull () {
		return getContext ();
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
				mFavouritCallObjectRealmResults = mRealm.where (CallObject.class)
						.equalTo ("favourit", true)
						.greaterThan ("mEndTimestamp", 0L)
						.sort ("mBeginTimestamp", Sort.DESCENDING)
						.findAll ();
			} catch (Exception e) {
				LogUtils.LOGE (TAG, e.getMessage ());
				LogUtils.LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (mFavouritCallObjectRealmResults != null) {
				mFavouritCallObjectRealmResults.addChangeListener (incomingCallObjectRealmResults -> {
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
		if (mFavouritCallObjectRealmResults != null) {
			mFavouritCallObjectRealmResults.removeAllChangeListeners ();
			mFavouritCallObjectRealmResults = null;
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
		super.onDestroy ();
		LogUtils.LOGD (TAG, "Fragment destroy");
	}

	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate (R.layout.fragment_favourite_layout, container, false);
		mScrollView = view.findViewById (R.id.scroll_view_favourite);
		mMainLinearLayout = view.findViewById (R.id.linear_layout_favourite);
		mRecyclerView = view.findViewById (R.id.recycler_view_favourite);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager (mRecyclerView.getContext ());
		linearLayoutManager.setOrientation (RecyclerView.VERTICAL);
		mRecyclerView.setHasFixedSize (true);
		mRecyclerView.setLayoutManager (linearLayoutManager);
		mRecyclerView.setItemAnimator (new DefaultItemAnimator ());
		List<CallObject> favouritCallObjectList = null;
		if (mRealm != null) {
			favouritCallObjectList = mRealm.copyFromRealm (mFavouritCallObjectRealmResults);
		}
		if (favouritCallObjectList == null) {
			favouritCallObjectList = new ArrayList<> (mFavouritCallObjectRealmResults);
		}
		setAdapter (populateAdapter (mRecyclerView.getContext (), favouritCallObjectList));
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
		return R.drawable.img_favourite;
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

	private FavouriteItem populateAdapter (@NonNull Context context, @NonNull List<CallObject> callObjectList) {
		Calendar calendar = Calendar.getInstance ();
		int todayDayOfYear = calendar.get (Calendar.DAY_OF_YEAR), yesterdayDayOfYear = todayDayOfYear - 1;
		boolean hasToday = false, hasYesterday = false;
		List<CallObject> list = new ArrayList<> ();
		if (!callObjectList.isEmpty ()) {
			calendar.setTime (new Date (callObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
				hasToday = true;
			}
			if (hasToday) {
				list.add (new CallObject(true, context.getString (R.string.txt_today)));
				for (Iterator<CallObject> iterator = callObjectList.iterator (); iterator.hasNext () ; ) {
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
		if (!callObjectList.isEmpty ()) {
			calendar.setTime (new Date (callObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
				hasYesterday = true;
			}
			if (hasYesterday) {
				list.add (new CallObject(true, context.getString (R.string.txt_yesterday)));
				for (Iterator<CallObject> iterator = callObjectList.iterator (); iterator.hasNext () ; ) {
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
		if (!callObjectList.isEmpty ()) {
			list.add (new CallObject(true, context.getString (R.string.txt_older)));
			list.addAll (callObjectList);
		}
		try {
			if (ActivityCompat.checkSelfPermission (getContextNonNull (), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
				return new FavouriteItem(context, list, true);
			}
		} catch (Exception e) {
			LogUtils.LOGE (TAG, e.getMessage ());
			LogUtils.LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		return new FavouriteItem(context, list);
	}

	private void setAdapter (@NonNull FavouriteItem incomingFavouriteItem) {
		if (mRecyclerView != null) {
			mRecyclerView.setAdapter (incomingFavouriteItem);
			mRecyclerView.setItemViewCacheSize (incomingFavouriteItem.getItemCount ());
		}
	}
}
