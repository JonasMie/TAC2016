package tac.android.de.truckcompanion.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jonas Miederer.
 * Date: 19.07.2016
 * Time: 23:06
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class CustomCanvas extends View {

    // View related stuff
    private ShapeDrawable shapeDrawable;
    private RectF boundingBox = new RectF(0, 0, 0, 0);
    private Rect rect = new Rect();
    private Paint paint;


    private float rotation = -90;
    private float arcAngle = 0;
    private double strokeWidth = 0;

    /**
     * Instantiates a new Custom canvas.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CustomCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareDraw();
    }

    /**
     * Instantiates a new Custom canvas.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public CustomCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepareDraw();
    }

    /**
     * Instantiates a new Custom canvas.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     * @param defStyleRes  the def style res
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomCanvas(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        prepareDraw();
    }

    /**
     * Instantiates a new Custom canvas.
     *
     * @param context the context
     */
    public CustomCanvas(Context context) {
        super(context);
        prepareDraw();
    }

    private void prepareDraw() {
        // prepare the drawing of the circle overlay
        // the circle's radius is between the bounds of the pie chart entries and is completely transparent.
        // But it has a stroke of the width of the piechart entries representing the overlay
        if (paint == null) {
            paint = new Paint();
            paint.setColor(Color.parseColor("#7f7f7f"));
            paint.setAlpha(255);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) strokeWidth);
            paint.setAntiAlias(true);
        }

        shapeDrawable = new ShapeDrawable(new OvalShape());

        if (boundingBox != null) {
            boundingBox.roundOut(rect);
            shapeDrawable.setBounds(rect);
        } else {
            shapeDrawable.setBounds(0, 0, 0, 0);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(boundingBox, rotation, arcAngle, false, paint);
    }

    /**
     * Sets bounding box.
     *
     * @param boundingBox the bounding box
     */
    public void setBoundingBox(RectF boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * Gets bounding box.
     *
     * @return the bounding box
     */
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

    /**
     * Gets the arc angle.
     *
     * @return the arc angle
     */
    public float getArcAngle() {
        return arcAngle;
    }

    /**
     * Sets the arc angle.
     *
     * @param arcAngle the arc angle
     */
    public void setArcAngle(float arcAngle) {
        this.arcAngle = arcAngle;
    }

    /**
     * Sets the stroke width.
     *
     * @param strokeWidth the stroke width
     */
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        if (paint != null) {
            paint.setStrokeWidth((float) strokeWidth);
        }
    }

    /**
     * Gets the stroke width.
     *
     * @return the stroke width
     */
    public double getStrokeWidth() {
        return strokeWidth;
    }
}
