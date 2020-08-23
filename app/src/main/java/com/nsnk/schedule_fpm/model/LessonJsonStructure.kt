package com.nsnk.schedule_fpm.model

data class LessonJsonStructure(
    var pairs: List<PairClass>,
    var version: Long?,
    var error: String?
)