package com.asfoundation.wallet.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by Joao Raimundo on 20/05/2018.
 */
@SuppressWarnings("unused")
public class ImageBehavior extends CoordinatorLayout.Behavior<View> {
  private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

  public ImageBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
    if (dependency instanceof AppBarLayout) {

      animate(dependency, child);
      return true;
    }
    return false;
  }

  private void animate(final View dependency, final View view) {
    float ratio = getCollapsingRation(dependency);
    view.animate()
        .cancel();

    view.animate()
        .scaleX(ratio)
        .scaleY(ratio)
        .setInterpolator(INTERPOLATOR)
        .setDuration(1)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationStart(Animator animator) {
            if (view.getVisibility() == View.INVISIBLE) {
              view.setVisibility(View.VISIBLE);
            }
          }

          @Override public void onAnimationEnd(Animator animator) {
            if (ratio == 0) {
              view.setVisibility(View.VISIBLE);
            }
          }
        })
        .start();
  }

  private float getCollapsingRation(View dependency) {
    float height = dependency.getHeight();
    float bottom = dependency.getBottom();
    float totalScroll = ((AppBarLayout) dependency).getTotalScrollRange();

    return 1 - ((height - bottom) / totalScroll);
  }
}
