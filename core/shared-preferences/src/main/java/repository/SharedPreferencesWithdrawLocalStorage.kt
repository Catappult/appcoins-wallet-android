package repository

import android.content.SharedPreferences
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = WithdrawLocalStorage::class)
class SharedPreferencesWithdrawLocalStorage @Inject constructor(
    private val preferences: SharedPreferences
) : WithdrawLocalStorage {
  companion object {
    const val WITHDRAW_EMAIL_KEY = "WITHDRAW_EMAIL_KEY"
  }

  override fun getUserEmail(): Single<String> {
    return Single.fromCallable {
      return@fromCallable preferences.getString(WITHDRAW_EMAIL_KEY, null) ?: throw RuntimeException(
          "Couldn't find user e-mail.")
    }
  }

  override fun saveUserEmail(email: String) {
    val editPreferences = preferences.edit()
    editPreferences.putString(WITHDRAW_EMAIL_KEY, email)
    editPreferences.apply()
  }
}
