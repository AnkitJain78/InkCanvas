package com.example.notesapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NoteViewModel : ViewModel() {
    private val mutableColor = MutableLiveData<Int>()
    private val mutableIsUrl = MutableLiveData<Boolean>()
    private val mutableIsDeleteNote = MutableLiveData<Boolean>()
    private val mutableCurrentPhotoPath = MutableLiveData<String>()
    val color: LiveData<Int> get() = mutableColor
    val isUrl: LiveData<Boolean> get() = mutableIsUrl
    val isDeleteNote: LiveData<Boolean> get() = mutableIsDeleteNote
    val currentPhotoPath: LiveData<String> get() = mutableCurrentPhotoPath

    fun setColor(item: Int) {
        mutableColor.value = item
    }

    fun setIsUrl(item: Boolean) {
        mutableIsUrl.value = item
    }

    fun setIsDeleteNote(item: Boolean) {
        mutableIsDeleteNote.value = item
    }

    fun setCurrentPhotoPath(item: String) {
        mutableCurrentPhotoPath.value = item
    }
}
