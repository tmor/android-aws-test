/**
 * @file
 * @brief EC2アイテム操作
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test.ec2;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import jp.aws.test.ec2.DatabaseOpenHelper;

public class EC2ItemDao {

	private DatabaseOpenHelper helper = null;
	private Context context = null;

	public EC2ItemDao(Context context) {
		this.context = context;
		helper = new DatabaseOpenHelper(context);
	}

	/**
	 * EC2Itemの保存 rowidがnullの場合はinsert、rowidが!nullの場合はupdate
	 *
	 * @param ec2Item
	 *            保存対象のオブジェクト
	 * @return 保存結果
	 */
	public EC2Item save(EC2Item ec2Item) {
		SQLiteDatabase db = helper.getWritableDatabase();
		EC2Item result = null;
		try {
			ContentValues values = new ContentValues();
			values.put(EC2Item.COLUMN_INSTANCE_ID, ec2Item.instanceId);
			values.put(EC2Item.COLUMN_INSTANCE_TYPE, ec2Item.instanceType);
			values.put(EC2Item.COLUMN_STATE_NAME, ec2Item.stateName);

			Long rowId = ec2Item.getRowid();
			// IDがnullの場合はinsert
			if (rowId == null) {
				rowId = db.insert(EC2Item.TABLE_NAME, null, values);
			} else {
				db.update(EC2Item.TABLE_NAME, values, EC2Item.COLUMN_ID + "=?",
						new String[] { String.valueOf(rowId) });
			}
			result = load(rowId);
		} finally {
			db.close();
		}
		return result;
	}

	/**
	 * レコードの削除
	 *
	 * @param ec2Item
	 *            削除対象のオブジェクト
	 */
	public void delete(EC2Item ec2Item) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.delete(EC2Item.TABLE_NAME, EC2Item.COLUMN_ID + "=?",
					new String[] { String.valueOf(ec2Item.getRowid()) });
		} finally {
			db.close();
		}
	}

	/**
	 * idでEC2Itemをロードする
	 *
	 * @param rowId
	 *            PK
	 * @return ロード結果
	 */
	public EC2Item load(Long rowId) {
		SQLiteDatabase db = helper.getReadableDatabase();

		EC2Item ec2Item = null;
		try {
			Cursor cursor = db.query(EC2Item.TABLE_NAME, null,
					EC2Item.COLUMN_ID + "=?",
					new String[] { String.valueOf(rowId) }, null, null, null);
			cursor.moveToFirst();
			ec2Item = getEC2Item(cursor);
		} finally {
			db.close();
		}
		return ec2Item;
	}

	/**
	 * DBから一覧を取得する
	 *
	 * @return 検索結果
	 */
	public List<EC2Item> list() {
		SQLiteDatabase db = helper.getReadableDatabase();

		List<EC2Item> ec2ItemList;
		try {
			Cursor cursor = db.query(EC2Item.TABLE_NAME, null, null, null,
					null, null, EC2Item.COLUMN_ID);
			ec2ItemList = new ArrayList<EC2Item>();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ec2ItemList.add(getEC2Item(cursor));
				cursor.moveToNext();
			}
		} finally {
			db.close();
		}
		return ec2ItemList;
	}

	/**
	 * カーソルからオブジェクトへの変換
	 *
	 * @param cursor
	 *            カーソル
	 * @return 変換結果
	 */
	private EC2Item getEC2Item(Cursor cursor) {
		EC2Item ec2Item = new EC2Item();

		ec2Item.setRowid(cursor.getLong(0));
		ec2Item.instanceId = cursor.getString(1);
		ec2Item.instanceType = cursor.getString(2);
		return ec2Item;
	}
}
