package org.techtown.healtea;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.healtea.databinding.LoginBinding;

public class LoginActivity extends AppCompatActivity {
    private Button login; // 로그인 버튼
    private LoginBinding binding; // 뷰 바인딩 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = LoginBinding.inflate(getLayoutInflater()); // 뷰 바인딩 초기화
        setContentView(binding.getRoot()); // 뷰 설정

        login = binding.backBtn; // 뒤로 가기 버튼

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 뒤로 가기 버튼 클릭 시 토스트 메시지 표시
                Toast.makeText(LoginActivity.this, "뒤로 이동", Toast.LENGTH_LONG).show();
                // 메인 액티비티로 이동
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
