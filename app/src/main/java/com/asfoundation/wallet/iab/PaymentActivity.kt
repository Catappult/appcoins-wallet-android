package com.asfoundation.wallet.iab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.asf.wallet.R
import com.asfoundation.wallet.iab.di.GenericUriParser
import com.asfoundation.wallet.iab.di.PaymentActivityNavigatorFactory
import com.asfoundation.wallet.iab.di.PaymentManagerFactory
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.error.IABError
import com.asfoundation.wallet.iab.parser.UriParser
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity(), IABView {

  private val navHostFragment: NavHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment }

  @Inject
  lateinit var paymentManagerFactory: PaymentManagerFactory

  @Inject
  lateinit var paymentActivityFactory: PaymentActivityNavigatorFactory

  @Inject
  @GenericUriParser
  lateinit var uriParser: UriParser

  override val paymentManager: PaymentManager by lazy {
    paymentManagerFactory.create(
      purchaseData = purchaseData.first
        ?: throw RuntimeException("Unable to inject purchase data because its null")
    )
  }

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

    navigator.navigateToInitialScreen(purchaseData.second)
  }

}
