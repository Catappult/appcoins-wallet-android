package com.asfoundation.wallet.main.splash

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.SplashExtenderFragmentBinding
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.util.RxBus
import com.asfoundation.wallet.main.splash.bus.SplashFinishEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashExtenderFragment : Fragment(),
  com.appcoins.wallet.ui.arch.SingleStateFragment<SplashExtenderState, SplashExtenderSideEffect> {

  private val viewModel: SplashExtenderViewModel by viewModels()
  private val views by viewBinding(SplashExtenderFragmentBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.splash_extender_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: SplashExtenderState) = Unit

  override fun onSideEffect(sideEffect: SplashExtenderSideEffect) {
    when (sideEffect) {
      is SplashExtenderSideEffect.ShowVipAnimation -> {
        showVipAnimation(sideEffect.shouldShow)
      }
    }
  }

  private fun showVipAnimation(shouldShowVipAnimation: Boolean) {
    if (shouldShowVipAnimation) {
      views.splashVipAnimation.visibility = View.VISIBLE
      views.splashLogo.visibility = View.GONE

      views.splashVipAnimation.playAnimation()
      views.splashVipAnimation.addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) = Unit
        override fun onAnimationEnd(animation: Animator?) = finishSplash()
        override fun onAnimationCancel(animation: Animator?) = Unit
        override fun onAnimationStart(animation: Animator?) = Unit
      })
    } else {
      finishSplash()
    }
  }

  fun finishSplash() {
    RxBus.publish(SplashFinishEvent())
  }
}

