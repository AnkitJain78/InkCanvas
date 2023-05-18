package com.example.notesapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapplication.adapter.NotesAdapter
import com.example.notesapplication.databinding.ActivityMainBinding
import com.example.notesapplication.model.Note
import com.example.notesapplication.view.AddNote
import com.example.notesapplication.view.CanvasActivity
import com.example.notesapplication.viewModel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var notesAdapter: NotesAdapter
    var noteList: List<Note> = listOf()
    private var viewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.notesRecyclerView.setHasFixedSize(true)
        binding.notesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        notesAdapter = NotesAdapter(this)
        binding.notesRecyclerView.adapter = notesAdapter
        binding.addNoteButton.setOnClickListener {
            val intent = Intent(this, AddNote::class.java)
            startActivity(intent)
        }
        viewModel.allNotes.observe(this) { allNotes ->
            noteList = allNotes
            notesAdapter.setData(allNotes)
            deleteImages(allNotes)
        }
        binding.canvas.setOnClickListener {
            val intent = Intent(this, CanvasActivity::class.java)
            startActivity(intent)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return true
            }
        })

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = notesAdapter.atPosition(viewHolder.adapterPosition)
                lifecycleScope.launch {
                    if (note.image != null) {
                        val file = Uri.parse(note.image).path?.let { File(it) }
                        file?.delete()
                    }
                    note.id?.let { viewModel.deleteNote(it) }
                }
            }
        }).attachToRecyclerView(binding.notesRecyclerView)
    }

    private fun search(query: String?) {
        val temp = ArrayList<Note>()
        noteList.forEach { item ->
            run {
                if (item.title?.contains(query.toString()) == true
                    || item.description?.contains(query.toString()) == true
                )
                    temp.add(item)
            }
        }
        notesAdapter.setData(temp)
    }

    private fun deleteImages(allNotes: List<Note>?) {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileList = dir?.listFiles()
        val filePathList = ArrayList<String>()
        val iterator = fileList?.iterator()
        while (iterator?.hasNext() == true) {
            filePathList.add(iterator.next().path)
        }
        Log.d("files", filePathList.toString())
        val imageList = ArrayList<String?>()
        val iterator2 = allNotes?.iterator()
        while (iterator2?.hasNext() == true) {
            imageList.add(iterator2.next().image)
        }
        Log.d("image", imageList.toString())
        val deleteList = filePathList - imageList.toSet()
        deleteList.forEach { item ->
            val file = item?.let { File(it) }
            if (!item.isNullOrEmpty())
                file?.delete()
        }
    }
}