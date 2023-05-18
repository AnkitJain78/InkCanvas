package com.example.notesapplication.model

import android.graphics.Bitmap
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Note(
    @PrimaryKey
    var id: Int? = null,
    var title: String? = null,
    var description: String? = null,
    var link: String? = null,
    var color: Int? = null,
    var dateTime: String? = null,
    var image: String? = null,
    var bitmap: String? = null
) : RealmObject()