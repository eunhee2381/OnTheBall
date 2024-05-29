package com.company.boogie.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.company.boogie.R
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.StatusCode
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID

class Manager_Product_AddActivity : AppCompatActivity() {

    private lateinit var firestoreProductModel: FirestoreProductModel
    private lateinit var storage: FirebaseStorage
    private lateinit var imageUri: Uri
    private lateinit var imageView: ImageView
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var isImageSelected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_product_add)

        firestoreProductModel = FirestoreProductModel()
        storage = FirebaseStorage.getInstance()
        imageView = findViewById(R.id.imageView)

        setupNavigationButtons()
        setupImagePickers()

        val productAddButton: Button = findViewById(R.id.product_add)
        productAddButton.setOnClickListener {
            showAddConfirmationDialog()
        }

        val photoAddButton: Button = findViewById(R.id.photo2)
        photoAddButton.setOnClickListener {
            showPhotoOptionsDialog()
        }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.manager_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }

        val managerListButton: ImageButton = findViewById(R.id.manager_list)
        val managerRentalButton: ImageButton = findViewById(R.id.manager_rental)
        val managerCameraButton: ImageButton = findViewById(R.id.manager_camera)
        val managerMypageButton: ImageButton = findViewById(R.id.manager_mypage)
        val managerAlarmButton: ImageButton = findViewById(R.id.manager_alarm)

        managerAlarmButton.setOnClickListener {
            startActivity(Intent(this, Manager_NotificationActivity::class.java))
        }

        managerListButton.setOnClickListener {
            startActivity(Intent(this, Manager_ListActivity::class.java))
        }
        managerRentalButton.setOnClickListener {
            startActivity(Intent(this, Manager_RentalActivity::class.java))
        }
        managerCameraButton.setOnClickListener {
            startActivity(Intent(this, Manager_CameraActivity::class.java))
        }
        managerMypageButton.setOnClickListener {
            startActivity(Intent(this, Manager_MypageActivity::class.java))
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.managermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.show()
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.managermenulist -> {
                startActivity(Intent(this, Manager_ListActivity::class.java))
                true
            }
            R.id.managermenumypage -> {
                startActivity(Intent(this, Manager_MypageActivity::class.java))
                true
            }
            R.id.managermenucamera -> {
                startActivity(Intent(this, Manager_CameraActivity::class.java))
                true
            }
            R.id.managermenurental -> {
                startActivity(Intent(this, Manager_RentalActivity::class.java))
                true
            }
            R.id.managermenunotification -> {
                startActivity(Intent(this, Manager_NotificationActivity::class.java))
                true
            }
            R.id.menudevelop -> {
                startActivity(Intent(this, DeveloperActivity::class.java))
                true
            }
            else -> false
        }
    }

    private fun showAddConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("추가하겠습니까?")
            .setPositiveButton("확인") { dialog, id ->
                addProductToFirestore()
            }
            .setNegativeButton("취소") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun setupImagePickers() {
        selectImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data!!
                imageView.setImageURI(imageUri)
                isImageSelected = true
            }
        }

        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                imageView.setImageURI(imageUri)
                isImageSelected = true
            }
        }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("갤러리에서 선택", "카메라로 촬영")
        AlertDialog.Builder(this)
            .setTitle("사진 추가")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> takePictureWithCamera()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectImageLauncher.launch(intent)
        }
    }

    private fun takePictureWithCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 2)
        } else {
            val photoFile = createImageFile()
            imageUri = Uri.fromFile(photoFile)
            takePictureLauncher.launch(imageUri)
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir)
    }

    private fun addProductToFirestore() {
        val productName = findViewById<EditText>(R.id.product_name).text.toString()
        val productLocation = findViewById<EditText>(R.id.location).text.toString()
        val productNumber = findViewById<EditText>(R.id.count).text.toString().toIntOrNull()
        val classificationCode = findViewById<EditText>(R.id.remain).text.toString().toIntOrNull()
        val productDetail = findViewById<EditText>(R.id.explain).text.toString()

        if (productNumber != null && classificationCode != null) {
            if (isImageSelected) {
                val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
                imageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            firestoreProductModel.addProduct(
                                addProductId = productNumber,
                                addName = productName,
                                addLocation = productLocation,
                                addImage = uri.toString(),
                                addDetail = productDetail,
                                addClassificationCode = classificationCode
                            ) { statusCode ->
                                if (statusCode == StatusCode.SUCCESS) {
                                    Toast.makeText(this, "기자재 추가 성공", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, Manager_ListActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "기자재 추가 실패", Toast.LENGTH_SHORT).show()
                                    Log.e("Manager_Product_AddActivity", "기자재 추가 실패")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Manager_Product_AddActivity", "이미지 업로드 실패", e)
                    }
            } else {
                // 기본 이미지 URL 사용
                val defaultImageUrl = "https://example.com/default_image.jpg"
                firestoreProductModel.addProduct(
                    addProductId = productNumber,
                    addName = productName,
                    addLocation = productLocation,
                    addImage = defaultImageUrl,
                    addDetail = productDetail,
                    addClassificationCode = classificationCode
                ) { statusCode ->
                    if (statusCode == StatusCode.SUCCESS) {
                        Toast.makeText(this, "기자재 추가 성공", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Manager_ListActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "기자재 추가 실패", Toast.LENGTH_SHORT).show()
                        Log.e("Manager_Product_AddActivity", "기자재 추가 실패")
                    }
                }
            }
        } else {
            Toast.makeText(this, "모든 필드를 올바르게 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
