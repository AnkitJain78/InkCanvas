package com.example.notesapplication.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapplication.MainActivity
import com.example.notesapplication.databinding.ItemNoteBinding
import com.example.notesapplication.model.Note
import com.example.notesapplication.view.CanvasActivity
import com.example.notesapplication.view.UpdateNote

class NotesAdapter(private val activity: MainActivity) :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    lateinit var notesList: List<Note>

    class NotesViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val currentNote: Note = notesList[position]
        currentNote.image?.let {
            holder.binding.noteImage.setImageURI(Uri.parse(it))
            holder.binding.noteImage.visibility = View.VISIBLE
        }
        holder.binding.textViewTitle.text = currentNote.title
        holder.binding.textViewDescription.text = currentNote.description
        holder.binding.textViewTitle.visibility =
            if (currentNote.title.isNullOrEmpty()) View.GONE else View.VISIBLE
        holder.binding.textViewDescription.visibility =
            if (currentNote.description.isNullOrEmpty()) View.GONE else View.VISIBLE

        currentNote.color?.let {
            holder.binding.cardView.strokeColor =
                ContextCompat.getColor(activity.applicationContext, it)
        }
        holder.binding.dateTime.text = currentNote.dateTime
        holder.binding.cardView.setOnClickListener {
            if (currentNote.bitmap != null) {
                val intent = Intent(activity, CanvasActivity::class.java)
                intent.putExtra("bitmapToUpdate", currentNote.bitmap)
                activity.startActivity(intent)
            } else {
                val intent = Intent(activity, UpdateNote::class.java)
                intent.putExtra("titleToUpdate", currentNote.title)
                intent.putExtra("descriptionToUpdate", currentNote.description)
                intent.putExtra("id", currentNote.id)
                intent.putExtra("linkToUpdate", currentNote.link)
                intent.putExtra("imageToUpdate", currentNote.image)
                intent.putExtra("colourToUpdate", currentNote.color)
                activity.startActivity(intent)
        }
        }

    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    fun atPosition(position: Int): Note {
        return notesList[position]
    }

    fun setData(notesList: List<Note>) {
        this.notesList = notesList
        notifyDataSetChanged()
    }
}