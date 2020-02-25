package com.example.schedule.model

data class JSONStructure(
    var pairs: List<PairClass>,
    var version: Long,
    var error: String?
)