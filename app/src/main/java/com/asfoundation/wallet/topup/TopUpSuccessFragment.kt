package com.asfoundation.wallet.topup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_top_up_success.*


/**
 * Created by Joao Raimundo on 04/03/2019.
 */
class TopUpSuccessFragment : DaggerFragment() {
  companion object {
    @JvmStatic
    fun newInstance(amount: Double): TopUpSuccessFragment {
      val fragment = TopUpSuccessFragment()
      val bundle = Bundle()
      bundle.putDouble("amount", amount)
      fragment.arguments = bundle
      return fragment
    }
  }

  private lateinit var topUpActivityView: TopUpActivityView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TopUpActivityView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    topUpActivityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_top_up_success, container, false);
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    top_up_success_animation.setAnimation(R.raw.top_up_success_animation)
    top_up_success_animation.playAnimation()
    top_up_success_animation.repeatCount = 0

    button.setOnClickListener { topUpActivityView.close() }
  }

  override fun onDestroy() {
    super.onDestroy()
    top_up_success_animation.removeAllAnimatorListeners()
    top_up_success_animation.removeAllUpdateListeners()
    top_up_success_animation.removeAllLottieOnCompositionLoadedListener()
  }

}