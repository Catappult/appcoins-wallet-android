package com.asfoundation.wallet.rating

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.rating.entry.RatingEntryFragment
import com.asfoundation.wallet.ui.BaseActivity
import io.reactivex.subjects.PublishSubject

class RatingActivity : BaseActivity() {

  internal val onBackPressedSubject: PublishSubject<Any> = PublishSubject.create()
  private var backEnabled = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rating)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .replace(R.id.fragment_container, RatingEntryFragment.newInstance())
          .commit()
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