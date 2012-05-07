package jp.aws.test;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class KeyValueArrayAdapter<T> extends ArrayAdapter<T> {

	/** Spinnerに登録したオブジェクトの表示を変換するConverter */
	private Converter<T> converter;

	/**
	 * ArrayAdapter(Context, int)にConverterを追加したコンストラクタ。
	 *
	 * 必要であれば他のコンストラクタも追加してください。
	 */
	public KeyValueArrayAdapter(Context context, int textViewResourceId,
			Converter<T> converter) {
		super(context, textViewResourceId);
		this.converter = converter;
	}

	/**
	 * getViewのOverride。 通常の方法ではObjectのtoStringしたものがTextViewに格納されているが、
	 * その結果を横取りして設定しなおしている。
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("KeyValueArrayAdapter", "getView start");
		TextView view = (TextView) super.getView(position, convertView, parent);

		view.setText(converter.toDisplayString(getItem(position)));
		return view;
	}

	/**
	 * こちらはSpinnerをクリックしたときに表示されるダイアログみたいなやつ用。
	 * こっちも上書きしてやらないと、toString()の結果が表示されちゃうよ。
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getDropDownView(position, convertView,
				parent);
		view.setText(converter.toDisplayString(getItem(position)));
		return view;
	}
}
