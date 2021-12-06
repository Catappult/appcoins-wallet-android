package com.asfoundation.wallet.my_wallets.create_wallet

import android.animation.Animator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentCreateWalletDialogLayoutBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class CreateWalletDialogFragment : DaggerDialogFragment(),
    SingleStateFragment<CreateWalletState, CreateWalletSideEffect> {

  @Inject
  lateinit var viewModelFactory: CreateWalletDialogViewModelFactory

  @Inject
  lateinit var navigator: CreateWalletDialogNavigator

  private val viewModel: CreateWalletDialogViewModel by viewModels { viewModelFactory }
  private val views by viewBinding(FragmentCreateWalletDialogLayoutBinding::bind)

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return object : Dialog(requireContext(), theme) {
      override fun onBackPressed() {
        // Do nothing
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_create_wallet_dialog_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onDestroy() {
    super.onDestroy()
    setFragmentResult("CreateWalletDialogFragment", bundleOf("fragmentEnded" to "result"))
  }

  override fun getTheme(): Int = R.style.NoBackgroundDialog

  override fun onStateChanged(state: CreateWalletState) {
    when (state.walletCreationAsync) {
      Async.Uninitialized,
      is Async.Loading -> {
        views.walletCreationAnimation.visibility = View.VISIBLE
        views.createWalletAnimation.playAnimation()
      }
      is Async.Success -> {
        views.createWalletAnimation.setAnimation(R.raw.success_animation)
        views.createWalletText.text = getText(R.string.provide_wallet_created_header)
        views.createWalletAnimation.addAnimatorListener(object : Animator.AnimatorListener {
          override fun onAnimationRepeat(animation: Animator?) = Unit
          override fun onAnimationEnd(animation: Animator?) = navigator.navigateBack()
          override fun onAnimationCancel(animation: Animator?) = Unit
          override fun onAnimationStart(animation: Animator?) = Unit
        })
        views.createWalletAnimation.repeatCount = 0
        views.createWalletAnimation.playAnimation()
      }
      else -> Unit
    }
  }

  override fun onSideEffect(sideEffect: CreateWalletSideEffect) = Unit

}