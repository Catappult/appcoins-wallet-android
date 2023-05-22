package com.asfoundation.wallet.rating

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.rating.entry.RatingEntryFragment
import com.asfoundation.wallet.rating.positive.RatingPositiveFragment
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class RatingActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity() {

  @Inject
  lateinit var ratingInteractor: RatingInteractor

  internal val onBackPressedSubject: PublishSubject<Any> = PublishSubject.create()
  private var backEnabled = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rating)
    if (savedInstanceState == null) {
      if (ratingInteractor.isNotFirstTime()) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RatingPositiveFragment.newInstance())
            .commit()
      } else {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RatingEntryFragment.newInstance())
            .commit()
      }
    }
  }

  override fun onBackPressed() {
    if (backEnabled) {
      super.onBackPressed()
    } else {
      onBackPressedSubject.onNext(Unit)
    }
  }

  fun disableBack() {
    backEnabled = false
  }

  fun enableBack() {
    backEnabled = true
  }

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, RatingActivity::class.java)
    }
  }
}