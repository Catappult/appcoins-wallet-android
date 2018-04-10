package com.asfoundation.wallet.poa;

import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.GasSettingsRepositoryType;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.service.AccountKeystoreService;
import io.reactivex.Single;
import java.io.IOException;
import java.math.BigDecimal;
import org.web3j.protocol.core.DefaultBlockParameterName;

public class TransactionFactory {
  private final Web3jProvider web3jProvider;
  private final WalletRepositoryType walletRepositoryType;
  private final GasSettingsRepositoryType gasSettingsRepository;
  private final AccountKeystoreService accountKeystoreService;
  private final PasswordStore passwordStore;
  private final DefaultTokenProvider defaultTokenProvider;
  private final EthereumNetworkRepositoryType networkRepositoryType;
  private final DataMapper dataMapper;

  public TransactionFactory(Web3jProvider web3jProvider, WalletRepositoryType walletRepositoryType,
      GasSettingsRepositoryType gasSettingsRepository,
      AccountKeystoreService accountKeystoreService, PasswordStore passwordStore,
      DefaultTokenProvider defaultTokenProvider,
      EthereumNetworkRepositoryType networkRepositoryType, DataMapper dataMapper) {
    this.web3jProvider = web3jProvider;
    this.walletRepositoryType = walletRepositoryType;
    this.gasSettingsRepository = gasSettingsRepository;
    this.accountKeystoreService = accountKeystoreService;
    this.passwordStore = passwordStore;
    this.defaultTokenProvider = defaultTokenProvider;
    this.networkRepositoryType = networkRepositoryType;
    this.dataMapper = dataMapper;
  }

  public Single<byte[]> createProofTransaction(String proof) {
    return defaultTokenProvider.getAdsAddress()
        .flatMap(adsAddress -> walletRepositoryType.getDefaultWallet()
            .flatMap(wallet -> gasSettingsRepository.getGasSettings(true)
                .flatMap(gasSettings -> passwordStore.getPassword(wallet)
                    .flatMap(
                        password -> accountKeystoreService.signTransaction(wallet.address, password,
                            adsAddress, BigDecimal.ZERO, gasSettings.gasPrice, gasSettings.gasLimit,
                            getNonce(wallet.address), dataMapper.getData(proof),
                            networkRepositoryType.getDefaultNetwork().chainId)))));
  }

  private long getNonce(String address) throws IOException {
    return web3jProvider.get()
        .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
        .send()
        .getTransactionCount()
        .longValue();
  }
}
