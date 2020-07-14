package com.asfoundation.wallet.wallet_validation.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_validation_loading.*
import javax.inject.Inject

class ValidationLoadingDialogFragment : DaggerFragment(), ValidationLoadingDialogView {

  companion object {
    @JvmStatic
    fun newInstance(validationInfo: ValidationInfo): ValidationLoadingDialogFragment {
      val bundle = Bundle().apply {
        putSerializable(VALIDATION, validationInfo)
      }
      return ValidationLoadingDialogFragment().apply { arguments = bundle }
    }

    private const val VALIDATION = "validation"
  }

  @Inject
  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract

  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract

  @Inject
  lateinit var analytics: WalletValidationAnalytics

  private lateinit var presenter: ValidationLoadingDialogPresenter

  private lateinit var walletValidationDialogView: WalletValidationDialogView

  val data: ValidationInfo by lazy {
    if (arguments!!.containsKey(VALIDATION)) {
      arguments!!.getSerializable(VALIDATION) as ValidationInfo
    } else {
      throw IllegalArgumentException("previous validation info not found")
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is WalletValidationDialogView) {
      throw IllegalStateException(
          "Express checkout buy fragment must be attached to IAB activity")
    }
    walletValidationDialogView = context
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        ValidationLoadingDialogPresenter(this, walletValidationDialogView,
            findDefaultWalletInteract,
            smsValidationInteract, data, AndroidSchedulers.mainThread(), Schedulers.io(),
            CompositeDisposable(), analytics)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_validation_loading, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun show() {
    validation_loading_animation.setAnimation(R.raw.transact_loading_animation)
    validation_loading_animation.playAnimation()
  }

  override fun clean() {
    validation_loading_animation.removeAllAnimatorListeners()
    validation_loading_animation.removeAllUpdateListeners()
    validation_loading_animation.removeAllLottieOnCompositionLoadedListener()
  }

}