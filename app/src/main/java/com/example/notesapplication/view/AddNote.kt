package com.example.notesapplication.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.notesapplication.MainActivity
import com.example.notesapplication.databinding.ActivityAddNoteBinding
import com.example.notesapplication.model.Note
import com.example.notesapplication.viewModel.MainViewModel
import com.example.notesapplication.viewModel.NoteViewModel
import io.realm.Realm
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddNote : AppCompatActivity() {
    lateinit var binding: ActivityAddNoteBinding
    private lateinit var bottomSheet: BottomSheet
    private lateinit var viewModel: NoteViewModel
    private lateinit var realm: Realm
    private var noteColor: Int? = null
    private var noteUrl: String? = null
    private var noteImageUri: String? = null
    private var currentPhotoPath: String? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncher2: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        realm = Realm.getDefaultInstance()
        register()
        register2()
        binding.bottomNav.setOnClickListener {
            bottomSheet = BottomSheet(activityResultLauncher, activityResultLauncher2)
            bottomSheet.show(supportFragmentManager, "fragment_bottom_sheet")
        }
        viewModel.color.observe(this) { item ->
            binding.noteColour.setBackgroundResource(item)
            noteColor = item
        }
        viewModel.isUrl.observe(this) { item ->
            if (item) {
                binding.etWebLink.setText(noteUrl)
                binding.urlLayout.visibility = View.VISIBLE
            }
        }
        viewModel.currentPhotoPath.observe(this) { item ->
            currentPhotoPath = item
        }
        binding.save.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()
            saveNote(title, description, noteUrl, noteColor, noteImageUri)
        }
        binding.back.setOnClickListener {
            finish()
        }
        binding.imgDelete.setOnClickListener {
            binding.imgNote.setImageDrawable(null)
            binding.imgNote.visibility = View.GONE
            binding.imgDelete.visibility = View.GONE
            noteImageUri = null
            currentPhotoPath = null
        }
        binding.btnOk.setOnClickListener {
            noteUrl = binding.etWebLink.text.toString()
            Toast.makeText(this, "Url saved", Toast.LENGTH_SHORT).show()
            binding.tvUrl.text = noteUrl
            binding.urlLayout.visibility = View.GONE
            binding.linearLayoutUrl.visibility = View.VISIBLE
        }
        binding.btnCancel.setOnClickListener {
            binding.etWebLink.setText("")
            binding.urlLayout.visibility = View.GONE
        }
        binding.urlDelete.setOnClickListener {
            binding.linearLayoutUrl.visibility = View.GONE
            noteUrl = null
            binding.tvUrl.text = null
        }
    }

    private fun register2() {
        activityResultLauncher2 =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val resultCode = it.resultCode
                val data = it.data
                val contentResolver = applicationContext.contentResolver
                if (resultCode == RESULT_OK) {
                    val imageUri = data?.data
                    val photoFile: File? = try {
                        createCapturedPhoto()
                    } catch (ex: IOException) {
                        null
                    }
                    val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
                    val bufferedInputStream = BufferedInputStream(inputStream)
                    val outstream = FileOutputStream(photoFile)
                    outstream.write(bufferedInputStream.readBytes())
                    binding.imgNote.setImageURI(imageUri)
                    binding.imgNote.visibility = View.VISIBLE
                    binding.imgDelete.visibility = View.VISIBLE
                    noteImageUri = currentPhotoPath
                }
            }
    }

    private fun register() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val resultCode = it.resultCode
                if (resultCode == RESULT_OK) {
                    val imageUri = Uri.parse(currentPhotoPath)
                    noteImageUri = currentPhotoPath
                    binding.imgNote.setImageURI(imageUri)
                    binding.imgNote.visibility = View.VISIBLE
                    binding.imgDelete.visibility = View.VISIBLE
                }
            }
    }

    private fun saveNote(
        noteTitle: String?,
        noteDescription: String?,
        noteUrl: String?,
        noteColour: Int?,
        noteImage: String?
    ) {
        try {
            val currentId: Int? = realm.where(Note::class.java).max("id")?.toInt()
            Log.d("taggg", "$currentId")
            val nextId = if (currentId == null) 1 else currentId + 1
            val mainViewModel = MainViewModel()
            mainViewModel.addNote(
                nextId,
                noteTitle,
                noteDescription,
                noteUrl,
                noteColour,
                noteImage = noteImage
            )
            Toast.makeText(this@AddNote, "Note added", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this@AddNote, e.localizedMessage, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
            viewModel.setCurrentPhotoPath(absolutePath)
        }
    }
}