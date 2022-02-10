package cm.aptoide.skills.interfaces

import android.content.Context
import android.content.Intent

interface ExternalAuthenticationProvider {
  fun hasAuthenticationPermission(): Boolean
  fun getAuthenticationIntent(context: Context): Intent
}
