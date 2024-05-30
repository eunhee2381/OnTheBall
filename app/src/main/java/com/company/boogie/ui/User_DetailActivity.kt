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
import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.app.AlertDialog
import com.company.boogie.utils.FirebaseRequestUtil
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar as Calendar

class User_DetailActivity : AppCompatActivity() {
    private lateinit var detailProduct: Product
    private lateinit var documentId: String
    private var canBorrow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_detail)
        setupNavigationButtons()

        findViewById<ImageButton>(R.id.close_button).setOnClickListener {
            startActivity(Intent(this, User_ListActivity::class.java))
            finish()
        }

        documentId = intent?.getStringExtra("documentId") ?: ""
        canBorrow = intent?.getBooleanExtra("canBorrow", false) ?: false

        if (canBorrow) {
            findViewById<Button>(R.id.button_edit).setOnClickListener {
                showDatePickerDialog()
            }
        } else {
            findViewById<Button>(R.id.button_edit).text = "대여 불가"
            //findViewById<Button>(R.id.button_edit).setOnClickListener {
                //showDatePickerDialog()
            //}
        }

        fetchDetailData(documentId, canBorrow)
    }

    // 기자재 상세 정보를 가져와 UI 수정
    private fun fetchDetailData(documentId: String, canBorrow: Boolean) {
        val firestoreProductModel = FirestoreProductModel()
        firestoreProductModel.getProductsByDocumentId(documentId, canBorrow) { STATUS_CODE, product ->
            if (STATUS_CODE == StatusCode.SUCCESS && product != null) {
                Log.d("User_DetailActivity", "기자재 상세 정보 가져오기 성공 product=${product}")
                detailProduct = product

                // UI 수정
                val detailString: String = "이름: " + product.name + "\n" +
                        "설명: " + product.detail + "\n" +
                        "위치: " + product.location + "\n" +
                        "대여 현황: " + if (product.canBorrow) "대여 가능" else "대여 중"
                findViewById<TextView>(R.id.user_detailtext).text = detailString

                Log.d("User_DetailActivity", "이미지 비트맵 ${product.img}")
                firestoreProductModel.getProductImgBitmap(product.img) { STATUS_CODE, bitmap ->
                    if (STATUS_CODE == StatusCode.SUCCESS) {
                        Log.d("User_DetailActivity", "이미지 비트맵 가져오기 성공")
                        findViewById<ImageView>(R.id.product_imageView).setImageBitmap(bitmap)
                    }
                    else {
                        Log.w("User_DetailActivity", "이미지 비트맵 가져오기 실패")
                        findViewById<ImageView>(R.id.product_imageView).setImageResource(R.drawable.cancel)
                    }
                }
            }
            else {
                Log.w("User_DetailActivity", "$documentId 기자재 상세 정보 가져오기 실패")
            }
        }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.user_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }

        // 버튼들 인식
        val userListButton: ImageButton = findViewById(R.id.user_list)
        val userRentalButton: ImageButton = findViewById(R.id.user_rental)
        val userAlarmButton: ImageButton = findViewById(R.id.user_alarm)
        val userMypageButton: ImageButton = findViewById(R.id.user_mypage)

        userListButton.setOnClickListener {
            startActivity(Intent(this, User_ListActivity::class.java))
        }
        userRentalButton.setOnClickListener {
            startActivity(Intent(this, User_RentalActivity::class.java))
        }
        userAlarmButton.setOnClickListener {
            startActivity(Intent(this, User_NotificationActivity::class.java))
        }
        userMypageButton.setOnClickListener {
            startActivity(Intent(this, User_MypageActivity::class.java))
        }
    }

    private fun showPopupMenu(view: View) { // 팝업창
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.usermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.show()
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.usermenulist -> {
                startActivity(Intent(this, User_ListActivity::class.java))
                true
            }
            R.id.usermenumypage -> {
                startActivity(Intent(this, User_MypageActivity::class.java))
                true
            }
            R.id.usermenurental -> {
                startActivity(Intent(this, User_RentalActivity::class.java))
                true
            }
            R.id.usermenunotification -> {
                startActivity(Intent(this, User_NotificationActivity::class.java))
                true
            }
            R.id.menudevelop -> {
                startActivity(Intent(this, DeveloperActivity::class.java)) // Ensure this activity is correctly implemented
                true
            }
            else -> false
        }
    }
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            showApplicationDialog(selectedDate)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.datePicker.maxDate = cal.apply { add(Calendar.DAY_OF_YEAR, 30) }.timeInMillis
        datePickerDialog.show()
    }

    private fun showApplicationDialog(selectedDate: Calendar) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.user_detail_dialog, null)
        val dateDisplay = dialogView.findViewById<TextView>(R.id.date_display)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateDisplay.text = sdf.format(selectedDate.time)
        val firebaseUtil = FirebaseRequestUtil()
        val formattedDate = sdf.format(selectedDate.time)

        val dialog = AlertDialog.Builder(this@User_DetailActivity) // Context 명시적으로 지정
        dialog.setView(dialogView)
        dialog.setPositiveButton("신청하기") { _, _ ->
            Log.d("User_DetailActivity", "신청하기 버튼 클릭됨")
            firebaseUtil.productToBorrowing(detailProduct.name, detailProduct.productId, formattedDate) { statusCode ->
                Log.d("User_DetailActivity", "Firebase 응답: $statusCode")
                // 상태 코드에 따른 후속 처리
            }
            //firebaseUtil.borrowingToProduct(detailProduct.name, detailProduct.productId) { statusCode ->
              //Log.d("User_DetailActivity", "Firebase 응답: $statusCode")
            //}
        }
        dialog.setNegativeButton("취소", null)
        dialog.show()
    }

    private fun sendRentalRequestNotification(productName: String) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val message = "$currentUserEmail 님이 $productName 을 대여요청 하였습니다."
        val title = "대여 요청"
        sendNotificationToServer(currentUserEmail, title, message)
    }

    private fun sendNotificationToServer(email: String, title: String, message: String) {
        // 서버에 알림 요청을 보냅니다. (HTTP 요청을 사용하여 백엔드 서버 호출)
    }

    private fun sendReservationRequest(){

    }
}