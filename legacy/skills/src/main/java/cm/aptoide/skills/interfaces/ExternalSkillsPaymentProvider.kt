package cm.aptoide.skills.interfaces

import android.content.Context
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.PaymentResult
import cm.aptoide.skills.model.Price
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
import io.reactivex.Single
import java.math.BigDecimal

interface ExternalSkillsPaymentProvider {
  fun getBalance(): Single<BigDecimal>
  fun pay(eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket): Single<PaymentResult>
  fun getLocalFiatAmount(value: BigDecimal, currency: String): Single<Price>
  fun getFiatToAppcAmount(value: BigDecimal, currency: String): Single<Price>
  fun getFormattedAppcAmount(value: BigDecimal, currency: String): Single<String>
  fun sendUserToTopUpFlow(context: Context)
  fun sendUserToVerificationFlow(context: Context)
  fun isWalletVerified(): Single<Boolean>
}
