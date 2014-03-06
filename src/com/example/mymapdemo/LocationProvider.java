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
 * 定位和定位图层的管理
 * 
 */
public class LocationProvider {
	private Context mContext;

	private LocationClient mLocClient; // 定位SDK核心类
	private BDLocation myLocation; // 用户当前定位信息

	private MyMapView mMapView; // 显示地图的View
	private LocationData mLocData; // 用户位置信息
	private MyLocationOverlay myLocOverlay; // 用户位置信息显示图层

	private boolean isRequest = false; // 是否手动触发请求定位
	private boolean isFirstLoc = true; // 是否首次定位
	private Toast mToast;

	public LocationProvider(Context context, MyMapView mapView) {
		this.mContext = context;
		this.mMapView = mapView;

		// 实例化定位服务，LocationClient类必须在主线程中声明
		// getApplicationContext()获取全进程有效的context
		mLocClient = new LocationClient(mContext);

		/** 定位图层初始化 */
		// 使用继承MyLocationOverlay的MyOwnLocationOverlay目的是重写dispatchTap事件实现泡泡处理
		// 如果不处理dispatchTap事件，则无需继承，直接使用MyLocationOverlay即可
		this.myLocOverlay = new MyOwnLocationOverlay(mMapView);
		this.mLocData = new LocationData(); // 实例化用户位置信息
		myLocOverlay.setData(mLocData); // 将用户位置信息设置在我的位置图层
		mMapView.getOverlays().add(myLocOverlay); // 添加定位图层
		mMapView.refresh(); // 修改位置数据后刷新图层生效
	}

	/** 开始定位 */
	public void startLocation() {
		// 注册继承自BDLocationListener的监听函数MyLocationListener
		mLocClient.registerLocationListener(new MyLocationListener());
		mLocClient.setLocOption(initLocationOption()); // 设置定位参数
		mLocClient.start(); // 调用此方法开始定位
	}

	/** 停止定位 */
	public void stopLocation() {
		if (mLocClient != null) {
			mLocClient.stop();
		}
	}

	/** 手动请求定位 */
	public void requestLocation() {
		isRequest = true;
		if (mLocClient != null && mLocClient.isStarted()) {
			showToast("正在定位");
			mLocClient.requestLocation();
		} else {
			Log.d("request location", "locClient is null or not started");
		}
	}

	/** 用来定位的接口，需要实现两个方法 */
	class MyLocationListener implements BDLocationListener {

		/** 接收异步返回的定位结果，参数是BDLocation类型参数 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			myLocation = location;
			mLocData.latitude = location.getLatitude();
			mLocData.longitude = location.getLongitude();
			// 如果不显示定位精度圈，将accuracy赋值为0即可
			// mLocData.accuracy = location.getRadius();
			mLocData.direction = location.getDerect();

			myLocOverlay.setData(mLocData); // 将定位数据设置到定位图层里
			mMapView.refresh(); // 更新图层数据执行刷新后生效

			if (isFirstLoc || isRequest) {
				// 将给定的位置点以动画形式移动至地图中心
				mMapView.getController().animateTo(
						new GeoPoint((int) (location.getLatitude() * 1e6),
								(int) (location.getLongitude() * 1e6)));
				showPopupOverlay(location);
				isRequest = false;
			}
			isFirstLoc = false;
		}

		/** 接收异步返回的POI查询结果，参数是BDLocation类型参数 */
		@Override
		public void onReceivePoi(BDLocation poiLocation) {
		}

	}

	/** 继承MyLocationOverlay重写dispatchTap方法 */
	class MyOwnLocationOverlay extends MyLocationOverlay {

		public MyOwnLocationOverlay(MapView mapView) {
			super(mapView);
		}

		/** 在“我的位置”坐标上处理点击事件 */
		@Override
		protected boolean dispatchTap() {
			// 点击我的位置显示PopupOverlay
			showPopupOverlay(myLocation);
			return super.dispatchTap();
		}
	}

	/** LocationClientOption类用来设置定位SDK的定位方式 */
	private LocationClientOption initLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开GPRS
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先
		option.setScanSpan(10000); // 设置发起定位请求的间隔时间为10000ms
		option.disableCache(false);// 禁止启用缓存定位
		// option.setPoiNumber(5); //最多返回POI个数
		// option.setPoiDistance(1000); //poi查询距离
		// option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息
		return option;
	}

	/** 显示弹出窗口图层PopupOverlay */
	private void showPopupOverlay(BDLocation location) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.map_popwindow_view, null);
		TextView text_title = (TextView) view.findViewById(R.id.marker_title);
		TextView text_text = (TextView) view.findViewById(R.id.marker_text);
		text_title.setText("[我的位置]");
		text_text.setText(location.getAddrStr());

		mMapView.getPopupOverlay().showPopup(
				BDMapUtils.getBitmapFromView(view),
				new GeoPoint((int) (location.getLatitude() * 1e6),
						(int) (location.getLongitude() * 1e6)), 15);

	}

	/** 显示Toast消息，判断当前是否有Toast消息 */
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
