package com.example.mymapdemo;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.google.common.collect.Lists;

public class MapActivity extends Activity {

	// ʹ�ü̳�MapView��MyMapViewĿ������дtouch�¼�ʵ�����ݴ���
	// ���������touch�¼���������̳У�ֱ��ʹ��MapView����
	private MyMapView mMapView = null; // ��ʾ��ͼ��View
	private ZoomControlView mZoomController; // ��ͼ���ſؼ�������
	private LocationProvider mLocProvider; // ��λ֧����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * ʹ�õ�ͼsdkǰ��Ҫ�ȳ�ʼ��BMapManager��������setContentView����֮ǰ��
		 * BMapManager��ȫ�ֵģ���Ϊ���MapView���ã�����Ҫ�ڵ�ͼģ�鴴��֮ǰ������
		 * ���ڵ�ͼ��ͼģ�����ٺ����٣�ֻҪ���е�ͼģ����ʹ�ã�BMapManager�Ͳ�Ӧ������
		 */
		UserApplication app = (UserApplication) this.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(getApplicationContext());
			// ���BMapManagerû�г�ʼ�����ʼ��BMapManager
			app.mBMapManager.init(UserApplication.BAIDU_MAP_KEY,
					new UserApplication.MyGeneralListener());
		}

		setContentView(R.layout.activity_main);

		mMapView = (MyMapView) findViewById(R.id.bmapView);
		// �����Դ��ĵ�ͼ���ſؼ�
		mMapView.setBuiltInZoomControls(false);
		// �õ�mMapView�Ŀ���Ȩ,�����������ƺ�����ƽ�ƺ�����
		MapController mMapController = mMapView.getController();
		// mMapController.setCenter(centerPoint); // ���õ�ͼ���ĵ�
		mMapController.setZoom(18); // ���õ�ͼzoom�������ֵ��ȡֵ��Χ��[3,18]��

		initZoomController(); // ��ʼ����ͼ���ſؼ�

		setMapMarker(); // ��ע������

		// ��ʼ����λ֧����
		mLocProvider = new LocationProvider(getApplicationContext(), mMapView);
		mLocProvider.startLocation(); // ��ʼ��λ
	}

	/** �����λ��ť�ֶ�����λ */
	public void requestLocation(View view) {
		mLocProvider.requestLocation();
	}

	@Override
	protected void onResume() {
		// MapView������������Activityͬ������activity�ָ�ʱ�����MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.destroy()
		mMapView.destroy();

		// �˳�ʱ���ٶ�λ
		mLocProvider.stopLocation();

		// ������APP�����˳�֮ǰ����MapApi��destroy()��������Ҫ��ÿ��activity��OnDestroy�е��ã�
		// ����MapApi�ظ�������ʼ�������Ч��
		UserApplication app = (UserApplication) this.getApplication();
		if (app.mBMapManager != null) {
			app.mBMapManager.destroy();
			app.mBMapManager = null;
		}
		super.onDestroy();
	}

	/** ����������� */
	private void setMapMarker() {
		// �ø����ľ�γ�ȹ���һ��GeoPoint����λ��΢�� (�� * 1E6) ��һ���ǲ�����ʾγ�ȣ��ڶ����Ǿ���
		GeoPoint point1 = new GeoPoint((int) (30.5136 * 1E6),
				(int) (114.4018 * 1E6));
		GeoPoint point2 = new GeoPoint((int) (30.5138 * 1E6),
				(int) (114.4032 * 1E6));
		GeoPoint point3 = new GeoPoint((int) (30.5125 * 1E6),
				(int) (114.4004 * 1E6));

		// ��ȡ�����ڵ�ͼ�ϱ�עһ������������ͼ��
		Drawable drawable = getResources().getDrawable(R.drawable.map_balloon);

		// ��OverlayItem׼��Overlay����
		OverlayItem overlayItem1 = new OverlayItem(point1, "point1", "point1");
		OverlayItem overlayItem2 = new OverlayItem(point2, "point2", "point2");
		OverlayItem overlayItem3 = new OverlayItem(point3, "point3", "point3");
		// ʹ��setMarker()��������overlayͼƬ,�����������ʹ�ù���ItemizedOverlayʱ��Ĭ������
		// overlayItem.setMarker(drawable);

		List<OverlayItem> overlayItemsList = Lists.newArrayList(overlayItem1,
				overlayItem2, overlayItem3);

		// ����MyItemizedOverlay
		MyItemizedOverlay myItemizedOverlay = new MyItemizedOverlay(drawable,
				mMapView, this);
		// ��IteminizedOverlay��ӵ�MapView��
		mMapView.getOverlays().clear();
		mMapView.getOverlays().add(myItemizedOverlay);
		// ���OverlayItem
		myItemizedOverlay.addItem(overlayItemsList);

		mMapView.refresh();
	}

	/** ��ʼ����ͼ���ſؼ� */
	private void initZoomController() {
		mZoomController = (ZoomControlView) findViewById(R.id.zoom_controller);
		mZoomController.setMapView(mMapView);
		// ע���ͼ��ʾ�¼�������
		mMapView.regMapViewListener(UserApplication.getInstance().mBMapManager,
				new MyMKMapViewListener());
		mZoomController.refreshZoomBtnStatus(18);
	}

	/** ��ͼ��ʾ�¼���������������ͼ��ʾ�¼����û���Ҫʵ�ָýӿ��Դ�����Ӧ�¼� */
	class MyMKMapViewListener implements MKMapViewListener {
		private int lastLevel = -1;

		@Override
		public void onClickMapPoi(MapPoi mapPoi) {
		}

		@Override
		public void onGetCurrentMap(Bitmap bitmap) {
		}

		/** ��������ʱ��ص�����Ϣ���ڴ˷�������������Ű�ť��״̬ */
		@Override
		public void onMapAnimationFinish() {
			zoomChange();
		}

		@Override
		public void onMapLoadFinish() {
		}

		/** �ƶ�����ʱ��ص�����Ϣ���ڴ˷�������������Ű�ť��״̬ */
		@Override
		public void onMapMoveFinish() {
			zoomChange();
		}

		/** �������Ű�ť��״̬ */
		private void zoomChange() {
			int zoomLevel = Math.round(mMapView.getZoomLevel());
			if (lastLevel < 0 || lastLevel != zoomLevel) {
				mZoomController.refreshZoomBtnStatus(zoomLevel);
				lastLevel = zoomLevel;
			}
		}
	}
}
