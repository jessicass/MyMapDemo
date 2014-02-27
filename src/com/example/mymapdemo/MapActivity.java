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

	private BMapManager mBMapMan = null; // 地图引擎管理类
	// 使用继承MapView的MyMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	private MyMapView mMapView = null; // 显示地图的View
	private LocationData mLocData; // 用户位置信息
	private MyLocationOverlay myLocOverlay; // 用户位置信息显示图层

	public LocationClient mLocClient; // 定位SDK核心类
	private BDLocation myLocation; // 用户当前位置

	private ZoomControlView mZoomController; // 地图缩放控件控制器

	private boolean isRequest = false; // 是否手动触发请求定位
	private boolean isFirstLoc = true; // 是否首次定位
	private Toast mToast; // 确保只有一个Toast显示

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 使用地图sdk前需要先初始化BMapManager，必须在setContentView方法之前
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(BAIDU_MAP_KEY, new MyMKGeneralListener());

		setContentView(R.layout.activity_main);

		mMapView = (MyMapView) findViewById(R.id.bmapView);
		// 隐藏自带的地图缩放控件
		mMapView.setBuiltInZoomControls(false);
		// 初始化地图缩放控件
		initZoomController();

		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		MapController mMapController = mMapView.getController();
		// mMapController.setCenter(centerPoint); // 设置地图中心点
		mMapController.setZoom(18); // 设置地图zoom级别，这个值的取值范围是[3,18]。

		// 标注三个点
		setMapMarker();

		// 设置定位图层
		setLocationOverlay();
	}

	/** 点击定位按钮手动请求定位 */
	public void requestLocation(View view) {
		isRequest = true;
		if (mLocClient != null && mLocClient.isStarted()) {
			showToast("正在定位");
			mLocClient.requestLocation();
		} else {
			Log.d("request location", "locClient is null or not started");
		}
	}

	@Override
	protected void onDestroy() {
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		mMapView.destroy();

		// 退出应用调用BMapManager的destroy()方法
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}

		// 退出时销毁定位
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

	/** 设置三个标点 */
	private void setMapMarker() {
		// 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6) 第一个是参数表示纬度，第二个是经度
		GeoPoint point1 = new GeoPoint((int) (30.5136 * 1E6),
				(int) (114.4018 * 1E6));
		GeoPoint point2 = new GeoPoint((int) (30.5138 * 1E6),
				(int) (114.4032 * 1E6));
		GeoPoint point3 = new GeoPoint((int) (30.5125 * 1E6),
				(int) (114.4004 * 1E6));

		// 获取用于在地图上标注一个地理坐标点的图标
		Drawable drawable = getResources().getDrawable(R.drawable.map_balloon);

		// 用OverlayItem准备Overlay数据
		OverlayItem overlayItem1 = new OverlayItem(point1, "point1", "point1");
		OverlayItem overlayItem2 = new OverlayItem(point2, "point2", "point2");
		OverlayItem overlayItem3 = new OverlayItem(point3, "point3", "point3");
		// 使用setMarker()方法设置overlay图片,如果不设置则使用构建ItemizedOverlay时的默认设置
		// overlayItem.setMarker(drawable);

		List<OverlayItem> overlayItemsList = Lists.newArrayList(overlayItem1,
				overlayItem2, overlayItem3);

		// 创建MyItemizedOverlay
		MyItemizedOverlay myItemizedOverlay = new MyItemizedOverlay(drawable,
				mMapView, this);
		// 将IteminizedOverlay添加到MapView中
		mMapView.getOverlays().clear();
		mMapView.getOverlays().add(myItemizedOverlay);
		// 添加OverlayItem
		myItemizedOverlay.addItem(overlayItemsList);

		mMapView.refresh();
	}

	private void setLocationOverlay() {
		// 实例化定位服务，LocationClient类必须在主线程中声明
		// getApplicationConext()获取全进程有效的context
		mLocClient = new LocationClient(getApplicationContext());
		// 注册继承自BDLocationListener的监听函数MyLocationListener
		mLocClient.registerLocationListener(new MyLocationListener());
		mLocClient.setLocOption(initLocationOption()); // 设置定位参数
		mLocClient.start(); // 调用此方法开始定位

		/** 定位图层初始化 */
		// 使用继承MyLocationOverlay的MyOwnLocationOverlay目的是重写dispatchTap事件实现泡泡处理
		// 如果不处理dispatchTap事件，则无需继承，直接使用MyLocationOverlay即可
		myLocOverlay = new MyOwnLocationOverlay(mMapView);

		// 实例化定位数据，并设置在我的位置图层
		mLocData = new LocationData();
		myLocOverlay.setData(mLocData);

		mMapView.getOverlays().add(myLocOverlay); // 添加定位图层
		mMapView.refresh(); // 修改定位数据后刷新图层生效
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

	/** 初始化地图缩放控件 */
	private void initZoomController() {
		mZoomController = (ZoomControlView) findViewById(R.id.zoom_controller);
		mZoomController.setMapView(mMapView);
		// 注册地图显示事件监听器
		mMapView.regMapViewListener(mBMapMan, new MyMKMapViewListener());
		mZoomController.refreshZoomBtnStatus(18);
	}

	/** 地图显示事件监听器，监听地图显示事件，用户需要实现该接口以处理相应事件 */
	class MyMKMapViewListener implements MKMapViewListener {
		private int lastLevel = -1;

		@Override
		public void onClickMapPoi(MapPoi mapPoi) {
		}

		@Override
		public void onGetCurrentMap(Bitmap bitmap) {
		}

		/** 动画结束时会回调此消息，在此方法里面更新缩放按钮的状态 */
		@Override
		public void onMapAnimationFinish() {
			zoomChange();
		}

		@Override
		public void onMapLoadFinish() {
		}

		/** 移动结束时会回调此消息，在此方法里面更新缩放按钮的状态 */
		@Override
		public void onMapMoveFinish() {
			zoomChange();
		}

		/** 更新缩放按钮的状态 */
		private void zoomChange() {
			int zoomLevel = Math.round(mMapView.getZoomLevel());
			if (lastLevel < 0 || lastLevel != zoomLevel) {
				mZoomController.refreshZoomBtnStatus(zoomLevel);
				lastLevel = zoomLevel;
			}
		}
	}

	/** 常用事件监听，用来处理通常的网络错误，授权验证错误等 */
	class MyMKGeneralListener implements MKGeneralListener {

		/** 一些网络状态的错误处理回调函数 */
		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				showToast("网络出错！");
			}
		}

		/** 授权错误的时候调用的回调函数 */
		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				showToast("请在输入正确的授权Key！");
			}
		}

	}

	/** 用来定位的接口，需要实现两个方法 */
	class MyLocationListener implements BDLocationListener {

		/** 接收异步返回的定位结果，参数是BDLocation类型参数 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			MapActivity.this.myLocation = location;
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

	/** 显示弹出窗口图层PopupOverlay */
	private void showPopupOverlay(BDLocation location) {
		View view = LayoutInflater.from(this).inflate(
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
			mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		} else {
			mToast.setText(msg);
			mToast.setDuration(Toast.LENGTH_LONG);
		}
		mToast.show();
	}
}
