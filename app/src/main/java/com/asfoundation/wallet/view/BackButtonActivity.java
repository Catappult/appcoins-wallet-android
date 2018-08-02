package com.asfoundation.wallet.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

public abstract class BackButtonActivity extends AppCompatActivity implements BackButton {

  private Set<BackButton.ClickHandler> clickHandlers;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    clickHandlers = new HashSet<>();
  }

  @Override public void registerClickHandler(ClickHandler clickHandler) {
    clickHandlers.add(clickHandler);
  }

  @Override public void unregisterClickHandler(ClickHandler clickHandler) {
    clickHandlers.remove(clickHandler);
  }

  @Override public void onBackPressed() {
    boolean handled = false;
    for (ClickHandler clickHandler : clickHandlers) {
      if (clickHandler.handle()) {
        handled = true;
      }
    }

    if (!handled) {
      super.onBackPressed();
    }
  }
}
