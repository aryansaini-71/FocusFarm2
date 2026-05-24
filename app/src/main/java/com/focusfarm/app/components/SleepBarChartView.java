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
 * Minimal 7-day sleep bar chart for the Stats screen.
 */
public class SleepBarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barMutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goalLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private float[] hours = new float[7];
    private String[] dayLabels = new String[7];
    private float goalHours = 8f;

    public SleepBarChartView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SleepBarChartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SleepBarChartView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        barPaint.setColor(context.getColor(R.color.moss));
        barMutedPaint.setColor(context.getColor(R.color.stats_accent_green));
        labelPaint.setColor(context.getColor(R.color.stats_text_secondary));
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        goalLinePaint.setColor(context.getColor(R.color.stats_accent_blue));
        goalLinePaint.setStrokeWidth(2f);
        goalLinePaint.setStyle(Paint.Style.STROKE);
    }

    public void setSleepData(float[] sleepHours, String[] labels, float goal) {
        if (sleepHours != null && sleepHours.length > 0) {
            this.hours = sleepHours;
        }
        if (labels != null && labels.length == hours.length) {
            this.dayLabels = labels;
        }
        this.goalHours = goal;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (hours.length == 0) {
            return;
        }

        float labelArea = 40f;
        float chartTop = 12f;
        float chartBottom = getHeight() - labelArea;
        float chartHeight = chartBottom - chartTop;
        float barGap = 12f;
        float barWidth = (getWidth() - barGap * (hours.length + 1)) / hours.length;
        float maxHours = goalHours;

        for (float hour : hours) {
            maxHours = Math.max(maxHours, hour);
        }

        float goalY = chartBottom - (goalHours / maxHours) * chartHeight;
        canvas.drawLine(barGap, goalY, getWidth() - barGap, goalY, goalLinePaint);

        for (int i = 0; i < hours.length; i++) {
            float left = barGap + i * (barWidth + barGap);
            float barHeight = (hours[i] / maxHours) * chartHeight;
            float top = chartBottom - barHeight;

            barRect.set(left, top, left + barWidth, chartBottom);
            boolean metGoal = hours[i] >= goalHours * 0.85f;
            canvas.drawRoundRect(barRect, 8f, 8f, metGoal ? barPaint : barMutedPaint);

            if (i < dayLabels.length) {
                canvas.drawText(dayLabels[i], left + barWidth / 2f, getHeight() - 10f, labelPaint);
            }
        }
    }
}
