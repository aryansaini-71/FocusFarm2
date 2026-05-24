package com.focusfarm.app.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.focusfarm.app.R;
import com.focusfarm.app.data.SharedPrefsManager;

/**
 * Swaps treelvl1/2/3 PNGs with a crossfade when the tree grows.
 */
public class TreeView extends FrameLayout {

    private ImageView treeCurrent;
    private ImageView treeNext;
    private int currentLevel = -1;

    public TreeView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TreeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TreeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.component_tree_view, this, true);
        treeCurrent = findViewById(R.id.tree_image_current);
        treeNext = findViewById(R.id.tree_image_next);
        treeNext.setAlpha(0f);
    }

    /** Binds tree level from SharedPrefs and animates if it changed. */
    public void bindFromPrefs(Context context) {
        setLevel(SharedPrefsManager.getTreeLevel(context), false);
    }

    public void setLevel(int level) {
        setLevel(level, true);
    }

    public void setLevel(int level, boolean animate) {
        level = Math.max(1, Math.min(3, level));
        if (level == currentLevel) {
            return;
        }

        int drawable = SharedPrefsManager.treeDrawableForLevel(level);
        int size = getResources().getDimensionPixelSize(SharedPrefsManager.treeSizeDimForLevel(level));

        LayoutParams params = new LayoutParams(size, size);
        treeCurrent.setLayoutParams(params);
        treeNext.setLayoutParams(new LayoutParams(size, size));

        if (currentLevel < 0 || !animate) {
            treeCurrent.setImageResource(drawable);
            treeCurrent.setAlpha(1f);
            treeNext.setAlpha(0f);
            currentLevel = level;
            return;
        }

        treeNext.setImageResource(drawable);
        treeNext.setAlpha(0f);
        treeNext.animate().alpha(1f).setDuration(450).start();
        treeCurrent.animate().alpha(0f).setDuration(450).withEndAction(() -> {
            treeCurrent.setImageResource(drawable);
            treeCurrent.setAlpha(1f);
            treeNext.setAlpha(0f);
        }).start();

        AlphaAnimation pulse = new AlphaAnimation(1f, 0.85f);
        pulse.setDuration(200);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(1);
        startAnimation(pulse);

        currentLevel = level;
    }
}
