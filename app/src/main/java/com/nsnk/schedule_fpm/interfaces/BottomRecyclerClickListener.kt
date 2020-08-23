package com.nsnk.schedule_fpm.interfaces

interface BottomRecyclerClickListener {
    fun onItemClicked(position: Int, needToUpdate: Boolean = false)
}