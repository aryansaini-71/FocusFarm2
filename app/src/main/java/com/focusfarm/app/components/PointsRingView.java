package com.focusfarm.app.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.focusfarm.app.R;

/**
 * Soft circular ring for the stats header points display.
 */
public class PointsRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private float progress = 0.68f;

    public PointsRingView(@NonNull Context context) {
        super(context);
        init();
    }

    public PointsRingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PointsRingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(getContext().getColor(R.color.stats_ring_track));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(getContext().getColor(R.color.stats_points_ring));
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float stroke = getWidth() * 0.06f;
        trackPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeWidth(stroke);

        float pad = stroke / 2f + 4f;
        arcBounds.set(pad, pad, getWidth() - pad, getHeight() - pad);

        canvas.drawArc(arcBounds, 0f, 360f, false, trackPaint);
        canvas.drawArc(arcBounds, -90f, progress * 360f, false, progressPaint);
    }
}
