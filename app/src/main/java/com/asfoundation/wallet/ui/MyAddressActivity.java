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
import androidx.lifecycle.ViewModelProviders;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.viewmodel.MyAddressViewModel;
import com.asfoundation.wallet.viewmodel.MyAddressViewModelFactory;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.Key.WALLET;

public class MyAddressActivity extends BaseActivity implements View.OnClickListener {

  public static final String KEY_ADDRESS = "key_address";
  private static final float QR_IMAGE_WIDTH_RATIO = 0.9f;
  @Inject protected NetworkInfo defaultNetwork;
  @Inject MyAddressViewModelFactory myAddressViewModelFactory;
  MyAddressViewModel viewModel;

  private Wallet wallet;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_my_address);

    toolbar();

    viewModel = ViewModelProviders.of(this, myAddressViewModelFactory)
        .get(MyAddressViewModel.class);
    wallet = getIntent().getParcelableExtra(WALLET);
    String suggestion = getString(R.string.suggestion_this_is_your_address, defaultNetwork.name);
    ((TextView) findViewById(R.id.address_suggestion)).setText(suggestion);
    ((TextView) findViewById(R.id.address)).setText(wallet.address);
    findViewById(R.id.copy_action).setOnClickListener(this);
    final Bitmap qrCode = createQRImage(wallet.address);
    ((ImageView) findViewById(R.id.qr_image)).setImageBitmap(qrCode);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        viewModel.showTransactions(this);
        break;
      }
    }
    return super.onOptionsItemSelected(item);
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
    ClipData clip = ClipData.newPlainText(KEY_ADDRESS, wallet.address);
    if (clipboard != null) {
      clipboard.setPrimaryClip(clip);
    }
    Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void onBackPressed() {
    viewModel.showTransactions(this);
  }
}
