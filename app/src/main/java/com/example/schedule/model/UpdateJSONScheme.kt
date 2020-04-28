package com.example.schedule.model

data class UpdateJSONScheme(
    val version: Int = 0,
    val link: String = "",
    val size: String = "",
    val description: String = "",
    val upload_time: Long = 0
)