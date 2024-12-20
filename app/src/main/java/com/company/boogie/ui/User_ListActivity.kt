package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreProductModel
import com.company.boogie.models.Product
import com.company.boogie.ui.adapter.ListAdapter

class User_ListActivity : AppCompatActivity() {
    private lateinit var listRecyclerView: RecyclerView
    private lateinit var listAdapter: ListAdapter
    private lateinit var productList: List<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_list)

        setupNavigationButtons()

        setupRecyclerView() // RecyclerView 설정

        // 검색 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.search_button).setOnClickListener {
            // 검색 query 가져와 검색 처리 로직
            val query = findViewById<EditText>(R.id.editText).text.toString()
            filterProducts(query)
        }
    }

    // RecyclerView 설정
    private fun setupRecyclerView() {
        listRecyclerView = findViewById(R.id.list_recycler_view)
        listRecyclerView.layoutManager = LinearLayoutManager(this)

        productList = emptyList()
        listAdapter = ListAdapter(productList)
        listAdapter.setOnItemClickListener { showDetailList(it) }

        listRecyclerView.adapter = listAdapter
        fetchListData()
    }

    // RecyclerView에 데이터 표시
    private fun fetchListData() {
        val firestoreProductModel = FirestoreProductModel()
        firestoreProductModel.getProductsByProductId1 { STATUS_CODE, products ->
            if (STATUS_CODE == StatusCode.SUCCESS && products != null) {
                Log.d("User_ListActivity", "리사이클러뷰 데이터 가져오기 성공 products=${products}")
                productList = products
                listAdapter.updateList(productList)
            }
            else {
                Log.w("User_ListActivity", "리사이클러뷰 데이터 가져오기 실패")
            }
        }
    }

    // 검색 기능
    private fun filterProducts(query: String) {
        val noSpaceQuery = query.replace(" ", "") //검색어 공백 제거
        val filteredList = if (noSpaceQuery.isEmpty()) { // 검색어 입력 x - 원래 리스트 리턴
            productList
        } else { // 검색어 입력 o - 검색 결과에 해당하는 리스트를 필터링해 리턴
            productList.filter {// ex) "아두이노우노"로 검색 시 "아두이노 우노"를 리턴하도록 함
                it.name.replace(" ", "").contains(noSpaceQuery, ignoreCase = true)
            }
        }

        listAdapter.updateList(filteredList)
    }

    // 리스트 클릭 시 디테일 리스트로 이동 (클릭한 기자재의 classificationCode에 해당하는 기자재 리스트를 보여줌)
    private fun showDetailList(classificationCode: Int) {
        Log.d("User_ListActivity", "리사이클러뷰 클릭으로 액티비티 전환")
        val detailListIntent = Intent(this, User_DetailListActivity::class.java)
        detailListIntent.putExtra("classificationCode", classificationCode)
        startActivity(detailListIntent)
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
