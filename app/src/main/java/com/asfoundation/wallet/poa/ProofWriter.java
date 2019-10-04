package com.asfoundation.wallet.poa;

import io.reactivex.Single;

public interface ProofWriter {
  Single<String> writeProof(Proof proof);

  Single<ProofSubmissionData> hasWalletPrepared(int chainId, String packageName, int versionCode);

  Single<PoaInformationModel> retrievePoaInformation(String address);
}

