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

/** Farm metric ring — bold stroke and saturated progress for clear at-a-glance reading. */
public class MetricRingView extends View {

    private static final float PROGRESS_STROKE_DP = 8f;
    private static final float TRACK_STROKE_DP = 5.5f;
    private static final float GLOW_STROKE_DP = 11f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.metric_ring_track));
        trackPaint.setStrokeWidth(TRACK_STROKE_DP * density);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setStrokeWidth(GLOW_STROKE_DP * density);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        ringPaint.setStrokeWidth(PROGRESS_STROKE_DP * density);

        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(ContextCompat.getColor(context, R.color.soil));
        valuePaint.setFakeBoldText(true);

        subValuePaint.setTextAlign(Paint.Align.CENTER);
        subValuePaint.setColor(ContextCompat.getColor(context, R.color.bark));

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(ContextCompat.getColor(context, R.color.moss));
        titlePaint.setFakeBoldText(true);

        valuePaint.setTextSize(16f * density);
        subValuePaint.setTextSize(10f * density);
        titlePaint.setTextSize(11f * density);
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
        int min = (int) (100 * getResources().getDisplayMetrics().density);
        int titleSpace = (int) (20 * getResources().getDisplayMetrics().density);
        int w = resolveSize(min, widthMeasureSpec);
        int h = resolveSize(min + titleSpace, heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float titleH = 18f * density;
        float ringAreaH = getHeight() - titleH - 4f * density;
        float cx = getWidth() / 2f;
        float cy = ringAreaH / 2f;
        float maxStroke = Math.max(ringPaint.getStrokeWidth(), glowPaint.getStrokeWidth());
        float radius = Math.min(cx, cy) - maxStroke * 1.1f;

        setArcBounds(cx, cy, radius);

        if (ringColor != 0) {
            trackPaint.setColor(blendAlpha(ringColor, 0x33));
            trackPaint.setStrokeWidth(TRACK_STROKE_DP * density);
        } else {
            trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.metric_ring_track));
        }
        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint);

        if (progress > 0f && ringColor != 0) {
            float sweep = Math.max(6f, 360f * progress);

            glowPaint.setColor(blendAlpha(ringColor, 0x55));
            canvas.drawArc(arcBounds, -90f, sweep, false, glowPaint);

            ringPaint.setColor(ringColor);
            canvas.drawArc(arcBounds, -90f, sweep, false, ringPaint);
        }

        if (valueSubText.isEmpty()) {
            canvas.drawText(valueText, cx, cy + 5f * density, valuePaint);
        } else {
            canvas.drawText(valueText, cx, cy - 2f * density, valuePaint);
            canvas.drawText(valueSubText, cx, cy + 11f * density, subValuePaint);
        }

        canvas.drawText(titleText, cx, getHeight() - 4f * density, titlePaint);
    }

    private void setArcBounds(float cx, float cy, float radius) {
        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);
    }

    private static int blendAlpha(@ColorInt int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
