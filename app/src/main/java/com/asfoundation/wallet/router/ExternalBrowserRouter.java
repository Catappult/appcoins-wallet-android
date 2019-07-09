package com.asfoundation.wallet.router;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.asf.wallet.R;

public class ExternalBrowserRouter {

  public void open(Context context, Uri uri) {
    try {
      Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
      context.startActivity(launchBrowser);
    } catch (ActivityNotFoundException exception) {
      exception.printStackTrace();
      Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT)
          .show();
    }
  }
}
