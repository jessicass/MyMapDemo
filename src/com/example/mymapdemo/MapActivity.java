package com.example.mymapdemo;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.google.common.collect.Lists;

public class MapActivity extends Activity {

	public static final String BAIDU_MAP_KEY = "kFvtrKqSFkG9mTbtGBpuLbbK";

	private BMapManager mBMapMan = null; // ��ͼ���������
	// ʹ�ü̳�MapView��MyMapViewĿ������дtouch�¼�ʵ�����ݴ���
	// ���������touch�¼���������̳У�ֱ��ʹ��MapView����
	private MyMapView mMapView = null; // ��ʾ��ͼ��View
	private LocationData mLocData; // �û�λ����Ϣ
	private MyLocationOverlay myLocOverlay; // �û�λ����Ϣ��ʾͼ��

	public LocationClient mLocClient; // ��λSDK������
	private BDLocation myLocation; // �û���ǰλ��

	private ZoomControlView mZoomController; // ��ͼ���ſؼ�������

	private boolean isRequest = false; // �Ƿ��ֶ���������λ
	private boolean isFirstLoc = true; // �Ƿ��״ζ�λ
	private Toast mToast; // ȷ��ֻ��һ��Toast��ʾ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ʹ�õ�ͼsdkǰ��Ҫ�ȳ�ʼ��BMapManager��������setContentView����֮ǰ
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(BAIDU_MAP_KEY, new MyMKGeneralListener());

		setContentView(R.layout.activity_main);

		mMapView = (MyMapView) findViewById(R.id.bmapView);
		// �����Դ��ĵ�ͼ���ſؼ�
		mMapView.setBuiltInZoomControls(false);
		// ��ʼ����ͼ���ſؼ�
		initZoomController();

		// �õ�mMapView�Ŀ���Ȩ,�����������ƺ�����ƽ�ƺ�����
		MapController mMapController = mMapView.getController();
		// mMapController.setCenter(centerPoint); // ���õ�ͼ���ĵ�
		mMapController.setZoom(18); // ���õ�ͼzoom�������ֵ��ȡֵ��Χ��[3,18]��

		// ��ע������
		setMapMarker();

		// ���ö�λͼ��
		setLocationOverlay();
	}

	/** �����λ��ť�ֶ�����λ */
	public void requestLocation(View view) {
		isRequest = true;
		if (mLocClient != null && mLocClient.isStarted()) {
			showToast("���ڶ�λ");
			mLocClient.requestLocation();
		} else {
			Log.d("request location", "locClient is null or not started");
		}
	}

	@Override
	protected void onDestroy() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.destroy()
		mMapView.destroy();

		// �˳�Ӧ�õ���BMapManager��destroy()����
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}

		// �˳�ʱ���ٶ�λ
		if (mLocClient != null) {
			mLocClient.stop();
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}
		super.onResume();
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

	private void setLocationOverlay() {
		// ʵ������λ����LocationClient����������߳�������
		// getApplicationConext()��ȡȫ������Ч��context
		mLocClient = new LocationClient(getApplicationContext());
		// ע��̳���BDLocationListener�ļ�������MyLocationListener
		mLocClient.registerLocationListener(new MyLocationListener());
		mLocClient.setLocOption(initLocationOption()); // ���ö�λ����
		mLocClient.start(); // ���ô˷�����ʼ��λ

		/** ��λͼ���ʼ�� */
		// ʹ�ü̳�MyLocationOverlay��MyOwnLocationOverlayĿ������дdispatchTap�¼�ʵ�����ݴ���
		// ���������dispatchTap�¼���������̳У�ֱ��ʹ��MyLocationOverlay����
		myLocOverlay = new MyOwnLocationOverlay(mMapView);

		// ʵ������λ���ݣ����������ҵ�λ��ͼ��
		mLocData = new LocationData();
		myLocOverlay.setData(mLocData);

		mMapView.getOverlays().add(myLocOverlay); // ��Ӷ�λͼ��
		mMapView.refresh(); // �޸Ķ�λ���ݺ�ˢ��ͼ����Ч
	}

	/** LocationClientOption���������ö�λSDK�Ķ�λ��ʽ */
	private LocationClientOption initLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // ��GPRS
		option.setAddrType("all");// ���صĶ�λ���������ַ��Ϣ
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
		option.setPriority(LocationClientOption.GpsFirst); // ����GPS����
		option.setScanSpan(10000); // ���÷���λ����ļ��ʱ��Ϊ10000ms
		option.disableCache(false);// ��ֹ���û��涨λ
		// option.setPoiNumber(5); //��෵��POI����
		// option.setPoiDistance(1000); //poi��ѯ����
		// option.setPoiExtraInfo(true); //�Ƿ���ҪPOI�ĵ绰�͵�ַ����ϸ��Ϣ
		return option;
	}

	/** ��ʼ����ͼ���ſؼ� */
	private void initZoomController() {
		mZoomController = (ZoomControlView) findViewById(R.id.zoom_controller);
		mZoomController.setMapView(mMapView);
		// ע���ͼ��ʾ�¼�������
		mMapView.regMapViewListener(mBMapMan, new MyMKMapViewListener());
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

	/** �����¼���������������ͨ�������������Ȩ��֤����� */
	class MyMKGeneralListener implements MKGeneralListener {

		/** һЩ����״̬�Ĵ�����ص����� */
		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				showToast("�������");
			}
		}

		/** ��Ȩ�����ʱ����õĻص����� */
		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				showToast("����������ȷ����ȨKey��");
			}
		}

	}

	/** ������λ�Ľӿڣ���Ҫʵ���������� */
	class MyLocationListener implements BDLocationListener {

		/** �����첽���صĶ�λ�����������BDLocation���Ͳ��� */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			MapActivity.this.myLocation = location;
			mLocData.latitude = location.getLatitude();
			mLocData.longitude = location.getLongitude();
			// �������ʾ��λ����Ȧ����accuracy��ֵΪ0����
			// mLocData.accuracy = location.getRadius();
			mLocData.direction = location.getDerect();

			myLocOverlay.setData(mLocData); // ����λ�������õ���λͼ����
			mMapView.refresh(); // ����ͼ������ִ��ˢ�º���Ч

			if (isFirstLoc || isRequest) {
				// ��������λ�õ��Զ�����ʽ�ƶ�����ͼ����
				mMapView.getController().animateTo(
						new GeoPoint((int) (location.getLatitude() * 1e6),
								(int) (location.getLongitude() * 1e6)));
				showPopupOverlay(location);
				isRequest = false;
			}
			isFirstLoc = false;
		}

		/** �����첽���ص�POI��ѯ�����������BDLocation���Ͳ��� */
		@Override
		public void onReceivePoi(BDLocation poiLocation) {
		}

	}

	/** �̳�MyLocationOverlay��дdispatchTap���� */
	class MyOwnLocationOverlay extends MyLocationOverlay {

		public MyOwnLocationOverlay(MapView mapView) {
			super(mapView);
		}

		/** �ڡ��ҵ�λ�á������ϴ������¼� */
		@Override
		protected boolean dispatchTap() {
			// ����ҵ�λ����ʾPopupOverlay
			showPopupOverlay(myLocation);
			return super.dispatchTap();
		}
	}

	/** ��ʾ��������ͼ��PopupOverlay */
	private void showPopupOverlay(BDLocation location) {
		View view = LayoutInflater.from(this).inflate(
				R.layout.map_popwindow_view, null);
		TextView text_title = (TextView) view.findViewById(R.id.marker_title);
		TextView text_text = (TextView) view.findViewById(R.id.marker_text);
		text_title.setText("[�ҵ�λ��]");
		text_text.setText(location.getAddrStr());

		mMapView.getPopupOverlay().showPopup(
				BDMapUtils.getBitmapFromView(view),
				new GeoPoint((int) (location.getLatitude() * 1e6),
						(int) (location.getLongitude() * 1e6)), 15);

	}

	/** ��ʾToast��Ϣ���жϵ�ǰ�Ƿ���Toast��Ϣ */
	private void showToast(String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		} else {
			mToast.setText(msg);
			mToast.setDuration(Toast.LENGTH_LONG);
		}
		mToast.show();
	}
}
