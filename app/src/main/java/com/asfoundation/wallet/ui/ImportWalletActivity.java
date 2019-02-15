package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import com.asf.wallet.R;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.ui.widget.OnImportKeystoreListener;
import com.asfoundation.wallet.ui.widget.OnImportPrivateKeyListener;
import com.asfoundation.wallet.ui.widget.adapter.TabPagerAdapter;
import com.asfoundation.wallet.viewmodel.ImportWalletViewModel;
import com.asfoundation.wallet.viewmodel.ImportWalletViewModelFactory;
import com.google.android.material.tabs.TabLayout;
import dagger.android.AndroidInjection;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.ErrorCode.ALREADY_ADDED;

public class ImportWalletActivity extends BaseActivity
    implements OnImportKeystoreListener, OnImportPrivateKeyListener {

  private static final int KEYSTORE_FORM_INDEX = 0;
  private static final int PRIVATE_KEY_FORM_INDEX = 1;

  private final List<Pair<String, Fragment>> pages = new ArrayList<>();

  @Inject ImportWalletViewModelFactory importWalletViewModelFactory;
  ImportWalletViewModel importWalletViewModel;
  private Dialog dialog;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_import_wallet);
    toolbar();

    pages.add(KEYSTORE_FORM_INDEX,
        new Pair<>(getString(R.string.tab_keystore), ImportKeystoreFragment.create()));
    pages.add(PRIVATE_KEY_FORM_INDEX,
        new Pair<>(getString(R.string.tab_private_key), ImportPrivateKeyFragment.create()));
    ViewPager viewPager = findViewById(R.id.viewPager);
    viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager(), pages));
    TabLayout tabLayout = findViewById(R.id.tabLayout);
    tabLayout.setupWithViewPager(viewPager);

    importWalletViewModel = ViewModelProviders.of(this, importWalletViewModelFactory)
        .get(ImportWalletViewModel.class);
    importWalletViewModel.progress()
        .observe(this, this::onProgress);
    importWalletViewModel.error()
        .observe(this, this::onError);
    importWalletViewModel.wallet()
        .observe(this, this::onWallet);
  }

  private void onWallet(Wallet wallet) {
    Intent result = new Intent();
    result.putExtra(C.Key.WALLET, wallet);
    setResult(RESULT_OK, result);
    finish();
  }

  @Override public void onBackPressed() {
    setResult(RESULT_CANCELED);
    super.onBackPressed();
  }

  @Override protected void onPause() {
    super.onPause();

    hideDialog();
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    hideDialog();
    String message = TextUtils.isEmpty(errorEnvelope.message) ? getString(R.string.error_import)
        : errorEnvelope.message;
    if (errorEnvelope.code == ALREADY_ADDED) {
      message = getString(R.string.error_already_added);
    }
    dialog = new AlertDialog.Builder(this).setTitle(R.string.title_dialog_error)
        .setMessage(message)
        .setPositiveButton(R.string.ok, null)
        .create();
    dialog.show();
  }

  private void onProgress(boolean shouldShowProgress) {
    hideDialog();
    if (shouldShowProgress) {
      dialog = new AlertDialog.Builder(this).setTitle(R.string.title_dialog_handling)
          .setView(new ProgressBar(this))
          .setCancelable(false)
          .create();
      dialog.show();
    }
  }

  private void hideDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
  }

  @Override public void onKeystore(String keystore, String password) {
    importWalletViewModel.onKeystore(keystore, password);
  }

  @Override public void onPrivateKey(String key) {
    importWalletViewModel.onPrivateKey(key);
  }
}
