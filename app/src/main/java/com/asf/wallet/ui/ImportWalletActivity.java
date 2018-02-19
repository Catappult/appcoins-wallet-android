package com.asf.wallet.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.ProgressBar;
import com.asf.wallet.C;
import com.asf.wallet.R;
import com.asf.wallet.entity.ErrorEnvelope;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.ui.widget.adapter.TabPagerAdapter;
import com.asf.wallet.viewmodel.ImportWalletViewModel;
import com.asf.wallet.viewmodel.ImportWalletViewModelFactory;
import dagger.android.AndroidInjection;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static com.asf.wallet.C.ErrorCode.ALREADY_ADDED;

public class ImportWalletActivity extends BaseActivity {

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

  @Override protected void onResume() {
    super.onResume();

    ((ImportKeystoreFragment) pages.get(KEYSTORE_FORM_INDEX).second).setOnImportKeystoreListener(
        importWalletViewModel);
    ((ImportPrivateKeyFragment) pages.get(
        PRIVATE_KEY_FORM_INDEX).second).setOnImportPrivateKeyListener(importWalletViewModel);
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
}
