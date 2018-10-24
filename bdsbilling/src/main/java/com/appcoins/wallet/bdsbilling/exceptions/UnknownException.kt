package com.appcoins.wallet.billing.exceptions

import com.appcoins.wallet.bdsbilling.exceptions.BillingException

class UnknownException(errorCode: Int) : BillingException(errorCode)
