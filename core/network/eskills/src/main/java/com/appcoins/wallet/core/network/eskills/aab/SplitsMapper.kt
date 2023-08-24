package cm.aptoide.pt.aab

class SplitsMapper {

  fun mapSplits(splits: List<com.appcoins.wallet.core.network.eskills.aab.v7.Split>?): List<Split> {
    val splitsMapResult = ArrayList<Split>()
    if (splits == null) return splitsMapResult
    for (split in splits) {
      splitsMapResult.add(
        Split(
          split.name, split.type, split.path, split.filesize,
          split.md5sum
        )
      )
    }

    return splitsMapResult
  }

}