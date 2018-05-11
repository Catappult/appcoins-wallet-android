package com.asfoundation.wallet.ui.toolbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.asf.wallet.R;

/**
 * Created by Joao Raimundo on 09/05/2018.
 */
public class ToolbarArcBackground extends View {
  private float scale = 0.0f;
  private float extenderOverBoundary = 250.0f;
  private float strokeWidth = 150.0f;
  private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private RectF rectF = new RectF();
  public ToolbarArcBackground(Context context) {
    super(context);
    paint.setColor(context.getResources().getColor(R.color.activity_background_color));

  }

  public ToolbarArcBackground(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    paint.setColor(context.getResources().getColor(R.color.activity_background_color));

  }

  public ToolbarArcBackground(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    paint.setColor(context.getResources().getColor(R.color.activity_background_color));
    paint.setColor(context.getResources().getColor(R.color.activity_background_color));
  }


  public void setScale(float scale) {
    this.scale = (scale < 0) ? 0f : scale;
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(strokeWidth);

    rectF = new RectF(-extenderOverBoundary,
        (getHeight() + (strokeWidth/2)) * scale,
        getWidth() + extenderOverBoundary,
        getHeight() + (strokeWidth/2));
    canvas.drawArc(rectF, 0f, 180f, false, paint);
  }
}
