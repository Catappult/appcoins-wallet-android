package com.asfoundation.wallet.iab.di

import com.asfoundation.wallet.iab.parser.UriParser
import com.asfoundation.wallet.iab.parser.UriParserImpl
import com.asfoundation.wallet.iab.parser.osp.OSPUriParserImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Qualifier

@Module
@InstallIn(ActivityComponent::class)
interface IAPModule {

  @Binds
  @GenericUriParser
  fun bindUriParser(uriParserImpl: UriParserImpl): UriParser

  @Binds
  @OSPUriParser
  fun bindOSPUriParser(ospParserImpl: OSPUriParserImpl): UriParser
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GenericUriParser

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OSPUriParser
