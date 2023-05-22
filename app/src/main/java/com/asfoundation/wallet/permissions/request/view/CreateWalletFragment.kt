package com.asfoundation.wallet.permissions.request.view

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentCreateWalletLayoutBinding
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.appcoins.wallet.feature.walletInfo.data.WalletCreatorInteract
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class CreateWalletFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), CreateWalletView {
  companion object {
    fun newInstance() = CreateWalletFragment()
  }

  @Inject
  lateinit var interactor: com.appcoins.wallet.feature.walletInfo.data.WalletCreatorInteract

  private lateinit var presenter: CreateWalletPresenter
  private lateinit var navigator: CreateWalletNavigator
  private lateinit var finishAnimationFinishEvent: BehaviorRelay<Any>

  private val views by viewBinding(FragmentCreateWalletLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = CreateWalletPresenter(this, CompositeDisposable(), interactor,
        AndroidSchedulers.mainThread())
    finishAnimationFinishEvent = BehaviorRelay.create()
  }


  override fun onAttach(context: Context) {
    super.onAttach(context)
    when (context) {
      is CreateWalletNavigator -> navigator = context
      else -> throw IllegalArgumentException(
          "${CreateWalletFragment::class} has to be attached to an activity that implements ${CreateWalletNavigator::class}")
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentCreateWalletLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    views.createWalletAnimation.removeAllAnimatorListeners()
    views.createWalletAnimation.removeAllUpdateListeners()
    views.createWalletAnimation.removeAllLottieOnCompositionLoadedListener()
    super.onDestroyView()
  }

  override fun getOnCreateWalletClick() = RxView.clicks(views.provideWalletCreateWalletButton)

  override fun getCancelClick() = RxView.clicks(views.provideWalletCancel)

  override fun closeSuccess() = navigator.closeSuccess()

  override fun showFinishAnimation() {
    views.createWalletAnimation.setAnimation(R.raw.success_animation)
    views.createWalletText.text = getText(R.string.provide_wallet_created_header)
    views.createWalletAnimation.playAnimation()
    views.createWalletAnimation.repeatCount = 0
    views.createWalletAnimation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator) = Unit
      override fun onAnimationEnd(animation: Animator) = finishAnimationFinishEvent.accept(Any())
      override fun onAnimationCancel(animation: Animator) = Unit
      override fun onAnimationStart(animation: Animator) = Unit
    })
  }

  override fun getFinishAnimationFinishEvent(): BehaviorRelay<Any> = finishAnimationFinishEvent

  override fun closeCancel() = navigator.closeCancel()

  override fun showLoading() {
    views.createWalletGroup.visibility = View.INVISIBLE
    views.createWalletAnimation.visibility = View.VISIBLE
    views.createWalletText.visibility = View.VISIBLE
    views.createWalletAnimation.playAnimation()
  }
}
