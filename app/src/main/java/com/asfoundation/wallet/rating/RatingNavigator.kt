package com.asfoundation.wallet.rating

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.rating.finish.RatingFinishFragment
import com.asfoundation.wallet.rating.negative.RatingNegativeFragment
import com.asfoundation.wallet.rating.positive.RatingPositiveFragment
import io.reactivex.Observable
import javax.inject.Inject


class RatingNavigator @Inject constructor(fragment: Fragment,
                      private val fragmentManager: FragmentManager) {

  private val ratingActivity = fragment.activity as RatingActivity

  fun disableActivityBack() = ratingActivity.disableBack()

  fun enableActivityBack() = ratingActivity.enableBack()

  fun onBackPressed(): Observable<Any> {
    return ratingActivity.onBackPressedSubject
  }

  fun navigateToSuggestions() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, RatingNegativeFragment.newInstance())
        .addToBackStack(null)
        .commit()
  }

  fun navigateToThankYou() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, RatingPositiveFragment.newInstance())
        .addToBackStack(null)
        .commit()
  }

  fun navigateToRate() {
    ratingActivity.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")))
  }

  fun closeActivity() = ratingActivity.finish()

  fun navigateToFinish() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, RatingFinishFragment())
        .commit()
  }

}