package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.models.Product

class Manager_DetailActivity : AppCompatActivity() {
    private lateinit var detailProduct: Product
    private lateinit var documentId: String
    private var canBorrow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_detail)
        setupNavigationButtons()

        val editButton: Button = findViewById(R.id.button_edit) // 수정하기 버튼을 누를 경우 수정페이지로
        editButton.setOnClickListener {
            val intent = Intent(this, Manager_ModifyActivity::class.java)
            startActivity(intent)
        }

        val closeButton: ImageButton = findViewById(R.id.close_button) // 나가기 버튼을 누를경우 리스트로 돌아감
        closeButton.setOnClickListener{
            val intent = Intent(this, Manager_ListActivity::class.java)
            startActivity(intent)
            finish()
        }

        documentId = intent?.getStringExtra("documentId") ?: ""
        canBorrow = intent?.getBooleanExtra("canBorrow", false) ?: false

        // 기자재 상세 정보를 가져와 UI 수정
        fetchDetailData(documentId, canBorrow)
    }

    // 기자재 상세 정보를 가져와 UI 수정
    private fun fetchDetailData(documentId: String, canBorrow: Boolean) {
        val firestoreProductModel = FirestoreProductModel()
        firestoreProductModel.getProductsByDocumentId(documentId, canBorrow) { STATUS_CODE, product ->
            if (STATUS_CODE == StatusCode.SUCCESS && product != null) {
                Log.d("Manager_DetailActivity", "기자재 상세 정보 가져오기 성공 product=${product}")
                detailProduct = product

                // UI 수정
                val detailString: String = "이름: " + product.name + "\n" +
                        "설명: " + product.detail + "\n" +
                        "위치: " + product.location + "\n" +
                        "대여 현황: " + if (product.canBorrow) "대여 가능" else "대여 중"
                findViewById<TextView>(R.id.manager_detailtext).text = detailString

                Log.d("Manager_DetailActivity", "이미지 비트맵 ${product.img}")
                firestoreProductModel.getProductImgBitmap(product.img) { STATUS_CODE, bitmap ->
                    if (STATUS_CODE == StatusCode.SUCCESS) {
                        Log.d("Manager_DetailActivity", "이미지 비트맵 가져오기 성공")
                        findViewById<ImageView>(R.id.product_imageView).setImageBitmap(bitmap)
                    }
                    else {
                        Log.w("Manager_DetailActivity", "이미지 비트맵 가져오기 실패")
                        findViewById<ImageView>(R.id.product_imageView).setImageResource(R.drawable.cancel)
                    }
                }
            }
            else {
                Log.w("Manager_DetailActivity", "$documentId 기자재 상세 정보 가져오기 실패")
            }
        }
    }

    private fun setupNavigationButtons() { // 하단바 버튼들
        findViewById<ImageButton>(R.id.manager_list).setOnClickListener {
            startActivity(Intent(this, Manager_ListActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_rental).setOnClickListener {
            startActivity(Intent(this, Manager_RentalActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_camera).setOnClickListener {
            startActivity(Intent(this, Manager_CameraActivity::class.java))
            //startActivity(Intent(this, ::class.java))
        }

        findViewById<ImageButton>(R.id.manager_mypage).setOnClickListener {
            startActivity(Intent(this, Manager_MypageActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_alarm).setOnClickListener {
            startActivity(Intent(this, Manager_NotificationActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_menuButton).setOnClickListener { view ->
            showPopupMenu(view)
        }

    }


    private fun showPopupMenu(view: View) { // 팦 메뉴창
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.managermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> onMenuItemClick(item) }
        popup.show()
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
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
                //startActivity(Intent(this, ::class.java))
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
}
