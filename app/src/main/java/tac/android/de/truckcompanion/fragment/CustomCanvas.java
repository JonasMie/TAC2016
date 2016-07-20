package tac.android.de.truckcompanion.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

/**
 * Created by Jonas Miederer.
 * Date: 19.07.2016
 * Time: 23:06
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class CustomCanvas extends View {

    private ShapeDrawable mDrawable;
    RectF boundingBox;
    Rect rect = new Rect();
    Paint paint;

    float rotation = 0;
    float arcAngle = 90;

    public CustomCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareDraw();
    }

    public CustomCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepareDraw();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomCanvas(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        prepareDraw();
    }

    public CustomCanvas(Context context) {
        super(context);
    }

    private void prepareDraw() {

        if (paint == null) {
            paint = new Paint();
            paint.setColor(Color.parseColor("#5FBA7D"));
            paint.setAlpha(128);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(50);
            paint.setAntiAlias(true);
        }
        mDrawable = new ShapeDrawable(new OvalShape());

        if (boundingBox != null) {
            boundingBox.roundOut(rect);
            mDrawable.setBounds(rect);
        } else {
            mDrawable.setBounds(0, 0, 0, 0);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(boundingBox, -90, arcAngle, false, paint);
    }

    public void setBoundingBox(RectF boundingBox) {
        this.boundingBox = boundingBox;
    }

    public RectF getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getArcAngle() {
        return arcAngle;
    }

    public void setArcAngle(float arcAngle) {
        this.arcAngle = arcAngle;
    }
}
