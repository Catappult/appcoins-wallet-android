package com.asfoundation.wallet.wallet_validation

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
import kotlinx.android.synthetic.main.fragment_validation_success.*
import javax.inject.Inject

class ValidationSuccessFragment : DaggerFragment(), ValidationSuccessFragmentView {

  @Inject
  lateinit var proofOfAttentionService: ProofOfAttentionService

  private lateinit var walletValidationActivityView: WalletValidationActivityView
  private lateinit var presenter: ValidationSuccessPresenter
  private lateinit var notificationManager: NotificationManager

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is WalletValidationActivityView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    walletValidationActivityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    notificationManager =
        context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    presenter =
        ValidationSuccessPresenter(this, proofOfAttentionService)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_validation_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun setupUI() {
    validation_success_animation.setAnimation(R.raw.top_up_success_animation)
    validation_success_animation.playAnimation()
    validation_success_animation.repeatCount = 0
    validation_success_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
        presenter.updatePoA()
        notificationManager.cancel(VERIFICATION_SERVICE_ID)
        walletValidationActivityView.finish()
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
  }

  override fun clean() {
    validation_success_animation.removeAllAnimatorListeners()
    validation_success_animation.removeAllUpdateListeners()
    validation_success_animation.removeAllLottieOnCompositionLoadedListener()
  }

  fun close() {
    walletValidationActivityView.close(null)
  }

  companion object {
    @JvmStatic
    fun newInstance(): ValidationSuccessFragment {
      return ValidationSuccessFragment()
    }
  }

}