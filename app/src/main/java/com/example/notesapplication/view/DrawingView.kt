package com.example.notesapplication.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.notesapplication.BuildConfig
import com.example.notesapplication.databinding.ActivityCanvasBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.lang.Byte.decode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var canvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private var drawPath: CustomPath? = null
    private var paths = ArrayList<CustomPath>()
    private var undonePaths = ArrayList<CustomPath>()
    private var brushSize: Float = 20F
    private var brushColor: Int = Color.BLACK
    lateinit var imageUri: String

    init {
        setUpDrawing()
    }

    fun bitmapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = CustomPath(brushColor, brushSize)
        drawPaint!!.color = brushColor
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val tempCanvasBitmap = CanvasActivity.Companion.canvasBitmap
        tempCanvasBitmap?.let {
            canvasBitmap = tempCanvasBitmap
            canvas = Canvas(canvasBitmap!!)
            canvas!!.drawBitmap(canvasBitmap!!, 0f, 0f, null)
        }
        if(canvasBitmap == null) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            canvas = Canvas(canvasBitmap!!)
            canvas!!.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (path in paths) {
            drawPaint!!.strokeWidth = path.brushThickness
            drawPaint!!.color = path.color
            canvas.drawPath(path, drawPaint!!)
        }

        if (!drawPath!!.isEmpty) {
            drawPaint!!.strokeWidth = drawPath!!.brushThickness
            drawPaint!!.color = drawPath!!.color
            canvas.drawPath(drawPath!!, drawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath!!.color = brushColor
                drawPath!!.brushThickness = brushSize
                drawPath!!.reset()
                drawPath!!.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath!!.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                paths.add(drawPath!!)
                drawPath = CustomPath(brushColor, brushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun setBrushSize(size: Float): Float {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, size,
            resources.displayMetrics
        )
        drawPaint!!.strokeWidth = brushSize
        return brushSize
    }

    fun setBrushColor(newColor: Int) {
        brushColor = newColor
        drawPaint!!.color = brushColor
    }

    fun undoPath() {
        when {
            paths.size > 0 -> {
                undonePaths.add(paths.removeAt(paths.size - 1))
                invalidate()
            }
        }
    }

    fun redoPath() {
        when {
            undonePaths.size > 0 -> {
                paths.add(undonePaths.removeAt(undonePaths.size - 1))
                invalidate()
            }
        }
    }

    fun clearDrawing() {
        drawPath?.reset()
        paths.clear()
        invalidate()
    }

    fun saveBitmap(bitmap: Bitmap): Pair<String, String> {
        val resolver = context.contentResolver
        val canvasFile: File? = try {
            createCapturedPhoto()
        } catch (ex: IOException) {
            null
        }
        canvasFile?.also {
            val canvasUri =
                FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    it
                )
            if (canvasUri != null)
                saveDrawingToStream(bitmap, resolver.openOutputStream(canvasUri))
        }
        return imageUri to bitmapToString(bitmap)
    }

    fun getBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawing = view.background
        when {
            bgDrawing != null -> bgDrawing.draw(canvas)
            else -> canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    private fun saveDrawingToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            imageUri = absolutePath
        }
    }

    inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}