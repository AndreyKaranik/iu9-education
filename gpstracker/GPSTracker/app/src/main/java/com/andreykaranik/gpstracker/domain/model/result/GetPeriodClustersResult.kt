package com.andreykaranik.gpstracker.domain.model.result

import com.andreykaranik.gpstracker.domain.model.ClusterCenter

sealed class GetPeriodClustersResult {
    data class Success(
        val centers: List<ClusterCenter>
    ) : GetPeriodClustersResult()
    object Unauthorized : GetPeriodClustersResult()
    object AreNotInSameGroup : GetPeriodClustersResult()
    object Failure : GetPeriodClustersResult()
    object Pending : GetPeriodClustersResult()
    object None : GetPeriodClustersResult()
}