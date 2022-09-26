package com.asfoundation.wallet.di.annotations

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
annotation class BackendBlockchainRetrofit

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
annotation class BackendDefaultRetrofit

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FIELD,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.VALUE_PARAMETER
)
annotation class BackendShortTimeoutRetrofit


