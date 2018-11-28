package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_gamification_how_it_works.*
import javax.inject.Inject

class HowItWorksFragment : DaggerFragment(), HowItWorksView {
  @Inject
  lateinit var gamificationInteractor: GamificationInteractor
  private lateinit var presenter: HowItWorksPresenter
  private lateinit var gamificationView: GamificationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = HowItWorksPresenter(this, gamificationInteractor, Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  override fun getOkClick(): Observable<Any> {
    return RxView.clicks(fragment_gamification_how_it_works_ok_button)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context !is GamificationView) {
      throw IllegalArgumentException(
          HowItWorksFragment::class.java.simpleName + " needs to be attached to a " + GamificationView::class.java.simpleName)
    }
    gamificationView = context
  }

  override fun showLevels(viewLevels: List<ViewLevel>) {
    Log.d(TAG, "showLevels() called with: viewLevels = [$viewLevels]")
  }

  override fun close() {
    gamificationView.close()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_gamification_how_it_works, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {
    private val TAG = HowItWorksFragment::class.java.simpleName
    @JvmStatic
    fun newInstance(): HowItWorksFragment {
      return HowItWorksFragment()
    }
  }
}