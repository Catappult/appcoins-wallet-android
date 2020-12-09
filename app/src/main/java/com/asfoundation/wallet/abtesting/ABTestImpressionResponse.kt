package com.asfoundation.wallet.abtesting

data class ABTestImpressionResponse(val cache: Boolean, val payload: String?,
                                    val assignment: String?, val context: String?,
                                    val status: ABTestStatus)

enum class ABTestStatus {
  IMPRESSION, EXPERIMENT_OVER, EXPERIMENT_PAUSED, EXPERIMENT_NOT_FOUND, EXPERIMENT_DRAFT
}
