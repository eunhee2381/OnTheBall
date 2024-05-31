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
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.company.boogie.R
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.StatusCode
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var classificationSpinner: Spinner
    private lateinit var productIdTextView: TextView
    private var isImageSelected: Boolean = false

    private val classificationMap = mapOf(
        "아두이노 우노" to 0,
        "라즈베리파이 wh" to 1,
        "라즈베리파이 제로" to 2 ,
        "라즈베리파이 W" to 3 ,
        "라즈베리파이 2 W" to 4 ,
        "라즈베리파이 5" to 5 ,
        "라즈베리파이 4 모델 B" to 6 ,
        "라즈베리파이 3 모델 B" to 7 ,
        "라즈베리파이 3 모델 B+" to 8 ,
        "라즈베리파이 3 모델 A+" to 9 ,
        "라즈베리파이 2 모델 B" to 10 ,
        "라즈베리파이 1 모델 B+" to 11 ,
        "라즈베리파이 1 모델 A+" to 12 ,
        "아두이노 레오나르도" to 13,
        "아두이노 마이크로" to 14,
        "아두이노 듀에" to 15,
        "아두이노 나노" to 16,
        "아두이노 메가" to 17,
        "아두이노 프로" to 18,
        "아두이노 제로" to 19
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_product_add)

        firestoreProductModel = FirestoreProductModel()
        storage = FirebaseStorage.getInstance()
        imageView = findViewById(R.id.imageView)
        classificationSpinner = findViewById(R.id.classification_spinner)
        productIdTextView = findViewById(R.id.product_id_textview)

        setupNavigationButtons()
        setupImagePickers()
        setupClassificationSpinner()

        val productAddButton: Button = findViewById(R.id.product_add)
        productAddButton.setOnClickListener {
            showAddConfirmationDialog()
        }

        val photoAddButton: Button = findViewById(R.id.photo2)
        photoAddButton.setOnClickListener {
            showPhotoOptionsDialog()
        }

        classificationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                fetchLastProductId(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
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
                    0 -> checkGalleryPermission()
                    1 -> checkCameraPermission()
                }
            }
            .show()
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                2
            )
        } else {
            takePictureWithCamera()
        }
    }

    private fun takePictureWithCamera() {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir)
        imageUri = Uri.fromFile(file)
        takePictureLauncher.launch(imageUri)
    }

    private fun setupClassificationSpinner() {
        classificationSpinner = findViewById(R.id.classification_spinner)
        val classificationNames = classificationMap.keys.toTypedArray()
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            classificationNames
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            classificationSpinner.adapter = adapter
        }
    }

    private fun fetchLastProductId(classificationCode: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Product")
            .whereEqualTo("classificationCode", classificationCode)
            .orderBy("productId", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val lastProduct = documents.documents[0]
                    val lastProductId = lastProduct.getLong("productId")?.toInt() ?: 0
                    productIdTextView.text = (lastProductId + 1).toString()
                } else {
                    productIdTextView.text = "1"
                }
            }
            .addOnFailureListener { e ->
                Log.e("Manager_Product_AddActivity", "마지막 제품 ID를 가져오지 못했습니다.", e)
                productIdTextView.text = "1"
            }
    }

    private fun addProductToFirestore() {
        val productName = findViewById<EditText>(R.id.product_name).text.toString()
        val productLocation = findViewById<EditText>(R.id.location).text.toString()
        val productNumber = productIdTextView.text.toString().toIntOrNull()
        val classificationName = classificationSpinner.selectedItem as String
        val classificationCode = classificationMap[classificationName] ?: 0
        val productDetail = findViewById<EditText>(R.id.explain).text.toString()

        if (productNumber != null) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
            2 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePictureWithCamera()
                } else {
                    Toast.makeText(this, "카메라 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
