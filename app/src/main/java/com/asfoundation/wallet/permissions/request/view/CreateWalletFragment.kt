package com.asfoundation.wallet.permissions.request.view

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallets.WalletCreatorInteract
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_create_wallet_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class CreateWalletFragment : BasePageViewFragment(), CreateWalletView {
  companion object {
    fun newInstance() = CreateWalletFragment()
  }

  @Inject
  lateinit var interactor: WalletCreatorInteract

  private lateinit var presenter: CreateWalletPresenter
  private lateinit var navigator: CreateWalletNavigator
  private lateinit var finishAnimationFinishEvent: BehaviorRelay<Any>
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
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_create_wallet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    create_wallet_animation.removeAllAnimatorListeners()
    create_wallet_animation.removeAllUpdateListeners()
    create_wallet_animation.removeAllLottieOnCompositionLoadedListener()
    super.onDestroyView()
  }

  override fun getOnCreateWalletClick() = RxView.clicks(provide_wallet_create_wallet_button)

  override fun getCancelClick() = RxView.clicks(provide_wallet_cancel)

  override fun closeSuccess() = navigator.closeSuccess()

  override fun showFinishAnimation() {
    create_wallet_animation.setAnimation(R.raw.success_animation)
    create_wallet_text.text = getText(R.string.provide_wallet_created_header)
    create_wallet_animation.playAnimation()
    create_wallet_animation.repeatCount = 0
    create_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator) = Unit
      override fun onAnimationEnd(animation: Animator) = finishAnimationFinishEvent.accept(Any())
      override fun onAnimationCancel(animation: Animator) = Unit
      override fun onAnimationStart(animation: Animator) = Unit
    })
  }

  override fun getFinishAnimationFinishEvent(): BehaviorRelay<Any> = finishAnimationFinishEvent

  override fun closeCancel() = navigator.closeCancel()

  override fun showLoading() {
    create_wallet_group.visibility = View.INVISIBLE
    create_wallet_animation.visibility = View.VISIBLE
    create_wallet_text.visibility = View.VISIBLE
    create_wallet_animation.playAnimation()
  }
}
