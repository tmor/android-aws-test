/**
 * @file
 * @brief AMI一覧表示アクティビティ
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 275 $
 */

package jp.aws.test.ec2;

import jp.aws.test.KeyValueArrayAdapter;
import jp.aws.test.Converter;
import jp.aws.test.MultiLineListRow;
import jp.aws.test.MultiLineListRowAdapter;
import jp.aws.test.MultiLineListRowImpl;
import jp.aws.test.AwsTestActivity;
import jp.aws.test.MyPrefs;
import jp.aws.test.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jp.aws.test.ec2.AMIItem;
import jp.aws.test.ec2.EC2Region;
import jp.aws.test.ec2.EC2Instance;
import jp.aws.test.AlertActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

/**
 * 一覧表示アクティビティ
 */
public class AMIListActivity extends AlertActivity implements
		OnItemClickListener, AdapterView.OnItemSelectedListener {
	// 一覧表示用ListView
	private ListView listView = null;

	// private ArrayAdapter<AMIItem> arrayAdapter = null;
	private MultiLineListRowAdapter arrayAdapter = null;
	private List<AMIItem> amiItem_list_cache = null; // キャッシュ

	// / AMI filter
	public static final HashMap<String, String> filters = new HashMap<String, String>() {
		{
			put("ownerid:self", "My AMIs");
			put("ownerid:self,root-device-type:ebs", "My AMIs with EBS");
			put("ownerid:amazon", "Amazon AMIs");
			put("ownerid:amazon,root-device-type:ebs", "Amazon AMIs with EBS");
			put("ownerid:amazon,name:amzn-ami*", "Amazon Linux AMIs");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 自動生成されたR.javaの定数を指定してXMLからレイアウトを生成
		setContentView(R.layout.ami_list);

		// XMLで定義したandroid:idの値を指定してListViewを取得します。
		listView = (ListView) findViewById(R.id.list_view);

		// ListViewに表示する要素を保持するアダプタを生成します。
		List<MultiLineListRow> items = new ArrayList<MultiLineListRow>();
		arrayAdapter = new MultiLineListRowAdapter(this,
				R.layout.com_multiline_row, items);

		// アダプタを設定
		listView.setAdapter(arrayAdapter);

		// リスナの追加
		listView.setOnItemClickListener(this);

		// 設定から読み出し
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		EC2Region ec2Region = new EC2Region();
		String filter = prefs.getString(MyPrefs.AMI_FILTER,
				ec2Region.DEFAULT_REGION);

		// フィルター設定
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
		int i = 0, pos = 0;
		for (Iterator<String> it = this.filters.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			adapter.add(new Pair<String, String>(key, this.filters.get(key)));
			if (filter == key)
				pos = i;
			i++;
		}
		Spinner filterSpinner = (Spinner) findViewById(R.id.filter_spinner);
		// アダプターを設定します
		filterSpinner.setAdapter(adapter);
		// スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
		filterSpinner.setOnItemSelectedListener(this);
		filterSpinner.setSelection(pos);

		// キャッシュ初期化
		amiItem_list_cache = new ArrayList<AMIItem>();
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
		String beforItem = prefs.getString(MyPrefs.AMI_FILTER,
				EC2Region.DEFAULT_REGION);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyPrefs.AMI_FILTER, item.first); // us-east-1等のIDを保存
		editor.commit();
		if (Debug.isDebuggerConnected()) {
			Log.d("onItemSelected", item.first);
		}

		// フィルターを変更したら更新
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

	/**
	 * オプションメニューの生成
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// XMLで定義したmenuを指定する。
		inflater.inflate(R.menu.ami_list, menu);
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
		}
		return true;
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * List要素クリック時の処理
	 * 
	 * 選択されたエンティティを詰めて参照画面へ遷移する
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		// 選択された要素を取得する
		// AMIItem amiItem = (AMIItem)parent.getItemAtPosition(position);
		AMIItem amiItem = amiItem_list_cache.get(position);
		// 参照画面へ遷移する明示的インテントを生成
		Intent showIntent = new Intent(this,
				jp.aws.test.ec2.EC2LaunchActivity.class);
		// 選択されたオブジェクトをインテントに詰める
		showIntent.putExtra(AMIItem.TABLE_NAME, amiItem);
		// アクティビティを開始する
		startActivity(showIntent);
	}

	/**
	 * アクティビティが前面に来るたびにデータを更新
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// データ取得タスクの実行
		DataLoadTask task = new DataLoadTask();
		task.execute();
	}

	/**
	 * 一覧データの取得と表示を行うタスク
	 */
	public class DataLoadTask extends AsyncTask<Object, Integer, List<AMIItem>> {
		// 処理中ダイアログ
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// バックグラウンドの処理前にUIスレッドでダイアログ表示
			progressDialog = new ProgressDialog(AMIListActivity.this);
			progressDialog.setMessage(getResources().getText(
					R.string.data_loading));
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected List<AMIItem> doInBackground(Object... params) {
			// フィルター値を取得
			Spinner spinner = (Spinner) findViewById(R.id.filter_spinner);
			Pair<String, String> item = (Pair<String, String>) spinner
					.getSelectedItem();

			try {
				// 一覧データの取得をバックグラウンドで実行
				// AMIItemDao dao = new AMIItemDao(EC2ListActivity.this);
				EC2Instance dao = new EC2Instance(AwsTestActivity.clientManager);
				List<AMIItem> list = null;

				HashMap<String, String> filter = string2map(item.first);
				if (amiItem_list_cache.isEmpty()) {
					list = dao.ami_list(filter); // 時間のかかる処理
					amiItem_list_cache = list;
				}
			} catch (Exception e) {
				setStackAndPost(e); // エラーダイアログを出す
			}

			return amiItem_list_cache;
		}

		// / StringからHashMapへ変換
		protected HashMap<String, String> string2map(String str) {
			HashMap<String, String> map = new HashMap<String, String>();

			String[] csv = str.split(",");
			for (int i = 0; i < csv.length; i++) {
				String[] tmp = csv[i].split(":");
				map.put(tmp[0], tmp[1]);
			}

			return map;
		}

		@Override
		protected void onPostExecute(List<AMIItem> result) {
			// 処理中ダイアログをクローズ
			progressDialog.dismiss();

			// 表示データのクリア
			arrayAdapter.clear();

			// 表示データの設定
			for (AMIItem amiItem : result) {
				arrayAdapter.add(MultiLineListRowImpl.create().addAMIItem(
						amiItem));
				// arrayAdapter.add(amiItem);
			}
		}
	}

	/**
	 * アイテムキャッシュのクリア
	 */
	protected void clearItemCache() {
		amiItem_list_cache.clear();
	}
}
