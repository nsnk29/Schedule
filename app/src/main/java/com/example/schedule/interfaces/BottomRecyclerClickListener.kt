package com.example.schedule.interfaces

interface BottomRecyclerClickListener {
    fun onItemClicked(position: Int, needToUpdate: Boolean = false)
}