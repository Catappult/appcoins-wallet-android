package cm.aptoide.skills.usecase

import android.net.Uri
import cm.aptoide.skills.util.UriValidationResult
import cm.aptoide.skills.util.parseStartGame
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class ValidateUrlUseCase @Inject constructor(
) {

  operator fun invoke(uriString: String): UriValidationResult {
    val uri: Uri = Uri.parse(uriString)
//    Commented since this two verifications were giving problems, especially the username verification
//    if (hasInvalidRequestStructure(uriString, uri)) {
//      return UriValidationResult.Invalid(SkillsViewModel.RESULT_INVALID_URL)
//    }
//    if (usernameContainsInvalidCharacters(uri)) {
//      return UriValidationResult.Invalid(SkillsViewModel.RESULT_INVALID_USERNAME)
//    }
    val paymentData = uri.parseStartGame()
    return UriValidationResult.Valid(paymentData)
  }

  private fun usernameContainsInvalidCharacters(eskillsUri: Uri): Boolean {
    val pattern = Pattern.compile("[^A-Za-z0-9_ ]+")
    val username = eskillsUri.getQueryParameter("user_name")!!
    val matcher: Matcher = pattern.matcher(username)
    return matcher.find()

  }

  private fun hasInvalidRequestStructure(uriString: String, parsedUri: Uri): Boolean {
    val parametersString = uriString.split("?")[1]
    if (parametersString.contains("#")) {
      return true
    }
    val parametersArray = parametersString.split("&")
    return parsedUri.queryParameterNames.size != parametersArray.size
  }
}