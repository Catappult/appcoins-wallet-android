package com.asfoundation.wallet.ui.toolbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;
import com.asf.wallet.R;

/**
 * Created by Joao Raimundo on 10/05/2018.
 */
public class TextViewBehaviour extends CoordinatorLayout.Behavior<TextView> {
  private final static float MIN_VIEW_PERCENTAGE_SIZE = 0.3f;
  private final static int EXTRA_VIEW_AVATAR_PADDING = 80;

  private final static String TAG = "behavior";
  private Context mContext;

  private float mCustomFinalYPosition;
  private float mCustomStartXPosition;
  private float mCustomStartToolbarPosition;
  private float mCustomStartHeight;
  private float mCustomFinalHeight;

  private float mViewMaxSize;
  private float mFinalLeftViewPadding;
  private float mStartPosition;
  private int mStartXPosition;
  private float mStartToolbarPosition;
  private int mStartYPosition;
  private int mFinalYPosition;
  private int mStartHeight;
  private int mFinalXPosition;
  private float mChangeBehaviorPoint;

  public TextViewBehaviour(Context context, AttributeSet attrs) {
    mContext = context;

    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewBehavior);
      mCustomFinalYPosition = a.getDimension(R.styleable.TextViewBehavior_finalYPosition, 0);
      mCustomStartXPosition = a.getDimension(R.styleable.TextViewBehavior_startXPosition, 0);
      mCustomStartToolbarPosition =
          a.getDimension(R.styleable.TextViewBehavior_startToolbarPosition, 0);
      mCustomStartHeight = a.getDimension(R.styleable.TextViewBehavior_startHeight, 0);
      mCustomFinalHeight = a.getDimension(R.styleable.TextViewBehavior_finalHeight, 0);

      a.recycle();
    }

    mFinalLeftViewPadding = context.getResources()
        .getDimension(R.dimen.big_margin);

    mViewMaxSize = mContext.getResources()
        .getDimension(R.dimen.large_margin);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, TextView child, View dependency) {
    return dependency instanceof Toolbar;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, TextView child, View dependency) {
    maybeInitProperties(child, dependency);

    final int maxScrollDistance = (int) (mStartToolbarPosition);
    float expandedPercentageFactor = dependency.getY() / maxScrollDistance;

    if (expandedPercentageFactor < mChangeBehaviorPoint) {
      float heightFactor = (mChangeBehaviorPoint - expandedPercentageFactor) / mChangeBehaviorPoint;

      float distanceXToSubtract =
          ((mStartXPosition - mFinalXPosition) * heightFactor) + (child.getHeight() / 2);
      float distanceYToSubtract =
          ((mStartYPosition - mFinalYPosition) * (1f - expandedPercentageFactor))
              + (child.getHeight() / 2);

      child.setX(mStartXPosition - distanceXToSubtract);
      child.setY(mStartYPosition - distanceYToSubtract);

      float heightToSubtract = ((mStartHeight - mCustomFinalHeight) * heightFactor);

      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      lp.width = (int) (mStartHeight - heightToSubtract);
      lp.height = (int) (mStartHeight - heightToSubtract);
      child.setLayoutParams(lp);
    } else {
      float distanceYToSubtract =
          ((mStartYPosition - mFinalYPosition) * (1f - expandedPercentageFactor)) + (mStartHeight
              / 2);

      child.setX(mStartXPosition - child.getWidth() / 2);
      child.setY(mStartYPosition - distanceYToSubtract);

      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      lp.width = (int) (mStartHeight);
      lp.height = (int) (mStartHeight);
      child.setLayoutParams(lp);
    }
    return true;
  }

  private void maybeInitProperties(TextView child, View dependency) {
    if (mStartYPosition == 0) mStartYPosition = (int) (dependency.getY());

    if (mFinalYPosition == 0) mFinalYPosition = (dependency.getHeight() / 2);

    if (mStartHeight == 0) mStartHeight = child.getHeight();

    if (mStartXPosition == 0) mStartXPosition = (int) (child.getX() + (child.getWidth() / 2));

    if (mFinalXPosition == 0) {
      mFinalXPosition = mContext.getResources()
          .getDimensionPixelOffset(R.dimen.abc_action_bar_content_inset_material)
          + ((int) mCustomFinalHeight / 2);
    }

    if (mStartToolbarPosition == 0) mStartToolbarPosition = dependency.getY();

    if (mChangeBehaviorPoint == 0) {
      mChangeBehaviorPoint =
          (child.getHeight() - mCustomFinalHeight) / (2f * (mStartYPosition - mFinalYPosition));
    }
  }

  public int getStatusBarHeight() {
    int result = 0;
    int resourceId = mContext.getResources()
        .getIdentifier("status_bar_height", "dimen", "android");

    if (resourceId > 0) {
      result = mContext.getResources()
          .getDimensionPixelSize(resourceId);
    }
    return result;
  }
}
