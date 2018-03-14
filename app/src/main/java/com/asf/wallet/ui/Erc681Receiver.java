package com.asf.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.asf.wallet.ui.iab.IabActivity;

/**
 * Created by trinkes on 13/03/2018.
 */

public class Erc681Receiver extends BaseActivity {

  public static final int REQUEST_CODE = 234;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getIntent().getData()
        .toString()
        .contains("/buy?")) {
      createIntent(IabActivity.class);
    } else {
      createIntent(SendActivity.class);
    }
  }

  private void createIntent(Class<? extends Activity> activity) {
    Intent intent = new Intent(getIntent());
    intent.setClass(this, activity);
    startActivityForResult(intent, REQUEST_CODE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
      setResult(resultCode, data);
      finish();
    }
  }
}
