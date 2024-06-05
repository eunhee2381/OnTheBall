package com.company.boogie.ui

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.StatusCode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream
import java.util.UUID

class Manager_Product_AddActivity : AppCompatActivity() {

    private lateinit var firestoreProductModel: FirestoreProductModel
    private lateinit var storage: FirebaseStorage
    private lateinit var imageUri: Uri
    private lateinit var imageView: ImageView
    private lateinit var classificationSpinner: Spinner
    private lateinit var productIdEditText: EditText
    private var isImageSelected: Boolean = false

    private val classificationMap = mapOf(
        "아두이노 우노" to 0,
        "라즈베리파이 wh" to 1,
        "라즈베리파이 제로" to 2,
        "라즈베리파이 W" to 3,
        "라즈베리파이 2 W" to 4,
        "라즈베리파이 5" to 5,
        "라즈베리파이 4 모델 B" to 6,
        "라즈베리파이 3 모델 B" to 7,
        "라즈베리파이 3 모델 B+" to 8,
        "라즈베리파이 3 모델 A+" to 9,
        "라즈베리파이 2 모델 B" to 10,
        "라즈베리파이 1 모델 B+" to 11,
        "라즈베리파이 1 모델 A+" to 12,
        "아두이노 레오나르도" to 13,
        "아두이노 마이크로" to 14,
        "아두이노 듀에" to 15,
        "아두이노 나노" to 16,
        "아두이노 메가" to 17,
        "아두이노 프로" to 18,
        "아두이노 제로" to 19
    )

    private val PICK_IMAGE_REQUEST = 1
    private val defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/<your-app-id>.appspot.com/o/path%2Fto%2Fdefault_image.jpg?alt=media"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_product_add)

        firestoreProductModel = FirestoreProductModel()
        storage = FirebaseStorage.getInstance()
        imageView = findViewById(R.id.imageView)
        classificationSpinner = findViewById(R.id.classification_spinner)
        productIdEditText = findViewById(R.id.product_id_edittext)

        setupNavigationButtons()
        setupClassificationSpinner()

        val productAddButton: Button = findViewById(R.id.product_add)
        productAddButton.setOnClickListener {
            showAddConfirmationDialog()
        }

        val photoAddButton: Button = findViewById(R.id.photo2)
        photoAddButton.setOnClickListener {
            openImagePicker()
        }

        classificationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val classificationName = parent.getItemAtPosition(position) as String
                val classificationCode = classificationMap[classificationName] ?: 0
                fetchLastProductId(classificationCode) { productNumber ->
                    productIdEditText.setText(productNumber.toString())
                }
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
                val classificationName = classificationSpinner.selectedItem as String
                val classificationCode = classificationMap[classificationName] ?: 0
                if (isImageSelected) {
                    uploadImageAndAddProduct(classificationCode)
                } else {
                    addProductToFirestore(classificationCode, defaultImageUrl)
                }
            }
            .setNegativeButton("취소") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
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

    private fun fetchLastProductId(classificationCode: Int, onSuccess: (Int) -> Unit) {
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
                    onSuccess(lastProductId + 1)
                } else {
                    onSuccess(1)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Manager_Product_AddActivity", "마지막 product ID를 찾을 수 없습니다.", e)
                onSuccess(1)
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
                this.imageUri = it
                isImageSelected = true
            }
        }
    }

    private fun uploadImageAndAddProduct(classificationCode: Int) {
        val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    addProductToFirestore(classificationCode, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Manager_Product_AddActivity", "이미지 업로드 실패", e)
            }
    }

    private fun addProductToFirestore(classificationCode: Int, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val productId = productIdEditText.text.toString().toIntOrNull() ?: return
        val productName = findViewById<EditText>(R.id.product_name).text.toString()
        val productLocation = findViewById<EditText>(R.id.location).text.toString()
        val productDetail = findViewById<EditText>(R.id.explain).text.toString()

        if (productName.isEmpty() || productLocation.isEmpty() || productDetail.isEmpty()) {
            Toast.makeText(this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val product = hashMapOf(
            "productId" to productId,
            "name" to productName,
            "location" to productLocation,
            "classificationCode" to classificationCode,
            "detail" to productDetail,
            "img" to imageUrl // 필드명을 "image"에서 "img"로 변경
        )

        db.collection("Product")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "기자재 추가 성공", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Manager_ListActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "기자재 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Manager_Product_AddActivity", "기자재 추가 실패", e)
            }
    }
}
