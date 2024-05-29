package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.models.Product
import com.company.boogie.ui.adapter.DetailListAdapter

class User_DetailListActivity : AppCompatActivity() {
    private lateinit var detailListRecyclerView: RecyclerView
    private lateinit var detailListAdapter: DetailListAdapter
    private lateinit var productList: List<Product>
    private var classficationCode: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_detail_list)

        setupNavigationButtons()
        classficationCode = intent?.getIntExtra("classificationCode", -1) ?: -1
        setupRecyclerView() // RecyclerView 설정
    }

    // RecyclerView 설정
    private fun setupRecyclerView() {
        detailListRecyclerView = findViewById(R.id.detail_list_recycler_view)
        detailListRecyclerView.layoutManager = LinearLayoutManager(this)

        productList = emptyList()
        detailListAdapter = DetailListAdapter(productList)
        detailListAdapter.setOnItemClickListener(object: DetailListAdapter.OnItemClickListener {
            override fun onItemClick(item_document_id: String, item_can_borrow: Boolean) {
                showDetail(item_document_id, item_can_borrow)
            }
        })

        detailListRecyclerView.adapter = detailListAdapter
        fetchListData()
    }

    // RecyclerView에 데이터 표시
    private fun fetchListData() {
        val firestoreProductModel = FirestoreProductModel()
        firestoreProductModel.getProductsByClassificationCode(classficationCode) { STATUS_CODE, products ->
            if (STATUS_CODE == StatusCode.SUCCESS && products != null) {
                Log.d("User_DetailListActivity", "리사이클러뷰 데이터 가져오기 성공 products=${products}")
                productList = products
                detailListAdapter.updateList(productList)
            }
            else {
                Log.w("User_DetailListActivity", "리사이클러뷰 데이터 가져오기 실패")
            }
        }
    }

    // 리스트 클릭 시 해당 상품 디테일 페이지로 이동
    private fun showDetail(documentId: String, canBorrow: Boolean) {
        Log.d("User_DetailListActivity", "리사이클러뷰 클릭으로 액티비티 전환")
        val detailIntent = Intent(this, User_DetailActivity::class.java)
        detailIntent.putExtra("documentId", documentId)
        detailIntent.putExtra("canBorrow", canBorrow)
        startActivity(detailIntent)
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
}