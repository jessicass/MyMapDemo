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

	// 使用继承MapView的MyMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	private MyMapView mMapView = null; // 显示地图的View
	private ZoomControlView mZoomController; // 地图缩放控件控制器
	private LocationProvider mLocProvider; // 定位支持类

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * 使用地图sdk前需要先初始化BMapManager，必须在setContentView方法之前。
		 * BMapManager是全局的，可为多个MapView共用，它需要在地图模块创建之前创建，
		 * 并在地图地图模块销毁后销毁，只要还有地图模块在使用，BMapManager就不应该销毁
		 */
		UserApplication app = (UserApplication) this.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(getApplicationContext());
			// 如果BMapManager没有初始化则初始化BMapManager
			app.mBMapManager.init(UserApplication.BAIDU_MAP_KEY,
					new UserApplication.MyGeneralListener());
		}

		setContentView(R.layout.activity_main);

		mMapView = (MyMapView) findViewById(R.id.bmapView);
		// 隐藏自带的地图缩放控件
		mMapView.setBuiltInZoomControls(false);
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		MapController mMapController = mMapView.getController();
		// mMapController.setCenter(centerPoint); // 设置地图中心点
		mMapController.setZoom(18); // 设置地图zoom级别，这个值的取值范围是[3,18]。

		initZoomController(); // 初始化地图缩放控件

		setMapMarker(); // 标注三个点

		// 初始化定位支持类
		mLocProvider = new LocationProvider(getApplicationContext(), mMapView);
		mLocProvider.startLocation(); // 开始定位
	}

	/** 点击定位按钮手动请求定位 */
	public void requestLocation(View view) {
		mLocProvider.requestLocation();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		mMapView.destroy();

		// 退出时销毁定位
		mLocProvider.stopLocation();

		// 建议在APP整体退出之前调用MapApi的destroy()函数，不要在每个activity的OnDestroy中调用，
		// 避免MapApi重复创建初始化，提高效率
		UserApplication app = (UserApplication) this.getApplication();
		if (app.mBMapManager != null) {
			app.mBMapManager.destroy();
			app.mBMapManager = null;
		}
		super.onDestroy();
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

	/** 初始化地图缩放控件 */
	private void initZoomController() {
		mZoomController = (ZoomControlView) findViewById(R.id.zoom_controller);
		mZoomController.setMapView(mMapView);
		// 注册地图显示事件监听器
		mMapView.regMapViewListener(UserApplication.getInstance().mBMapManager,
				new MyMKMapViewListener());
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
}
