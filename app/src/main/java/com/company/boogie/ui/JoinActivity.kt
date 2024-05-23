package com.company.boogie.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.company.boogie.R

class JoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.join)

        val user_name = findViewById<EditText>(R.id.name) // 계정을 만들 이름 EditText
        val user_email = findViewById<EditText>(R.id.email) // 계정을 만들 이메일 EditText
        val user_password = findViewById<EditText>(R.id.password) // 계정을 만들 비밀번호 EditText
        val user_birthday = findViewById<EditText>(R.id.birthDay) // 계정을 만들 생일 EditText
        val user_studentID = findViewById<EditText>(R.id.studentID) // 계정을 만들 학번 EditText


        val accountSpinner: Spinner = findViewById(R.id.option_user) // 사용자/관리자중 택1 spinner
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
