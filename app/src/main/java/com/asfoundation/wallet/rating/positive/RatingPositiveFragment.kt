package com.asfoundation.wallet.rating.positive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

  private var _binding: FragmentRatingPositiveBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val title get() = binding.title
  private val description get() = binding.description
  private val animation get() = binding.animation
  private val rate_app_button get() = binding.rateAppButton
  private val remind_me_later_button get() = binding.remindMeLaterButton
  private val no_button get() = binding.noButton

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    _binding = FragmentRatingPositiveBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun initializeView(isNotFirstTime: Boolean) {
    if (isNotFirstTime) {
      title.setText(R.string.rate_us_back_title)
      description.setText(R.string.rate_us_back_body)
      animation.setMinFrame(196)
      animation.setMaxFrame(196)
    } else {
      animation.setMinFrame(97)
      animation.setMaxFrame(196)
      animation.playAnimation()
    }
  }

  override fun rateAppClickEvent(): Observable<Any> {
    return RxView.clicks(rate_app_button)
  }

  override fun remindMeLaterClickEvent(): Observable<Any> {
    return RxView.clicks(remind_me_later_button)
  }

  override fun noClickEvent(): Observable<Any> {
    return RxView.clicks(no_button)
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
