package me.yanu.onetime;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.bumptech.glide.request.animation.ViewPropertyAnimation;


public class AnimationUtil {

    public static ViewPropertyAnimation.Animator getTextAnimation() {
        ViewPropertyAnimation.Animator animation = new ViewPropertyAnimation.Animator() {
            @Override
            public void animate(View view) {
                view.setAlpha( 0f );
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat( view, "alpha", 0f, 1f );
                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(fadeAnim);
                animSetXY.setDuration( 1500 );
                animSetXY.start();
            }
        };
        return animation;
    }

    public static ViewPropertyAnimation.Animator getImageAnimation() {
        ViewPropertyAnimation.Animator animation = new ViewPropertyAnimation.Animator() {
            @Override
            public void animate(View view) {
                view.setAlpha( 0f );
                ObjectAnimator animX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f);
                ObjectAnimator animY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f);
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat( view, "alpha", 0f, 1f );
                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(animX, animY, fadeAnim);
                animSetXY.setDuration( 1500 );
                animSetXY.start();
            }
        };
        return animation;
    }

    public static ViewPropertyAnimation.Animator getAudioWaveAnimation() {
        ViewPropertyAnimation.Animator animation = new ViewPropertyAnimation.Animator() {
            @Override
            public void animate(View view) {
                view.setAlpha(0f);
                view.setPivotY(view.getMeasuredHeight());
                ObjectAnimator animY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat( view, "alpha", 0f, 1f );
                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(animY, fadeAnim);
                animSetXY.setDuration( 1500 );
                animSetXY.start();
            }
        };
        return animation;
    }
}
