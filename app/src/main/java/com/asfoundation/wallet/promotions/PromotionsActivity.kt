package com.asfoundation.wallet.promotions

import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity

class PromotionsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.promotions_activity_view)
    toolbar()
    val fragment = PromotionsFragment()
    // Display the fragment as the main content.
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, fragment)
        .addToBackStack(fragment::class.java.simpleName)
        .commit()
  }
}
