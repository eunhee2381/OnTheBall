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

class Manager_ListActivity : AppCompatActivity() {
    private lateinit var listRecyclerView: RecyclerView
    private lateinit var listAdapter: ListAdapter
    private lateinit var productList: List<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_list)

        setupNavigationButtons()
        setupRecyclerView() // RecyclerView 설정

        // 검색 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.search_button).setOnClickListener {
            // 검색 query 가져와 검색 처리 로직
            val query = findViewById<EditText>(R.id.editText).text.toString()
            filterProducts(query)
        }

        // add_product 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.add_product).setOnClickListener {
            startActivity(Intent(this, Manager_Product_AddActivity::class.java))
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
                Log.d("Manager_ListActivity", "리사이클러뷰 데이터 가져오기 성공 products=${products}")
                productList = products
                listAdapter.updateList(productList)
            }
            else {
                Log.w("Manager_ListActivity", "리사이클러뷰 데이터 가져오기 실패")
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
        Log.d("Manager_ListActivity", "리사이클러뷰 클릭으로 액티비티 전환")
        val detailListIntent = Intent(this, Manager_DetailListActivity::class.java)
        detailListIntent.putExtra("classificationCode", classificationCode)
        startActivity(detailListIntent)
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
}
