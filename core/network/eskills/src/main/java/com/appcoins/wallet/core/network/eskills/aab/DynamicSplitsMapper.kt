package cm.aptoide.pt.aab

import com.appcoins.wallet.core.network.eskills.aab.DynamicSplitsResponse

class DynamicSplitsMapper {

  fun mapDynamicSplits(
      serverDynamicSplitsList: List<DynamicSplitsResponse.DynamicSplit>): List<DynamicSplit> {

    return serverDynamicSplitsList.map {
      DynamicSplit(it.name, it.type, it.md5sum, it.path, it.filesize, it.deliveryTypes,
          mapDynamicConfigSplits(it.splits))
    }
  }

  private fun mapDynamicConfigSplits(
      splits: List<com.appcoins.wallet.core.network.eskills.aab.v7.Split>): List<Split> {
    return splits.map { Split(it.name, it.type, it.path, it.filesize, it.md5sum) }
  }
}