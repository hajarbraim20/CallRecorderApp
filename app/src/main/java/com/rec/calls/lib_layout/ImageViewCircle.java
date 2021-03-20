package com.rec.calls.lib_layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;


public class ImageViewCircle extends androidx.appcompat.widget.AppCompatImageView {

	public static final int DEFAULT_COLOR = 0xff000000;

	public static final int DEFAULT_RGB = 0;
	private float mCornerRadius = 0;
	private RectF rectF;
	private PorterDuffXfermode porterDuffXfermode;


	public ImageViewCircle(Context context) {
		super (context, null);
		rectF = new RectF ();
		porterDuffXfermode = new PorterDuffXfermode (PorterDuff.Mode.SRC_IN);
	}


	public ImageViewCircle(Context context, AttributeSet attributes) {
		super (context, attributes);
		rectF = new RectF ();
		porterDuffXfermode = new PorterDuffXfermode (PorterDuff.Mode.SRC_IN);
	}

	@Override
	protected void onDraw (Canvas canvas) {
		Drawable myDrawable = getDrawable ();
		if (myDrawable != null && myDrawable instanceof BitmapDrawable && mCornerRadius > 0) {
			rectF.set (myDrawable.getBounds ());
			int prevCount = canvas.saveLayer (rectF, null, Canvas.ALL_SAVE_FLAG);
			getImageMatrix ().mapRect (rectF);
			Paint paint = ((BitmapDrawable) myDrawable).getPaint ();
			paint.setAntiAlias (true);
			paint.setColor (DEFAULT_COLOR);
			Xfermode prevMode = paint.getXfermode ();
			canvas.drawARGB (DEFAULT_RGB, DEFAULT_RGB, DEFAULT_RGB, DEFAULT_RGB);
			canvas.drawRoundRect (rectF, mCornerRadius, mCornerRadius, paint);
			paint.setXfermode (porterDuffXfermode);
			super.onDraw (canvas);
			paint.setXfermode (prevMode);
			canvas.restoreToCount (prevCount);
		} else {
			super.onDraw (canvas);
		}
	}


	public float getCornerRadius () {
		return this.mCornerRadius;
	}


	public void setCornerRadius (float cornerRadius) {
		this.mCornerRadius = cornerRadius;
	}
}