/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file and all BarcodeXXX and CameraXXX files in this project edited by
 * Daniell Algar (included due to copyright reason)
 */
package com.asfoundation.wallet.ui.barcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.camera.CameraSource;
import com.asfoundation.wallet.ui.camera.CameraSourcePreview;
import com.appcoins.wallet.core.utils.android_common.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.wallet.appcoins.core.legacy_base.BaseActivity;

import java.io.IOException;

public final class BarcodeCaptureActivity extends BaseActivity
    implements BarcodeTracker.BarcodeGraphicTrackerCallback, CameraResultListener {

  // Constants used to pass extra data in the intent
  public static final String BarcodeObject = "Barcode";
  public static final String ERROR_CODE = "error";
  private static final String TAG = "Barcode-reader";
  // Intent request code to handle updating play services if needed.
  private static final int RC_HANDLE_GMS = 9001;
  // Permission request codes need to be < 256
  private static final int RC_HANDLE_CAMERA_PERM = 2;
  private static final boolean AUTO_FOCUS = true;
  private static final boolean USE_FLASH = false;
  private CameraSource mCameraSource;
  private CameraSourcePreview mPreview;

  /**
   * Initializes the UI and creates the detector pipeline.
   */
  @Override public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.layout_barcode_capture);

    mPreview = findViewById(R.id.preview);
    mPreview.addCameraResultListener(this);

    // Check for the camera permission before accessing the camera.  If the
    // permission is not granted yet, request permission.
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED) {
      createCameraSource(AUTO_FOCUS, USE_FLASH);
    } else {
      requestCameraPermission();
    }
  }

  /**
   * Releases the resources associated with the camera source, the associated detectors, and the
   * rest of the processing pipeline.
   */
  @Override protected void onDestroy() {
    super.onDestroy();
    if (mPreview != null) {
      mPreview = null;
    }
    if (mCameraSource !=null){
      mCameraSource = null;
    }
  }

  @Override public void onCameraError() {
    Toast.makeText(this, getString(R.string.no_camera_available), Toast.LENGTH_SHORT)
        .show();
    finish();
  }

  @Override public void onDetectedQrCode(Barcode barcode) {
    Intent intent = new Intent();
    if (barcode != null) {
      intent.putExtra(BarcodeObject, barcode);
      setResult(CommonStatusCodes.SUCCESS, intent);
      finish();
    }
  }

  // Handles the requesting of the camera permission.
  private void requestCameraPermission() {
    Log.w(TAG, "Camera permission is not granted. Requesting permission");
    final String[] permissions = new String[] { Manifest.permission.CAMERA };
    ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
  }

  /**
   * Creates and starts the camera.
   *
   * Suppressing InlinedApi since there is a check that the minimum version is met before using
   * the constant.
   */
  @SuppressLint("InlinedApi") private void createCameraSource(boolean autoFocus, boolean useFlash) {
    Context context = getApplicationContext();

    // A barcode_capture detector is created to track barcodes.  An associated multi-processor
    // instance
    // is set to receive the barcode_capture detection results, track the barcodes, and maintain
    // graphics for each barcode_capture on screen.  The factory is used by the multi-processor to
    // create a separate tracker instance for each barcode_capture.
    BarcodeDetector barcodeDetector =
        new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.ALL_FORMATS)
            .build();
    BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(this);
    barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

    if (!barcodeDetector.isOperational()) {
      // Note: The first time that an app using the barcode_capture or face API is installed on a
      // device, GMS will download a native libraries to the device in order to do detection.
      // Usually this completes before the app is run for the first time.  But if that
      // download has not yet completed, then the above call will not detect any barcodes
      // and/or faces.
      //
      // isOperational() can be used to check if the required native libraries are currently
      // available.  The detectors will automatically become operational once the library
      // downloads complete on device.
      Log.w(TAG, "Detector dependencies are not yet available.");

      // Check for low storage.  If there is low storage, the native library will not be
      // downloaded, so detection will not become operational.
      IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
      boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

      if (hasLowStorage) {
        Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG)
            .show();
        Log.w(TAG, getString(R.string.low_storage_error));
      }
    }

    // Creates and starts the camera.  Note that this uses a higher resolution in comparison
    // to other detection examples to enable the barcode_capture detector to detect small barcodes
    // at long distances.
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay()
        .getMetrics(metrics);

    CameraSource.Builder builder =
        new CameraSource.Builder(getApplicationContext(), barcodeDetector).setFacing(
            CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(metrics.widthPixels, metrics.heightPixels)
            .setRequestedFps(24.0f);

    // make sure that auto focus is an available option
    builder =
        builder.setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);

    mCameraSource = builder.setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
        .build();
  }

  // Stops the camera
  @Override protected void onPause() {
    super.onPause();
    if (mPreview != null) {
      mPreview.stop();
    }
  }

  // Restarts the camera
  @Override protected void onResume() {
    super.onResume();
    startCameraSource();
    sendPageViewEvent();
  }

  /**
   * Callback for the result from requesting permissions. This method
   * is invoked for every call on {@link #requestPermissions(String[], int)}.
   * <p>
   * <strong>Note:</strong> It is possible that the permissions request interaction
   * with the user is interrupted. In this case you will receive empty permissions
   * and results arrays which should be treated as a cancellation.
   * </p>
   *
   * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
   * @param permissions The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions
   * which is either {@link PackageManager#PERMISSION_GRANTED}
   * or {@link PackageManager#PERMISSION_DENIED}. Never null.
   *
   * @see #requestPermissions(String[], int)
   */
  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode == RC_HANDLE_CAMERA_PERM) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d(TAG, "Camera permission granted - initialize the camera source");
        // we have permission, so create the camerasource
        createCameraSource(AUTO_FOCUS, USE_FLASH);
      } else {
        Log.e(TAG, "Permission not granted: results len = " + grantResults.length);
        if (grantResults.length > 0) {
          Log.e(TAG, " Result code = " + grantResults[0]);
        }
        DialogInterface.OnClickListener listener = (dialog, id) -> finish();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.no_camera_permission)
            .setPositiveButton(R.string.ok, listener)
            .show();
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  /**
   * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() throws SecurityException {
    // check that the device has play services available<uses-permission android:name="android
    // .permission.CAMERA" />.
    int code = GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(getApplicationContext());
    if (code != ConnectionResult.SUCCESS) {
      Dialog dlg = GoogleApiAvailability.getInstance()
          .getErrorDialog(this, code, RC_HANDLE_GMS);
      dlg.show();
    }

    if (mCameraSource != null) {
      try {
        mPreview.start(mCameraSource);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        mCameraSource.release();
        mCameraSource = null;
      }
    }
  }
}
