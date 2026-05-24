package com.focusfarm.app.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Soft cherry-blossom petal shower drawn over the sleep tree hero.
 */
public class FallingLeavesOverlayView extends View {

    private static final int PETAL_COUNT = 28;
    private static final float MAX_FRAME_DELTA_SEC = 0.05f;

    private static final int[] PETAL_COLORS = {
            0xE6FFB7C5, // blossom pink
            0xE6FFC1D8,
            0xD9FFF0F5, // pale white-pink
            0xCCF8BBD0,
            0xB3E8B4C8,
            0xB3F5E6EB,
            0x99D4A5B9,
    };

    private final Paint petalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path petalPath = new Path();
    private final Matrix petalMatrix = new Matrix();
    private final Random random = new Random();
    private final List<Petal> petals = new ArrayList<>(PETAL_COUNT);

    private final Choreographer choreographer = Choreographer.getInstance();
    private final Choreographer.FrameCallback frameCallback = this::onFrame;

    private boolean running;
    private long lastFrameNs;
    private float fallSpeedMultiplier = 1.0f;

    public FallingLeavesOverlayView(@NonNull Context context) {
        super(context);
        init();
    }

    public FallingLeavesOverlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FallingLeavesOverlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setClickable(false);
        setFocusable(false);
        setFocusableInTouchMode(false);
        petalPaint.setStyle(Paint.Style.FILL);
    }

    /** 1.0 = default; lower = slower, dreamier drift. */
    public void setFallSpeed(float multiplier) {
        fallSpeedMultiplier = Math.max(0.25f, Math.min(2.5f, multiplier));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public void pauseAnimation() {
        stopAnimationLoop();
    }

    public void resumeAnimation() {
        startAnimationLoop();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) {
            return;
        }
        petals.clear();
        for (int i = 0; i < PETAL_COUNT; i++) {
            Petal petal = new Petal();
            resetPetal(petal, true);
            petals.add(petal);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimationLoop();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimationLoop();
        super.onDetachedFromWindow();
    }

    private void startAnimationLoop() {
        if (running) {
            return;
        }
        running = true;
        lastFrameNs = 0;
        choreographer.postFrameCallback(frameCallback);
    }

    private void stopAnimationLoop() {
        running = false;
        lastFrameNs = 0;
        choreographer.removeFrameCallback(frameCallback);
    }

    private void onFrame(long frameTimeNanos) {
        if (!running) {
            return;
        }

        float deltaSec = lastFrameNs == 0
                ? 1f / 60f
                : (frameTimeNanos - lastFrameNs) / 1_000_000_000f;
        lastFrameNs = frameTimeNanos;
        deltaSec = Math.min(deltaSec, MAX_FRAME_DELTA_SEC);

        if (getWidth() > 0 && getHeight() > 0) {
            updatePetals(deltaSec);
            invalidate();
        }

        choreographer.postFrameCallback(frameCallback);
    }

    private void updatePetals(float deltaSec) {
        float scaledDelta = deltaSec * fallSpeedMultiplier;
        int height = getHeight();
        int width = getWidth();

        for (Petal petal : petals) {
            petal.age += scaledDelta;
            petal.y += petal.fallSpeed * scaledDelta;
            petal.x += petal.driftSpeed * scaledDelta;
            petal.x += (float) Math.sin(petal.age * petal.swayFrequency + petal.swayPhase)
                    * petal.swayAmplitude * scaledDelta;
            petal.rotation += petal.spinSpeed * scaledDelta;

            float margin = petal.size * 2f;
            if (petal.y > height + margin || petal.x < -margin || petal.x > width + margin) {
                resetPetal(petal, false);
            }
        }
    }

    private void resetPetal(@NonNull Petal petal, boolean scatterVertically) {
        int width = Math.max(getWidth(), 1);
        int height = Math.max(getHeight(), 1);

        petal.size = dp(6f) + random.nextFloat() * dp(10f);
        petal.x = random.nextFloat() * width;
        petal.y = scatterVertically
                ? random.nextFloat() * height
                : -petal.size - random.nextFloat() * dp(40f);
        petal.fallSpeed = dp(28f) + random.nextFloat() * dp(42f);
        petal.driftSpeed = dp(-8f) + random.nextFloat() * dp(16f);
        petal.swayPhase = random.nextFloat() * (float) (Math.PI * 2);
        petal.swayFrequency = 1.2f + random.nextFloat() * 1.8f;
        petal.swayAmplitude = dp(12f) + random.nextFloat() * dp(20f);
        petal.spinSpeed = -90f + random.nextFloat() * 180f;
        petal.rotation = random.nextFloat() * 360f;
        petal.alpha = 0.28f + random.nextFloat() * 0.32f;
        petal.color = PETAL_COLORS[random.nextInt(PETAL_COLORS.length)];
        petal.age = random.nextFloat() * 4f;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        for (Petal petal : petals) {
            drawPetal(canvas, petal);
        }
    }

    private void drawPetal(@NonNull Canvas canvas, @NonNull Petal petal) {
        petalPaint.setColor(petal.color);
        petalPaint.setAlpha((int) (petal.alpha * 255));

        petalMatrix.reset();
        petalMatrix.postTranslate(petal.x, petal.y);
        petalMatrix.postRotate(petal.rotation);

        float scale = petal.size / 10f;
        petalMatrix.postScale(scale, scale * 1.15f);

        canvas.save();
        canvas.concat(petalMatrix);
        canvas.drawPath(petalPath, petalPaint);
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (petalPath.isEmpty()) {
            buildPetalPath();
        }
    }

    private void buildPetalPath() {
        petalPath.reset();
        // Teardrop petal centered at origin (~20dp base size before scale)
        petalPath.moveTo(0f, -10f);
        petalPath.cubicTo(7f, -6f, 8f, 4f, 0f, 12f);
        petalPath.cubicTo(-8f, 4f, -7f, -6f, 0f, -10f);
        petalPath.close();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static final class Petal {
        float x;
        float y;
        float size;
        float fallSpeed;
        float driftSpeed;
        float swayPhase;
        float swayFrequency;
        float swayAmplitude;
        float spinSpeed;
        float rotation;
        float alpha;
        float age;
        int color;
    }
}
