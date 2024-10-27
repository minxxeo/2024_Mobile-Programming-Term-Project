package org.techtown.healtea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.healtea.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private ActivityLoginBinding binding; // 뷰 바인딩을 위한 바인딩 객체
    private FirebaseAuth auth; // 파이어베이스 인증 객체

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater()); // 뷰 바인딩 초기화
        setContentView(binding.getRoot()); // 뷰 설정

        EditText id = binding.loginId; // 아이디 입력란
        EditText pw = binding.loginPw; // 비밀번호 입력란
        CheckBox saveIdCheckBox = binding.loginRemember; // 아이디 저장 체크박스

        Button register = binding.loginRegister; // 회원가입 버튼
        Button login = binding.loginBtn; // 로그인 버튼
        Button forgotPassword = binding.loginForget; // 비밀번호 찾기 버튼

        auth = FirebaseAuth.getInstance(); // 파이어베이스 인증 객체 초기화

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // 저장된 아이디 불러오기
        String savedId = sharedPreferences.getString("savedId", "");
        if (!savedId.isEmpty()) {
            id.setText(savedId);
            saveIdCheckBox.setChecked(true);
        }

        register.setOnClickListener(view -> {
            // 회원가입 액티비티로 이동
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        login.setOnClickListener(view -> {
            String userId = id.getText().toString().trim(); // 입력된 아이디
            String userPw = pw.getText().toString().trim(); // 입력된 비밀번호

            if (userId.isEmpty() || userPw.isEmpty()) {
                // 아이디나 비밀번호가 비어있을 때
                Toast.makeText(MainActivity.this, "빈 값을 입력하셨습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 비어 있지 않으면 로그인 시도
                if (saveIdCheckBox.isChecked()) {
                    // 체크박스가 체크되어 있으면 아이디 저장
                    editor.putString("savedId", userId);
                } else {
                    // 체크박스가 체크되어 있지 않으면 저장된 아이디 삭제
                    editor.remove("savedId");
                }
                editor.apply();

                loginUser(userId, userPw);
            }
        });

        forgotPassword.setOnClickListener(view -> {
            String userId = id.getText().toString().trim(); // 입력된 아이디

            if (userId.isEmpty()) {
                Toast.makeText(MainActivity.this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(userId);
            }
        });
    }

    // 로그인 메서드
    private void loginUser(String userId, String userPw) {
        auth.signInWithEmailAndPassword(userId, userPw) // 파이어베이스 인증을 사용하여 로그인 시도
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공 시
                        Toast.makeText(MainActivity.this, "로그인에 성공했습니다!", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // 사용자 이름이 Firebase에 설정되어 있으면 MainActivity2로 이동
                            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                            startActivity(intent);
                            finish(); // 현재 액티비티 종료
                        }
                    } else {
                        // 로그인 실패 시
                        Toast.makeText(MainActivity.this, "아이디와 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 비밀번호 재설정 메서드
    private void resetPassword(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "비밀번호 재설정 이메일을 전송했습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "비밀번호 재설정 이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
