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
	 * ����MapView
	 * 
	 * @param mapView
	 */
	public void setMapView(MapView mapView) {
		this.mMapView = mapView;
		// ��ȡ�������ż���
		maxZoomLevel = mapView.getMaxZoomLevel();
		// ��ȡ��С�����ż���
		minZoomLevel = mapView.getMinZoomLevel();
	}

	/**
	 * ����MapView�����ż���������Ű�ť��״̬�����ﵽ������ż��� ����mZoomInBtnΪ���ܵ������֮����mZoomOutBtn
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
