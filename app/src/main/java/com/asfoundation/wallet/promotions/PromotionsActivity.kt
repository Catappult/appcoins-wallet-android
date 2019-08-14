package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection

class PromotionsActivity : BaseActivity() {

  private lateinit var transactionsRouter: TransactionsRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)

    super.onCreate(savedInstanceState)

    setContentView(R.layout.promotions_activity_view)
    toolbar()
    enableDisplayHomeAsUp()
    transactionsRouter = TransactionsRouter()
    val promotionsFragment = PromotionsFragment()
    // Display the fragment as the main content.
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, promotionsFragment)
        .commit()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        transactionsRouter.open(this, true)
      }
    }
    return super.onOptionsItemSelected(item)
  }
}
