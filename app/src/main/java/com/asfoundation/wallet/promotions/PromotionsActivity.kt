package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity

class PromotionsActivity : BaseActivity(), PromotionsActivityView {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_promotions)
    toolbar()
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .add(R.id.fragment_container, PromotionsFragment.newInstance())
          .commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //Let Fragment handle the click
    return false
  }
}
