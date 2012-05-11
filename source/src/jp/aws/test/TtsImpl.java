/**
 * @file
 * @brief TTS実装
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 275 $
 */
package jp.aws.test;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TtsImpl implements TextToSpeech.OnInitListener {
	public static TextToSpeech tts = null;
	private final static String TAG = AwsTestActivity.class.getSimpleName();
	private Activity activity = null;

	public TtsImpl(Activity activity) {
		this.activity = activity;
		// TTS初期化
		if(tts != null){
			destroy();
		}
		tts = new TextToSpeech(activity, this);
	}

	public void destroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US); // 日本に変更:Locale.JAPAN
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.d(TAG, "Language is not available.");
			} else {
				// 使用可能
				Log.d(TAG, "TextToSpeech initialized.");
			}
		} else {
			// 初期化失敗
			Log.d(TAG, "Could not initialize TextToSpeech.");
		}
	}

	/**
	 * 音声合成を実行
	 *
	 * @param text
	 *            読み上げたいテキスト
	 */
	public void startTTS(String text) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.activity);
		boolean tts_enable = prefs.getBoolean("prefs_tts_enable", false);

		if (tts != null && tts_enable) {
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
