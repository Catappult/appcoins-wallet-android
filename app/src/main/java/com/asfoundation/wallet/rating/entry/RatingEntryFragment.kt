package com.asfoundation.wallet.rating.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_rating_entry.*
import javax.inject.Inject

@AndroidEntryPoint
class RatingEntryFragment : BasePageViewFragment(), RatingEntryView {

  @Inject
  lateinit var presenter: RatingEntryPresenter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_rating_entry, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    animation.setMaxFrame(97)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun yesClickEvent(): Observable<Any> {
    return RxView.clicks(yes_button)
  }

  override fun noClickEvent(): Observable<Any> {
    return RxView.clicks(no_button)
  }

  companion object {

    @JvmStatic
    fun newInstance(): RatingEntryFragment {
      return RatingEntryFragment()
    }
  }
}
