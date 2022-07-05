package com.asfoundation.wallet.main.appsflyer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.asf.wallet.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class ApkOriginVerification {
  private static final String LOG_TAG = "AppsFlyerOneLinkSimApp";
  private Context context;

  public ApkOriginVerification(Context context) {
    this.context = context;
    init();
  }

  public void init() {
    AppsFlyerLib appsflyer = AppsFlyerLib.getInstance();

    AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
      @Override public void onConversionDataSuccess(Map<String, Object> conversionDataMap) {
        getDataNewInstall();
      }

      @Override public void onConversionDataFail(String errorMessage) {
        Log.d(LOG_TAG, "error getting conversion data: " + errorMessage);
      }

      @Override public void onAppOpenAttribution(Map<String, String> attributionData) {
        Log.d(LOG_TAG, "onAppOpenAttribution: This is fake call.");
      }

      @Override public void onAttributionFailure(String errorMessage) {
        Log.d(LOG_TAG, "error onAttributionFailure : " + errorMessage);
      }
    };

    //Initiate and start the appsflyer tool in the device so we can use it to send the events.
    appsflyer.init(BuildConfig.APPSFLYER_KEY, conversionListener, context);
    appsflyer.start(context);
  }

  /* name: calculateMD5
     arguments:
               - apkFile (File) -> The apk file of the app that is running.
     return:
               - output (String) -> The MD5 checksum of the apk targeted.

     description: Read data from the apk file in bytes, using digest and hashAlgorithm = "MD5".
     Proceed to converting the bytes to a String.
  */
  private String calculateMD5(File apkFile) {

    MessageDigest digest;

    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      return null;
    }

    InputStream is;
    try {
      is = new FileInputStream(apkFile);
    } catch (FileNotFoundException e) {
      return null;
    }

    byte[] buffer = new byte[8192];
    int read;
    try {

      while ((read = is.read(buffer)) > 0) {
        digest.update(buffer, 0, read);
      }

      byte[] md5sum = digest.digest();
      BigInteger bigInt = new BigInteger(1, md5sum);
      String output = bigInt.toString(16);
      output = String.format("%32s", output)
          .replace(' ', '0');
      return output;
    } catch (IOException e) {
      throw new RuntimeException("Unable to process file for MD5", e);
    } finally {
      try {
        is.close();
      } catch (IOException ignored) {
      }
    }
  }

  private ApkOriginService provideService() {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(ApkOriginService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    return retrofit.create(ApkOriginService.class);
  }

  /* name: sendDataNewInstall
     arguments:
               - deviceIdentifier (String) -> Android_ID, an unique ID to identify devices.
               - packageName (String) -> Package name of the app running.
               - apkMd5sum (String) -> Value of this app MD5 hash code.
               - installedFromMarket (String) -> Value given by the android package manager if the
               app was downloaded from a recognized store.
               - deviceManufacturer (String) -> The device brand.
               - deviceInstalledMarkets (List<String>) -> List of the package names of the appstores
               that are installed in the device.
     return: void
     description: Send the data received in the arguments to an endpoint provided by catappult.
                  Based on the data sent receive the result of which store does the app was
                  downloaded
                  from. Send that result to appsflyer.
  */
  private void sendDataNewInstall(String deviceIdentifier, String packageName, String apkMd5sum,
      String installedFromMarket, String deviceManufacturer, List<String> deviceInstalledMarkets) {
    ApkOriginService apkOriginService = provideService();

    //Create a Call object to call the function defined in the retrofitAPI interface.
    Call<String> call =
        apkOriginService.sendData("newInstall", deviceIdentifier, packageName, apkMd5sum,
            installedFromMarket, deviceManufacturer, deviceInstalledMarkets);

    //Executing asynchronously the call created previously
    call.enqueue(new Callback<String>() {
      @Override public void onResponse(Call<String> call, Response<String> response) {
        Map<String, Object> eventValues = new HashMap<String, Object>();
        //Adding extra data to the event that might be useful in the future (model phone)
        eventValues.put(AFInAppEventParameterName.REVIEW_TEXT,
            response.body() + " | Model phone: " + deviceManufacturer);
        //Sending the response given by the endpoint to the appsflyer, using an event
        AppsFlyerLib.getInstance()
            .logEvent(context, response.body()
                .split("\\s")[0], eventValues);
      }

      @Override public void onFailure(Call<String> call, Throwable t) {
        //handle error or failure cases here
        Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  /* name: getDataNewInstall
     arguments:
     return: void
     description: Get all the useful data from the device and call the function to use it.
     The data retrieved:
          - installedFromMarket -> If the app was downloaded from an appstore recognized by the
          android
          if yes, returns the appstore package name.
          - deviceManufacturer -> The device brand.
          - deviceInstalledMarkets -> All the appstores that are installed in the device, in form
          of the appstores' package names.
          - deviceIdentifier (String) -> Android_ID, an unique ID to identify devices.
          - packageName (String) -> Package name of the app running.
          - apkMd5sum (String) -> The value of this app MD5 hash code.
  */
  private void getDataNewInstall() {
    ApkOriginService apkOriginService = provideService();
    Call<List<String>> call = apkOriginService.getData("newInstall");

    //Executing asynchronously the call created previously
    call.enqueue(new Callback<List<String>>() {
      @Override public void onResponse(Call<List<String>> call, Response<List<String>> response) {

          /*The response of the previous call is all the queries needed to make intent to the device
          so we can get all the appstores that are installed in the device */
        List<String> queries = response.body();

        final PackageManager packageManager = context.getPackageManager();
        try {
          final ApplicationInfo applicationInfo =
              packageManager.getApplicationInfo(context.getPackageName(), 0);

          //In case the app was downloaded from an appstore recognized by android it will return
          // the package name of the store
          String installedFromMarket =
              packageManager.getInstallerPackageName(applicationInfo.packageName);

          //Get the device brand
          String deviceManufacturer = Build.BRAND;

          List<String> deviceInstalledMarkets = new ArrayList<>();
            /* Create intents with all the queries received and add the result (all appstores in the
            device) to a list. */
          for (String query : Objects.requireNonNull(queries)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
            //Getting the results of the query
            List<ResolveInfo> resInfo = context.getPackageManager()
                .queryIntentActivities(intent, 0);

            if (!resInfo.isEmpty()) {
              for (ResolveInfo resolveInfo : resInfo) {
                //Getting the packageName of the appstores in the phone and adding to a list
                CharSequence packageNameStores = resolveInfo.activityInfo.packageName;
                deviceInstalledMarkets.add(packageNameStores.toString());
              }
            }
          }

          String[] AccountData;
          AccountData = getAccountData();

          //Call sendDataNewInstall to use all the information gathered
          sendDataNewInstall(AccountData[0], AccountData[1], AccountData[2], installedFromMarket,
              deviceManufacturer, deviceInstalledMarkets);
        } catch (PackageManager.NameNotFoundException e) {
          e.printStackTrace();
        }
      }

      @Override public void onFailure(Call<List<String>> call, Throwable t) {
        //handle error or failure cases here
        Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  /**
   * name: getAccountData
   * arguments:
   * return: String[deviceIdentifier,packageName,apkMd5sum]
   * description: Get all the useful data from the device to check the Account
   * The data retrieved:
   * - deviceIdentifier (String) -> Android_ID, an unique ID to identify devices.
   * - packageName (String) -> Package name of the app running.
   * - apkMd5sum (String) -> The value of this app MD5 hash code.
   **/

  @SuppressLint("HardwareIds") private String[] getAccountData() {

    String[] data = new String[3];

    //Get more data for controlling accountData (deviceIdentifier, packageName, apkMd5sum)

    //deviceIdentifier
    data[0] = Settings.Secure.getString(context.getContentResolver(),
        Settings.Secure.ANDROID_ID);//works with phones and tablets

    //packageName
    data[1] = context.getApplicationContext()
        .getPackageName();

    //make an apk file using pathname
    File file = new File(context.getApplicationContext()
        .getApplicationInfo().sourceDir);
    //apkMd5sum
    data[2] = calculateMD5(file);

    return data;
  }
}

interface ApkOriginService {
  String BASE_URL = "https://ws.catappult.io/api/utils/android/markets/";

  /**
   * The return type is important here
   * The class structure that you've defined in Call<T>
   * should exactly match with your json response
   **/
  @GET("queries") Call<List<String>> getData(@Query("name") String name);

  @POST("events/newInstall") @FormUrlEncoded Call<String> sendData(@Field("name") String name,
      @Field("deviceIdentifier") String deviceIdentifier, @Field("packageName") String packageName,
      @Field("apkMd5sum") String apkMd5sum,
      @Field("installedFromMarket") String installedFromMarket,
      @Field("deviceManufacturer") String deviceManufacturer,
      @Field("deviceInstalledMarkets") List<String> deviceInstalledMarkets);
}