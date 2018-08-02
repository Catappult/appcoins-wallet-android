package com.asfoundation.wallet.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import com.asf.wallet.R;
import com.asfoundation.wallet.billing.view.card.CreditCardFragment;

public class MainActivity extends BackButtonActivity implements BackButton {

  private Fragment getFragment() {
    return CreditCardFragment.newInstance();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragment_container);

    if (findViewById(R.id.fragment_container) != null) {

      if (savedInstanceState != null) {
        return;
      }

      CreditCardFragment fragment = new CreditCardFragment();

      fragment.setArguments(getIntent().getExtras());

      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container, fragment)
          .commit();
    }
  }

  public void buyButton(View view) {

  }
}
