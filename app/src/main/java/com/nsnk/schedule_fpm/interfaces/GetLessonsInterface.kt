package com.nsnk.schedule_fpm.interfaces

import com.nsnk.schedule_fpm.model.LessonJsonStructure

interface GetLessonsInterface {
    fun onLessonsReady(lessonJsonStructure: LessonJsonStructure)
}