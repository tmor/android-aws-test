/**
 * @file
 * @brief 参照アクティビティ
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 275 $
 */

package jp.aws.test.ec2;

import jp.aws.test.R;

import jp.aws.test.AlertActivity;
import jp.aws.test.ec2.EC2Item;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 参照アクティビティ
 */
public class EC2ShowActivity extends AlertActivity {

	// 現在表示中のEC2Itemオブジェクト
	private EC2Item ec2Item = null;

	// UI部品
	public TextView regionName = null;
	public TextView architecture = null;
	public TextView instanceId = null;
	public TextView instanceType = null;
	public TextView keyName = null;
	public TextView launchTime = null;
	public TextView privateDnsName = null;
	public TextView privateIpAddress = null;
	public TextView publicDnsName = null;
	public TextView publicIpAddress = null;
	public TextView rootDeviceType = null;
	public TextView rootDeviceName = null;
	public TextView securityGroups = null;
	public TextView stateName = null;
	public ImageView stateIcon = null;
	public TextView imageId = null;
	public TextView platform = null;
	public TextView tags = null;
	public TextView availabilityZone = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 自動生成されたR.javaの定数を指定してXMLからレイアウトを生成
		setContentView(R.layout.show);

		// Intentから対象のEC2Itemオブジェクトを取得
		ec2Item = (EC2Item) getIntent()
				.getSerializableExtra(EC2Item.TABLE_NAME);

		// UI部品の取得
		regionName = (TextView) findViewById(R.id.regionName);
		architecture = (TextView) findViewById(R.id.architecture);
		instanceId = (TextView) findViewById(R.id.instanceId);
		instanceType = (TextView) findViewById(R.id.instanceType);
		keyName = (TextView) findViewById(R.id.keyName);
		launchTime = (TextView) findViewById(R.id.launchTime);
		privateDnsName = (TextView) findViewById(R.id.privateDnsName);
		privateIpAddress = (TextView) findViewById(R.id.privateIpAddress);
		publicDnsName = (TextView) findViewById(R.id.publicDnsName);
		publicIpAddress = (TextView) findViewById(R.id.publicIpAddress);
		rootDeviceType = (TextView) findViewById(R.id.rootDeviceType);
		rootDeviceName = (TextView) findViewById(R.id.rootDeviceName);
		securityGroups = (TextView) findViewById(R.id.securityGroups);
		stateName = (TextView) findViewById(R.id.stateName);
		stateIcon = (ImageView) findViewById(R.id.stateIcon);
		imageId = (TextView) findViewById(R.id.imageId);
		platform = (TextView) findViewById(R.id.platform);
		tags = (TextView) findViewById(R.id.tags);
		availabilityZone = (TextView) findViewById(R.id.availabilityZone);

		// 表示内容の更新
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

		architecture.setText(ec2Item.architecture);
		instanceId.setText(ec2Item.instanceId);
		instanceType.setText(ec2Item.instanceType);
		keyName.setText(ec2Item.keyName);
		launchTime.setText(ec2Item.launchTime);
		privateDnsName.setText(ec2Item.privateDnsName);
		privateIpAddress.setText(ec2Item.privateIpAddress);
		// publicDnsName.setText(ec2Item.publicDnsName);
		publicIpAddress.setText(ec2Item.publicIpAddress);
		rootDeviceType.setText(ec2Item.rootDeviceType);
		rootDeviceName.setText(ec2Item.rootDeviceName);
		securityGroups.setText(ec2Item.securityGroups);
		stateName.setText(ec2Item.stateName);
		stateIcon.setImageResource(EC2State.getResource(ec2Item.stateName));
		imageId.setText(ec2Item.imageId);
		platform.setText(ec2Item.platform);
		tags.setText(ec2Item.tags);
		availabilityZone.setText(ec2Item.availabilityZone);

		// Public DNSをリンクしたいが、http://, https:// を含まないためautoLinkが使えないため、独自でリンク生成
		SpannableString publicDnsSpan = new SpannableString(
				ec2Item.publicDnsName);
		// リンク選択時の処理、リンク箇所を設定
		publicDnsSpan.setSpan(new PublicDNSSpan(), 0,
				ec2Item.publicDnsName.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		publicDnsName.setText(publicDnsSpan);
		publicDnsName.setMovementMethod(LinkMovementMethod.getInstance());
	}

	/**
	 * Public DNSがクリックされた際の処理
	 */
	class PublicDNSSpan extends ClickableSpan {
		@Override
		public void onClick(View widget) {
			String url = "http://" + ec2Item.publicDnsName + "/";
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}
	}

	/**
	 * オプションメニューの生成
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show, menu);
		return true;
	}

	/**
	 * オプションメニューの選択
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		// 前の画面へ戻る
		case R.id.Back:
			finish();
			break;
		case R.id.Clipboard:
			copyClipboard();
			break;
		case R.id.Clipboard_PublicDNS:
			ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			cm.setText(publicDnsName.getText());
			break;
		}
		return true;
	};

	/**
	 * 編集画面の結果に応じて画面をリフレッシュ
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// データベースから再読み込み
			// EC2ItemDao dao = new EC2ItemDao(this);
			// ec2Item = dao.load(ec2Item.getRowid());
			// 表示を更新
			updateView();
		}
	}

	/**
	 * クリップボードへコピー
	 */
	protected void copyClipboard() {
		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		StringBuilder builder = new StringBuilder();

		builder.append("RegionName: " + regionName.getText() + "\n");
		builder.append("Arch: " + architecture.getText() + "\n");
		builder.append("InstanceId: " + instanceId.getText() + "\n");
		builder.append("InstanceType: " + instanceType.getText() + "\n");
		builder.append("keyName: " + keyName.getText() + "\n");
		builder.append("LaunchTime: " + launchTime.getText() + "\n");
		builder.append("PrivateDnsName: " + privateDnsName.getText() + "\n");
		builder.append("PrivateIpAddress: " + privateIpAddress.getText() + "\n");
		builder.append("PublicDnsName: " + publicDnsName.getText() + "\n");
		builder.append("PublicIpAddress: " + publicIpAddress.getText() + "\n");
		builder.append("RootDeviceType: " + rootDeviceType.getText() + "\n");
		builder.append("RootDeviceName: " + rootDeviceName.getText() + "\n");
		builder.append("SecurityGroups: " + securityGroups.getText() + "\n");
		builder.append("StateName: " + stateName.getText() + "\n");
		builder.append("Platform: " + platform.getText() + "\n");
		builder.append("Tags: " + tags.getText() + "\n");
		builder.append("AvailabilityZone: " + availabilityZone.getText() + "\n");

		cm.setText(builder.toString());
	}
}
