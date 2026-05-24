package com.focusfarm.app.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.focusfarm.app.R;

/** Styled progress ring with soft fill, glow, and end-cap dot. */
public class MetricRingView extends View {

    private static final float STROKE_DP = 8f;
    private static final float GAP_DEG = 14f;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private float progress = 0.5f;
    private String valueText = "";
    private String valueSubText = "";
    private String titleText = "";
    @ColorInt private int ringColor = 0;

    public MetricRingView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MetricRingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MetricRingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(0x66FFFFFF);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);

        dotPaint.setStyle(Paint.Style.FILL);

        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(ContextCompat.getColor(context, R.color.soil));
        valuePaint.setFakeBoldText(true);

        subValuePaint.setTextAlign(Paint.Align.CENTER);
        subValuePaint.setColor(ContextCompat.getColor(context, R.color.bark));

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(ContextCompat.getColor(context, R.color.moss));
        titlePaint.setFakeBoldText(true);

        ringPaint.setStrokeWidth(STROKE_DP * density);
        trackPaint.setStrokeWidth(STROKE_DP * density);
        glowPaint.setStrokeWidth(STROKE_DP * density * 1.8f);

        valuePaint.setTextSize(14f * density);
        subValuePaint.setTextSize(10f * density);
        titlePaint.setTextSize(10.5f * density);
    }

    public void setMetric(String title, String value, float progress, @ColorInt int ringColor) {
        setMetric(title, value, "", progress, ringColor);
    }

    public void setMetric(String title, String value, String valueSub, float progress,
                          @ColorInt int ringColor) {
        this.titleText = title;
        this.valueText = value;
        this.valueSubText = valueSub != null ? valueSub : "";
        this.progress = Math.max(0f, Math.min(1f, progress));
        this.ringColor = ringColor;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int min = (int) (116 * getResources().getDisplayMetrics().density);
        int w = resolveSize(min, widthMeasureSpec);
        int h = resolveSize(min + (int) (22 * getResources().getDisplayMetrics().density), heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float titleH = 18f * density;
        float ringAreaH = getHeight() - titleH - 6f * density;
        float cx = getWidth() / 2f;
        float cy = ringAreaH / 2f;
        float radius = Math.min(cx, cy) - ringPaint.getStrokeWidth() * 1.6f;

        canvas.drawCircle(cx, cy, radius + ringPaint.getStrokeWidth() * 0.4f, fillPaint);

        setArcBounds(cx, cy, radius);

        int trackAlpha = 0x55;
        trackPaint.setColor(blendAlpha(ringColor != 0 ? ringColor : 0xFF97C459, trackAlpha));
        trackPaint.setStrokeWidth(ringPaint.getStrokeWidth() * 0.55f);
        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint);

        trackPaint.setColor(0x40FFFFFF);
        trackPaint.setStrokeWidth(ringPaint.getStrokeWidth());
        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint);

        float sweep = Math.max(8f, (360f - GAP_DEG) * progress);
        float startAngle = -90f + GAP_DEG / 2f;

        glowPaint.setColor(blendAlpha(ringColor, 0x44));
        canvas.drawArc(arcBounds, startAngle, sweep, false, glowPaint);

        ringPaint.setColor(ringColor);
        canvas.drawArc(arcBounds, startAngle, sweep, false, ringPaint);

        if (progress > 0.02f) {
            double endRad = Math.toRadians(startAngle + sweep);
            float dotX = cx + (float) (radius * Math.cos(endRad));
            float dotY = cy + (float) (radius * Math.sin(endRad));
            dotPaint.setColor(ringColor);
            canvas.drawCircle(dotX, dotY, ringPaint.getStrokeWidth() * 0.55f, dotPaint);
            dotPaint.setColor(0xCCFFFFFF);
            canvas.drawCircle(dotX, dotY, ringPaint.getStrokeWidth() * 0.28f, dotPaint);
        }

        if (valueSubText.isEmpty()) {
            canvas.drawText(valueText, cx, cy + 5f * density, valuePaint);
        } else {
            canvas.drawText(valueText, cx, cy - 1f * density, valuePaint);
            canvas.drawText(valueSubText, cx, cy + 11f * density, subValuePaint);
        }

        canvas.drawText(titleText, cx, getHeight() - 5f * density, titlePaint);
    }

    private void setArcBounds(float cx, float cy, float radius) {
        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);
    }

    private static int blendAlpha(@ColorInt int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
