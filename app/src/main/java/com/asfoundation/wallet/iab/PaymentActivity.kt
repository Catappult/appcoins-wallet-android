package com.asfoundation.wallet.iab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.asf.wallet.R
import com.asfoundation.wallet.iab.di.GenericUriParser
import com.asfoundation.wallet.iab.di.PaymentActivityNavigatorFactory
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.error.IABError
import com.asfoundation.wallet.iab.parser.UriParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {

  private val navHostFragment: NavHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment }

  @Inject
  lateinit var paymentActivityFactory: PaymentActivityNavigatorFactory

  @Inject
  @GenericUriParser
  lateinit var uriParser: UriParser

  private val navigator by lazy { paymentActivityFactory.create(navHostFragment.navController) }

  private val uri by lazy { intent?.data }

  private val purchaseData: Pair<PurchaseData?, IABError?> by lazy {
    runCatching { uriParser.parse(uri) }
      .mapCatching { it to null }
      .onFailure {
        it.printStackTrace()
        null to IABError(it.message ?: "Unknown error")
      }
      .getOrDefault(null to IABError("Unknown error occurred while parsing"))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_payment)

    navigator.navigateToInitialScreen(purchaseData.first, purchaseData.second)
  }

}
