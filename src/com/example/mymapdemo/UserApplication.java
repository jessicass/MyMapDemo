package com.example.mymapdemo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;

public class UserApplication extends Application {

	private static UserApplication mInstance = null;
	BMapManager mBMapManager = null; // ��ͼ���������

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
					"BMapManager��ʼ������", Toast.LENGTH_LONG).show();
		}
	}

	public static UserApplication getInstance() {
		return mInstance;
	}

	/** �����¼���������������ͨ�������������Ȩ��֤����� */
	static class MyGeneralListener implements MKGeneralListener {

		/** һЩ����״̬�Ĵ�����ص����� */
		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"�����������", Toast.LENGTH_LONG).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"��������ȷ�ļ���������", Toast.LENGTH_LONG).show();
			}
			// ...
		}

		/** ��Ȩ�����ʱ����õĻص����� */
		@Override
		public void onGetPermissionState(int iError) {
			// ����ֵ��ʾKey��֤δͨ��
			if (iError != 0) {
				// ��ȨKey����
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"����UserApplication.java�ļ���������ȷ����ȨKey����������������Ƿ������� error: "
								+ iError, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(
						UserApplication.getInstance().getApplicationContext(),
						"Key��֤�ɹ�", Toast.LENGTH_LONG).show();
			}
		}
	}
}