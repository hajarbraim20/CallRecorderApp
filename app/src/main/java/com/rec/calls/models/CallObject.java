package com.rec.calls.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;


public class CallObject extends RealmObject {
	private String mPhoneNumber;
	private long mBeginTimestamp, mEndTimestamp;
	private boolean mIsInProgress;
	private String mSimOperator, mSimOperatorName, mSimCountryIso;
	private String mSimSerialNumber;
	private String mNetworkOperator, mNetworkOperatorName, mNetworkCountryIso;
	private int mAudioSource, mOutputFormat, mAudioEncoder;
	private String mOutputFile;
	private boolean mIsSaved;
	private String type;
	private boolean favourit;
	@Ignore
	private boolean mIsHeader = false;
	@Ignore
	private String mHeaderTitle = null;
	@Ignore
	private boolean mIsLastInCategory = false;
	@Ignore
	private String mCorrespondentName = null;


	public CallObject() {
	}


	public CallObject(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}


	public CallObject(String phoneNumber,
					  long beginTimestamp, long endTimestamp,
					  boolean isInProgress,
					  String simOperator, String simOperatorName, String simCountryIso,
					  String simSerialNumber,
					  String networkOperator, String networkOperatorName, String networkCountryIso,
					  int audioSource, int outputFormat, int audioEncoder,
					  String outputFile,
					  String type,
					  boolean favourit,
					  boolean isSaved) {
		mPhoneNumber = phoneNumber;
		mBeginTimestamp = beginTimestamp;
		mEndTimestamp = endTimestamp;
		mIsInProgress = isInProgress;
		mSimOperator = simOperator;
		mSimOperatorName = simOperatorName;
		mSimCountryIso = simCountryIso;
		mSimSerialNumber = simSerialNumber;
		mNetworkOperator = networkOperator;
		mNetworkOperatorName = networkOperatorName;
		mNetworkCountryIso = networkCountryIso;
		mAudioSource = audioSource;
		mOutputFormat = outputFormat;
		mAudioEncoder = audioEncoder;
		mOutputFile = outputFile;
		this.type = type;
		this.favourit = favourit;
		mIsSaved = isSaved;
	}


	public CallObject(boolean isHeader, String headerTitle) {
		mIsHeader = isHeader;
		mHeaderTitle = headerTitle;
	}


	public String getPhoneNumber () {
		return mPhoneNumber;
	}


	public void setPhoneNumber (String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}


	public long getBeginTimestamp () {
		return mBeginTimestamp;
	}


	public void setBeginTimestamp (long beginTimestamp) {
		mBeginTimestamp = beginTimestamp;
	}


	public long getEndTimestamp () {
		return mEndTimestamp;
	}

	public void setEndTimestamp (long endTimestamp) {
		mEndTimestamp = endTimestamp;
	}


	public boolean isIsInProgress () {
		return mIsInProgress;
	}


	public void setIsInProgress (boolean isInProgress) {
		mIsInProgress = isInProgress;
	}


	public String getSimOperator () {
		return mSimOperator;
	}


	public void setSimOperator (String simOperator) {
		mSimOperator = simOperator;
	}


	public String getSimOperatorName () {
		return mSimOperatorName;
	}


	public void setSimOperatorName (String simOperatorName) {
		mSimOperatorName = simOperatorName;
	}


	public String getSimCountryIso () {
		return mSimCountryIso;
	}

	public void setSimCountryIso (String simCountryIso) {
		mSimCountryIso = simCountryIso;
	}


	public String getSimSerialNumber () {
		return mSimSerialNumber;
	}


	public void setSimSerialNumber (String simSerialNumber) {
		mSimSerialNumber = simSerialNumber;
	}


	public String getNetworkOperator () {
		return mNetworkOperator;
	}

	public void setNetworkOperator (String networkOperator) {
		mNetworkOperator = networkOperator;
	}


	public String getNetworkOperatorName () {
		return mNetworkOperatorName;
	}


	public void setNetworkOperatorName (String networkOperatorName) {
		mNetworkOperatorName = networkOperatorName;
	}


	public String getNetworkCountryIso () {
		return mNetworkCountryIso;
	}


	public void setNetworkCountryIso (String networkCountryIso) {
		mNetworkCountryIso = networkCountryIso;
	}


	public int getAudioSource () {
		return mAudioSource;
	}


	public void setAudioSource (int audioSource) {
		mAudioSource = audioSource;
	}


	public int getOutputFormat () {
		return mOutputFormat;
	}


	public void setOutputFormat (int outputFormat) {
		mOutputFormat = outputFormat;
	}


	public int getAudioEncoder () {
		return mAudioEncoder;
	}


	public void setAudioEncoder (int audioEncoder) {
		mAudioEncoder = audioEncoder;
	}

	public String getOutputFile () {
		return mOutputFile;
	}


	public void setOutputFile (String outputFile) {
		mOutputFile = outputFile;
	}

	public boolean getIsSaved () {
		return mIsSaved;
	}


	public void setIsSaved (boolean isSaved) {
		mIsSaved = isSaved;
	}


	public boolean getIsHeader () {
		return mIsHeader;
	}

	public void setIsHeader (boolean isHeader) {
		mIsHeader = isHeader;
	}


	public String getHeaderTitle () {
		return mHeaderTitle.substring (0, 1).toUpperCase () + mHeaderTitle.substring (1).toLowerCase ();
	}


	public void setHeaderTitle (String headerTitle) {
		mHeaderTitle = headerTitle;
	}


	public boolean getIsLastInCategory () {
		return mIsLastInCategory;
	}

	public void setIsLastInCategory (boolean isLastInCategory) {
		mIsLastInCategory = isLastInCategory;
	}


	public String getCorrespondentName () {
		return mCorrespondentName;
	}


	public void setCorrespondentName (String correspondentName) {
		mCorrespondentName = correspondentName;
	}


	public String getType () {
		return type;
	}


	public void setType (String type) {
		this.type = type;
	}


	public boolean isFavourit () {
		return favourit;
	}


	public void setFavourit (boolean favourit) {
		this.favourit = favourit;
	}
}
