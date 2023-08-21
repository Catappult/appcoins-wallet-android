package com.asfoundation.wallet.rating.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.databinding.FragmentRatingEntryBinding
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class RatingEntryFragment : BasePageViewFragment(), RatingEntryView {

  @Inject
  lateinit var presenter: RatingEntryPresenter

  private val views by viewBinding(FragmentRatingEntryBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentRatingEntryBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.animation.setMaxFrame(97)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun yesClickEvent(): Observable<Any> {
    return RxView.clicks(views.yesButton)
  }

  override fun noClickEvent(): Observable<Any> {
    return RxView.clicks(views.noButton)
  }

  companion object {

    @JvmStatic
    fun newInstance(): RatingEntryFragment {
      return RatingEntryFragment()
    }
  }
}
