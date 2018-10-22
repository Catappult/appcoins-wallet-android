package com.appcoins.wallet.billing.exceptions

import com.appcoins.wallet.bdsbilling.exceptions.BillingException

class ServiceUnavailableException(errorCode: Int) : BillingException(errorCode)
