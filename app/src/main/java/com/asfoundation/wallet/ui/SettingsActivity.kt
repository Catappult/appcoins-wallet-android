package com.asfoundation.wallet.ui

import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class SettingsActivity : BaseActivity(), HasAndroidInjector {

  @Inject
  lateinit var androidInjector: DispatchingAndroidInjector<Any>

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)
    toolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SettingsFragment())
        .commit()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      TransactionsRouter().open(this, true)
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun androidInjector() = androidInjector
}