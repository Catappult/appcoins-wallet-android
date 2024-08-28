package com.asfoundation.wallet.iab.presentation.main

import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.fragment.navArgs
import com.asfoundation.wallet.iab.IabBaseFragment
import com.asfoundation.wallet.iab.domain.model.PurchaseData

class MainFragment : IabBaseFragment() {

  private val args by navArgs<MainFragmentArgs>()

  private val purchaseData by lazy { args.purchaseDataExtra }

  @Composable
  override fun FragmentContent() = MainScreen(purchaseData)
}

@Composable
private fun MainScreen(purchaseData: PurchaseData) {
  Text(
    modifier = Modifier.background(Color.White),
    text = "Hello"
  )
}
