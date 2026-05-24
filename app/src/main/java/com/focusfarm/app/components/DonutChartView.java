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

import com.focusfarm.app.R;

/**
 * Minimal donut chart for focus distribution (placeholder-friendly custom view).
 */
public class DonutChartView extends View {

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private float[] segmentFractions = {0.5f, 0.2f, 0.3f};
    @ColorInt
    private int[] segmentColors = new int[3];

    public DonutChartView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DonutChartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DonutChartView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        segmentColors[0] = context.getColor(R.color.stats_accent_green);
        segmentColors[1] = context.getColor(R.color.stats_accent_blue);
        segmentColors[2] = context.getColor(R.color.stats_accent_purple);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    /** @param fractions Must sum to ~1.0 (e.g. 0.5, 0.2, 0.3). */
    public void setSegments(float[] fractions, @ColorInt int[] colors) {
        if (fractions != null && fractions.length > 0) {
            this.segmentFractions = fractions;
        }
        if (colors != null && colors.length > 0) {
            this.segmentColors = colors;
        }
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float stroke = getWidth() * 0.12f;
        trackPaint.setStrokeWidth(stroke);
        segmentPaint.setStrokeWidth(stroke);

        float pad = stroke / 2f + 8f;
        arcBounds.set(pad, pad, getWidth() - pad, getHeight() - pad);

        trackPaint.setColor(getContext().getColor(R.color.stats_ring_track));
        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint);

        float startAngle = -90f;
        if (segmentColors == null || segmentColors.length == 0) {
            return;
        }
        for (int i = 0; i < segmentFractions.length; i++) {
            float sweep = segmentFractions[i] * 360f;
            segmentPaint.setColor(segmentColors[Math.min(i, segmentColors.length - 1)]);
            canvas.drawArc(arcBounds, startAngle, sweep, false, segmentPaint);
            startAngle += sweep;
        }
    }
}
