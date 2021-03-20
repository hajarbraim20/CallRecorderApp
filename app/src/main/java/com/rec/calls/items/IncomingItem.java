package com.rec.calls.items;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rec.calls.R;
import com.rec.calls.activity.LayoutCall;
import com.rec.calls.envrenement.EnvironmentApplication;
import com.rec.calls.models.CallObject;
import com.rec.calls.utils.ResourceUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.rec.calls.utils.LogUtils;
import io.realm.Realm;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.rec.calls.utils.LogUtils.LOGE;


public class IncomingItem extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
	private static final int VIEW_HEADER = 0, VIEW_ITEM = 1;
	private LayoutInflater mLayoutInflater = null;
	private List<CallObject> mIncomingCallObjectList, mIncomingCallObjectFilteredList;
	private boolean mReadContacts = false;


	public IncomingItem(@NonNull Context context, @NonNull List<CallObject> incomingCallObjectList) {
		try {
			mLayoutInflater = LayoutInflater.from (context);
		} catch (Exception e) {
			e.printStackTrace ();
		}
		if (mLayoutInflater == null) {
			try {
				mLayoutInflater = (LayoutInflater) context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
			} catch (Exception e) {
				e.printStackTrace ();
			}
		}
		mIncomingCallObjectList = mIncomingCallObjectFilteredList = incomingCallObjectList;
	}


	public IncomingItem(@NonNull Context context, @NonNull List<CallObject> incomingCallObjectList, boolean readContacts) {
		this (context, incomingCallObjectList);
		mReadContacts = readContacts;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder viewHolder;
		if (viewType == VIEW_HEADER) {
			View view = mLayoutInflater.inflate (R.layout.item_day_layout, parent, false);
			viewHolder = new HeaderViewHolder (view);
		} else {
			View view = mLayoutInflater.inflate (R.layout.item_layout, parent, false);
			viewHolder = new ItemViewHolder (view);
		}
		return viewHolder;
	}

	@Override
	public void onBindViewHolder (@NonNull RecyclerView.ViewHolder holder, int position) {
		CallObject incomingCallObject = mIncomingCallObjectFilteredList.get (position);
		if (incomingCallObject != null) {
			boolean isFirstItem = position == 0, isLastItem = position == mIncomingCallObjectFilteredList.size () - 1;
			if (holder instanceof HeaderViewHolder) {
				HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
				headerViewHolder.titleTextView.setText (incomingCallObject.getHeaderTitle ());
			} else {
				ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
				String correspondent = incomingCallObject.getPhoneNumber ();
				if (correspondent != null && !correspondent.trim ().isEmpty ()) {
					if (mReadContacts) {
						try {
							Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode (correspondent));
							Cursor cursor = holder.itemView.getContext ().getContentResolver ().query (uri, new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
							if (cursor != null) {
								if (cursor.moveToFirst ()) {
									String tempDisplayName = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup.DISPLAY_NAME));
									if (tempDisplayName != null && !tempDisplayName.trim ().isEmpty ()) {
										incomingCallObject.setCorrespondentName (correspondent = tempDisplayName);
									}
									String id = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup._ID));
									if (id != null && !id.trim ().isEmpty ()) {
										InputStream inputStream = null;
										try {
											inputStream = ContactsContract.Contacts.openContactPhotoInputStream (holder.itemView.getContext ().getContentResolver (), ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, Long.valueOf (id)));
										} catch (Exception e) {
											e.printStackTrace ();
										}
										if (inputStream != null) {
											Bitmap bitmap = null;
											try {
												bitmap = BitmapFactory.decodeStream (inputStream);
											} catch (Exception e) {
												e.printStackTrace ();
											}
											if (bitmap != null) {
											}
										}
									}
								}
								cursor.close ();
							}
						} catch (Exception e) {
							e.printStackTrace ();
						}
					}
					if (correspondent.equals (incomingCallObject.getPhoneNumber ())) {
						itemViewHolder.numberTextView.setText (holder.itemView.getContext ().getString (R.string.txt_unknown_number));
					} else {
						itemViewHolder.numberTextView.setText (correspondent);
					}
				} else {
					itemViewHolder.numberTextView.setText (correspondent = holder.itemView.getContext ().getString (R.string.txt_unknown_number));
				}
				String beginDateTime = null;
				if (!DateFormat.is24HourFormat (holder.itemView.getContext ())) {
					try {
						beginDateTime = new SimpleDateFormat ("hh:mm a", Locale.getDefault ()).format (new Date (incomingCallObject.getBeginTimestamp ()));
					} catch (Exception e) {
						e.printStackTrace ();
					}
				} else {
					try {
						beginDateTime = new SimpleDateFormat ("HH:mm", Locale.getDefault ()).format (new Date (incomingCallObject.getBeginTimestamp ()));
					} catch (Exception e) {
						e.printStackTrace ();
					}
				}
				String durationString = null;
				Date beginDate = new Date (incomingCallObject.getBeginTimestamp ());
				Date endDate = new Date (incomingCallObject.getEndTimestamp ());
				long durationMs = endDate.getTime () - beginDate.getTime ();
				try {
					durationString = String.format (Locale.getDefault (), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours (durationMs),
							TimeUnit.MILLISECONDS.toMinutes (durationMs) - TimeUnit.HOURS.toMinutes (TimeUnit.MILLISECONDS.toHours (durationMs)),
							TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (TimeUnit.MILLISECONDS.toMinutes (durationMs)));
				} catch (Exception e) {
					LogUtils.LOGE (TAG, e.getMessage ());
					LogUtils.LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
				itemViewHolder.beginDateTimeTextView.setText (beginDateTime != null ? beginDateTime + "\n(" + durationString + ")" : "N/A");
				String finalCorrespondent = correspondent;
				itemViewHolder.menuImageButton.setOnClickListener (view -> showItemMenuDialog (holder.itemView.getContext (), incomingCallObject, finalCorrespondent));
				itemViewHolder.imageView.setImageResource (R.drawable.img_inoming);
				itemViewHolder.imageView.setColorFilter (ContextCompat.getColor (holder.itemView.getContext (), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
				itemViewHolder.tvPhoneNumer.setText (String.valueOf (incomingCallObject.getPhoneNumber ()));
				if (incomingCallObject.isFavourit ()) {
					itemViewHolder.ivFavourit.setImageResource (R.drawable.img_favourite);
				} else {
					itemViewHolder.ivFavourit.setImageResource (R.drawable.img_favourite_strok);
				}
				itemViewHolder.ivFavourit.setColorFilter (ContextCompat.getColor (holder.itemView.getContext (), R.color.color_favourite_item), android.graphics.PorterDuff.Mode.SRC_IN);
			}
		}
	}

	@Override
	public int getItemCount () {
		return mIncomingCallObjectFilteredList != null ? mIncomingCallObjectFilteredList.size () : 0;
	}

	@Override
	public long getItemId (int position) {
		return position;
	}

	@Override
	public int getItemViewType (int position) {
		return mIncomingCallObjectFilteredList.get (position).getIsHeader () ? VIEW_HEADER : VIEW_ITEM;
	}

	@Override
	public Filter getFilter () {
		return new Filter () {
			@Override
			protected FilterResults performFiltering (CharSequence charSequence) {
				String query = charSequence != null ? charSequence.toString () : null;
				if (query != null && !query.trim ().isEmpty ()) {
					List<CallObject> newIncomingCallObjectFilteredList = new ArrayList<> ();
					for (CallObject incomingCallObject : mIncomingCallObjectList) {
						if (!incomingCallObject.getIsHeader ()) {
							String phoneNumber = incomingCallObject.getPhoneNumber ();
							if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
								if (phoneNumber.toLowerCase (Locale.getDefault ()).contains (query.toLowerCase (Locale.getDefault ()))) {
									newIncomingCallObjectFilteredList.add (incomingCallObject);
								}
							}
							if (mReadContacts) {
								String correspondentName = incomingCallObject.getCorrespondentName ();
								if (correspondentName != null && !correspondentName.trim ().isEmpty ()) {
									if (correspondentName.toLowerCase (Locale.getDefault ()).contains (query.toLowerCase (Locale.getDefault ()))) {
										newIncomingCallObjectFilteredList.add (incomingCallObject);
									}
								}
							}
						}
					}
					mIncomingCallObjectFilteredList = newIncomingCallObjectFilteredList;
				} else {
					mIncomingCallObjectFilteredList = mIncomingCallObjectList;
				}
				FilterResults filterResults = new FilterResults ();
				filterResults.values = mIncomingCallObjectFilteredList;
				filterResults.count = mIncomingCallObjectFilteredList.size ();
				return filterResults;
			}

			@Override
			protected void publishResults (CharSequence charSequence, FilterResults filterResults) {
				mIncomingCallObjectFilteredList = (ArrayList<CallObject>) filterResults.values;
				notifyDataSetChanged ();
			}
		};
	}

	private void openIncomingCall (@NonNull Context context, @NonNull CallObject incomingCallObject) {
		Intent intent = new Intent (context, LayoutCall.class);
		intent.putExtra (EnvironmentApplication.INTENT_ACTION_INCOMING_CALL, true);
		intent.putExtra ("mBeginTimestamp", incomingCallObject.getBeginTimestamp ());
		intent.putExtra ("mEndTimestamp", incomingCallObject.getEndTimestamp ());
		intent.putExtra ("mType", incomingCallObject.getType ());
		if (incomingCallObject.getCorrespondentName () != null && !incomingCallObject.getCorrespondentName ().trim ().isEmpty ()) {
			intent.putExtra ("mCorrespondentName", incomingCallObject.getCorrespondentName ());
		}
		try {
			context.startActivity (intent);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	private boolean showItemMenuDialog (@NonNull Context context, @NonNull CallObject incomingCallObject, @NonNull String correspondent) {
Resources resources = context.getResources();
		CharSequence[] menuItems = {incomingCallObject.isFavourit () ? resources.getString(R.string.txt_unfavourite) :resources.getString(R.string.txt_favou) , resources.getString(R.string.txt_open_call_recorder)  , resources.getString(R.string.txt_delete) };		Drawable drawable = ResourceUtil.getDrawable (context, R.drawable.img_inoming);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			drawable.setTint (ResourceUtil.getColor (context, R.color.red));
		} else {
			DrawableCompat.setTint (drawable, ResourceUtil.getColor (context, R.color.red));
		}
		Dialog dialog = new AlertDialog.Builder (context,R.style.StyleDialogTheme)
				.setIcon (drawable)
				.setTitle (context.getString (R.string.txt_incoming_call) + " - " + correspondent)
				.setItems (menuItems, (dialogInterface, which) -> {
					dialogInterface.dismiss ();
					switch (which) {
						case 0:
							Realm realmf = null;
							try {
								realmf = Realm.getDefaultInstance ();
							} catch (Exception e) {
								e.printStackTrace ();
							}
							if (realmf != null && !realmf.isClosed ()) {
								try {
									realmf.beginTransaction ();
									List<CallObject> incomingCallObjects = realmf.where (CallObject.class)
											.equalTo ("mPhoneNumber", incomingCallObject.getPhoneNumber ())
											.findAll ();
									if (incomingCallObjects != null) {
										for (CallObject callObject : incomingCallObjects) {
											callObject.setFavourit (!incomingCallObject.isFavourit ());
										}
										realmf.commitTransaction ();
									} else {
										realmf.cancelTransaction ();
										//Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
									}
									realmf.close ();
								} catch (Exception e) {
									e.printStackTrace ();
								}
							} else {
								//Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
							}
							break;
						case 1:
							openIncomingCall (context, incomingCallObject);
							break;
						case 2:
							new AlertDialog.Builder (context,R.style.StyleAlertDialog)
									.setTitle (resources.getString(R.string.txt_delete_call_recorder))
									.setMessage (resources.getString(R.string.txt_are_you_sure_you))
									.setPositiveButton (R.string.txt_yes, (dialogInterface1, i) -> {
										dialogInterface1.dismiss ();
										Realm realm = null;
										try {
											realm = Realm.getDefaultInstance ();
										} catch (Exception e) {
											e.printStackTrace ();
										}
										if (realm != null && !realm.isClosed ()) {
											try {
												realm.beginTransaction ();
												CallObject incomingCallObject1 = realm.where (CallObject.class)
														.equalTo ("mBeginTimestamp", incomingCallObject.getBeginTimestamp ())
														.equalTo ("mEndTimestamp", incomingCallObject.getEndTimestamp ())
														.beginGroup ()
														.equalTo ("type", "incoming")
														.endGroup ()
														.findFirst ();
												if (incomingCallObject1 != null) {
													File outputFile = null;
													try {
														outputFile = new File (incomingCallObject1.getOutputFile ());
													} catch (Exception e) {
														e.printStackTrace ();
													}
													if (outputFile != null) {
														if (outputFile.exists () && outputFile.isFile ()) {
															try {
																outputFile.delete ();
															} catch (Exception e) {
																e.printStackTrace ();
															}
														}
													}
													incomingCallObject1.deleteFromRealm ();
													realm.commitTransaction ();
													//Toast.makeText (context, "Call recording is deleted", Toast.LENGTH_SHORT).show ();
												} else {
													realm.cancelTransaction ();
													//Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
												}
												realm.close ();
											} catch (Exception e) {
												e.printStackTrace ();
											}
										} else {
											//Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
										}
									})
									.setNegativeButton (R.string.txt_no, (dialogInterface1, i) -> dialogInterface1.dismiss ())
									.create ().show ();
							break;
					}
				}).create ();
		dialog.show ();
		return dialog.isShowing ();
	}


	class HeaderViewHolder extends RecyclerView.ViewHolder {

		TextView titleTextView;


		public HeaderViewHolder (@NonNull View itemView) {
			super (itemView);
			titleTextView = itemView.findViewById (R.id.txt_information);
		}
	}


	class ItemViewHolder extends RecyclerView.ViewHolder {

		ImageView imageView;

		TextView numberTextView,
		beginDateTimeTextView;
		TextView tvPhoneNumer;

		ImageButton menuImageButton;

		ImageView ivFavourit;


		public ItemViewHolder (@NonNull View itemView) {
			super (itemView);
			imageView = itemView.findViewById (R.id.img_type_of_call_item);
			numberTextView = itemView.findViewById (R.id.txt_number_phone_item);
			beginDateTimeTextView = itemView.findViewById (R.id.int_date_item);
			menuImageButton = itemView.findViewById (R.id.img_menu_item);
			tvPhoneNumer = itemView.findViewById (R.id.int_number_phone_item);
			ivFavourit = itemView.findViewById (R.id.img_favourite_item);
		}
	}
}
