package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import io.reactivex.subjects.PublishSubject

class PromotionsActivity : BaseActivity(), PromotionsActivityView {

  private lateinit var transactionsRouter: TransactionsRouter

  private var backEnabled = true

  private var onBackPressedSubject: PublishSubject<Any>? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_promotions)
    toolbar()
    onBackPressedSubject = PublishSubject.create()
    transactionsRouter = TransactionsRouter()
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, PromotionsFragment.newInstance())
        .commit()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == android.R.id.home) {
      if (backEnabled) {
        transactionsRouter.open(this, true)
      } else {
        onBackPressedSubject?.onNext("")
      }
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  override fun backPressed() = onBackPressedSubject!!

  override fun enableBack() {
    backEnabled = true
  }

  override fun disableBack() {
    backEnabled = false
  }
}
