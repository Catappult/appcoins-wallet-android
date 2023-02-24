package cm.aptoide.skills.usecase

import android.content.Intent
import javax.inject.Inject

class BuildShareReferralIntentUseCase @Inject constructor(
) {
  operator fun invoke(referralCode: String): Intent {
    val shareIntent = Intent.createChooser(Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, referralCode)
      type = "text/plain"
    }, "Share referral")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return shareIntent
  }
}