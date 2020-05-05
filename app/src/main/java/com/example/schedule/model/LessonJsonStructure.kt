package com.example.schedule.model

data class LessonJsonStructure(
    var pairs: List<PairClass>,
    var version: Long?,
    var error: String?
)