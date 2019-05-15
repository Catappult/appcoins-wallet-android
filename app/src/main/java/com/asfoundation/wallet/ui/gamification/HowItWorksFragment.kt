package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
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
  @Inject
  lateinit var levelResourcesMapper: LevelResourcesMapper
  @Inject
  lateinit var analytics: GamificationAnalytics

  private lateinit var presenter: HowItWorksPresenter
  private lateinit var gamificationView: GamificationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = HowItWorksPresenter(this, gamificationInteractor, analytics, Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  override fun getOkClick(): Observable<Any> {
    return RxView.clicks(fragment_gamification_how_it_works_ok_button)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is GamificationView) {
      throw IllegalArgumentException(
          HowItWorksFragment::class.java.simpleName + " needs to be attached to a " + GamificationView::class.java.simpleName)
    }
    gamificationView = context
  }

  override fun showLevels(levels: List<ViewLevel>) {
    fragment_gamification_how_it_works_loading.visibility = View.GONE
    fragment_gamification_how_it_works_ok_button.visibility = View.VISIBLE
    var view: View?

    for (level in levels) {
      view = layoutInflater.inflate(R.layout.fragment_gamification_how_it_works_level,
          fragment_gamification_how_it_works_levels_layout, false)
      view.findViewById<TextView>(R.id.message).text =
          getString(R.string.gamification_how_table_a2
              , level.amount)
      view.findViewById<TextView>(R.id.bonus).text =
          getString(R.string.gamification_how_table_b2, level.bonus.toString())
      view.findViewById<ImageView>(R.id.ic_level)
          .setImageResource(levelResourcesMapper.mapDarkIcons(level))
      (fragment_gamification_how_it_works_levels_layout as LinearLayout).addView(view)
    }
  }

  override fun close() {
    gamificationView.closeHowItWorksView()
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
    gamificationView.onHowItWorksClosed()
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