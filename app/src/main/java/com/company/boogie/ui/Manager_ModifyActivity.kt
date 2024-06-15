package com.company.boogie.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.models.Product
import org.w3c.dom.Text

class Manager_ModifyActivity : AppCompatActivity() {
    private lateinit var detailProduct: Product
    private lateinit var documentId: String
    private var canBorrow: Boolean = false
    private val PICK_IMAGE_REQUEST = 1001

    private val productName by lazy { findViewById<TextView>(R.id.product) }
    private val productLocation by lazy { findViewById<EditText>(R.id.location) }
    private val productExplain by lazy {findViewById<EditText>(R.id.explain) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_modify)
        setupNavigationButtons()

        documentId = intent?.getStringExtra("documentId") ?: ""
        canBorrow = intent?.getBooleanExtra("canBorrow", false) ?: false

        fetchDetailData(documentId, canBorrow) // 수정할 기자재의 정보 표시

        // 기자재 사진 변경 버튼 클릭 이벤트
        findViewById<Button>(R.id.modify_photo).setOnClickListener {
            // 사진 변경 로직
            pickImageFromGallery()
        }

        // 기자재 수정 버튼 클릭 이벤트
        findViewById<Button>(R.id.change).setOnClickListener {
            val location = productLocation.text.toString()
            val explain = productExplain.text.toString()

            // 기자재 수정 정보 예외처리 후 기자재 업데이트
            if (validateProduct(location, explain)) {
                updateProduct(location, explain)
            }
        }

        // 기자재 삭제 버튼 클릭 이벤트
        val deleteButton: Button = findViewById(R.id.delete)
        deleteButton.setOnClickListener {
            showDeleteDialog()
        }
    }

    // 수정할 기자재의 정보 표시
    private fun fetchDetailData(documentId: String, canBorrow: Boolean) {
        val firestoreProductModel = FirestoreProductModel()
        firestoreProductModel.getProductsByDocumentId(documentId, canBorrow) { STATUS_CODE, product ->
            if (STATUS_CODE == StatusCode.SUCCESS && product != null) {
                Log.d("Manager_ModifyActivity", "기자재 상세 정보 가져오기 성공 product=${product}")
                detailProduct = product

                // UI 수정
                productName.text = "${product.name} ${product.productId}" // 기자재 이름
                productLocation.setText(product.location) // 기자재 위치
                productExplain.setText(product.detail) // 기자재 설명
            }
            else {
                Log.w("Manager_ModifyActivity", "$documentId 기자재 상세 정보 가져오기 실패")
            }
        }
    }

    // 기자재 수정 정보 예외처리 (위치, 설명)
    private fun validateProduct(location: String, explain: String): Boolean {
        // 위치 - NotEmpty
        if (location.isEmpty()) {
            Toast.makeText(this, "기자재 위치를 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        // 설명 - NotEmpty
        if (explain.isEmpty()) {
            Toast.makeText(this, "기자재 설명을 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // 기자재 업데이트
    private fun updateProduct(location: String, explain: String) {
        val firestoreProductModel = FirestoreProductModel()
        firestoreProductModel.updateLocationDetail(documentId, canBorrow, location, explain) { STATUS_CODE ->
            if (STATUS_CODE == StatusCode.SUCCESS) {
                Toast.makeText(this, "업데이트에 성공했습니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
            startActivity(Intent(this, Manager_ListActivity::class.java))
            finish()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val firestoreProductModel = FirestoreProductModel()

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                firestoreProductModel.updateImg(uri, detailProduct.classificationCode, detailProduct.productId, documentId, canBorrow) { STATUS_CODE ->
                    if (STATUS_CODE == StatusCode.SUCCESS) {
                        Toast.makeText(this, "이미지 수정에 성공했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this, "이미지 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.manager_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }

        // 버튼들 인식
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
            // 추가하기
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
                // 추가하기
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

    private fun showDeleteDialog() {
        val intent = Intent(this, Manager_Delete_DialogActivity::class.java)
        intent.putExtra("documentId", documentId)
        intent.putExtra("canBorrow", canBorrow)
        startActivity(intent)
    }
}
