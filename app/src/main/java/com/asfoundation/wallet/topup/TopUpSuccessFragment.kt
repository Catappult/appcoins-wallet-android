package com.asfoundation.wallet.topup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_top_up_success.*
import java.math.BigDecimal
import java.util.*

class TopUpSuccessFragment : DaggerFragment(), TopUpSuccessFragmentView {

  companion object {
    @JvmStatic
    fun newInstance(amount: Double): TopUpSuccessFragment {
      val fragment = TopUpSuccessFragment()
      val bundle = Bundle()
      bundle.putDouble(PARAM_AMOUNT, amount)
      fragment.arguments = bundle
      return fragment
    }

    private const val PARAM_AMOUNT = "amount"
  }

  private lateinit var presenter: TopUpSuccessPresenter

  private lateinit var topUpActivityView: TopUpActivityView
  val amount: String? by lazy {
      if (arguments!!.containsKey(PARAM_AMOUNT)) {
        arguments!!.getDouble(PARAM_AMOUNT).toString()
      } else {
        throw IllegalArgumentException("product name not found")
      }
    }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TopUpActivityView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    topUpActivityView = context
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = TopUpSuccessPresenter(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_top_up_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    presenter.present()
    topUpActivityView.showToolbar()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun show() {
    top_up_success_animation.setAnimation(R.raw.top_up_success_animation)
    top_up_success_animation.playAnimation()
    top_up_success_animation.repeatCount = 0

    val formatter = Formatter()
    val appcValue = formatter.format(Locale.getDefault(), "%(,.2f",
        BigDecimal(amount).toDouble()).toString()
    value.text = String.format("%s APPC Credits", appcValue)
  }

  override fun clean() {
    top_up_success_animation.removeAllAnimatorListeners()
    top_up_success_animation.removeAllUpdateListeners()
    top_up_success_animation.removeAllLottieOnCompositionLoadedListener()
  }


  override fun close() {
    topUpActivityView.close()
  }

  override fun getOKClicks(): Observable<Any> {
    return RxView.clicks(button)
  }

}