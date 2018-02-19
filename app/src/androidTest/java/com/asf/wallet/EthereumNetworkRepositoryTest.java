package com.asf.wallet;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.asf.wallet.repository.EthereumNetworkRepository;
import com.asf.wallet.repository.EthereumNetworkRepositoryType;
import com.asf.wallet.repository.PreferenceRepositoryType;
import com.asf.wallet.repository.SharedPreferenceRepository;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class) public class EthereumNetworkRepositoryTest {

  private EthereumNetworkRepositoryType networkRepository;

  @Before public void setUp() {
    Context context = InstrumentationRegistry.getTargetContext();
    PreferenceRepositoryType preferenceRepositoryType = new SharedPreferenceRepository(context);
    networkRepository = new EthereumNetworkRepository(preferenceRepositoryType);
  }
}
