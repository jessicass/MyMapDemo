package com.example.mymapdemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * ��λ�Ͷ�λͼ��Ĺ���
 * 
 */
public class LocationProvider {
	private Context mContext;

	private LocationClient mLocClient; // ��λSDK������
	private BDLocation myLocation; // �û���ǰ��λ��Ϣ

	private MyMapView mMapView; // ��ʾ��ͼ��View
	private LocationData mLocData; // �û�λ����Ϣ
	private MyLocationOverlay myLocOverlay; // �û�λ����Ϣ��ʾͼ��

	private boolean isRequest = false; // �Ƿ��ֶ���������λ
	private boolean isFirstLoc = true; // �Ƿ��״ζ�λ
	private Toast mToast;

	public LocationProvider(Context context, MyMapView mapView) {
		this.mContext = context;
		this.mMapView = mapView;

		// ʵ������λ����LocationClient����������߳�������
		// getApplicationContext()��ȡȫ������Ч��context
		mLocClient = new LocationClient(mContext);

		/** ��λͼ���ʼ�� */
		// ʹ�ü̳�MyLocationOverlay��MyOwnLocationOverlayĿ������дdispatchTap�¼�ʵ�����ݴ���
		// ���������dispatchTap�¼���������̳У�ֱ��ʹ��MyLocationOverlay����
		this.myLocOverlay = new MyOwnLocationOverlay(mMapView);
		this.mLocData = new LocationData(); // ʵ�����û�λ����Ϣ
		myLocOverlay.setData(mLocData); // ���û�λ����Ϣ�������ҵ�λ��ͼ��
		mMapView.getOverlays().add(myLocOverlay); // ��Ӷ�λͼ��
		mMapView.refresh(); // �޸�λ�����ݺ�ˢ��ͼ����Ч
	}

	/** ��ʼ��λ */
	public void startLocation() {
		// ע��̳���BDLocationListener�ļ�������MyLocationListener
		mLocClient.registerLocationListener(new MyLocationListener());
		mLocClient.setLocOption(initLocationOption()); // ���ö�λ����
		mLocClient.start(); // ���ô˷�����ʼ��λ
	}

	/** ֹͣ��λ */
	public void stopLocation() {
		if (mLocClient != null) {
			mLocClient.stop();
		}
	}

	/** �ֶ�����λ */
	public void requestLocation() {
		isRequest = true;
		if (mLocClient != null && mLocClient.isStarted()) {
			showToast("���ڶ�λ");
			mLocClient.requestLocation();
		} else {
			Log.d("request location", "locClient is null or not started");
		}
	}

	/** ������λ�Ľӿڣ���Ҫʵ���������� */
	class MyLocationListener implements BDLocationListener {

		/** �����첽���صĶ�λ�����������BDLocation���Ͳ��� */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			myLocation = location;
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

	/** ��ʾ��������ͼ��PopupOverlay */
	private void showPopupOverlay(BDLocation location) {
		View view = LayoutInflater.from(mContext).inflate(
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
			mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		} else {
			mToast.setText(msg);
			mToast.setDuration(Toast.LENGTH_LONG);
		}
		mToast.show();
	}
}
