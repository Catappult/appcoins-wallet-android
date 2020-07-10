package com.asfoundation.wallet.wallet_validation.dialog

import android.animation.Animator
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.advertise.WalletPoAService.VERIFICATION_SERVICE_ID
import com.asfoundation.wallet.poa.ProofOfAttentionService
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.fragment_validation_success.*
import javax.inject.Inject

class ValidationSuccessDialogFragment : DaggerFragment(), ValidationSuccessDialogView {

  @Inject
  lateinit var proofOfAttentionService: ProofOfAttentionService

  private lateinit var walletValidationDialogView: WalletValidationDialogView
  private lateinit var dialogPresenter: ValidationSuccessDialogPresenter
  private lateinit var notificationManager: NotificationManager

  private lateinit var animationCompleted: Subject<Boolean>

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is WalletValidationDialogView) {
      throw IllegalStateException(
          "Validation Success fragment must be attached to Wallet Validation Activity")
    }
    walletValidationDialogView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    notificationManager =
        context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    animationCompleted = BehaviorSubject.create()

    dialogPresenter =
        ValidationSuccessDialogPresenter(this, proofOfAttentionService, CompositeDisposable(),
            walletValidationDialogView)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_validation_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    dialogPresenter.present()
  }

  override fun onDestroyView() {
    dialogPresenter.stop()
    super.onDestroyView()
  }

  override fun setupUI() {
    validation_success_animation.setAnimation(R.raw.success_animation)
    validation_success_animation.playAnimation()
    validation_success_animation.repeatCount = 0
    validation_success_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
        animationCompleted.onNext(true)
        notificationManager.cancel(VERIFICATION_SERVICE_ID)
        walletValidationDialogView.closeSuccess()
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
  }

  override fun handleAnimationEnd(): Observable<Boolean> {
    return animationCompleted
  }

  override fun clean() {
    validation_success_animation.removeAllAnimatorListeners()
    validation_success_animation.removeAllUpdateListeners()
    validation_success_animation.removeAllLottieOnCompositionLoadedListener()
  }

  companion object {
    @JvmStatic
    fun newInstance(): ValidationSuccessDialogFragment {
      return ValidationSuccessDialogFragment()
    }
  }

}