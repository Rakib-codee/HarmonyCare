package com.harmonycare.app.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.harmonycare.app.R;

/**
 * Helper class for animations
 */
public class AnimationHelper {
    
    /**
     * Fade in animation
     * @param view View to animate
     * @param duration Duration in milliseconds
     */
    public static void fadeIn(View view, long duration) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null);
    }
    
    /**
     * Fade out animation
     * @param view View to animate
     * @param duration Duration in milliseconds
     */
    public static void fadeOut(View view, long duration) {
        if (view == null) return;
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
    }
    
    /**
     * Slide in from bottom
     * @param view View to animate
     * @param duration Duration in milliseconds
     */
    public static void slideInFromBottom(View view, long duration) {
        if (view == null) return;
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
    
    /**
     * Slide in from top
     * @param view View to animate
     * @param duration Duration in milliseconds
     */
    public static void slideInFromTop(View view, long duration) {
        if (view == null) return;
        view.setTranslationY(-view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
    
    /**
     * Scale in animation
     * @param view View to animate
     * @param duration Duration in milliseconds
     */
    public static void scaleIn(View view, long duration) {
        if (view == null) return;
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
    
    /**
     * Scale out animation
     * @param view View to animate
     * @param duration Duration in milliseconds
     */
    public static void scaleOut(View view, long duration) {
        if (view == null) return;
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
    }
    
    /**
     * Pulse animation
     * @param view View to animate
     * @param duration Duration in milliseconds
     */
    public static void pulse(View view, long duration) {
        if (view == null) return;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(duration);
        scaleY.setDuration(duration);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * Shake animation
     * @param view View to animate
     */
    public static void shake(View view) {
        if (view == null) return;
        view.animate()
            .translationX(-10)
            .setDuration(50)
            .withEndAction(() -> {
                view.animate()
                    .translationX(10)
                    .setDuration(50)
                    .withEndAction(() -> {
                        view.animate()
                            .translationX(-10)
                            .setDuration(50)
                            .withEndAction(() -> {
                                view.animate()
                                    .translationX(0)
                                    .setDuration(50)
                                    .start();
                            })
                            .start();
                    })
                    .start();
            })
            .start();
    }
    
    /**
     * Card slide in animation (staggered)
     * @param view View to animate
     * @param delay Delay in milliseconds
     */
    public static void cardSlideIn(View view, long delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(300)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(null);
    }
    
    /**
     * Button press animation
     * @param view View to animate
     */
    public static void buttonPress(View view) {
        if (view == null) return;
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setListener(null);
    }
    
    /**
     * Button release animation
     * @param view View to animate
     */
    public static void buttonRelease(View view) {
        if (view == null) return;
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(100)
            .setListener(null);
    }
}

