/**
 * @file
 * @brief AMIアイテム
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision: 266 $
 */

package jp.aws.test.ec2;

import java.io.Serializable;

/**
 * 1レコードのデータを保持するオブジェクト Intentに詰めてやり取りするのでSerializableをimplementsする
 */
@SuppressWarnings("serial")
public class AMIItem implements Serializable {
	// テーブル名
	public static final String TABLE_NAME = "ami_item";

	// カラム名
	public static final String COLUMN_ID = "_id";

	// プロパティ
	public Long rowid = null;
	public String imageId = null; // ami-00000000
	public String imageType = null; // kernel, machine
	public String imageLocation = null; // amazon/amzn-ami-2011.09.2.i386-ebs
	public String name = null; // amzn-ami-2011.09.2.i386-ebs
	public String architecture = null; // i386, x86_64
	public String platform = null; // Windows
	public String tags = null; //
	public String state = null; // available
	public String ownerId = null; // 00000000
	public String rootDeviceType = null; // ebs / instance-store
	public String rootDeviceName = null; // /dev/sda1
	public String description = null; // 説明

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
		builder.append(this.imageId);

		return builder.toString();
	}
}
