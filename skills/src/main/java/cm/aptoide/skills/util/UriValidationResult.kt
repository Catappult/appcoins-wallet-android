package cm.aptoide.skills.util

import android.net.Uri

sealed class UriValidationResult {
  data class Valid(val uri: Uri) : UriValidationResult()
  data class Invalid(val requestCode: Int) : UriValidationResult()
}