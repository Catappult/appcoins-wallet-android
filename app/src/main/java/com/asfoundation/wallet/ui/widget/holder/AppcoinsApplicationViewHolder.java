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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import com.asf.wallet.R;
import com.asfoundation.wallet.GlideApp;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.widget.CardHeaderTransformation;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import rx.functions.Action2;

public class AppcoinsApplicationViewHolder extends RecyclerView.ViewHolder {

  private final TextView appName;
  private final Action2<AppcoinsApplication, ApplicationClickAction> applicationClickListener;
  private final ImageView appIcon;
  private final TextView appRating;
  private final ImageView featuredGraphic;
  private final ImageView shareIcon;
  private final TextView shareTitle;

  public AppcoinsApplicationViewHolder(View itemView,
      Action2<AppcoinsApplication, ApplicationClickAction> applicationClickListener) {
    super(itemView);
    appName = itemView.findViewById(R.id.app_name);
    appIcon = itemView.findViewById(R.id.app_icon);
    featuredGraphic = itemView.findViewById(R.id.featured_graphic);
    appRating = itemView.findViewById(R.id.app_rating);
    shareIcon = itemView.findViewById(R.id.share_icon);
    shareTitle = itemView.findViewById(R.id.share_title);
    this.applicationClickListener = applicationClickListener;
  }

  public void bind(AppcoinsApplication appcoinsApplication) {
    appName.setText(appcoinsApplication.getName());

    Target<Bitmap> marketBitmap = new Target<Bitmap>() {
      @Override public void onLoadStarted(@Nullable Drawable placeholder) {
        appIcon.setImageDrawable(placeholder);
      }

      @Override public void onLoadFailed(@Nullable Drawable errorDrawable) {
        ColorDrawable whiteBackground = new ColorDrawable(0xffff);
        appIcon.setImageDrawable(whiteBackground);
        featuredGraphic.setImageDrawable(whiteBackground);
      }

      @Override public void onResourceReady(@NonNull Bitmap resource,
          @Nullable Transition<? super Bitmap> transition) {
        appIcon.setImageBitmap(resource);
        if (appcoinsApplication.getFeaturedGraphic() == null) {
          loadDefaultFeaturedGraphic(resource);
        }
      }

      @Override public void onLoadCleared(@Nullable Drawable placeholder) {
      }

      @Override public void getSize(@NonNull SizeReadyCallback cb) {
        cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
      }

      @Override public void removeCallback(@NonNull SizeReadyCallback cb) {
      }

      @Override public void onStart() {
      }

      @Override public void onStop() {
      }

      @Override public void onDestroy() {
      }

      @Override public void setRequest(@Nullable Request request) {
      }

      @Nullable @Override public Request getRequest() {
        return null;
      }
    };
    appIcon.setTag(marketBitmap);

    GlideApp.with(itemView.getContext())
        .asBitmap()
        .load(appcoinsApplication.getIcon())
        .apply(RequestOptions.bitmapTransform(new CircleCrop())
            .placeholder(android.R.drawable.progress_indeterminate_horizontal))
        .into(marketBitmap);

    int space = getSizeFromDp(itemView.getContext()
        .getResources()
        .getDisplayMetrics(), 8);
    GlideApp.with(itemView.getContext())
        .load(appcoinsApplication.getFeaturedGraphic())
        .apply(RequestOptions.bitmapTransform(
            new MultiTransformation<>(new CenterCrop(), new CardHeaderTransformation(space))))
        .into(featuredGraphic);
    appRating.setText(String.valueOf(appcoinsApplication.getRating()));
    setupClickListeners(appcoinsApplication);
  }

  private void setupClickListeners(AppcoinsApplication appcoinsApplication) {
    if (applicationClickListener != null) {
      appName.setOnClickListener(
          v -> applicationClickListener.call(appcoinsApplication, ApplicationClickAction.CLICK));
      appIcon.setOnClickListener(
          v -> applicationClickListener.call(appcoinsApplication, ApplicationClickAction.CLICK));
      appRating.setOnClickListener(
          v -> applicationClickListener.call(appcoinsApplication, ApplicationClickAction.CLICK));
      featuredGraphic.setOnClickListener(
          v -> applicationClickListener.call(appcoinsApplication, ApplicationClickAction.CLICK));

      shareIcon.setOnClickListener(
          v -> applicationClickListener.call(appcoinsApplication, ApplicationClickAction.SHARE));
      shareTitle.setOnClickListener(
          v -> applicationClickListener.call(appcoinsApplication, ApplicationClickAction.SHARE));
    }
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
