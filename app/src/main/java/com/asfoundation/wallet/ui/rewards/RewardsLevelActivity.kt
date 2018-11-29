package com.asfoundation.wallet.ui.rewards

import android.app.Fragment
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import javax.inject.Inject

class RewardsLevelActivity : BaseActivity() {

//  @Inject
//  var fragmentInjector: AndroidInjector<Fragment>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

//    AndroidInjection.inject(this)

    setContentView(R.layout.activity_rewards_level)
    toolbar()
    // Display the fragment as the main content.
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, MyLevelFragment())
        .commit()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        TransactionsRouter().open(this, true)
        finish()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

//  override fun fragmentInjector(): AndroidInjector<Fragment>? {
//    return fragmentInjector
//  }


}