package com.example.notesapplication.view

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.notesapplication.BuildConfig
import com.example.notesapplication.R
import com.example.notesapplication.databinding.FragmentBottomSheetUpdateBinding
import com.example.notesapplication.viewModel.NoteViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetUpdateNote(
    val activityResultLauncher: ActivityResultLauncher<Intent>,
    val activityResultLauncher2: ActivityResultLauncher<Intent>
) : BottomSheetDialogFragment(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {
    private lateinit var binding: FragmentBottomSheetUpdateBinding
    private lateinit var viewModel: NoteViewModel
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    private lateinit var currentPhotoPath: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[NoteViewModel::class.java]
        binding.fNote1.setOnClickListener {
            binding.imgNote1.setImageResource(R.drawable.ic_add)
            viewModel.setColor(R.color.BlueNote)
            dismiss()
        }
        binding.fNote2.setOnClickListener {
            binding.imgNote1.setImageResource(R.drawable.ic_add)
            viewModel.setColor(R.color.YellowNote)
            dismiss()
        }
        binding.fNote3.setOnClickListener {
            binding.imgNote1.setImageResource(R.drawable.ic_add)
            viewModel.setColor(R.color.PurpleNote)
            dismiss()
        }
        binding.fNote4.setOnClickListener {
            binding.imgNote1.setImageResource(R.drawable.ic_add)
            viewModel.setColor(R.color.GreenNote)
            dismiss()
        }
        binding.fNote5.setOnClickListener {
            binding.imgNote1.setImageResource(R.drawable.ic_add)
            viewModel.setColor(R.color.OrangeNote)
            dismiss()
        }
        binding.fNote6.setOnClickListener {
            binding.imgNote1.setImageResource(R.drawable.ic_add)
            viewModel.setColor(R.color.BlackNote)
            dismiss()
        }
        binding.layoutImage.setOnClickListener {
            showPictureDialog()
        }
        binding.layoutWebUrl.setOnClickListener {
            viewModel.setIsUrl(true)
            dismiss()
        }
        binding.layoutDeleteNote.setOnClickListener {
            viewModel.setIsDeleteNote(true)
        }
    }

    private fun hasCameraPermission(): Boolean? {
        return activity?.let { EasyPermissions.hasPermissions(it, Manifest.permission.CAMERA) }
    }

    private fun hasStoragePermission(): Boolean? {
        return activity?.let {
            EasyPermissions.hasPermissions(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(activity)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> {
                    openGallery()
                }
                1 -> {
                    openCamera()
                }
            }
        }
        pictureDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (activity?.let { EasyPermissions.somePermissionPermanentlyDenied(it, perms) } == true) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    private fun openCamera() {
        if (hasCameraPermission() == true) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                activity?.packageManager?.let {
                    intent.resolveActivity(it)?.also {
                        val photoFile: File? = try {
                            createCapturedPhoto()
                        } catch (ex: IOException) {
                            null
                        }
                        photoFile?.also {
                            val photoURI = activity?.applicationContext?.let { it1 ->
                                FileProvider.getUriForFile(
                                    it1,
                                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                                    it
                                )
                            }
                            viewModel.setCurrentPhotoPath(currentPhotoPath)
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            activityResultLauncher.launch(intent)
                        }
                    }
                }
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.rationale_camera),
                REQUEST_IMAGE_CAPTURE,
                Manifest.permission.CAMERA
            )
        }
    }

    private fun openGallery() {
        if (hasStoragePermission() == true) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            activityResultLauncher2.launch(intent)
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.storage),
                REQUEST_PICK_IMAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_PICK_IMAGE)
            openGallery()
        else if (requestCode == REQUEST_IMAGE_CAPTURE)
            openCamera()
    }
}