/**
 * @file
 * @brief EC2アイテム
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test.ec2;

import java.io.Serializable;

/**
 * 1レコードのデータを保持するオブジェクト Intentに詰めてやり取りするのでSerializableをimplementsする
 */
@SuppressWarnings("serial")
public class EC2Item implements Serializable {
	// テーブル名
	public static final String TABLE_NAME = "ec2_item";

	// カラム名
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_INSTANCE_ID = "instance_id";
	public static final String COLUMN_INSTANCE_TYPE = "instance_type";
	public static final String COLUMN_STATE_NAME = "state_name";

	// プロパティ
	public Long rowid = null;
	public String architecture = null; // i386, x86_64
	public String instanceId = null; // i-xxxxxxxx
	public String instanceType = null; // t1.micro
	public String keyName = null; //
	public String launchTime = null; // 2012-01-01 01:02:03 +9:00
	public String privateDnsName = null; // domU-xxx.compute-1.internal
	public String privateIpAddress = null; // 10.xxx.xxx.xxx
	public String publicDnsName = null; // ec2-xxx.compute-1.amazonaws.com
	public String publicIpAddress = null; // 201.xxx.xxx.xxx
	public String rootDeviceType = null; // ebs / s3
	public String rootDeviceName = null; // /dev/sda1
	public String securityGroups = null; // default
	/**
	 * stateName 0: pending :yellow 16: running :green 32: shutting-down :yellow
	 * 48: terminated :light red 64: stopping :yellow 80: stopped :light red
	 * rebooting : blue
	 *
	 * pending: the instance is in the process of being launched running: the
	 * instance launched (though booting may not be completed) shutting-down:
	 * the instance started shutting down terminated: the instance terminated
	 * stopping: the instance is in the process of stopping stopped: the
	 * instance has been stopped
	 */
	public String stateName = null;
	public String imageId = null; // ami-xxxxxxxx
	public String platform = null; // Windows
	public String tags = null; // tag
	public String availabilityZone = null; // us-east-1

	public Long getRowid() {
		return rowid;
	}

	public void setRowid(Long rowid) {
		this.rowid = rowid;
	}

	/**
	 * ListView表示の際に利用する
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.instanceId);
		builder.append(":");
		builder.append(this.instanceType);
		builder.append(":");
		builder.append(this.stateName);

		return builder.toString();
	}
}
