package com.andreykaranik.gpstracker.data.api.response

import com.andreykaranik.gpstracker.domain.model.ClusterCenter

data class GetPeriodClustersResponse (
    val centers: List<ClusterCenter>?
)