package org.techtown.healtea;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class dailyCheck extends AppCompatActivity {
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private int[] scores = new int[20];
    private int totalScore = 0;
    private int thresholdScore1 = 13;
    private int thresholdScore2 = 17;
    private int thresholdScore3 = 19;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dailycheck);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateDateTextViews();

        progressBar = findViewById(R.id.progressBar);

        // 파이어베이스에서 사용자 이름 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userName = dataSnapshot.getValue(String.class);
                    if (userName != null) {
                        TextView introMessage = findViewById(R.id.introMessage);
                        introMessage.setText(userName + "님! 오늘 하루는 어떠셨나요?");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // 처리하지 않음
                }
            });
        }

        // 초기화 시 모든 점수를 -1로 설정
        for (int i = 0; i < scores.length; i++) {
            scores[i] = -1;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateDateTextViews();

        progressBar = findViewById(R.id.progressBar);

        // 모든 RadioGroup에 대해 리스너 설정
        for (int i = 1; i <= 20; i++) {
            String radioGroupId = "satisfactionGroup" + i;
            int resId = getResources().getIdentifier(radioGroupId, "id", getPackageName());
            RadioGroup radioGroup = findViewById(resId);
            setupRadioGroupListener(radioGroup, i - 1);  // 인덱스는 0부터 시작하므로 i-1
        }

        Button saveBtn = findViewById(R.id.submitBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allQuestionsAnswered()) {
                    updateTotalScore(); // 총점 업데이트

                    if (totalScore <= thresholdScore1) {
                        showPopUp("설문 제출 완료!\n 오늘 하루도 수고 많으셨어요🤩");
                    } else if (totalScore < thresholdScore2) {
                        showPopUp("오늘 하루 스트레스로 많이 힘드셨군요! 스트레칭을 하면서 스트레스를 날려봐요!");
                    } else if (totalScore < thresholdScore3) {
                        showPopUp("많이 고된 하루였군요😭 취미 생활을 하며 스트레스를 날려보면 어떨까요??");
                    } else {
                        showPopUp("높은 스트레스 지수는 정신 질환으로 이어질 수 있어요.\n상담 전화: 02-1234-5678\n너무 걱정하지 마세요. 꾸준한 관리로 완화될 수 있어요!\n오늘 하루도 수고 많으셨어요🥰");
                    }

                    Toast.makeText(dailyCheck.this, "총점: " + totalScore, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(dailyCheck.this, "모든 질문에 응답 해주세요😂", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showPopUp(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage(message);

        builder.setPositiveButton("확인했습니다.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupRadioGroupListener(RadioGroup radioGroup, final int index) {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (progressStatus < 100) {
                    progressStatus += 5; // 각 문항 완료 시 5% 증가
                    progressBar.setProgress(progressStatus);
                }
                RadioButton selectedButton = group.findViewById(checkedId);
                if (selectedButton != null && selectedButton.getTag() != null) {
                    try {
                        int score = Integer.parseInt(selectedButton.getTag().toString()); // Tag에서 점수 읽기
                        scores[index] = score;  // 해당 RadioGroup의 점수 저장
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateTotalScore() {
        totalScore = 0; // 클래스 멤버 변수를 초기화
        for (int score : scores) {
            totalScore += score;
        }
    }

    private boolean allQuestionsAnswered() {
        for (int score : scores) {
            if (score == -1) return false;
        }
        return true;
    }

    private void updateDateTextViews() {
        TextView monthTextView = findViewById(R.id.Month);
        TextView dayTextView = findViewById(R.id.Date);

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DAY_OF_MONTH);

        monthTextView.setText(String.format("%02d.", month));
        dayTextView.setText(String.format("%02d", date));
    }
}
