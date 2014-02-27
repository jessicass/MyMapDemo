package com.example.mymapdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.baidu.mapapi.map.MapView;

public class ZoomControlView extends RelativeLayout implements OnClickListener {
	private ImageButton mZoomInBtn;
	private ImageButton mZoomOutBtn;
	private int maxZoomLevel;
	private int minZoomLevel;
	private MapView mMapView;

	public ZoomControlView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 关联MapView
	 * 
	 * @param mapView
	 */
	public void setMapView(MapView mapView) {
		this.mMapView = mapView;
		// 获取最大的缩放级别
		maxZoomLevel = mapView.getMaxZoomLevel();
		// 获取最小的缩放级别
		minZoomLevel = mapView.getMinZoomLevel();
	}

	/**
	 * 根据MapView的缩放级别更新缩放按钮的状态，当达到最大缩放级别， 设置mZoomInBtn为不能点击，反之设置mZoomOutBtn
	 * 
	 * @param level
	 */
	public void refreshZoomBtnStatus(int level) {
		if (mMapView == null) {
			throw new NullPointerException(
					"you can call setMapView(MapView mapView) at first.");
		}

		if (level < maxZoomLevel && level > minZoomLevel) {
			if (!mZoomInBtn.isEnabled()) {
				mZoomInBtn.setEnabled(true);
			}
			if (!mZoomOutBtn.isEnabled()) {
				mZoomOutBtn.setEnabled(true);
			}
		} else if (level == maxZoomLevel) {
			mZoomInBtn.setEnabled(false);
		} else if (level == minZoomLevel) {
			mZoomOutBtn.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		if (mMapView == null) {
			throw new NullPointerException(
					"you can call setMapView(MapView mapView) at first");
		}
		switch (v.getId()) {
		case R.id.zoominBtn: {
			mMapView.getController().zoomIn();
			break;
		}
		case R.id.zoomoutBtn: {
			mMapView.getController().zoomOut();
			break;
		}
		}
	}

	private void init() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.zoom_controller_layout, null);
		mZoomInBtn = (ImageButton) view.findViewById(R.id.zoominBtn);
		mZoomOutBtn = (ImageButton) view.findViewById(R.id.zoomoutBtn);
		mZoomInBtn.setOnClickListener(this);
		mZoomOutBtn.setOnClickListener(this);
		addView(view);
	}

}
