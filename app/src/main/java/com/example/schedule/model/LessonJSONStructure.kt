package com.example.schedule.model

data class LessonJSONStructure(
    var pairs: List<PairClass>,
    var version: Long,
    var error: String?
)