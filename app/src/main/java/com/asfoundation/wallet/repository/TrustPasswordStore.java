package com.asfoundation.wallet.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.asfoundation.wallet.entity.ServiceErrorException;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.util.KS;
import com.crashlytics.android.Crashlytics;
import com.wallet.pwd.trustapp.PasswordManager;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.security.SecureRandom;
import java.util.Map;

public class TrustPasswordStore implements PasswordStore {

  private final Context context;
  private final String defaultAddress = "0x123456789";

  public TrustPasswordStore(Context context) {
    this.context = context;

    migrate();
  }

  private void migrate() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return;
    }
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    Map<String, ?> passwords = pref.getAll();
    for (String key : passwords.keySet()) {
      if (key.contains("-pwd")) {
        String address = key.replace("-pwd", "");
        try {
          KS.put(context, address.toLowerCase(), PasswordManager.getPassword(address, context));
        } catch (Exception ex) {
          Toast.makeText(context, "Could not process passwords.", Toast.LENGTH_LONG)
              .show();
          ex.printStackTrace();
        }
      }
    }
  }

  @Override public Single<String> getPassword(Wallet wallet) {
    return Single.fromCallable(() -> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return new String(KS.get(context, wallet.address));
      } else {
        return PasswordManager.getPassword(wallet.address, context);
      }
    })
        .onErrorResumeNext(throwable -> getPasswordFallBack(wallet.address));
  }

  @Override public Completable setPassword(String address, String password) {
    return Completable.fromAction(() -> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        KS.put(context, address, password);
      } else {
        try {
          PasswordManager.setPassword(address, password, context);
        } catch (Exception e) {
          throw new ServiceErrorException(ServiceErrorException.KEY_STORE_ERROR);
        }
      }
    });
  }

  @Override public Single<String> generatePassword() {
    return Single.fromCallable(() -> {
      byte bytes[] = new byte[256];
      SecureRandom random = new SecureRandom();
      random.nextBytes(bytes);
      return new String(bytes);
    });
  }

  @Override public Completable setBackUpPassword(String masterPassword) {
    return setPassword(defaultAddress, masterPassword);
  }

  private Single<String> getPasswordFallBack(String walletAddress) {
    return Single.fromCallable(() -> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
          return new String(KS.get(context, defaultAddress));
        } catch (Exception ex) {
          Crashlytics.logException(ex);
          throw new ServiceErrorException(ServiceErrorException.KEY_STORE_ERROR,
              "Failed to get the password from the store.");
        }
      } else {
        try {
          return PasswordManager.getPassword(defaultAddress, context);
        } catch (Exception ex) {
          Crashlytics.logException(ex);
          throw new ServiceErrorException(ServiceErrorException.KEY_STORE_ERROR,
              "Failed to get the password from the password manager.");
        }
      }
    })
        .flatMap(password -> setPassword(walletAddress, password).andThen(Single.just(password)));
  }
}
