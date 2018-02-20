package com.asf.wallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.asf.wallet.R;
import com.asf.wallet.entity.ErrorEnvelope;
import com.asf.wallet.entity.Token;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.ui.widget.adapter.TokensAdapter;
import com.asf.wallet.viewmodel.TokensViewModel;
import com.asf.wallet.viewmodel.TokensViewModelFactory;
import com.asf.wallet.widget.SystemView;
import dagger.android.AndroidInjection;
import java.math.BigDecimal;
import javax.inject.Inject;

import static com.asf.wallet.C.ErrorCode.EMPTY_COLLECTION;
import static com.asf.wallet.C.Key.WALLET;

public class TokensActivity extends BaseActivity implements View.OnClickListener {
  @Inject TokensViewModelFactory transactionsViewModelFactory;
  private TokensViewModel viewModel;

  private SystemView systemView;
  private TokensAdapter adapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_tokens);

    toolbar();

    adapter = new TokensAdapter(this::onTokenClick);
    SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
    systemView = findViewById(R.id.system_view);

    RecyclerView list = findViewById(R.id.list);

    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);

    systemView.attachRecyclerView(list);
    systemView.attachSwipeRefreshLayout(refreshLayout);

    viewModel = ViewModelProviders.of(this, transactionsViewModelFactory)
        .get(TokensViewModel.class);
    viewModel.progress()
        .observe(this, systemView::showProgress);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.tokens()
        .observe(this, this::onTokens);
    viewModel.total()
        .observe(this, this::onTotal);
    viewModel.wallet()
        .observe(this, this::onWallet);

    refreshLayout.setOnRefreshListener(viewModel::fetchTokens);
  }

  private void onTotal(BigDecimal totalInCurrency) {
    adapter.setTotal(totalInCurrency);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_add, menu);
    getMenuInflater().inflate(R.menu.menu_edit, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add: {
        viewModel.showAddToken(this);
      }
      break;
      case R.id.action_edit: {
        viewModel.showEditTokens(this);
      }
      break;
      case android.R.id.home: {
        adapter.clear();
        viewModel.showTransactions(this);
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    viewModel.showTransactions(this);
  }

  @Override protected void onResume() {
    super.onResume();

    viewModel.wallet()
        .postValue(getIntent().getParcelableExtra(WALLET));
  }

  private void onTokenClick(View view, Token token) {
    Context context = view.getContext();
    viewModel.showSendToken(context, token.tokenInfo.address, token.tokenInfo.symbol,
        token.tokenInfo.decimals);
  }

  private void onWallet(Wallet wallet) {
    viewModel.fetchTokens();
  }

  private void onTokens(Token[] tokens) {
    adapter.setTokens(tokens);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if (errorEnvelope.code == EMPTY_COLLECTION) {
      systemView.showEmpty(getString(R.string.no_tokens));
    }
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.try_again: {
        viewModel.fetchTokens();
      }
      break;
    }
  }
}
