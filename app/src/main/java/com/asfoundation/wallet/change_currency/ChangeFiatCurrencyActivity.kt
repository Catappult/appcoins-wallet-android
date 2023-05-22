package com.asfoundation.wallet.change_currency

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import com.appcoins.wallet.feature.changecurrency.ui.ChangeFiatCurrencyRoute
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeFiatCurrencyActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity() {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, ChangeFiatCurrencyActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      WalletTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          ChangeFiatCurrencyRoute(
            onExitClick = { this.finishAffinity() },
            chatClick = { displayChat() }
          )
        }
      }
    }
  }
}