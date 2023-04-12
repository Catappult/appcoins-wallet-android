package com.asfoundation.wallet.rating.negative

import android.animation.Animator
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentRatingNegativeBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class RatingNegativeFragment : BasePageViewFragment(), RatingNegativeView {

  @Inject
  lateinit var presenter: RatingNegativePresenter

  private val views by viewBinding(FragmentRatingNegativeBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_rating_negative, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setAndRunAnimation()
    setTextWatcher()
    presenter.present()
  }

  private fun setAndRunAnimation() {
    views.animation.setMinFrame(97)
    views.animation.setMaxFrame(123)
    views.animation.playAnimation()
    views.animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(anim: Animator) = Unit
      override fun onAnimationEnd(anim: Animator) {
        if (views.animation.minFrame == 97f) {
          views.animation.setMaxFrame(283)
          views.animation.setMinFrame(219)
          views.animation.playAnimation()
        }
      }

      override fun onAnimationCancel(anim: Animator) = Unit
      override fun onAnimationStart(anim: Animator) = Unit
    })
  }

  private fun setTextWatcher() {
    views.feedbackInputText.addTextWatcher(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) {
        if (!TextUtils.isEmpty(s)) views.feedbackInputText.reset()
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })
  }

  override fun submitClickEvent(): Observable<String> {
    return RxView.clicks(views.submitButton)
        .map { views.feedbackInputText.getText() }
  }

  override fun noClickEvent(): Observable<Any> {
    return RxView.clicks(views.noButton)
  }

  override fun setLoading() {
    views.progressBar.visibility = View.VISIBLE
    views.feedbackInputText.visibility = View.INVISIBLE
    views.submitButton.isEnabled = false
  }

  override fun showEmptySuggestionsError() {
    views.feedbackInputText.setError(getString(R.string.rate_us_improve_field__empty_error))
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    @JvmStatic
    fun newInstance(): RatingNegativeFragment {
      return RatingNegativeFragment()
    }
  }
}
