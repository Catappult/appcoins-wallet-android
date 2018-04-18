package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.asf.wallet.R;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Transaction;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.poa.TransactionFactory;
import com.asfoundation.wallet.service.AirDropService;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsAdapter;
import com.asfoundation.wallet.util.RootUtil;
import com.asfoundation.wallet.viewmodel.BaseNavigationActivity;
import com.asfoundation.wallet.viewmodel.TransactionsViewModel;
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory;
import com.asfoundation.wallet.widget.DepositView;
import com.asfoundation.wallet.widget.EmptyTransactionsView;
import com.asfoundation.wallet.widget.SystemView;
import dagger.android.AndroidInjection;
import java.util.Map;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.ETHEREUM_NETWORK_NAME;
import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;

public class TransactionsActivity extends BaseNavigationActivity implements View.OnClickListener {

  public static final String AIRDROP_MORE_INFO_URL =
      "https://appstorefoundation.org/asf-wallet#wallet-steps";
  private static final String TAG = TransactionsActivity.class.getSimpleName();
  @Inject TransactionsViewModelFactory transactionsViewModelFactory;
  @Inject AddTokenInteract addTokenInteract;
  @Inject TransactionFactory transactionFactory;
  private TransactionsViewModel viewModel;
  private SystemView systemView;
  private TransactionsAdapter adapter;
  private Dialog dialog;
  private AlertDialog loadingDialog;
  private EmptyTransactionsView emptyView;
  private AlertDialog successDialog;
  private AlertDialog errorDialog;
  private AlertDialog programEndedDialog;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_transactions);

    toolbar();
    setTitle(getString(R.string.unknown_balance_with_symbol));
    setSubtitle("");
    initBottomNavigation();
    dissableDisplayHomeAsUp();

    adapter = new TransactionsAdapter(this::onTransactionClick);
    SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
    systemView = findViewById(R.id.system_view);

    RecyclerView list = findViewById(R.id.list);

    list.setLayoutManager(new LinearLayoutManager(this));
    list.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
          RecyclerView.State state) {
        int position = list.getChildAdapterPosition(view);
        if (position == 0) {
          outRect.top = (int) getResources().getDimension(R.dimen.big_margin);
        }
      }
    });
    list.setAdapter(adapter);

    systemView.attachRecyclerView(list);
    systemView.attachSwipeRefreshLayout(refreshLayout);

    viewModel = ViewModelProviders.of(this, transactionsViewModelFactory)
        .get(TransactionsViewModel.class);
    viewModel.progress()
        .observe(this, systemView::showProgress);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.defaultWalletBalance()
        .observe(this, this::onBalanceChanged);
    viewModel.defaultWallet()
        .observe(this, this::onDefaultWallet);
    viewModel.transactions()
        .observe(this, this::onTransactions);
    viewModel.onAirdrop()
        .observe(this, this::onAirdrop);

    refreshLayout.setOnRefreshListener(() -> viewModel.fetchTransactions(true));
  }

  private void onBalanceChanged(Map<String, String> balance) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      return;
    }

    for (Map.Entry<String, String> entry : balance.entrySet()) {
      if (entry.getKey()
          .equals(C.USD_SYMBOL)) {
        actionBar.setSubtitle(C.USD_SYMBOL + balance.get(C.USD_SYMBOL));
      } else {
        actionBar.setTitle(entry.getValue()
            .toUpperCase() + " " + entry.getKey());
        break;
      }
    }
  }

  private void onTransactionClick(View view, Transaction transaction) {
    viewModel.showDetails(view.getContext(), transaction);
  }

  @Override protected void onPause() {
    super.onPause();

    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
    viewModel.pause();
  }

  @Override protected void onResume() {
    super.onResume();

    setTitle(getString(R.string.unknown_balance_without_symbol));
    setSubtitle("");
    adapter.clear();
    viewModel.prepare();
    checkRoot();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_settings, menu);

    NetworkInfo networkInfo = viewModel.defaultNetwork()
        .getValue();
    if (networkInfo != null && networkInfo.name.equals(ETHEREUM_NETWORK_NAME)) {
      getMenuInflater().inflate(R.menu.menu_deposit, menu);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings: {
        viewModel.showSettings(this);
      }
      break;
      case R.id.action_deposit: {
        openExchangeDialog();
      }
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.try_again: {
        viewModel.fetchTransactions(true);
        break;
      }
      case R.id.action_air_drop: {
        viewModel.showAirDrop();
        emptyView.setAirdropButtonEnable(false);
        break;
      }
      case R.id.activity_transactions_error_ok_button:
      case R.id.activity_transactions_program_ended_ok_button:
        dismissDialogs();
        break;
      case R.id.activity_transactions_success_ok_button:
      case R.id.action_learn_more:
        openLearnMore();
        dismissDialogs();
        break;
    }
  }

  private void openLearnMore() {
    viewModel.onLearnMoreClick(this, Uri.parse(AIRDROP_MORE_INFO_URL));
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_my_address: {
        viewModel.showMyAddress(this);
        return true;
      }
      case R.id.action_my_tokens: {
        viewModel.showTokens(this);
        return true;
      }
      case R.id.action_send: {
        viewModel.showSend(this);
        return true;
      }
    }
    return false;
  }

  private void onTransactions(Transaction[] transaction) {
    adapter.addTransactions(transaction);
    invalidateOptionsMenu();
  }

  private void onDefaultWallet(Wallet wallet) {
    adapter.setDefaultWallet(wallet);
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    adapter.setDefaultNetwork(networkInfo);
    setBottomMenu(R.menu.menu_main_network);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if ((errorEnvelope.code == EMPTY_COLLECTION || adapter.getItemCount() == 0)) {
      if (emptyView == null) {
        emptyView = new EmptyTransactionsView(this, this);
      }
      systemView.showEmpty(emptyView);
    }/* else {
            systemView.showError(getString(R.string.error_fail_load_transaction), this);
        }*/
  }

  private void checkRoot() {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    if (RootUtil.isDeviceRooted() && pref.getBoolean("should_show_root_warning", true)) {
      pref.edit()
          .putBoolean("should_show_root_warning", false)
          .apply();
      new AlertDialog.Builder(this).setTitle(R.string.root_title)
          .setMessage(R.string.root_body)
          .setNegativeButton(R.string.ok, (dialog, which) -> {
          })
          .show();
    }
  }

  private void openExchangeDialog() {
    Wallet wallet = viewModel.defaultWallet()
        .getValue();
    if (wallet == null) {
      Toast.makeText(this, getString(R.string.error_wallet_not_selected), Toast.LENGTH_SHORT)
          .show();
    } else {
      BottomSheetDialog dialog = new BottomSheetDialog(this);
      DepositView view = new DepositView(this, wallet);
      view.setOnDepositClickListener(this::onDepositClick);
      dialog.setContentView(view);
      BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
      dialog.setOnShowListener(d -> behavior.setPeekHeight(view.getHeight()));
      dialog.show();
      this.dialog = dialog;
    }
  }

  private void onDepositClick(View view, Uri uri) {
    viewModel.openDeposit(view.getContext(), uri);
  }

  private void onAirdrop(AirDropService.AirdropStatus airdropStatus) {
    Log.d(TAG, "onAirdrop() called with: airdropStatus = [" + airdropStatus + "]");
    switch (airdropStatus) {
      case PENDING:
        showPendingDialog();
        break;
      case ERROR:
        showErrorDialog();
        emptyView.setAirdropButtonEnable(true);
        break;
      case ENDED:
        showProgramEndedDialog();
        break;
      case SUCCESS:
        showSuccessDialog();
        break;
      default:
        dismissDialogs();
    }
  }

  private void showProgramEndedDialog() {
    dismissDialogs();
    if (programEndedDialog == null) {
      View dialogView =
          getLayoutInflater().inflate(R.layout.transactions_activity_airdrop_program_ended,
              systemView, false);
      programEndedDialog = new AlertDialog.Builder(this).setView(dialogView)
          .setOnDismissListener(dialogInterface -> programEndedDialog = null)
          .create();
      dialogView.findViewById(R.id.activity_transactions_program_ended_ok_button)
          .setOnClickListener(this);
      programEndedDialog.show();
    }
  }

  private void showErrorDialog() {
    dismissDialogs();
    if (errorDialog == null) {
      View dialogView =
          getLayoutInflater().inflate(R.layout.transactions_activity_airdrop_error, systemView,
              false);
      errorDialog = new AlertDialog.Builder(this).setView(dialogView)
          .setOnDismissListener(dialogInterface -> errorDialog = null)
          .create();
      dialogView.findViewById(R.id.activity_transactions_error_ok_button)
          .setOnClickListener(this);
      errorDialog.show();
    }
  }

  private void showSuccessDialog() {
    dismissDialogs();
    if (successDialog == null) {
      View dialogView =
          getLayoutInflater().inflate(R.layout.transactions_activity_airdrop_success, systemView,
              false);
      successDialog = new AlertDialog.Builder(this).setView(dialogView)
          .setOnDismissListener(dialogInterface -> successDialog = null)
          .setCancelable(false)
          .create();
      dialogView.findViewById(R.id.activity_transactions_success_ok_button)
          .setOnClickListener(this);
      successDialog.show();
    }
  }

  private void showPendingDialog() {
    if (loadingDialog == null) {
      View dialogView =
          getLayoutInflater().inflate(R.layout.transactions_activity_airdrop_loading, systemView,
              false);
      loadingDialog = new AlertDialog.Builder(this).setView(dialogView)
          .setCancelable(false)
          .setOnDismissListener(dialogInterface -> loadingDialog = null)
          .create();
      loadingDialog.show();
    }
  }

  private void dismissDialogs() {
    if (loadingDialog != null) {
      loadingDialog.dismiss();
    }
    if (successDialog != null) {
      successDialog.dismiss();
    }
    if (errorDialog != null) {
      errorDialog.dismiss();
    }
    if (programEndedDialog != null) {
      programEndedDialog.dismiss();
    }
  }
}
