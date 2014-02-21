package com.example.mymapdemo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.OverlayItem;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private Context context;
	private MyMapView mapView;

	public MyItemizedOverlay(Drawable drawable, MyMapView mapView,
			Context context) {
		super(drawable, mapView);
		this.mapView = mapView;
		this.context = context;
	}

	@Override
	protected boolean onTap(int index) {
		final View view = LayoutInflater.from(context).inflate(
				R.layout.map_popwindow_view, null);
		TextView text_title = (TextView) view.findViewById(R.id.marker_title);
		TextView text_text = (TextView) view.findViewById(R.id.marker_text);

		text_title.setText(getItem(index).getTitle());
		text_text.setText("这是第" + index + "个");

		mapView.getPopupOverlay().showPopup(BDMapUtils.getBitmapFromView(view),
				getItem(index).getPoint(), 20);
		super.onTap(index);
		return false;
	}

}
