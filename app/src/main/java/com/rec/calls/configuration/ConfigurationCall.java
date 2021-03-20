package com.rec.calls.configuration;

import android.media.MediaRecorder;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;


public class ConfigurationCall {

	public static final int CALL_RECORDER_DEFAULT_AUDIO_SOURCE = getAudioSources ().get (0);

	public static final int CALL_RECORDER_DEFAULT_OUTPUT_FORMAT = getOutputFormats ().get (0);

	public static final int CALL_RECORDER_DEFAULT_AUDIO_ENCODER = getAudioEncoders ().get (0);


	public static List<Integer> getAudioSources () {
		List<Integer> list = new ArrayList<> ();
		list.add (MediaRecorder.AudioSource.VOICE_CALL);
		list.add (MediaRecorder.AudioSource.VOICE_COMMUNICATION);
		list.add (MediaRecorder.AudioSource.VOICE_RECOGNITION);
		list.add (MediaRecorder.AudioSource.MIC);
		list.add (MediaRecorder.AudioSource.DEFAULT);
		return list;
	}


	public static List<Integer> getOutputFormats () {
		List<Integer> list = new ArrayList<> ();
		list.add (MediaRecorder.OutputFormat.MPEG_4);
		list.add (MediaRecorder.OutputFormat.THREE_GPP);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			list.add (MediaRecorder.OutputFormat.AAC_ADTS);
		}
		list.add (MediaRecorder.OutputFormat.AMR_NB);
		list.add (MediaRecorder.OutputFormat.AMR_WB);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			list.add (MediaRecorder.OutputFormat.WEBM);
		}
		list.add (MediaRecorder.OutputFormat.OGG);
		list.add (MediaRecorder.OutputFormat.DEFAULT);
		return list;
	}


	public static List<Integer> getAudioEncoders () {
		List<Integer> list = new ArrayList<> ();
		list.add (MediaRecorder.AudioEncoder.AAC);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			list.add (MediaRecorder.AudioEncoder.HE_AAC);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			list.add (MediaRecorder.AudioEncoder.AAC_ELD);
		}
		list.add (MediaRecorder.AudioEncoder.AMR_NB);
		list.add (MediaRecorder.AudioEncoder.AMR_WB);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			list.add (MediaRecorder.AudioEncoder.VORBIS);
		}
		list.add (MediaRecorder.AudioEncoder.OPUS);
		list.add (MediaRecorder.AudioEncoder.DEFAULT);
		return list;
	}
}
