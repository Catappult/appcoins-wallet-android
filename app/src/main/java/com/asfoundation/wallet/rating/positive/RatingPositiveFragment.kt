package com.asfoundation.wallet.rating.positive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentRatingPositiveBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class RatingPositiveFragment : BasePageViewFragment(), RatingPositiveView {

  @Inject
  lateinit var presenter: RatingPositivePresenter

  private val binding by viewBinding(FragmentRatingPositiveBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentRatingPositiveBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun initializeView(isNotFirstTime: Boolean) {
    if (isNotFirstTime) {
      binding.title.setText(R.string.rate_us_back_title)
      binding.description.setText(R.string.rate_us_back_body)
      binding.animation.setMinFrame(196)
      binding.animation.setMaxFrame(196)
    } else {
      binding.animation.setMinFrame(97)
      binding.animation.setMaxFrame(196)
      binding.animation.playAnimation()
    }
  }

  override fun rateAppClickEvent(): Observable<Any> {
    return RxView.clicks(binding.rateAppButton)
  }

  override fun remindMeLaterClickEvent(): Observable<Any> {
    return RxView.clicks(binding.remindMeLaterButton)
  }

  override fun noClickEvent(): Observable<Any> {
    return RxView.clicks(binding.noButton)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    @JvmStatic
    fun newInstance(): RatingPositiveFragment {
      return RatingPositiveFragment()
    }
  }
}
