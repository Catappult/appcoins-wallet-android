package com.asfoundation.wallet.ui.iab

import com.asf.wallet.R

enum class ExternalAppEnum(val appName: String, val uriScheme: String, val appIcon: Int, val color: Int, val marketUri: String, val googlePlayUrl: String) {
  GOJEK(
    "Gojek",
    "gojek://",
    R.drawable.ic_gojek,
    R.color.gojek_green,
    "market://details?id=com.gojek.app",
    "https://play.google.com/store/apps/details?id=com.gojek.app"
  ),
  PHONEPE(
    "PhonePe",
    "phonepe://",
    R.drawable.ic_phonepe,
    R.color.styleguide_blue,
    "market://details?id=com.phonepe.app",
    "https://play.google.com/store/apps/details?id=com.phonepe.app"
  ),
  PAYTM(
    "PayTM UPI",
    "paytmmp://",
    R.drawable.ic_paytm,
    R.color.styleguide_blue,
    "market://details?id=net.one97.paytm",
    "https://play.google.com/store/apps/details?id=net.one97.paytm"
  ),
  BHIM(
    "BHIM",
    "bhim://",
    R.drawable.ic_bhim,
    R.color.styleguide_blue,
    "market://details?id=in.org.npci.upiapp",
    "https://play.google.com/store/apps/details?id=in.org.npci.upiapp"
  ),
}