package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.text.util.LocalePreferences.CalendarType
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.UserManager
import com.company.boogie.utils.FirebaseUserUtil
import java.util.Calendar

class JoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.join)

        // Spinner
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

        // 회원가입 버튼 클릭 이벤트
        findViewById<Button>(R.id.signup).setOnClickListener {
            // UI 정보 가져오기 (관리자여부, 이름, 이메일, 비밀번호, 생년월일, 학번)
            val isAdmin: Boolean = accountSpinner.selectedItem.toString() == "서비스 관리자 계정" // 관리자 여부
            val name: String = findViewById<EditText>(R.id.name).text.toString() // 이름
            val email: String = findViewById<EditText>(R.id.email).text.toString() // 이메일
            val password: String = findViewById<EditText>(R.id.password).text.toString() // 비밀번호
            val birthday: String = findViewById<EditText>(R.id.birthDay).text.toString() // 생년월일
            val studentID: String = findViewById<EditText>(R.id.studentID).text.toString() // 학번

            // 계정 정보 예외처리 후 회원가입
            if (validateUser(name, email, password, birthday, studentID)) {
                doSignUp(isAdmin, name, email, password, birthday, studentID)
            }
        }

    }

    // 계정 정보 예외처리 (이름, 이메일, 비밀번호, 생년월일, 학번)
    private fun validateUser(name: String, email: String, password: String, birthday: String, studentID: String): Boolean {
        // 이름 - NotEmpty
        if (name.isEmpty()) {
            Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        // 이메일 - Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "올바른 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        // 비밀번호 - 6자리 이상
        if (password.length < 6) {
            Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        // 생년월일 - yyyy-mm-dd의 8자리 숫자
        if (birthday.length != 8 || !birthday.all { it.isDigit() }) {
            Toast.makeText(this, "생년월일은 8자리 숫자여야 합니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        val year: Int = birthday.substring(0,4).toInt()
        val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        val month: Int = birthday.substring(4,6).toInt()
        val day: Int = birthday.substring(6,8).toInt()

        if (year !in 1900..currentYear) {
            Toast.makeText(this, "생년월일에 유효한 연도를 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (month !in 1..12) {
            Toast.makeText(this, "생년월일에 유효한 월을 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (day !in 1..31) {
            Toast.makeText(this, "생년월일에 유효한 일을 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        // 학번 - 숫자, NotEmpty
        if (!studentID.all { it. isDigit() } || studentID.isEmpty()) {
            Toast.makeText(this, "학번은 숫자여야 합니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // 회원가입 (관리자여부, 이름, 이메일, 비밀번호, 생년월일, 학번)
    private fun doSignUp(isAdmin: Boolean, name: String, email: String, password: String, birthday: String, studentID: String) {
        Log.d("JoinActivity", "검증 완료 후 회원가입 진행")
        FirebaseUserUtil.doSignUp(email, password, name, birthday, studentID, isAdmin) { STATUS_CODE, uid ->
            if (STATUS_CODE == StatusCode.SUCCESS && uid != null) {
                // 회원가입 성공 - 관리자면 Manager_ListActivity, 사용자면 User_ListActivity 실행
                Toast.makeText(this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                Log.d("JoinActivity", "회원가입 성공: UID = $uid")

                // 사용자 정보 가져오기 호출
                FirebaseUserUtil.getUser(uid) { STATUS_CODE, user ->
                    if (STATUS_CODE == StatusCode.SUCCESS && user != null) {
                        Log.d("JoinActivity", "[${uid}]: 사용자명 [${user.name}]: 관리자 여부 [${user.isAdmin}] 사용자 정보 성공적으로 가져옴")

                        // 관리자 여부 저장
                        UserManager.isAdmin = user.isAdmin
                        Log.d("JoinActivity", "isAdmin = ${UserManager.isAdmin}")

                        // 관리자 계정이면 Manager_ListActivity 실행
                        if (UserManager.isAdmin) {
                            Log.d("JoinActivity", "Manager_ListActivity 실행 (관리자 계정)")
                            startActivity(Intent(this, Manager_ListActivity::class.java))
                            finish()
                        }
                        // 사용자면 User_ListActivity 실행
                        else {
                            Log.d("JoinActivity", "User_ListActivity 실행 (사용자 계정)")
                            startActivity(Intent(this, User_ListActivity::class.java))
                            finish()
                        }
                    } else {
                        Log.d("JoinActivity", "사용자 정보를 가져오는데 실패했습니다: STATUS_CODE = $STATUS_CODE")
                    }
                }
            } else {
                Log.d("JoinActivity", "회원가입 실패: STATUS_CODE = $STATUS_CODE, UID = $uid")
                Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
