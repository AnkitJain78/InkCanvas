package com.example.notesapplication.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.notesapplication.MainActivity
import com.example.notesapplication.R
import com.example.notesapplication.databinding.ActivityUpdateNoteBinding
import com.example.notesapplication.model.Note
import com.example.notesapplication.viewModel.MainViewModel
import com.example.notesapplication.viewModel.NoteViewModel
import io.realm.Realm
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class UpdateNote : AppCompatActivity() {
    lateinit var binding: ActivityUpdateNoteBinding
    private lateinit var bottomSheet: BottomSheetUpdateNote
    private lateinit var viewModel: NoteViewModel
    private lateinit var realm: Realm
    private var noteColor: Int? = null
    private var noteUrl: String? = null
    private var noteImageUri: String? = null
    private var id: Int? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncher2: ActivityResultLauncher<Intent>
    private var currentPhotoPath: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        binding = ActivityUpdateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        register()
        register2()
        realm = Realm.getDefaultInstance()
        displayData()
        binding.bottomNav.setOnClickListener {
            bottomSheet = BottomSheetUpdateNote(activityResultLauncher, activityResultLauncher2)
            bottomSheet.show(supportFragmentManager, "fragment_bottom_sheet_update")
        }
        viewModel.color.observe(this, Observer { item ->
            binding.noteColour.setBackgroundResource(item)
            noteColor = item
        })
        viewModel.currentPhotoPath.observe(this) {
            currentPhotoPath = it
        }
        viewModel.isUrl.observe(this, Observer { item ->
            if (item) {
                binding.etWebLink.setText(noteUrl)
                binding.urlLayout.visibility = View.VISIBLE
            }
        })
        viewModel.isDeleteNote.observe(this, Observer { item ->
            if (item)
                id?.let { MainViewModel().deleteNote(it) }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        })
        binding.save.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val description = binding.editTextDescription.text.toString()
            saveNote(id, title, description, noteUrl, noteColor, noteImageUri)
        }
        binding.back.setOnClickListener {
            finish()
        }
        binding.imgDelete.setOnClickListener {
            binding.imgNote.setImageDrawable(null)
            binding.imgNote.visibility = View.GONE
            binding.imgDelete.visibility = View.GONE
            noteImageUri = null
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

    private fun displayData() {
        val intent = intent
        binding.editTextTitle.setText(intent.getStringExtra("titleToUpdate"))
        binding.editTextDescription.setText(intent.getStringExtra("descriptionToUpdate"))
        intent.getStringExtra("imageToUpdate")?.let {
            binding.imgNote.setImageURI(Uri.parse(it))
            binding.imgNote.visibility = View.VISIBLE
            binding.imgDelete.visibility = View.VISIBLE
            noteImageUri = it
        }
        intent.getStringExtra("linkToUpdate")?.let {
            binding.tvUrl.text = it
            binding.linearLayoutUrl.visibility = View.VISIBLE
            noteUrl = it
        }
        intent.getIntExtra("colorToUpdate", R.color.WhiteNote).let {
            noteColor = it
            binding.noteColour.setBackgroundResource(it)
        }
        id = intent.getIntExtra("id", 0)
        if (!noteImageUri.isNullOrEmpty())
            currentPhotoPath = Uri.parse(noteImageUri).toString()
    }

    private fun saveNote(
        id: Int?,
        noteTitle: String?,
        noteDescription: String?,
        noteUrl: String?,
        noteColour: Int?,
        noteImage: String?
    ) {
        val mainViewModel = MainViewModel()
        val simpleDate = DateFormat.getDateTimeInstance()
        val currentDate = simpleDate.format(Date())
        try {
                mainViewModel.updateNote(
                    id,
                    noteTitle,
                    noteDescription,
                    noteUrl,
                    noteColour,
                    noteImage
                )
            Toast.makeText(this@UpdateNote, "Note Updated", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this@UpdateNote, e.localizedMessage, Toast.LENGTH_SHORT)
                .show()
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

    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
            viewModel.setCurrentPhotoPath(absolutePath)
        }
    }
}
