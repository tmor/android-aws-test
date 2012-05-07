/**
 * @file
 * @brief 複数行表示可能なListのRow部分
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test;

public interface MultiLineListRow {

	/**
	 * 先頭に付与するイメージのIDを取得します。
	 *
	 * もし先頭にイメージを表示しない場合はnullが返却されます。
	 *
	 * @return 先頭に付与するイメージのID
	 */
	Integer getPrefixImageId();

	/**
	 * 末尾に付与するイメージのIDを取得します。
	 *
	 * もし末尾にイメージを表示しない場合はnullが返却されます。
	 *
	 * @return 末尾に付与するイメージのID
	 */
	Integer getSuffixImageId();

	/**
	 * 表示するテキストの行数を取得します。
	 *
	 * @return テキストの行数
	 */
	int sieze();

	/**
	 * 表示するテキストを取得します。
	 *
	 * @param position
	 *            表示する位置
	 * @return 表示するテキスト
	 */
	String getText(int position);

	/**
	 * 表示するテキストのサイズを取得します。
	 *
	 * @param position
	 *            表示する位置
	 * @return 表示するテキストのサイズ
	 */
	float getTextSize(int position);
}
