package org.techtown.healtea;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";
    private DatabaseReference userRef;
    private EditText recordTextView;
    private Button recordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        recordTextView = findViewById(R.id.Message3);
        recordButton = findViewById(R.id.saveButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            recordButton.setOnClickListener(v -> saveProfileData());

            String userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            // 사용자 이름 가져와서 Message1에 넣기
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue(String.class);
                    if (name != null) {
                        TextView message1TextView = findViewById(R.id.Message1);
                        String message1 = message1TextView.getText().toString();
                        message1 = message1.replace("00", name);
                        message1TextView.setText(message1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                }
            });

            // 현재 날짜를 가져와서 해당 날짜의 데이터를 가져와서 표시
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            userRef.child("records").child(currentDate).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String record = dataSnapshot.getValue(String.class);
                    if (record != null) {
                        recordTextView.setText(record);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                }
            });
        }

        // BottomNavigationView 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.stretch_page) {
                        startActivity(new Intent(MainActivity2.this, StretchActivity.class));
                        return true;
                    } else if (item.getItemId() == R.id.habit_page) {
                        startActivity(new Intent(MainActivity2.this, HabitActivity.class));
                        return true;
                    } else if (item.getItemId() == R.id.setting_page) {
                        startActivity(new Intent(MainActivity2.this, ProfileActivity.class));
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            Log.e(TAG, "BottomNavigationView is null");
        }

        // 현재 날짜와 요일을 가져와서 텍스트뷰를 업데이트
        updateDateTextViews();

        // 요일 버튼 클릭 리스너 설정
        setDayButtonClickListener(R.id.sun);
        setDayButtonClickListener(R.id.mon);
        setDayButtonClickListener(R.id.tue);
        setDayButtonClickListener(R.id.wed);
        setDayButtonClickListener(R.id.thu);
        setDayButtonClickListener(R.id.fri);
        setDayButtonClickListener(R.id.sat);

        // rectangularButton 클릭 리스너 설정
        Button rectangularButton = findViewById(R.id.rectangularButton);
        rectangularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, dailyCheck.class);
                startActivity(intent);
            }
        });

        // 현재 요일에 해당하는 버튼 아래로 movingBar를 이동
        moveBarToCurrentDay();

        // Save button click listener
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });
    }

    private void updateDateTextViews() {
        TextView monthTextView = findViewById(R.id.Month);
        TextView dayTextView = findViewById(R.id.Day);

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더합니다.
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        monthTextView.setText(String.format("%02d.", month)); // 두 자리 숫자로 포맷
        dayTextView.setText(String.format("%02d", day)); // 두 자리 숫자로 포맷
    }

    private void setDayButtonClickListener(int buttonId) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, HabitActivity.class);
                startActivity(intent);
            }
        });
    }

    private void moveBarToCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int buttonId;
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                buttonId = R.id.sun;
                break;
            case Calendar.MONDAY:
                buttonId = R.id.mon;
                break;
            case Calendar.TUESDAY:
                buttonId = R.id.tue;
                break;
            case Calendar.WEDNESDAY:
                buttonId = R.id.wed;
                break;
            case Calendar.THURSDAY:
                buttonId = R.id.thu;
                break;
            case Calendar.FRIDAY:
                buttonId = R.id.fri;
                break;
            case Calendar.SATURDAY:
                buttonId = R.id.sat;
                break;
            default:
                return;
        }

        Button dayButton = findViewById(buttonId);
        View movingBar = findViewById(R.id.movingBar);

        dayButton.post(new Runnable() {
            @Override
            public void run() {
                float dayButtonX = dayButton.getX();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) movingBar.getLayoutParams();
                layoutParams.leftMargin = (int) dayButtonX;
                movingBar.setLayoutParams(layoutParams);
            }
        });
    }

    private void saveProfileData() {
        String record = recordTextView.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            // 현재 날짜를 가져와서 해당 날짜의 데이터를 Firebase에 저장
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            userRef.child("records").child(currentDate).setValue(record).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity2.this, "프로필 데이터가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();

                    // Pass the text to HabitActivity
                    Intent intent = new Intent(MainActivity2.this, HabitActivity.class);
                    intent.putExtra("recordText", record);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity2.this, "프로필 데이터 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

