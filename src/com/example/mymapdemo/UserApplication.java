package com.example.mymapdemo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;

public class UserApplication extends Application {

	private static UserApplication mInstance = null;
	BMapManager mBMapManager = null; // 地图引擎管理类

	public static final String BAIDU_MAP_KEY = "kFvtrKqSFkG9mTbtGBpuLbbK";

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		initEngineManager(this);
	}

	public void initEngineManager(Context context) {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(context);
		}

		if (!mBMapManager.init(BAIDU_MAP_KEY, new MyGeneralListener())) {
			Toast.makeText(
					UserApplication.getInstance().getApplicationContext(),
					"BMapManager初始化错误！", Toast.LENGTH_LONG).show();
		}
	}

	public static UserApplication getInstance() {
		return mInstance;
	}

	/** 常用事件监听，用来处理通常的网络错误，授权验证错误等 */
	static class MyGeneralListener implements MKGeneralListener {

		/** 一些网络状态的错误处理回调函数 */
		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"您的网络出错！", Toast.LENGTH_LONG).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"请输入正确的检索条件！", Toast.LENGTH_LONG).show();
			}
			// ...
		}

		/** 授权错误的时候调用的回调函数 */
		@Override
		public void onGetPermissionState(int iError) {
			// 非零值表示Key验证未通过
			if (iError != 0) {
				// 授权Key错误
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"请在UserApplication.java文件中输入正确的授权Key，并坚持您的网络是否正常！ error: "
								+ iError, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"Key认证成功", Toast.LENGTH_LONG).show();
			}
		}
	}
}