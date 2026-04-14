package com.andreykaranik.gpstracker.domain.model

data class ModeParameters(
    val kalmanEnabled: Boolean = false,
    val timeFrom: String = "2025-06-02 10:40:00.000",
    val timeTo: String = "2025-06-02 10:40:00.000",
    val eps: String = "0.01",
    val minPts: String = "3",
    val minIntervalMinutes: String = "0"
)