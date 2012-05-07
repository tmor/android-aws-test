/**
 * @file
 * @brief AmazonClientManager
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 * @note
 * - 使用元でstatic変数として使った方が良い
 *
 * $Revision:$
 */
package jp.aws.test;

import jp.aws.test.ec2.EC2Region;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.ec2.AmazonEC2Client;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AmazonClientManager {
	private static final String LOG_TAG = "AmazonClientManager";

	private Context context = null;
	private AmazonEC2Client ec2Client = null;
	private AmazonS3Client s3Client = null;
	private int connectionTimeout = 15 * 1000; // 接続タイムアウト(ms)

	public AmazonClientManager(Context context) {
		this.context = context;
	}

	public AmazonEC2Client ec2() {
		validateCredentials();
		return ec2Client;
	}

	public AmazonS3Client s3() {
		validateCredentials();
		return s3Client;
	}

	/**
	 * 設定が有効か確認
	 *
	 * @return
	 */
	public boolean hasCredentials() {
		// 設定から読み込み
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.context);
		if (prefs.getString("prefs_account_access_key", "") == ""
				&& prefs.getString("prefs_account_secret_key", "") == "") {
			return false;
		}
		return true;
	}

	/**
	 * インスタンス生成
	 */
	public void validateCredentials() {
		if (s3Client == null || ec2Client == null) {
			Log.i(LOG_TAG, "Creating New Clients.");

			// 設定から読み込み
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this.context);
			String access_key = prefs.getString("prefs_account_access_key", "");
			String secret_key = prefs.getString("prefs_account_secret_key", "");

			// インスタンス生成
			ClientConfiguration clientconfiguration = new ClientConfiguration();
			clientconfiguration.setConnectionTimeout(this.connectionTimeout); // タイムアウト(ms)

			AWSCredentials credentials = new BasicAWSCredentials(access_key,
					secret_key);
			s3Client = new AmazonS3Client(credentials, clientconfiguration);
			ec2Client = new AmazonEC2Client(credentials, clientconfiguration);

			// リージョン変更
			this.changeRegion();
		}
	}

	/**
	 * インスタンスの削除
	 */
	public void clearClients() {
		s3Client = null;
		ec2Client = null;
	}

	/**
	 * リージョン変更
	 */
	public void changeRegion() {
		validateCredentials();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.context);
		String region = prefs.getString("region", EC2Region.DEFAULT_REGION);
		ec2Client.setEndpoint("ec2." + region + ".amazonaws.com"); // リージョン指定
	}
}
