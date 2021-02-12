package com.asfoundation.wallet.billing.adyen

import android.os.Bundle
import java.util.*

data class PurchaseBundleModel(val bundle: Bundle, val renewal: Date? = null)
