package com.asfoundation.wallet.ui.widget.holder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.widget.CardHeaderTransformation;
import com.asfoundation.wallet.widget.CircleTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import rx.functions.Action1;

public class AppcoinsApplicationViewHolder extends RecyclerView.ViewHolder {

  private final TextView appName;
  private final Action1<AppcoinsApplication> applicationClickListener;
  private final ImageView appIcon;
  private final TextView appRating;
  private final ImageView featuredGraphic;

  public AppcoinsApplicationViewHolder(View itemView,
      Action1<AppcoinsApplication> applicationClickListener) {
    super(itemView);
    appName = itemView.findViewById(R.id.app_name);
    appIcon = itemView.findViewById(R.id.app_icon);
    featuredGraphic = itemView.findViewById(R.id.featured_graphic);
    appRating = itemView.findViewById(R.id.app_rating);
    this.applicationClickListener = applicationClickListener;
  }

  public void bind(AppcoinsApplication appcoinsApplication) {
    appName.setText(appcoinsApplication.getName());
    Target target = new Target() {
      @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        appIcon.setImageBitmap(bitmap);
        if (appcoinsApplication.getFeaturedGraphic() == null) {
          loadDefaultFeaturedGraphic(bitmap);
        }
      }

      @Override public void onBitmapFailed(Drawable errorDrawable) {
        ColorDrawable whiteBackground = new ColorDrawable(0xffff);
        appIcon.setImageDrawable(whiteBackground);
        featuredGraphic.setImageDrawable(whiteBackground);
      }

      @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
        appIcon.setImageDrawable(placeHolderDrawable);
      }
    };
    appIcon.setTag(target);

    Picasso.with(itemView.getContext())
        .load(appcoinsApplication.getIcon())
        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
        .transform(new CircleTransformation())
        .into(target);
    int space = getSizeFromDp(itemView.getContext()
        .getResources()
        .getDisplayMetrics(), 8);
    Picasso.with(itemView.getContext())
        .load(appcoinsApplication.getFeaturedGraphic())
        .fit()
        .centerCrop()
        .transform(new CardHeaderTransformation(space))
        .into(featuredGraphic);
    appRating.setText(String.valueOf(appcoinsApplication.getRating()));
    itemView.setOnClickListener(v -> applicationClickListener.call(appcoinsApplication));
  }

  private void loadDefaultFeaturedGraphic(Bitmap bitmap) {
    Palette.from(bitmap)
        .generate(palette -> {
          int dominantColor = palette.getDominantColor(0x36aeeb);
          DisplayMetrics displayMetrics = itemView.getContext()
              .getResources()
              .getDisplayMetrics();
          int space = getSizeFromDp(displayMetrics, 8);

          Bitmap image = Bitmap.createBitmap(getSizeFromDp(displayMetrics, 260),
              getSizeFromDp(displayMetrics, 16), Bitmap.Config.ARGB_8888);
          image.eraseColor(dominantColor);
          featuredGraphic.setImageBitmap(
              addGradient(new CardHeaderTransformation(space).transform(image),
                  palette.getDominantColor(0x36aeeb), palette.getDominantColor(0x36aeeb) + 300));
        });
  }

  private int getSizeFromDp(DisplayMetrics displayMetrics, int value) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics);
  }

  private Bitmap addGradient(Bitmap src, int color1, int color2) {
    int w = src.getWidth();
    int h = src.getHeight();
    Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(result);

    canvas.drawBitmap(src, 0, 0, null);

    Paint paint = new Paint();
    LinearGradient shader = new LinearGradient(0, 0, w, 0, color1, color2, Shader.TileMode.CLAMP);
    paint.setShader(shader);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawRect(0, 0, w, h, paint);

    return result;
  }
}
