package com.example.notesapplication.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapplication.model.Note
import com.example.notesapplication.view.DrawingView
import io.realm.Realm
import java.text.DateFormat
import java.util.*

class MainViewModel : ViewModel() {
    private var realm = Realm.getDefaultInstance()

    val allNotes: LiveData<List<Note>>
        get() = getAllNotes()

    fun addNote(
        id: Int,
        noteTitle: String? = null,
        noteDescription: String? = null,
        noteLink: String? = null,
        noteColor: Int? = null,
        noteImage: String? = null,
        bitmap: String? = null
    ) {
        realm.executeTransaction {
            val simpleDate = DateFormat.getDateTimeInstance()
            val currentDate = simpleDate.format(Date())
            val note = Note(
                id,
                noteTitle,
                noteDescription,
                noteLink,
                noteColor,
                currentDate,
                noteImage,
                bitmap
            )
            realm.insertOrUpdate(note)
        }
    }

    private fun getAllNotes(): MutableLiveData<List<Note>> {
        val list = MutableLiveData<List<Note>>()
        val notes = realm.where(Note::class.java).findAll()
        list.value = notes?.subList(0, notes.size)
        return list
    }

    fun updateNote(
        id: Int?,
        noteTitle: String? = null,
        noteDescription: String? = null,
        noteLink: String? = null,
        noteColor: Int? = null,
        noteImage: String? = null,
        bitmap: String? = null
    ) {
        val simpleDate = DateFormat.getDateTimeInstance()
        val currentDate = simpleDate.format(Date())
        val target = realm.where(Note::class.java)
            .equalTo("id", id)
            .findFirst()

        realm.executeTransaction {
            target?.title = noteTitle
            target?.description = noteDescription
            target?.link = noteLink
            target?.color = noteColor
            target?.image = noteImage
            target?.bitmap = bitmap
            target?.dateTime = currentDate
            if (target != null) {
                realm.insertOrUpdate(target)
            }
        }
    }

    fun deleteNote(id: Int) {
        val notes = realm.where(Note::class.java)
            .equalTo("id", id)
            .findFirst()

        realm.executeTransaction {
            notes!!.deleteFromRealm()
        }
    }

}
