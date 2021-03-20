package com.rec.calls.lib_layout;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import com.rec.calls.R;


public class PreferenceSwitchCompat extends SwitchPreferenceCompat {
	private Context context;


	public PreferenceSwitchCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super (context, attrs, defStyleAttr, defStyleRes);
		this.context = context;
	}

	public PreferenceSwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
		super (context, attrs, defStyleAttr);
		this.context = context;
	}

	public PreferenceSwitchCompat(Context context, AttributeSet attrs) {
		super (context, attrs);
		this.context = context;
	}


	public PreferenceSwitchCompat(Context context) {
		super (context);
		this.context = context;
	}

	@Override
	public void onBindViewHolder (PreferenceViewHolder holder) {
		super.onBindViewHolder (holder);
		Resources resources = context.getResources();
		holder.itemView.setMinimumHeight (context.getResources ().getDimensionPixelSize (R.dimen._74sdp));
		TextView title = (TextView) holder.findViewById (android.R.id.title);
		title.setTextColor (resources.getColor(R.color.black_to_white));
		TextView summary = (TextView) holder.findViewById (android.R.id.summary);
		summary.setTextColor (resources.getColor(R.color.green_to_white));
		SwitchCompat switchCompat = (SwitchCompat) holder.findViewById (R.id.switchWidget);
		switchCompat.setTrackResource (R.drawable.track_settings);
		switchCompat.setThumbResource (R.drawable.thumb_settings);
	}
}
