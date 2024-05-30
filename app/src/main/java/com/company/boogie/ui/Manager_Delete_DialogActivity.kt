package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreProductModel

class Manager_Delete_DialogActivity : AppCompatActivity() {
    private lateinit var documentId: String
    private var canBorrow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 바 제거
        setContentView(R.layout.manager_delete_dialog)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        documentId = intent?.getStringExtra("documentId") ?: ""
        canBorrow = intent?.getBooleanExtra("canBorrow", false) ?: false

        val btnDelete: Button = findViewById(R.id.btn_delete)
        val btnCancel: Button = findViewById(R.id.btn_cancel)

        btnDelete.setOnClickListener {
            val firestoreProductModel = FirestoreProductModel()
            firestoreProductModel.deleteProductByDocumentId(documentId, canBorrow) { STATUS_CODE ->
                if (STATUS_CODE == StatusCode.SUCCESS) {
                    Toast.makeText(this, "삭제에 성공했습니다.", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            startActivity(Intent(this, Manager_ListActivity::class.java))
            finish() // 다이얼로그 닫기
        }

        btnCancel.setOnClickListener {
            finish() // 다이얼로그 닫기
        }
    }
}
