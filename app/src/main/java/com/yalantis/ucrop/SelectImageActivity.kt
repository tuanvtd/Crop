package com.yalantis.ucrop

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.yalantis.ucrop.databinding.ActivitySelectImageBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class SelectImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelect.setOnClickListener {
            select()
        }

        requestPermission()


//        binding.cropView.addOnCropListener(object : OnCropListener {
//            override fun onSuccess(bitmap: Bitmap) {
//                val pathCrop = createDownloadFile()
//                saveBitmap(bitmap, pathCrop)
//                Toast.makeText(baseContext, "Success", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onFailure(e: Exception) {
//
//            }
//        })
//
//
//        binding.btnCropImage.setOnClickListener {
//            binding.cropView.crop()
//        }


    }

    // Hàm để lưu bitmap vào một tệp ảnh
    fun saveBitmap(bitmap: Bitmap, path:String): File? {
        val file = File(path)

        var outputStream: OutputStream? = null
        try {
            // Mở một luồng đầu ra đến tệp
            outputStream = FileOutputStream(file)

            // Lưu bitmap vào tệp ảnh
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            // Ghi dữ liệu vào tệp
            outputStream.flush()

            // Trả về đối tượng File đã tạo
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            // Xử lý lỗi nếu có
        } finally {
            // Đảm bảo luồng đầu ra được đóng
            outputStream?.close()
        }
        return null
    }



    private fun select() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1234)
    }

    // Permission
    private var permissionsResult: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.entries.all { it.value }) {

            } else {
                showDialogPermission()
            }
        }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var permissionsApiTo33 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.CAMERA,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private var permissionsApiFrom5To12 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
    )

    private fun arrayPermission(): Array<String> {
        val permission: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsApiTo33
        } else {
            permissionsApiFrom5To12
        }
        return permission
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermission(): Boolean {
        return checkStorePermission() && checkCameraPermission() && checkNotificationPermission()
    }

    fun requestPermission() {
        permissionsResult.launch(arrayPermission())
    }

    fun checkStorePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    fun checkCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private var alertDialog: AlertDialog? = null
    private fun dialogPermission() {
        alertDialog = AlertDialog.Builder(this, R.style.AlertDialogCustom).create()
        alertDialog?.setTitle(getString(R.string.Grant_Permission))
        alertDialog?.setMessage(getString(R.string.Please_grant_all_permissions))
        alertDialog?.setButton(
            -1, getString(R.string.Go_to_setting) as CharSequence
        ) { _, _ ->
            alertDialog?.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", applicationContext.packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun showDialogPermission() {
        alertDialog?.setOnShowListener {
            alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(resources.getColor(R.color.black))
        }
        alertDialog?.show()
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            data?.data?.let { start(it) }
            //data?.data?.let { binding.cropView.setUri(it) }
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            binding.imgView.setImageURI(resultUri)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
        }

    }

    private fun createDownloadDirectory() {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ABC/")
        if (!directory.exists()) {
            directory.mkdir()
        }
    }

    fun createDownloadFile(): String {
        createDownloadDirectory()
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ABC/")

        val fileName = "FaceSwap${System.currentTimeMillis()}.png"

        val file = File(directory, "/$fileName")

//            if (file.exists()) {
//                file.delete()
//            }

        val newFile = File(directory, "/$fileName")

        try {
            if (!newFile.exists()) {
                directory.mkdirs()

                newFile.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return newFile.absolutePath
    }


    private fun start(uri : Uri){
        val pathCrop = createDownloadFile()
        output = pathCrop
        input = RealPathUtil.getRealPath(baseContext, uri)
        pathCrop.let {
            val uriOut = FileProvider.getUriForFile(baseContext, "${BuildConfig.APPLICATION_ID}.provider", File(it))

            val options = UCrop.Options()
            options.setCompressionQuality(100)
            options.setShowCropGrid(true)
            //options.setHideBottomControls(true)
            UCrop.of(uri, uriOut)
                //.withAspectRatio(9F, 16F)
               // .useSourceImageAspectRatio()
                .withOptions(options)
                .start(this)
        }
    }




    companion object{
        var input = ""
        var output = ""
    }

}