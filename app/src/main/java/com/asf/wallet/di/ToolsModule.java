package com.asf.wallet.di;

import android.content.Context;
import com.asf.wallet.App;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.TrustPasswordStore;
import com.asf.wallet.service.RealmManager;
import com.asf.wallet.util.LogInterceptor;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module class ToolsModule {
  @Provides Context provideContext(App application) {
    return application.getApplicationContext();
  }

  @Singleton @Provides Gson provideGson() {
    return new Gson();
  }

  @Singleton @Provides OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder().addInterceptor(new LogInterceptor())
        .connectTimeout(15, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .build();
  }

  @Singleton @Provides PasswordStore passwordStore(Context context) {
    return new TrustPasswordStore(context);
  }

  @Singleton @Provides RealmManager provideRealmManager() {
    return new RealmManager();
  }
}
