package com.asf.wallet.di;

import com.asf.wallet.App;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import javax.inject.Singleton;

@Singleton @Component(modules = {
    AndroidSupportInjectionModule.class, ToolsModule.class, RepositoriesModule.class,
    BuildersModule.class
}) public interface AppComponent {

  void inject(App app);

  @Component.Builder interface Builder {
    @BindsInstance Builder application(App app);

    AppComponent build();
  }
}
