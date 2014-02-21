package com.example.mymapdemo;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.MeasureSpec;

public class BDMapUtils {
	public static Bitmap getBitmapFromView(View view) {
		view.destroyDrawingCache();
		view.measure(
				MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = view.getDrawingCache(true);
		return bitmap;
	}
}
