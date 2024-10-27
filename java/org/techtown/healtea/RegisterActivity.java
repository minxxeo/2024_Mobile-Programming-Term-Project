package org.techtown.healtea;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.healtea.databinding.ActivityRegisterBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding; // 뷰 바인딩 객체
    private Button register_btn; // 가입하기 버튼
    private EditText register_id; // 이메일 입력란
    private EditText register_pw; // 비밀번호 입력란
    private EditText register_repw; // 비밀번호 확인 입력란
    private EditText register_name; // 이름 입력란
    private EditText register_birth; // 생년월일 입력란
    private EditText register_phone; // 휴대전화 입력란
    private FirebaseAuth auth; // Firebase 인증 객체
    private DatabaseReference databaseReference; // Firebase 데이터베이스 참조 객체


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater()); // 뷰 바인딩 초기화
        setContentView(binding.getRoot()); // 뷰 설정

        getSupportActionBar().setTitle("회원가입");

        register_btn = binding.registerBtn; // 가입하기 버튼
        register_id = binding.registerId; // 이메일 입력란
        register_pw = binding.registerPw; // 비밀번호 입력란
        register_repw = binding.registerRepw; // 비밀번호 확인 입력란
        register_name = binding.registerName; // 이름 입력란
        register_birth = binding.registerBirth; // 생년월일 입력란
        register_phone = binding.registerPhone; // 휴대전화 입력란

        auth = FirebaseAuth.getInstance(); // Firebase 인증 객체 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Firebase 데이터베이스 참조 객체 초기화

        register_btn.setOnClickListener(view -> {
            // 입력된 값 가져오기
            String userId = register_id.getText().toString().trim(); // 이메일
            String userPw = register_pw.getText().toString().trim(); // 비밀번호
            String userRepw = register_repw.getText().toString().trim(); // 비밀번호 확인
            String userName = register_name.getText().toString().trim(); // 이름
            String userBirth = register_birth.getText().toString().trim(); // 생년월일
            String userPhone = register_phone.getText().toString().trim(); // 휴대전화

            // 이메일, 비밀번호, 이름, 생년월일, 휴대전화가 모두 입력되었는지 확인
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userPw) || TextUtils.isEmpty(userName)
                    || TextUtils.isEmpty(userBirth) || TextUtils.isEmpty(userPhone)) {
                Toast.makeText(RegisterActivity.this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
            } else if (!userPw.equals(userRepw)) {
                // 비밀번호와 비밀번호 확인이 일치하는지 확인
                Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            } else { // 사용자 생성 메소드 호출
                createUser(userId, userPw, userName, userBirth, userPhone);
            }
        });
    }

    private void createUser(String id, String pw, String name, String birth, String phone) {
        auth.createUserWithEmailAndPassword(id, pw)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 회원가입 성공
                        Toast.makeText(RegisterActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                        // 사용자 정보 데이터베이스에 저장
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            // 사용자의 이름을 Firebase에 저장
                            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build())
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // 사용자의 이름 업데이트 성공
                                            Toast.makeText(RegisterActivity.this, "사용자 이름 저장 완료", Toast.LENGTH_SHORT).show();
                                            // 사용자의 이름을 Realtime Database에 저장
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                                            userRef.child("name").setValue(name);
                                            userRef.child("birth").setValue(birth);
                                            userRef.child("phone").setValue(phone);
                                        } else {
                                            // 사용자의 이름 업데이트 실패
                                            Toast.makeText(RegisterActivity.this, "사용자 이름 저장 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        finish(); // 액티비티 종료
                    } else {
                        // 회원가입 실패
                        Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
