/**
 * @file
 * @brief EC2 起動アクティビティ
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 275 $
 */

package jp.aws.test.ec2;

import jp.aws.test.AwsTestActivity;
import jp.aws.test.R;
import jp.aws.test.TtsImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.ec2.model.Instance;

import jp.aws.test.ec2.AMIItem;
import jp.aws.test.ec2.EC2Region;
import jp.aws.test.ec2.EC2Instance;
import jp.aws.test.AlertActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * EC2起動アクティビティ
 */
public class EC2LaunchActivity extends AlertActivity implements OnClickListener {

	// 現在表示中のEC2Itemオブジェクト
	private AMIItem amiItem = null;

	// UI部品
	public TextView regionName = null;
	public TextView imageId = null;
	public TextView rootDeviceType = null;
	public TextView imageLocation = null;
	public TextView architecture = null;
	public TextView platform = null;
	public Button instanceType = null;
	public Button keyPair = null;
	public Button availabilityZone = null;
	public Button securityGroup = null;
	public EditText additionalInfo = null;
	public EditText userData = null;
	public Button launchButton = null;
	public Button cancelButton = null;

	private String[] instanceType_cache = null; // キャッシュ
	private int instanceType_selected = 0;
	private List<String> keyPair_cache = null; // キャッシュ
	private int keyPair_selected = 0;
	private List<String> availabilityZone_cache = null; // キャッシュ
	private int availabilityZone_selected = 0;
	private List<String> securityGroup_cache = null; // キャッシュ
	private boolean[] securityGroup_checked = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 自動生成されたR.javaの定数を指定してXMLからレイアウトを生成
		setContentView(R.layout.launch);

		// Intentから対象のEC2Itemオブジェクトを取得
		amiItem = (AMIItem) getIntent()
				.getSerializableExtra(AMIItem.TABLE_NAME);

		// UI部品の取得
		regionName = (TextView) findViewById(R.id.regionName);
		imageId = (TextView) findViewById(R.id.imageId);
		rootDeviceType = (TextView) findViewById(R.id.rootDeviceType);
		imageLocation = (TextView) findViewById(R.id.imageLocation);
		architecture = (TextView) findViewById(R.id.architecture);
		platform = (TextView) findViewById(R.id.platform);
		instanceType = (Button) findViewById(R.id.instanceType);
		instanceType.setOnClickListener(this);
		keyPair = (Button) findViewById(R.id.keyPair);
		keyPair.setOnClickListener(this);
		availabilityZone = (Button) findViewById(R.id.availabilityZone);
		availabilityZone.setOnClickListener(this);
		securityGroup = (Button) findViewById(R.id.securityGroup);
		securityGroup.setOnClickListener(this);
		additionalInfo = (EditText) findViewById(R.id.additionalInfo);
		userData = (EditText) findViewById(R.id.userData);
		launchButton = (Button) findViewById(R.id.launchButton);
		launchButton.setOnClickListener(this);
		cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(this);

		// キャッシュ初期化
		instanceType_cache = EC2InstanceType.getInstanceTypes(
				amiItem.rootDeviceType, amiItem.architecture);
		keyPair_cache = new ArrayList<String>();
		availabilityZone_cache = new ArrayList<String>();
		securityGroup_cache = new ArrayList<String>();

		cancelButton.setFocusable(true);
		cancelButton.setFocusableInTouchMode(true);
		cancelButton.requestFocus();

		// 画面更新
		updateView();
	}

	/**
	 * 画面の表示内容を更新する
	 */
	private void updateView() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		EC2Region ec2Region = new EC2Region();
		String region = prefs.getString("region", ec2Region.DEFAULT_REGION);
		regionName.setText(EC2Region.getName(region));

		imageId.setText(amiItem.imageId);
		rootDeviceType.setText(amiItem.rootDeviceType);
		imageLocation.setText(amiItem.imageLocation);
		architecture.setText(amiItem.architecture);
		platform.setText(amiItem.platform);

		if (instanceType_cache.length != 0) {
			instanceType.setText(instanceType_cache[instanceType_selected]);
		}

		if (!keyPair_cache.isEmpty()) {
			keyPair.setText(keyPair_cache.get(keyPair_selected));
		}

		if (!availabilityZone_cache.isEmpty()) {
			availabilityZone.setText(availabilityZone_cache
					.get(availabilityZone_selected));
		}

		if (!securityGroup_cache.isEmpty()) {
			securityGroup.setText(getItemCheckedText(securityGroup_cache,
					securityGroup_checked));
		}
	}

	/**
	 * チェックボックスから文字列へ変換
	 *
	 * @param items
	 * @param flags
	 * @return
	 */
	private String getItemCheckedText(List<String> items, boolean[] flags) {
		if (items.isEmpty() || flags.length == 0)
			return "";

		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String item : items) {
			if (flags[i]) {
				builder.append(item);
				builder.append(", ");
			}
			i++;
		}
		return builder.toString();
	}

	/**
	 * チェックが付いている項目を取得
	 *
	 * @param items
	 * @param flags
	 * @return
	 */
	private List<String> getItemCheckedList(List<String> items, boolean[] flags) {
		List<String> itemList = new ArrayList<String>();
		if (items.isEmpty() || flags.length == 0)
			return itemList;

		int i = 0;
		for (String item : items) {
			if (flags[i] == true) {
				itemList.add(item);
			}
			i++;
		}
		return itemList;
	}

	/**
	 * オプションメニューの生成
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		// XMLで定義したmenuを指定する。
		inflater.inflate(R.menu.launch, menu);
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
		// 前の画面へ戻る
		case R.id.Back:
			finish();
			break;
		}
		return true;
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ボタンクリック
	 */
	public void onClick(View v) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info);

		if (v == cancelButton) {
			// キャンセル
			setResult(RESULT_CANCELED);
			finish(); // 終了
		} else if (v == launchButton) {
			// 起動
			// 必須チェック
			if (instanceType.getText().toString().length() == 0
					|| keyPair.getText().toString().length() == 0
					|| securityGroup.getText().toString().length() == 0) {
				alertDialogBuilder
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(getString(R.string.confirm))
						.setMessage(
								getString(R.string.ec2_launch_required_message))
						.setPositiveButton(getString(android.R.string.ok),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).show();
			} else {
				launchEC2Instance();
			}
		} else if (v == instanceType) {
			// インスタンスタイプの選択
			if (instanceType_cache.length <= 0) {
				Toast.makeText(this, getString(R.string.no_value_message),
						Toast.LENGTH_LONG).show();
				return;
			}
			alertDialogBuilder
					.setTitle("Please select Instance Type")
					.setSingleChoiceItems(instanceType_cache,
							instanceType_selected,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									instanceType_selected = which;
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									instanceType
											.setText(instanceType_cache[instanceType_selected]);
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									instanceType
											.setText(instanceType_cache[instanceType_selected]);
								}
							}).show();
		} else if (v == keyPair) {
			// キーペアの選択
			if (keyPair_cache.isEmpty()) {
				Toast.makeText(this, getString(R.string.no_value_message),
						Toast.LENGTH_LONG).show();
				return;
			}
			alertDialogBuilder
					.setTitle("Please select Key Pair")
					.setSingleChoiceItems(keyPair_cache.toArray(new String[0]),
							keyPair_selected,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									keyPair_selected = which;
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									keyPair.setText(keyPair_cache
											.get(keyPair_selected));
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									keyPair.setText(keyPair_cache
											.get(keyPair_selected));
								}
							}).show();
		} else if (v == availabilityZone) {
			// AZの選択
			if (availabilityZone_cache.isEmpty()) {
				Toast.makeText(this, getString(R.string.no_value_message),
						Toast.LENGTH_LONG).show();
				return;
			}
			alertDialogBuilder
					.setTitle("Please select Availability Zone")
					.setSingleChoiceItems(
							availabilityZone_cache.toArray(new String[0]),
							availabilityZone_selected,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									availabilityZone_selected = which;
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									availabilityZone.setText(availabilityZone_cache
											.get(availabilityZone_selected));
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									availabilityZone.setText(availabilityZone_cache
											.get(availabilityZone_selected));
								}
							}).show();
		} else if (v == securityGroup) {
			// セキュリティグループの選択
			if (securityGroup_cache.isEmpty()) {
				Toast.makeText(this, getString(R.string.no_value_message),
						Toast.LENGTH_LONG).show();
				return;
			}

			alertDialogBuilder
					.setTitle("Please select Security Group")
					.setMultiChoiceItems(
							securityGroup_cache.toArray(new String[0]),
							securityGroup_checked,
							new DialogInterface.OnMultiChoiceClickListener() {
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									securityGroup_checked[which] = isChecked;
								}
							})
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* OKボタンをクリックした時の処理 */
									securityGroup.setText(getItemCheckedText(
											securityGroup_cache,
											securityGroup_checked));
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* Cancel ボタンをクリックした時の処理 */
									securityGroup.setText(getItemCheckedText(
											securityGroup_cache,
											securityGroup_checked));
								}
							}).show();
		}
	};

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
	 * データの取得と表示を行うタスク
	 */
	public class DataLoadTask extends AsyncTask<Object, Integer, List<String>> {
		// 処理中ダイアログ
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// バックグラウンドの処理前にUIスレッドでダイアログ表示
			progressDialog = new ProgressDialog(EC2LaunchActivity.this);
			progressDialog.setMessage(getResources().getText(
					R.string.data_loading));
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected List<String> doInBackground(Object... params) {
			// バックグラウンドで実行
			List<String> itemList = new ArrayList<String>();

			try {
				EC2Instance dao = new EC2Instance(AwsTestActivity.clientManager);
				if (keyPair_cache.isEmpty()) {
					keyPair_cache = dao.getKeyPairs();
				}
				if (availabilityZone_cache.isEmpty()) {
					availabilityZone_cache = dao.getAvailabilityZones();
				}
				if (securityGroup_cache.isEmpty()) {
					securityGroup_cache = dao.getSecurityGroups();
					if (!securityGroup_cache.isEmpty()) {
						securityGroup_checked = new boolean[securityGroup_cache
								.size()];
						Arrays.fill(securityGroup_checked, false); // 初期化
						// defaultにチェックを付ける
						int idx = securityGroup_cache.indexOf("default");
						if (idx >= 0)
							securityGroup_checked[idx] = true;
					}
				}
			} catch (Exception e) {
				setStackAndPost(e); // エラーダイアログを出す
			}

			return itemList;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			// 処理中ダイアログをクローズ
			progressDialog.dismiss();

			// 表示データのクリア

			// 表示データの設定
			updateView();
		}
	}

	/**
	 * アイテムキャッシュのクリア
	 */
	protected void clearItemCache() {
		keyPair_cache.clear();
		availabilityZone_cache.clear();
		securityGroup_cache.clear();

		instanceType_selected = 0;
		keyPair_selected = 0;
		availabilityZone_selected = 0;
		securityGroup_checked = null;
	}

	/**
	 * EC2インスタンスの起動
	 */
	protected void launchEC2Instance() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(getString(R.string.ec2_launch_title))
				.setMessage(
						getString(R.string.ec2_launch_message)
								+ amiItem.imageId)
				.setPositiveButton(getString(R.string.launch),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								AwsTestActivity.tts
										.startTTS(getString(R.string.tts_ec2_launch));
								LaunchTask task = new LaunchTask();
								task.execute();
							}
						})
				.setNegativeButton(getString(android.R.string.no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* ここにNOの処理 */
							}
						}).show();
	}

	/**
	 * EC2起動を行うタスク
	 */
	public class LaunchTask extends AsyncTask<Object, Integer, List<Instance>> {
		// 処理中ダイアログ
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// バックグラウンドの処理前にUIスレッドでダイアログ表示
			progressDialog = new ProgressDialog(EC2LaunchActivity.this);
			progressDialog.setMessage(getResources().getText(
					R.string.data_loading));
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected List<Instance> doInBackground(Object... params) {
			// バックグラウンドで実行
			List<Instance> itemList = new ArrayList<Instance>();

			try {
				EC2Instance dao = new EC2Instance(AwsTestActivity.clientManager);
				itemList = dao.launchEC2Instances(
						amiItem.imageId,
						1,
						1,
						instanceType_cache[instanceType_selected],
						keyPair_cache.get(keyPair_selected),
						availabilityZone_cache.get(availabilityZone_selected),
						getItemCheckedList(securityGroup_cache,
								securityGroup_checked), additionalInfo
								.getText().toString(), userData.getText()
								.toString());
			} catch (Exception e) {
				setStackAndPost(e); // エラーダイアログを出す
			}

			return itemList;
		}

		@Override
		protected void onPostExecute(List<Instance> result) {
			// 処理中ダイアログをクローズ
			progressDialog.dismiss();

			if (result.isEmpty()) {
				// エラー
			} else {
				// 正常
				// EC2ListActivity画面へ
				Intent intent = new Intent(EC2LaunchActivity.this,
						jp.aws.test.ec2.EC2ListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

				// 終了
				setResult(RESULT_OK);
				finish();
			}
		}
	}
}
