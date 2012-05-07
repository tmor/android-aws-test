/**
 * @file
 * @brief 複行行表示可能なListのAdapter
 *
 * @note
 * - templateはcom_multi_line_row.xmlにあるのでそちらも参照
 * @author tmor
 * @licence Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 *
 * $Revision:$
 */

package jp.aws.test;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MultiLineListRowAdapter extends ArrayAdapter<MultiLineListRow> {

	private static final String TAG = "MultiLineListRowAdapter";

	/** displayed row */
	private List<MultiLineListRow> items;

	/** viewをクリックしたときのlistener */
	private OnClickListener listener;

	private LayoutInflater inflater;

	private int resourceId;

	public MultiLineListRowAdapter(Context context, int resourceId,
			List<MultiLineListRow> items) {
		this(context, resourceId, items, null);
	}

	public MultiLineListRowAdapter(Context context, int resourceId,
			List<MultiLineListRow> items, OnClickListener listener) {
		super(context, resourceId, items);
		this.resourceId = resourceId;
		this.items = items;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.listener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		// 初回はnullがわたってくる。
		// 2回目以降は以前作成したものがわたってくるらしい。
		if (view == null) {
			// view = inflater.inflate(R.layout.com_multiline_row, null);
			view = inflater.inflate(resourceId, null);
		}
		view = populateView(position, view, parent);
		return view;
	}

	protected View populateView(int position, View convertView, ViewGroup parent) {

		Log.d(TAG, "populateView position [" + position + "]");
		MultiLineListRow item = items.get(position);
		if (item.getPrefixImageId() != null) {
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.row_prefix_image);
			imageView.setImageResource(item.getPrefixImageId());
		}

		if (item.getSuffixImageId() != null) {
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.row_suffix_image);
			imageView.setImageResource(item.getSuffixImageId());
		}

		LinearLayout layout = (LinearLayout) convertView
				.findViewById(R.id.row_list_area);
		// 全て消しているが・・・
		// パフォーマンスを考えると他の方法を探したほうがいいかも。
		layout.removeAllViews();
		Log.d(TAG, "PopulateTextView size [" + item.sieze() + "]");
		for (int i = 0, n = item.sieze(); i < n; i++) {
			TextView textView = new TextView(parent.getContext());
			textView.setText(item.getText(i));
			if (item.getTextSize(i) > 1) {
				textView.setTextSize(item.getTextSize(i));
			}
			Log.d(TAG, "Add TextView text [" + item.getText(i) + "]");
			layout.addView(textView, i);
		}

		if (listener != null) {
			convertView.setOnClickListener(listener);
		}
		return convertView;
	}
}
