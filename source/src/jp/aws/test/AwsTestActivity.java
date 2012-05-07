/**
 * @file
 * @brief main
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */
package jp.aws.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;

import jp.aws.test.MultiLineListRow;
import jp.aws.test.AmazonClientManager;

public class AwsTestActivity extends Activity {
	public static final int REQUEST_CODE_PREFS = 1;
	public static final int REQUEST_CODE_EC2_LIST = 2;
	public static boolean isDebug; // / デバッグ中フラグ
	public static AmazonClientManager clientManager = null; // aws client

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// デバッグフラグ
		isDebug = Debug.isDebuggerConnected();

		if (isDebug) {
			// デバッグ時にstrings.xmlの値を使う
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("prefs_account_access_key",
					getString(R.string.debug_access_key));
			editor.putString("prefs_account_secret_key",
					getString(R.string.debug_secret_key));
			editor.commit();
		}

		// aws client。インスタンス生成は使用時に行われる
		this.clientManager = new AmazonClientManager(this);

		// EC2ListActivityがルートになるように初期化
		Intent intent = new Intent(this, jp.aws.test.ec2.EC2ListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, REQUEST_CODE_EC2_LIST);

		// populateContentsListView();

		// このアクティビティに戻れないように
		finish();
	}

	// / メニュー作成時に1度だけ
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	// / メニュー表示毎
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.Account).setEnabled(true);

		return true;
	}

	// / メニューが選択された時
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.Account:
			// 設定画面を開く
			startActivityForResult(new Intent(this, jp.aws.test.MyPrefs.class),
					REQUEST_CODE_PREFS);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 設定画面を閉じた
		if (requestCode == REQUEST_CODE_PREFS) {

			// プリファレンスオブジェクトの取得(1)
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			// プリファレンス読み込み
			if (isDebug) {
				Log.d("prefs_account_access_key",
						prefs.getString("prefs_account_access_key", ""));
				Log.d("prefs_account_secret_key",
						prefs.getString("prefs_account_secret_key", ""));
			}

			// プリファレンスへの書き込み
			// SharedPreferences.Editor editor=prefs.edit();
			// editor.putString("text",editText.getText().toString());
			// editor.commit();

			// 設定変更されたのでaws clientインスタンスを初期化
			this.clientManager.clearClients();
		}
	}

	protected void showAlertDialog(String message) {
		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setTitle("Alert");
		confirm.setMessage(message);
		confirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		confirm.show().show();
	}
}