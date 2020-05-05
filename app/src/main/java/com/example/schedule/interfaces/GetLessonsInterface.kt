package com.example.schedule.interfaces

import com.example.schedule.model.LessonJsonStructure

interface GetLessonsInterface {
    fun onLessonsReady(lessonJsonStructure: LessonJsonStructure)
}