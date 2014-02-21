package com.example.mymapdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.PopupOverlay;

/**
 * �̳�MapView��дonTouchEventʵ�ֵ����������
 */
public class MyMapView extends MapView {
	// PopupOverlay��������һ��pop����
	// ���캯��PopupOverlay(MapView mapView, PopupClickListener listener)
	private PopupOverlay pop = new PopupOverlay(this, null);

	public MyMapView(Context context) {
		super(context);
	}

	public MyMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!super.onTouchEvent(event)) {
			// ��������
			if (pop != null && event.getAction() == MotionEvent.ACTION_UP)
				pop.hidePop();
		}
		return true;
	}

	public PopupOverlay getPopupOverlay() {
		return pop;
	}
}
