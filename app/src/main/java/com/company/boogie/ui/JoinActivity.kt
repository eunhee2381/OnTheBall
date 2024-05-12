package com.company.boogie.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.company.boogie.R

class JoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val accountSpinner: Spinner = findViewById(R.id.option_user)
        val accountTypes = arrayOf("서비스 사용자 계정", "서비스 관리자 계정")
        accountSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountTypes)
        (accountSpinner.adapter as ArrayAdapter<*>).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // 선택된 계정 유형에 따라 동작
                Toast.makeText(this@JoinActivity, "${accountTypes[position]} 선택됨", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택이 해제되었을 때의 동작
            }
        }
    }
}
