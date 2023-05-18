package com.example.notesapplication.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.color.colorChooser
import com.example.notesapplication.MainActivity
import com.example.notesapplication.R
import com.example.notesapplication.databinding.ActivityCanvasBinding
import com.example.notesapplication.model.Note
import com.example.notesapplication.viewModel.MainViewModel
import io.realm.Realm
import kotlinx.coroutines.launch

class CanvasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCanvasBinding
    private var realm = Realm.getDefaultInstance()
    private var brushColor: Int = Color.BLACK
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_canvas)
        binding.canvas = this
        binding.drawing = binding.drawingView
        eraser()
        brush()
        setContentView(binding.root)
    }
    companion object{
        val intent = Intent()
        private val bitmapString = intent.getStringExtra("bitmapToUpdate")
        private val canvasBytes: ByteArray? =
            bitmapString?.let{
                Base64.decode(it, Base64.DEFAULT)
            }
                val canvasBitmap: Bitmap? =
                    canvasBytes?.let { BitmapFactory.decodeByteArray(canvasBytes, 0, it.size) }
            }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun brush() {
        binding.drawingView.setBrushColor(brushColor)
        binding.ibBrushSize.setOnLongClickListener {
            showBrushSizeDialog(false)
            binding.drawingView.setBrushColor(brushColor)
            return@setOnLongClickListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun eraser() {
        binding.drawingView.setBrushColor(Color.WHITE)

        binding.ibEraseDraw.setOnLongClickListener {
            showBrushSizeDialog(true)
            binding.drawingView.setBrushColor(Color.WHITE)
            return@setOnLongClickListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("CheckResult")
    fun brushColor() {
        @Suppress("DEPRECATION")
        val colors = intArrayOf(
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.MAGENTA, Color.GRAY, Color.CYAN,
            resources.getColor(R.color.BlueNote), resources.getColor(R.color.OrangeNote),
            resources.getColor(R.color.light_green), resources.getColor(R.color.purple)
        )

        MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.dialog_choose_color)
            colorChooser(colors, allowCustomArgb = true, showAlphaSelector = true) { _, color ->
                brushColor = color
                binding.drawingView.setBrushColor(brushColor)
            }
            positiveButton(R.string.dialog_select)
            negativeButton(R.string.dialog_negative)
        }
    }
    fun shareDrawing(){
        // to be implemented
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("CheckResult")
    private fun showBrushSizeDialog(eraser: Boolean) {
        val sizes = listOf(
            BasicGridItem(R.drawable.brush_small, getString(R.string.brush_small)),
            BasicGridItem(R.drawable.brush_medium, getString(R.string.brush_medium)),
            BasicGridItem(R.drawable.brush_large, getString(R.string.brush_large))
        )

        MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            when (eraser) {
                true -> title(R.string.dialog_choose_eraser_size)
                else -> title(R.string.dialog_choose_brush_size)
            }

            gridItems(sizes) { _, index, _ ->
                when (index) {
                    0 -> binding.drawingView.setBrushSize(5F)
                    1 -> binding.drawingView.setBrushSize(10F)
                    2 -> binding.drawingView.setBrushSize(20F)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun saveBitmap() {
        lifecycleScope.launch {
            val (imageCanvasUri, bitmap) = binding.drawingView.saveBitmap(
                binding.drawingView.getBitmap(
                    binding.drawingView
                )
            )
            val viewModel = MainViewModel()
            val currentId: Int? = realm.where(Note::class.java).max("id")?.toInt()
            val nextId = if (currentId == null) 1 else currentId + 1
            viewModel.addNote(id = nextId, noteImage = imageCanvasUri, bitmap = bitmap)
            val intent = Intent(this@CanvasActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_canvas, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> binding.drawingView.undoPath()
            R.id.redo -> binding.drawingView.redoPath()
            R.id.saveCanvas -> saveBitmap()
        }
        return true
    }
}