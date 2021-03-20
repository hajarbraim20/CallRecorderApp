package com.rec.calls.items;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.rec.calls.R;

import java.util.List;

public class FragmentItem extends FragmentPagerAdapter {
	private Context context;
	private List<ITabLayoutFragmentPagerAdapter> mTabLayoutFragmentPagerAdapterList;
	private List<ITabLayoutIconFragmentPagerAdapter> mTabLayoutIconFragmentPagerAdapterList;

	public FragmentItem(@NonNull FragmentManager fragmentManager,
						@Nullable List<ITabLayoutFragmentPagerAdapter> tabLayoutFragmentPagerAdapterList,
						@Nullable List<ITabLayoutIconFragmentPagerAdapter> tabLayoutIconFragmentPagerAdapterList, Context m) {
		super(fragmentManager);
		context = m;
		if (tabLayoutFragmentPagerAdapterList != null) {
			mTabLayoutFragmentPagerAdapterList = tabLayoutFragmentPagerAdapterList;
			return;
		}
		if (tabLayoutIconFragmentPagerAdapterList != null) {
			mTabLayoutIconFragmentPagerAdapterList = tabLayoutIconFragmentPagerAdapterList;
		}
	}

	@Override
	public Fragment getItem(int i) {
		if (mTabLayoutFragmentPagerAdapterList != null) {
			return mTabLayoutFragmentPagerAdapterList.get(i).getItem();
		}
		if (mTabLayoutIconFragmentPagerAdapterList != null) {
			return mTabLayoutIconFragmentPagerAdapterList.get(i).getItem();
		}
		return null;
	}

	@Override
	public int getCount() {
		if (mTabLayoutFragmentPagerAdapterList != null) {
			return mTabLayoutFragmentPagerAdapterList.size();
		}
		if (mTabLayoutIconFragmentPagerAdapterList != null) {
			return mTabLayoutIconFragmentPagerAdapterList.size();
		}
		return 0;
	}

	@Override
	public CharSequence getPageTitle(int i) {
		Resources r = context.getResources();
		if (i == 0) {
			return r.getString(R.string.txt_outgoing);
		}
		if (i == 1) {
			return r.getString(R.string.txt_incoming);
		}
		if (i == 2) {
			return r.getString(R.string.txt_favourite);
		}

		return  null;
	}

	public interface ITabLayoutFragmentPagerAdapter {

		Fragment getItem ();


	}


	public interface ITabLayoutIconFragmentPagerAdapter {

		Fragment getItem ();


		int getIcon ();
	}
}
