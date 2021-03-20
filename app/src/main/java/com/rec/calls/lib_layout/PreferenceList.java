package com.rec.calls.lib_layout;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import com.rec.calls.R;



public class PreferenceList extends ListPreference {
	private Context context;


	public PreferenceList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super (context, attrs, defStyleAttr, defStyleRes);
		this.context = context;
	}


	public PreferenceList(Context context, AttributeSet attrs, int defStyleAttr) {
		super (context, attrs, defStyleAttr);
		this.context = context;
	}


	public PreferenceList(Context context, AttributeSet attrs) {
		super (context, attrs);
		this.context = context;
	}


	public PreferenceList(Context context) {
		super (context);
		this.context = context;
	}

	@Override
	public void onBindViewHolder (PreferenceViewHolder holder) {
		Resources resources = context.getResources();
		super.onBindViewHolder (holder);
		holder.itemView.setMinimumHeight (context.getResources ().getDimensionPixelSize (R.dimen._74sdp));
		TextView title = (TextView) holder.findViewById (android.R.id.title);
		title.setTextColor (resources.getColor(R.color.black_to_white));
		TextView summary = (TextView) holder.findViewById (android.R.id.summary);
		summary.setTextColor (resources.getColor(R.color.green_to_white));



	}
}
