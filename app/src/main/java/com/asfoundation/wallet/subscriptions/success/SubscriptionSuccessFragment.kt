package com.asfoundation.wallet.subscriptions.success

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentSubscriptionCancelSuccessBinding
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionSuccessFragment : BasePageViewFragment(), SubscriptionSuccessView {

  @Inject
  lateinit var presenter: SubscriptionSuccessPresenter

  private val binding by viewBinding(FragmentSubscriptionCancelSuccessBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentSubscriptionCancelSuccessBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setBackListener(view)
    presenter.present()
  }

  override fun setupUi(successType: SubscriptionSuccess) {
    when (successType) {
      SubscriptionSuccess.CANCEL -> {
        binding.successAnimation.setAnimation(R.raw.subscription_cancel_success)
        binding.updateTitle.text = getString(R.string.subscriptions_cancel_confirmation_title)
      }
      SubscriptionSuccess.RENEW -> {
        binding.successAnimation.setAnimation(R.raw.success_animation)
        binding.updateTitle.text = getString(R.string.subscriptions_renewed_confirmation_title)
      }
    }
  }

  private fun setBackListener(view: View) {
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          presenter.navigateToListSubscriptions()
        }
        true
      }
    }
  }

  override fun getContinueClicks(): Observable<Any> = RxView.clicks(binding.continueButton)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  enum class SubscriptionSuccess { CANCEL, RENEW }

  companion object {
    const val SUCCESS_TYPE_KEY = "subscription_success_key"

    fun newInstance(successType: SubscriptionSuccess): SubscriptionSuccessFragment {
      return SubscriptionSuccessFragment().apply {
        arguments = Bundle().apply {
          putSerializable(SUCCESS_TYPE_KEY, successType)
        }
      }
    }
  }
}