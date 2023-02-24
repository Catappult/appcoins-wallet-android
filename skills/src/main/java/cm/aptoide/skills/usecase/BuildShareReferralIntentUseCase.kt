package cm.aptoide.skills.usecase

import android.content.Intent
import javax.inject.Inject

class BuildShareReferralIntentUseCase @Inject constructor(
) {
  val sendIntent: Intent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
    type = "text/plain"
  }

  operator fun invoke(): Intent {
    val shareIntent = Intent.createChooser(sendIntent, "Share referral")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return shareIntent
  }
}