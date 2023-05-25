package com.asfoundation.wallet.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.wallet.appcoins.core.legacy_base.BaseActivity;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

import static com.appcoins.wallet.core.utils.jvm_common.C.Key.WALLET;

@AndroidEntryPoint public class MyAddressActivity extends BaseActivity
    implements View.OnClickListener {

  public static final String KEY_ADDRESS = "key_address";
  private static final float QR_IMAGE_WIDTH_RATIO = 0.9f;
  @Inject protected NetworkInfo defaultNetwork;

  private Wallet wallet;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_my_address);

    toolbar();

    wallet = getIntent().getParcelableExtra(WALLET);
    String suggestion = getString(R.string.suggestion_this_is_your_address, defaultNetwork.name);
    ((TextView) findViewById(R.id.address_suggestion)).setText(suggestion);
    ((TextView) findViewById(R.id.address)).setText(wallet.getAddress());
    findViewById(R.id.copy_action).setOnClickListener(this);
    final Bitmap qrCode = createQRImage(wallet.getAddress());
    ((ImageView) findViewById(R.id.qr_image)).setImageBitmap(qrCode);
  }

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  protected Toolbar toolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setVisibility(View.VISIBLE);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      toolbar.setTitle(getTitle());
    }
    enableDisplayHomeAsUp();
    return toolbar;
  }
  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onResume() {
    super.onResume();
    sendPageViewEvent();
  }

  private Bitmap createQRImage(String address) {
    Point size = new Point();
    getWindowManager().getDefaultDisplay()
        .getSize(size);
    int imageSize = (int) (size.x * QR_IMAGE_WIDTH_RATIO);
    try {
      BitMatrix bitMatrix =
          new MultiFormatWriter().encode(address, BarcodeFormat.QR_CODE, imageSize, imageSize,
              null);
      BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
      return barcodeEncoder.createBitmap(bitMatrix);
    } catch (Exception e) {
      Toast.makeText(this, getString(R.string.error_fail_generate_qr), Toast.LENGTH_SHORT)
          .show();
    }
    return null;
  }

  @Override public void onClick(View v) {
    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(KEY_ADDRESS, wallet.getAddress());
    if (clipboard != null) {
      clipboard.setPrimaryClip(clip);
    }
    Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT)
        .show();
  }
}
