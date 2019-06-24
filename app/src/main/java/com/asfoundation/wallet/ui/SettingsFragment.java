package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import androidx.annotation.Nullable;
import com.asf.wallet.BuildConfig;
import com.asf.wallet.R;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.permissions.manage.view.ManagePermissionsActivity;
import com.asfoundation.wallet.router.ManageWalletsRouter;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

public class SettingsFragment extends PreferenceFragment {
  @Inject FindDefaultWalletInteract findDefaultWalletInteract;
  @Inject ManageWalletsRouter manageWalletsRouter;

  @Override public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.fragment_settings);

    final Preference redeem = findPreference("pref_redeem");
    redeem.setOnPreferenceClickListener(preference -> {
      findDefaultWalletInteract.find()
          .subscribe(wallet -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
              BuildConfig.MY_APPCOINS_BASE_HOST + "redeem?wallet_address=" + wallet.address))));
      return false;
    });
    final Preference wallets = findPreference("pref_wallet");

    wallets.setOnPreferenceClickListener(preference -> {
      manageWalletsRouter.open(getActivity(), false);
      return false;
    });
    findPreference("pref_permissions").setOnPreferenceClickListener(
        preference -> openPermissionScreen());

    findDefaultWalletInteract.find()
        .subscribe(wallet -> {
          PreferenceManager.getDefaultSharedPreferences(view.getContext())
              .edit()
              .putString("pref_wallet", wallet.address)
              .apply();
          wallets.setSummary(wallet.address);
        }, t -> {
        });

    String versionString = getVersion();
    Preference version = findPreference("pref_version");
    version.setSummary(versionString);

    final Preference twitter = findPreference("pref_twitter");
    twitter.setOnPreferenceClickListener(preference -> {
      Intent intent;
      try {
        // get the Twitter app if possible
        getActivity().getPackageManager()
            .getPackageInfo("com.twitter.android", 0);
        intent =
            new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=915531221551255552"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      } catch (Exception e) {
        // no Twitter app, revert to browser
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/AppCoinsProject"));
      }
      startActivity(intent);
      return false;
    });

    final Preference facebook = findPreference("pref_facebook");
    facebook.setOnPreferenceClickListener(preference -> {
      Intent intent =
          new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/AppCoinsOfficial"));
      startActivity(intent);
      return false;
    });

    final Preference email = findPreference("pref_email");
    email.setOnPreferenceClickListener(preference -> {

      Intent mailto = new Intent(Intent.ACTION_SEND_MULTIPLE);
      mailto.setType("message/rfc822"); // use from live device
      mailto.putExtra(Intent.EXTRA_EMAIL, new String[] {
          "info@appcoins.io"
      });
      mailto.putExtra(Intent.EXTRA_SUBJECT, "Android wallet support question");
      mailto.putExtra(Intent.EXTRA_TEXT, "Dear AppCoins support,");

      startActivity(Intent.createChooser(mailto, "Select email application."));
      return true;
    });

    final Preference credits = findPreference("pref_credits");
    credits.setOnPreferenceClickListener(preference -> {
      new AlertDialog.Builder(getActivity()).setPositiveButton(R.string.close,
          (dialog, which) -> dialog.dismiss())
          .setMessage(R.string.settings_fragment_credits)
          .create()
          .show();
      return true;
    });
  }

  private boolean openPermissionScreen() {
    startActivity(ManagePermissionsActivity.newIntent(getActivity()));
    return true;
  }

  public String getVersion() {
    String version = "N/A";
    try {
      PackageInfo pInfo = getActivity().getPackageManager()
          .getPackageInfo(getActivity().getPackageName(), 0);
      version = pInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return version;
  }
}

