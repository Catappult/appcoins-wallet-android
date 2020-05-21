package com.asfoundation.wallet.di

import com.asfoundation.wallet.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class,
      ToolsModule::class,
      RepositoriesModule::class,
      BuildersModule::class,
      ImportModule::class,
      BroadcastReceiverModule::class])
interface AppComponent {

  fun inject(app: App?)

  @Component.Builder
  interface Builder {
    @BindsInstance
    fun application(app: App): Builder
    fun build(): AppComponent
  }
}