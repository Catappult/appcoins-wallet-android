package com.asfoundation.wallet.onboarding

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.R
import com.asfoundation.wallet.ui.webview_login.WebViewLoginActivity
import javax.inject.Inject

class OnboardingNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateToNavBar() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToNavBarFragment()
    )
  }

  fun navigateToRecover() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToRecoverWalletGraph(onboardingLayout = true)
    )
  }

  fun navigateToCreateWalletDialog(isPayment: Boolean) {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateCreateWalletDialog(
        needsWalletCreation = true,
        isPayment = isPayment
      )
    )
  }

  fun navigateToBrowser(uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      fragment.requireContext()
        .startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(fragment.requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
        .show()
    }
  }

  fun navigateToVerify(flow: String) {
    when (flow) {
      OnboardingFlow.VERIFY_PAYPAL.name -> {
        navigate(
          fragment.findNavController(),
          OnboardingFragmentDirections.actionNavigateToVerifyPaypal()
        )
      }

      OnboardingFlow.VERIFY_CREDIT_CARD.name -> {
        navigate(
          fragment.findNavController(),
          OnboardingFragmentDirections.actionNavigateToVerifyCreditCard()
        )
        fragment.requireActivity().finish()
      }
    }
  }

  fun navigateToOnboardingPayment() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToFirstPaymentFragment()
    )
  }

  fun navigateToLogin() {
//    navigate(
//      fragment.findNavController(),
//      OnboardingFragmentDirections.actionNavigateToLogin(
//        url = "https://wallet.dev.aptoide.com/pt_PT/wallet/sign-in?domain=com.appcoins.diceroll.sdk.dev"
//      )
//    )

    val url = "https://wallet.dev.aptoide.com/pt_PT/wallet/sign-in?domain=com.appcoins.diceroll.sdk.dev"
    val intent = Intent(fragment.requireContext(), WebViewLoginActivity::class.java)
    intent.putExtra(WebViewLoginActivity.URL, url)
    fragment.startActivity(intent)
  }
}