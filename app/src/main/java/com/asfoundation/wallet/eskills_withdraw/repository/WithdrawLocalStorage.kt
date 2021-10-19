package com.asfoundation.wallet.eskills_withdraw.repository

import io.reactivex.Single

interface WithdrawLocalStorage {
  fun getUserEmail(): Single<String>
  fun saveUserEmail(email: String)
}
