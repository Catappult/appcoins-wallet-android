package com.asfoundation.wallet.ui;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.ui.widget.adapter.ChangeTokenCollectionAdapter;
import com.asfoundation.wallet.viewmodel.TokenChangeCollectionViewModel;
import com.asfoundation.wallet.viewmodel.TokenChangeCollectionViewModelFactory;
import com.asfoundation.wallet.widget.SystemView;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;
import static com.asfoundation.wallet.C.Key.WALLET;

public class TokenChangeCollectionActivity extends BaseActivity implements View.OnClickListener {

  @Inject protected TokenChangeCollectionViewModelFactory viewModelFactory;
  private TokenChangeCollectionViewModel viewModel;

  private ChangeTokenCollectionAdapter adapter;
  private SystemView systemView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {

    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_tokens);

    toolbar();

    adapter = new ChangeTokenCollectionAdapter(this::onTokenClick, this::onTokenDeleteClick);
    RecyclerView list = findViewById(R.id.list);
    systemView = findViewById(R.id.system_view);
    SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);

    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);

    systemView.attachRecyclerView(list);
    systemView.attachSwipeRefreshLayout(refreshLayout);

    viewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(TokenChangeCollectionViewModel.class);

    viewModel.progress()
        .observe(this, systemView::showProgress);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.tokens()
        .observe(this, this::onTokens);
    viewModel.wallet()
        .setValue(getIntent().getParcelableExtra(WALLET));

    refreshLayout.setOnRefreshListener(viewModel::fetchTokens);
  }

  private void onTokenClick(View view, Token token) {
    viewModel.setEnabled(token);
  }

  private void onTokenDeleteClick(View view, Token token) {
    viewModel.deleteToken(token);
  }

  @Override protected void onResume() {
    super.onResume();

    viewModel.prepare();
  }

  private void onTokens(Token[] tokens) {
    adapter.setTokens(tokens);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if (errorEnvelope.code == EMPTY_COLLECTION) {
      systemView.showEmpty(getString(R.string.no_tokens));
    } else {
      systemView.showError(getString(R.string.error_fail_load_tokens), this);
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
