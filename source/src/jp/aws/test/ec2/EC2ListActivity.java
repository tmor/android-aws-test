/**
 * @file
 * @brief 一覧表示アクティビティ
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test.ec2;

import jp.aws.test.AmazonClientManager;
import jp.aws.test.KeyValueArrayAdapter;
import jp.aws.test.Converter;
import jp.aws.test.MultiLineListRow;
import jp.aws.test.MultiLineListRowAdapter;
import jp.aws.test.MultiLineListRowImpl;
import jp.aws.test.MyPrefs;
import jp.aws.test.R;
import jp.aws.test.TtsImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.aws.test.ec2.EC2Item;
import jp.aws.test.ec2.EC2Region;
import jp.aws.test.ec2.EC2Instance;
import jp.aws.test.AlertActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

/**
 * 一覧表示アクティビティ
 */
public class EC2ListActivity extends AlertActivity implements
		OnItemClickListener, AdapterView.OnItemSelectedListener {

	public static final int REQUEST_CODE_PREFS = 1; // 設定画面

	public static boolean isDebug = false; // デバッグ中フラグ
	private static AmazonClientManager _clientManager = null; // aws client
	private final static String TAG = EC2ListActivity.class.getPackage()
			.getName() + "." + EC2ListActivity.class.getSimpleName();
	private static TtsImpl _tts = null; // tts.
										// finish()するとonDestory()が呼ばれて使えなくなる
	public static Context context; // getApplicationContext()

	// 一覧表示用ListView
	private ListView listView = null;

	// private ArrayAdapter<EC2Item> arrayAdapter = null;
	private MultiLineListRowAdapter arrayAdapter = null;
	private List<EC2Item> ec2item_list_cache = null; // キャッシュ

	// UI
	static final int MENU_EC2_TERMINATE = 1;
	static final int MENU_EC2_REBOOT = 2;
	static final int MENU_EC2_START = 3;
	static final int MENU_EC2_STOP = 4;

	/**
	 * staticイニシャライザ
	 * メモリ不足の時やクラスが再ロードした際に、static変数が初期化されるためNullPointerExceptionの防止
	 */
	static {
		// デバッグフラグ
		isDebug = Debug.isDebuggerConnected();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 自動生成されたR.javaの定数を指定してXMLからレイアウトを生成
		setContentView(R.layout.main);

		// アプリケーションコンテキストを保存
		context = getApplicationContext();

		// aws clientを初期化
		clientManager();

		// TTSは初期化
		tts();

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

		// XMLで定義したandroid:idの値を指定してListViewを取得します。
		listView = (ListView) findViewById(R.id.list_view);

		// ListViewに表示する要素を保持するアダプタを生成します。
		/*
		 * arrayAdapter = new ArrayAdapter<EC2Item>(this,
		 * android.R.layout.simple_list_item_1);
		 */
		List<MultiLineListRow> items = new ArrayList<MultiLineListRow>();
		arrayAdapter = new MultiLineListRowAdapter(this,
				R.layout.com_multiline_row, items);

		// アダプタを設定
		listView.setAdapter(arrayAdapter);

		// リスナの追加
		listView.setOnItemClickListener(this);

		// リストビューにコンテキストメニューを登録
		registerForContextMenu(listView);

		// 設定から読み出し
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		EC2Region ec2Region = new EC2Region();
		String region = prefs.getString(MyPrefs.REGION,
				ec2Region.DEFAULT_REGION);

		// regionLabel = (TextView)findViewById( R.id.region_text);
		// regionLabel.setText(ec2Region.getName(region));

		/*
		 * ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		 * android.R.layout.simple_spinner_item); // アダプタの作成
		 */
		// / スピナーにkey,valueを保持し、スピナーにはvalueを表示したい
		KeyValueArrayAdapter<Pair<String, String>> adapter = new KeyValueArrayAdapter<Pair<String, String>>(
				this, android.R.layout.simple_spinner_item,
				new Converter<Pair<String, String>>() {
					@Override
					public String toDisplayString(Pair<String, String> t) {
						return t.second;
					}

				});
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // レイアウトを指定。アイコンなし：simple_spinner_item
		// アイコンあり：simple_spinner_dropdown_item

		// アイテムを追加します
		/*
		 * ソートする場合 Object[] keys = EC2Region.regions.keySet().toArray();
		 * Arrays.sort(keys); // キーの昇順でソート
		 */
		int i = 0, pos = 0;
		for (Iterator<String> it = EC2Region.regions.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			// adapter.add(keys[i]);
			adapter.add(new Pair<String, String>(key, EC2Region.regions
					.get(key)));
			if (region == key)
				pos = i;
			i++;
		}
		Spinner regionSpinner = (Spinner) findViewById(R.id.region_spinner);
		// アダプターを設定します
		regionSpinner.setAdapter(adapter);
		// スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
		regionSpinner.setOnItemSelectedListener(this);
		regionSpinner.setSelection(pos);

		// キャッシュ初期化
		ec2item_list_cache = new ArrayList<EC2Item>();
	}

	@Override
	protected void onDestroy() {
		if (_tts != null) {
			_tts.destroy(); // TTSの破棄
			_tts = null;
		}

		super.onDestroy();
	}

	/**
	 * AmazonClientManagerインスタンスを返すsingleton実装
	 *
	 * @return
	 * @note - Activityが破棄されるとNullPointerExceptionが発生するため
	 */
	public static AmazonClientManager clientManager() {
		// aws clientを初期化
		if (_clientManager == null) {
			_clientManager = new AmazonClientManager(context);
		}

		return _clientManager;
	}

	/**
	 * TtsImplインスタンスを返すsingleton実装
	 *
	 * @return
	 * @note - Activityが破棄されるとNullPointerExceptionが発生するため
	 */
	public static TtsImpl tts() {
		// aws clientを初期化
		if (_tts == null) {
			_tts = new TtsImpl(context);
		}

		return _tts;
	}

	// コンテキストメニュー
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) menuInfo;
		ListView listView = (ListView) view;

		// EC2Item ec2Item =
		// (EC2Item)listView.getItemAtPosition(adapterinfo.position);
		EC2Item ec2Item = ec2item_list_cache.get(adapterinfo.position);

		// 以下条件の時はコンテキストメニューを出さない
		if (ec2Item.stateName.equals("terminated") // 終了した
				|| ec2Item.stateName.equals("pending") // ペンディング中
				|| ec2Item.stateName.equals("stopping") // 停止中
				|| ec2Item.stateName.equals("shutting-down") // 終了中
		) {
			return;
		}

		menu.setHeaderTitle(ec2Item.instanceId);
		menu.add(Menu.NONE, MENU_EC2_TERMINATE, Menu.NONE,
				R.string.ec2_terminate);
		menu.add(Menu.NONE, MENU_EC2_REBOOT, Menu.NONE, R.string.ec2_reboot);
		if (!ec2Item.stateName.equals("running")) {
			menu.add(Menu.NONE, MENU_EC2_START, Menu.NONE, R.string.ec2_start);
		}
		if (ec2Item.stateName.equals("running")) {
			menu.add(Menu.NONE, MENU_EC2_STOP, Menu.NONE, R.string.ec2_stop);
		}
	}

	// / コンテキストメニュー選択
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final EC2Item ec2Item = ec2item_list_cache.get(info.position);
		final EC2Instance ec2Instance = new EC2Instance(clientManager());

		if (ec2Item.instanceId.equals("")) {
			// インスタンスidが無い場合は終了
			return super.onContextItemSelected(item);
		}

		// 確認ダイアログ
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert);
		String ec2state;

		switch (item.getItemId()) {
		case MENU_EC2_TERMINATE:
			ec2state = getString(R.string.ec2_terminate);
			alertDialogBuilder
					.setTitle(ec2state + " instance")
					.setMessage(
							String.format(
									getString(R.string.ec2_confirm_message),
									ec2state)
									+ ec2Item.instanceId)
					.setPositiveButton(getString(android.R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにYESの処理 */
									try {

										tts().speak(
												getString(R.string.tts_ec2_terminate));
										ec2Instance
												.terminate(ec2Item.instanceId);
									} catch (Exception e) {
										setStackAndPost(e); // エラーダイアログを出す
									}
								}
							})
					.setNegativeButton(getString(android.R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにNOの処理 */
								}
							}).show();
			return true;
		case MENU_EC2_REBOOT:
			ec2state = getString(R.string.ec2_reboot);
			alertDialogBuilder
					.setTitle(ec2state + " instance")
					.setMessage(
							String.format(
									getString(R.string.ec2_confirm_message),
									ec2state)
									+ ec2Item.instanceId)
					.setPositiveButton(getString(android.R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにYESの処理 */
									try {
										tts().speak(
												getString(R.string.tts_ec2_reboot));
										ec2Instance.reboot(ec2Item.instanceId);
									} catch (Exception e) {
										setStackAndPost(e); // エラーダイアログを出す
									}
								}
							})
					.setNegativeButton(getString(android.R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにNOの処理 */
								}
							}).show();
			return true;
		case MENU_EC2_START:
			ec2state = getString(R.string.ec2_start);
			alertDialogBuilder
					.setTitle(ec2state + " instance")
					.setMessage(
							String.format(
									getString(R.string.ec2_confirm_message),
									ec2state)
									+ ec2Item.instanceId)
					.setPositiveButton(getString(android.R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにYESの処理 */
									try {

										tts().speak(
												getString(R.string.tts_ec2_start));
										ec2Instance.start(ec2Item.instanceId);
									} catch (Exception e) {
										setStackAndPost(e); // エラーダイアログを出す
									}
								}
							})
					.setNegativeButton(getString(android.R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにNOの処理 */
								}
							}).show();

			return true;
		case MENU_EC2_STOP:
			ec2state = getString(R.string.ec2_stop);
			alertDialogBuilder
					.setTitle(ec2state + " instance")
					.setMessage(
							String.format(
									getString(R.string.ec2_confirm_message),
									ec2state)
									+ ec2Item.instanceId)
					.setPositiveButton(getString(android.R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにYESの処理 */
									try {

										tts().speak(
												getString(R.string.tts_ec2_stop));
										ec2Instance.stop(ec2Item.instanceId);
									} catch (Exception e) {
										setStackAndPost(e); // エラーダイアログを出す
									}
								}
							})
					.setNegativeButton(getString(android.R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* ここにNOの処理 */
								}
							}).show();

			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * スピナー変更
	 */
	public void onItemSelected(AdapterView parent, View view, int position,
			long id) {
		Spinner spinner = (Spinner) parent;
		// 選択されたアイテムを取得します
		// String item = (String) spinner.getSelectedItem();
		Pair<String, String> item = (Pair<String, String>) spinner
				.getSelectedItem();

		// 設定に保存
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String beforItem = prefs.getString(MyPrefs.REGION,
				EC2Region.DEFAULT_REGION);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyPrefs.REGION, item.first); // us-east-1等のIDを保存
		editor.commit();
		if (Debug.isDebuggerConnected()) {
			Log.d("onItemSelected", item.first);
		}

		// リージョンを変更したら更新
		if (!item.equals(beforItem)) {
			this.clearItemCache();
			this.onResume();
		}
	}

	/**
	 * スピナー何も選択されなかった時の動作
	 */
	public void onNothingSelected(AdapterView parent) {
	}

	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
	 * (keyCode == KeyEvent.KEYCODE_BACK) { // このActivityをTopにしたいので戻るを無効 return
	 * true; } return false;
	 *
	 * }
	 */
	/**
	 * オプションメニューの生成
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// XMLで定義したmenuを指定する。
		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	// / メニュー表示毎
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		return true;
	}

	/**
	 * オプションメニューの選択
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.Update:
			// 画面の更新
			this.clearItemCache(); // キャッシュのクリア
			this.onResume();
			break;
		case R.id.Account:
			// 設定画面へ遷移
			startActivityForResult(new Intent(this, jp.aws.test.MyPrefs.class),
					REQUEST_CODE_PREFS);
			break;
		case R.id.Launch:
			// インスタンスの起動。AMI一覧画面へ遷移
			startActivity(new Intent(this,
					jp.aws.test.ec2.AMIListActivity.class));
			break;
		/*
		 * case R.id.menu_new: // 編集画面へ遷移 startActivity( new
		 * Intent(this,RegistActivity.class) ); break;
		 */
		}
		return true;
	};

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
				// デバッグ時はリソースから取得
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
			clientManager().clearClients();

			// キャッシュ消去
			ec2item_list_cache.clear();
		}
	}

	/**
	 * List要素クリック時の処理
	 *
	 * 選択されたエンティティを詰めて参照画面へ遷移する
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		// 選択された要素を取得する
		// EC2Item ec2Item = (EC2Item)parent.getItemAtPosition(position);
		EC2Item ec2Item = ec2item_list_cache.get(position);
		// 参照画面へ遷移する明示的インテントを生成
		Intent showIntent = new Intent(this,
				jp.aws.test.ec2.EC2ShowActivity.class);
		// 選択されたオブジェクトをインテントに詰める
		showIntent.putExtra(EC2Item.TABLE_NAME, ec2Item);
		// アクティビティを開始する
		startActivity(showIntent);
	}

	/**
	 * アクティビティが前面に来るたびにデータを更新
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// AccessKey, SecretKeyが設定されていない場合は設定画面を開く
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getString("prefs_account_access_key", "") == ""
				&& prefs.getString("prefs_account_secret_key", "") == "") {
			startActivity(new Intent(this, jp.aws.test.MyPrefs.class));
			return;
		}

		// データ取得タスクの実行
		DataLoadTask task = new DataLoadTask();
		task.execute();
	}

	/**
	 * 一覧データの取得と表示を行うタスク
	 */
	public class DataLoadTask extends AsyncTask<Object, Integer, List<EC2Item>> {
		// 処理中ダイアログ
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// バックグラウンドの処理前にUIスレッドでダイアログ表示
			progressDialog = new ProgressDialog(EC2ListActivity.this);
			progressDialog.setMessage(getResources().getText(
					R.string.data_loading));
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected List<EC2Item> doInBackground(Object... params) {
			try {
				// 一覧データの取得をバックグラウンドで実行
				// EC2ItemDao dao = new EC2ItemDao(EC2ListActivity.this);
				EC2Instance dao = new EC2Instance(clientManager());
				List<EC2Item> list = null;

				if (ec2item_list_cache.isEmpty()) {
					list = dao.list(); // 時間のかかる処理
					ec2item_list_cache = list;
				}
			} catch (Exception e) {
				setStackAndPost(e); // エラーダイアログを出す
			}

			return ec2item_list_cache;
		}

		@Override
		protected void onPostExecute(List<EC2Item> result) {
			// 処理中ダイアログをクローズ
			progressDialog.dismiss();

			// 表示データのクリア
			arrayAdapter.clear();

			// 表示データの設定
			for (EC2Item ec2Item : result) {
				arrayAdapter.add(MultiLineListRowImpl.create().addEC2Item(
						ec2Item));
				// arrayAdapter.add(ec2Item);
			}
		}
	}

	/**
	 * アイテムキャッシュのクリア
	 */
	protected void clearItemCache() {
		ec2item_list_cache.clear();
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
