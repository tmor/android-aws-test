/**
 * @file
 * @brief EC2Item SQLiteオープンヘルパ
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test.ec2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Debug;
import android.util.Log;
import jp.aws.test.ec2.EC2Item;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	// データベース名の定数
	private static final String DB_NAME = "ec2";

	/**
	 * 初期投入サンプルデータ
	 */
	private String[][] datas = new String[][] {
			{ "0x10000000", "t1.micro", "pending" },
			{ "0x20000000", "m1.small", "running" },
			{ "0x30000000", "m1.large", "terminated" } };

	/**
	 * コンストラクタ
	 */
	public DatabaseOpenHelper(Context context) {
		// 指定したデータベース名が存在しない場合は、新たに作成されonCreate()が呼ばれる
		// バージョンを変更するとonUpgrade()が呼ばれる
		super(context, DB_NAME, null, 1);
	}

	/**
	 * データベースの生成に呼び出されるので、 スキーマの生成を行う
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();

		try {
			// テーブルの生成
			StringBuilder createSql = new StringBuilder();
			createSql.append("create table " + EC2Item.TABLE_NAME + " (");
			createSql.append(EC2Item.COLUMN_ID
					+ " integer primary key autoincrement not null,");
			createSql.append(EC2Item.COLUMN_INSTANCE_ID + " text not null,");
			createSql.append(EC2Item.COLUMN_INSTANCE_TYPE + " text not null,");
			createSql.append(EC2Item.COLUMN_STATE_NAME + " text");
			createSql.append(")");

			if (Debug.isDebuggerConnected()) {
				Log.d("DatabaseOpenHelper::onCreate", createSql.toString());
			}

			db.execSQL(createSql.toString());

			// サンプルデータの投入
			for (String[] data : datas) {
				ContentValues values = new ContentValues();
				values.put(EC2Item.COLUMN_INSTANCE_ID, data[0]);
				values.put(EC2Item.COLUMN_INSTANCE_TYPE, data[1]);
				values.put(EC2Item.COLUMN_STATE_NAME, data[2]);
				db.insert(EC2Item.TABLE_NAME, null, values);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * データベースの更新
	 *
	 * 親クラスのコンストラクタに渡すversionを変更したときに呼び出される
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// データベースの更新
	}
}
