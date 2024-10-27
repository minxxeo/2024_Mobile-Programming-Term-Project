package org.techtown.healtea;

import static com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class HabitActivity extends AppCompatActivity {
    private TextView currentMonthTextView; // 현재 월을 표시하는 텍스트 뷰
    private int currentYear; // 현재 년도
    private int currentMonth; // 현재 월
    private MaterialButton selectedButton; // 선택된 버튼
    private LinearLayout habitContainer; // 습관 컨테이너 레이아웃
    private String selectedDate; // 선택된 날짜

    private HabitDatabaseHelper dbHelper; // 데이터베이스 헬퍼

    private DatabaseReference userRef; // 사용자 참조
    private Button recordButton; // 기록 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit);
        dbHelper = new HabitDatabaseHelper(this); // 데이터베이스 헬퍼 초기화

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // 현재 사용자 가져오기

        String recordText = getIntent().getStringExtra("recordText"); // 기록 텍스트 가져오기

        TextView newtextView = findViewById(R.id.newtext); // 새로운 텍스트 뷰
        newtextView.setText(recordText); // 텍스트 설정

        currentMonthTextView = findViewById(R.id.textViewCurrentMonth); // 현재 월 텍스트 뷰
        habitContainer = findViewById(R.id.habitContainer); // 습관 컨테이너 레이아웃
        habitMap = new HashMap<>(); // 습관 맵 초기화

        Calendar calendar = Calendar.getInstance(); // 캘린더 인스턴스 가져오기
        currentYear = calendar.get(Calendar.YEAR); // 현재 년도 가져오기
        currentMonth = calendar.get(Calendar.MONTH); // 현재 월 가져오기

        displayCurrentMonth(); // 현재 월 표시
        setupNavigationButtons(); // 네비게이션 버튼 설정
        setupCalendarButtons(); // 캘린더 버튼 설정
        setupAddHabitButton(); // 습관 추가 버튼 설정
        loadHabitsFromDatabase(); // 데이터베이스에서 습관 로드

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화

        recordButton = findViewById(R.id.loadRecordButton); // 기록 버튼

        recordButton.setOnClickListener(v -> openNotificationActivity()); // 기록 버튼 클릭 리스너 설정

        if (user != null) {
            String userId = user.getUid(); // 사용자 ID 가져오기
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId); // 사용자 참조 설정
        }
    }

    private final int[] habitColors = { // 습관 색상 배열
            Color.parseColor("#FFD1DC"), // 파스텔 핑크
            Color.parseColor("#AEC6CF"), // 파스텔 블루
            Color.parseColor("#77DD77"), // 파스텔 그린
            Color.parseColor("#FFDAB9"), // 피치퍼프
            Color.parseColor("#FFB347"), // 파스텔 오렌지
            Color.parseColor("#E6E6FA"), // 라벤더
            Color.parseColor("#F0E68C"), // 카키
            Color.parseColor("#D8BFD8"), // 시슬
            Color.parseColor("#FF968A"), // 토마토
            Color.parseColor("#55CBCD")  // 라이트씨그린
    };

    private Map<String, LinearLayout> habitMap; // 습관 맵

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu); // 메뉴 인플레이트
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // 아이템 ID 가져오기

        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class); // 메인 액티비티로 이동하는 인텐트 생성
            startActivity(intent); // 액티비티 시작
            finish(); // 현재 액티비티 종료
            return true;
        } else if (id == R.id.stretch) {
            // 스트레치 메뉴 아이템 처리
            Intent intent = new Intent(this, StretchActivity.class); // 스트레치 액티비티로 이동하는 인텐트 생성
            startActivity(intent); // 액티비티 시작
            return true;
        } else if (id == R.id.habit) {
            // 습관 메뉴 아이템 처리
            Intent intent = new Intent(this, HabitActivity.class); // 습관 액티비티로 이동하는 인텐트 생성
            startActivity(intent); // 액티비티 시작
            return true;
        } else if (id == R.id.setting) {
            // 설정 메뉴 아이템 처리
            Intent intent = new Intent(this, ProfileActivity.class); // 프로필 액티비티로 이동하는 인텐트 생성
            startActivity(intent); // 액티비티 시작
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void displayCurrentMonth() {
        currentMonthTextView.setText(currentYear + "." + (currentMonth + 1)); // 현재 월 텍스트 뷰에 설정
    }

    private void setupNavigationButtons() {
        Button prevButton = findViewById(R.id.prevButton); // 이전 버튼
        Button nextButton = findViewById(R.id.nextButton); // 다음 버튼

        prevButton.setOnClickListener(v -> {
            currentMonth--; // 현재 월 감소
            if (currentMonth < 0) {
                currentMonth = 11; // 월이 0보다 작으면 11로 설정
                currentYear--; // 년도 감소
            }
            displayCurrentMonth(); // 현재 월 표시
            habitContainer.removeAllViews(); // 습관 컨테이너 초기화
            setupCalendarButtons(); // 캘린더 버튼 설정
            loadHabitsFromDatabase(); // 데이터베이스에서 습관 로드
        });

        nextButton.setOnClickListener(v -> {
            currentMonth++; // 현재 월 증가
            if (currentMonth > 11) {
                currentMonth = 0; // 월이 11보다 크면 0으로 설정
                currentYear++; // 년도 증가
            }
            displayCurrentMonth(); // 현재 월 표시
            habitContainer.removeAllViews(); // 습관 컨테이너 초기화
            setupCalendarButtons(); // 캘린더 버튼 설정
            loadHabitsFromDatabase(); // 데이터베이스에서 습관 로드
        });
    }

    private void setupCalendarButtons() {
        if (selectedButton != null) {
            selectedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))); // 선택된 버튼 초기화
            selectedButton.setStrokeWidth(0);
            selectedButton = null;
        }

        Calendar calendar = Calendar.getInstance(); // 캘린더 인스턴스 가져오기
        calendar.set(currentYear, currentMonth, 1); // 현재 년도와 월로 설정
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 월의 일 수
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 현재 월의 첫 번째 요일

        int dayCounter = 1; // 일 카운터

        for (int week = 0; week < 5; week++) { // 5주 동안 반복
            for (int day = 0; day < 7; day++) { // 7일 동안 반복
                String buttonId = "day" + (week + 1) + "_" + (day + 1); // 버튼 ID 생성
                int resID = getResources().getIdentifier(buttonId, "id", getPackageName()); // 리소스 ID 가져오기
                MaterialButton button = findViewById(resID); // 버튼 가져오기

                String lineId = "lineContainerDay" + (week + 1) + "_" + (day + 1); // 선 컨테이너 ID 생성
                int lineResID = getResources().getIdentifier(lineId, "id", getPackageName()); // 리소스 ID 가져오기
                LinearLayout lineContainer = findViewById(lineResID); // 선 컨테이너 가져오기

                if (lineContainer != null) {
                    lineContainer.removeAllViews(); // 초기 상태에서 숨김
                }

                if (week == 0 && day < firstDayOfWeek) { // 첫 번째 주의 첫 번째 요일 이전
                    if (button != null) {
                        button.setText(""); // 버튼 텍스트 비우기
                    }
                } else if (dayCounter <= daysInMonth) { // 일 카운터가 월의 일 수 이하인 경우
                    if (button != null) {
                        button.setText(String.valueOf(dayCounter)); // 버튼 텍스트 설정
                        button.setTag(currentYear + "-" + (currentMonth + 1) + "-" + dayCounter); // 날짜 태그 설정
                    }
                    dayCounter++;
                } else {
                    if (button != null) {
                        button.setText(""); // 버튼 텍스트 비우기
                    }
                }

                if (button != null) {
                    button.setOnClickListener(v -> {
                        if (selectedButton != null && selectedButton != button) {
                            selectedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))); // 선택된 버튼 초기화
                            selectedButton.setStrokeWidth(0);
                        }
                        if (selectedButton == button) {
                            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))); // 버튼 초기화
                            button.setStrokeWidth(0);
                            selectedButton = null;
                            selectedDate = null;
                            habitContainer.removeAllViews(); // 모든 습관 박스 제거
                        } else {
                            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_red_dark))); // 버튼 강조
                            button.setStrokeWidth(4);
                            selectedButton = button;
                            selectedDate = button.getTag().toString(); // 선택된 날짜 설정
                            displayHabitsForSelectedDate(selectedDate); // 선택된 날짜에 해당하는 습관 박스 표시
                        }
                    });
                }
            }
        }

        updateAllHabitLines(); // 모든 습관 라인 업데이트
    }

    private void displayHabitsForSelectedDate(String date) {
        habitContainer.removeAllViews(); // 모든 습관 박스 제거
        if (habitMap.containsKey(date)) {
            habitContainer.addView(habitMap.get(date)); // 선택된 날짜의 습관 박스 추가
        }
    }

    private void setupAddHabitButton() {
        Button addHabitButton = findViewById(R.id.addHabitButton); // 습관 추가 버튼
        addHabitButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                showAddHabitDialog(); // 습관 추가 다이얼로그 표시
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("날짜를 먼저 선택하세요.") // 날짜 선택 알림
                        .setPositiveButton("확인", null)
                        .show();
            }
        });
    }

    private void showAddHabitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 다이얼로그 빌더
        LayoutInflater inflater = getLayoutInflater(); // 레이아웃 인플레이터
        View dialogView = inflater.inflate(R.layout.dialog_add_habit, null); // 다이얼로그 레이아웃 인플레이트
        EditText habitEditText = dialogView.findViewById(R.id.habitEditText); // 습관 입력 텍스트
        RadioGroup colorRadioGroup = dialogView.findViewById(R.id.colorRadioGroup); // 색상 라디오 그룹

        builder.setView(dialogView)
                .setTitle("습관 추가하기") // 다이얼로그 제목 설정
                .setPositiveButton("추가", (dialog, id) -> {
                    String habitText = habitEditText.getText().toString(); // 습관 텍스트 가져오기
                    int selectedColorIndex = -1;
                    if (colorRadioGroup.getCheckedRadioButtonId() != -1) {
                        RadioButton selectedRadioButton = dialogView.findViewById(colorRadioGroup.getCheckedRadioButtonId());
                        selectedColorIndex = Integer.parseInt(selectedRadioButton.getTag().toString()); // 선택된 색상 인덱스
                    }
                    if (selectedColorIndex != -1) {
                        int selectedColor = habitColors[selectedColorIndex]; // 선택된 색상
                        addHabit(habitText, selectedColor); // 습관 추가
                    } else {
                        // 기본 색상을 설정하거나 오류 메시지를 표시할 수 있습니다.
                    }
                })
                .setNegativeButton("취소", (dialog, id) -> dialog.cancel()); // 취소 버튼

        AlertDialog dialog = builder.create(); // 다이얼로그 생성
        dialog.show(); // 다이얼로그 표시
    }

    private void openNotificationActivity() {
        Intent intent = new Intent(this, NotificationActivity.class); // 알림 액티비티로 이동하는 인텐트 생성
        startActivity(intent); // 액티비티 시작
    }

    private void addHabit(String habitText, int color) {
        LinearLayout habitBox = (LinearLayout) getLayoutInflater().inflate(R.layout.habit_box, null); // 습관 박스 레이아웃 인플레이트
        TextView habitTextView = habitBox.findViewById(R.id.habitTextView); // 습관 텍스트 뷰
        habitTextView.setText(habitText); // 습관 텍스트 설정
        habitBox.setBackgroundColor(color); // 배경 색상 설정

        CheckBox checkBox = habitBox.findViewById(R.id.checkHabitBox); // 체크박스
        int id = dbHelper.addHabit(selectedDate, habitText, color, checkBox.isChecked() ? 1 : 0); // 데이터베이스에 습관 추가
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateHabit(id, habitText, color, isChecked ? 1 : 0); // 습관 업데이트
            updateHabitLine(selectedDate); // 습관 라인 업데이트
        });

        habitBox.setTag(id); // 습관 ID 태그 설정

        Button editButton = habitBox.findViewById(R.id.editHabitButton); // 수정 버튼
        editButton.setOnClickListener(v -> showEditHabitDialog(habitBox, checkBox, color, id)); // 수정 다이얼로그 표시

        Button deleteButton = habitBox.findViewById(R.id.deleteHabitButton); // 삭제 버튼
        deleteButton.setOnClickListener(v -> {
            dbHelper.deleteHabit(id); // 데이터베이스에서 습관 삭제
            habitContainer.removeView(habitBox); // 습관 컨테이너에서 삭제
            habitMap.get(selectedDate).removeView(habitBox); // 습관 맵에서 삭제
            if (habitMap.get(selectedDate).getChildCount() == 0) {
                habitMap.remove(selectedDate); // 습관이 남아 있지 않으면 날짜 삭제
            }
            removeHabitLine(selectedDate, id); // 특정 습관 ID의 선 제거
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16); // 아래쪽에 16dp 간격 추가
        habitBox.setLayoutParams(layoutParams); // 레이아웃 파라미터 설정

        if (!habitMap.containsKey(selectedDate)) {
            LinearLayout dateHabitsContainer = new LinearLayout(this); // 날짜별 습관 컨테이너 생성
            dateHabitsContainer.setOrientation(LinearLayout.VERTICAL); // 수직 방향 설정
            habitMap.put(selectedDate, dateHabitsContainer); // 습관 맵에 추가
        }

        if (habitBox.getParent() != habitMap.get(selectedDate)) {
            habitMap.get(selectedDate).addView(habitBox); // 습관 박스 추가
        }

        if (habitBox.getParent() != null) {
            ((ViewGroup) habitBox.getParent()).removeView(habitBox); // 기존 부모에서 제거
        }
        habitContainer.addView(habitBox); // 습관 컨테이너에 추가
        updateHabitLine(selectedDate); // 습관 라인 업데이트
    }

    private void updateHabitLine(String date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(currentYear, currentMonth, 1); // 현재 년도와 월로 설정
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 월의 일 수

            for (int day = 1; day <= daysInMonth; day++) {
                String currentDate = currentYear + "-" + (currentMonth + 1) + "-" + day; // 현재 날짜
                String buttonId = "day" + ((day + calendar.get(Calendar.DAY_OF_WEEK) - 2) / 7 + 1) + "_" + ((day + calendar.get(Calendar.DAY_OF_WEEK) - 2) % 7 + 1); // 버튼 ID 생성
                int resID = getResources().getIdentifier(buttonId, "id", getPackageName()); // 리소스 ID 가져오기
                MaterialButton button = findViewById(resID); // 버튼 가져오기

                String lineId = "lineContainerDay" + ((day + calendar.get(Calendar.DAY_OF_WEEK) - 2) / 7 + 1) + "_" + ((day + calendar.get(Calendar.DAY_OF_WEEK) - 2) % 7 + 1); // 선 컨테이너 ID 생성
                int lineResID = getResources().getIdentifier(lineId, "id", getPackageName()); // 리소스 ID 가져오기
                LinearLayout lineContainer = findViewById(lineResID); // 선 컨테이너 가져오기

                if (lineContainer != null) {
                    lineContainer.removeAllViews(); // 선 컨테이너 초기화

                    Cursor cursor = dbHelper.getHabits(currentDate); // 현재 날짜의 습관 가져오기
                    if (cursor.moveToFirst()) {
                        do {
                            int habitId = cursor.getInt(cursor.getColumnIndexOrThrow("_id")); // 습관 ID 가져오기
                            int isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("is_checked")); // 체크 상태
                            int color = cursor.getInt(cursor.getColumnIndexOrThrow("color")); // 색상
                            if (isChecked == 1) {
                                View line = new View(this); // 선 뷰 생성
                                line.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        10
                                ));
                                line.setBackgroundColor(color); // 배경 색상 설정
                                line.setTag(habitId); // 습관 ID 태그 설정
                                lineContainer.addView(line); // 선 컨테이너에 추가
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close(); // 커서 닫기

                    if (lineContainer.getChildCount() > 0) {
                        lineContainer.setVisibility(View.VISIBLE); // 선 컨테이너 표시
                    } else {
                        lineContainer.setVisibility(View.GONE); // 선 컨테이너 숨김
                    }
                }
            }
        }
    }

    private void updateAllHabitLines() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth, 1); // 현재 년도와 월로 설정
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 월의 일 수

        for (int day = 1; day <= daysInMonth; day++) {
            String date = currentYear + "-" + (currentMonth + 1) + "-" + day; // 현재 날짜
            updateHabitLine(date); // 모든 습관 라인 업데이트
        }
    }

    private void showEditHabitDialog(LinearLayout habitBox, CheckBox checkBox, int currentColor, int habitId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 다이얼로그 빌더
        LayoutInflater inflater = getLayoutInflater(); // 레이아웃 인플레이터
        View dialogView = inflater.inflate(R.layout.dialog_add_habit, null); // 다이얼로그 레이아웃 인플레이트
        EditText habitEditText = dialogView.findViewById(R.id.habitEditText); // 습관 입력 텍스트
        RadioGroup colorRadioGroup = dialogView.findViewById(R.id.colorRadioGroup); // 색상 라디오 그룹

        TextView habitTextView = habitBox.findViewById(R.id.habitTextView); // 습관 텍스트 뷰
        habitEditText.setText(habitTextView.getText().toString()); // 현재 습관 텍스트 설정

        for (int i = 0; i < colorRadioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) colorRadioGroup.getChildAt(i); // 라디오 버튼
            if (habitColors[Integer.parseInt(radioButton.getTag().toString())] == currentColor) {
                radioButton.setChecked(true); // 현재 색상 선택
                break;
            }
        }

        builder.setView(dialogView)
                .setTitle("수정") // 다이얼로그 제목
                .setPositiveButton("수정", (dialog, id) -> {
                    String habitText = habitEditText.getText().toString(); // 습관 텍스트
                    RadioButton selectedRadioButton = dialogView.findViewById(colorRadioGroup.getCheckedRadioButtonId());
                    int selectedColorIndex = Integer.parseInt(selectedRadioButton.getTag().toString()); // 선택된 색상 인덱스
                    int selectedColor = habitColors[selectedColorIndex]; // 선택된 색상
                    habitTextView.setText(habitText); // 습관 텍스트 설정
                    habitBox.setBackgroundColor(selectedColor); // 배경 색상 설정

                    dbHelper.updateHabit(habitId, habitText, selectedColor, checkBox.isChecked() ? 1 : 0); // 습관 업데이트

                    updateHabitLine(selectedDate); // 습관 라인 업데이트
                })
                .setNegativeButton("취소", (dialog, id) -> dialog.cancel()); // 취소 버튼

        AlertDialog dialog = builder.create(); // 다이얼로그 생성
        dialog.show(); // 다이얼로그 표시
    }

    private void loadHabitsFromDatabase() {
        habitMap.clear(); // 습관 맵 초기화
        habitContainer.removeAllViews(); // 습관 컨테이너 초기화

        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth, 1); // 현재 년도와 월로 설정
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 월의 일 수

        for (int day = 1; day <= daysInMonth; day++) {
            String date = currentYear + "-" + (currentMonth + 1) + "-" + day; // 현재 날짜
            loadHabitsForDate(date); // 날짜별 습관 로드
        }
    }

    private void loadHabitsForDate(String date) {
        Cursor cursor = dbHelper.getHabits(date); // 날짜별 습관 가져오기
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id")); // 습관 ID 가져오기
                String habitText = cursor.getString(cursor.getColumnIndexOrThrow("habit")); // 습관 텍스트
                int color = cursor.getInt(cursor.getColumnIndexOrThrow("color")); // 색상
                int isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("is_checked")); // 체크 상태

                LinearLayout habitBox = (LinearLayout) getLayoutInflater().inflate(R.layout.habit_box, null); // 습관 박스 인플레이트
                TextView habitTextView = habitBox.findViewById(R.id.habitTextView); // 습관 텍스트 뷰
                habitTextView.setText(habitText); // 습관 텍스트 설정
                habitBox.setBackgroundColor(color); // 배경 색상 설정

                CheckBox checkBox = habitBox.findViewById(R.id.checkHabitBox); // 체크박스
                checkBox.setChecked(isChecked == 1); // 체크 상태 설정
                checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
                    dbHelper.updateHabit(id, habitText, color, checked ? 1 : 0); // 습관 업데이트
                    updateHabitLine(date); // 습관 라인 업데이트
                });

                Button editButton = habitBox.findViewById(R.id.editHabitButton); // 수정 버튼
                editButton.setOnClickListener(v -> showEditHabitDialog(habitBox, checkBox, color, id)); // 수정 다이얼로그 표시

                Button deleteButton = habitBox.findViewById(R.id.deleteHabitButton); // 삭제 버튼
                deleteButton.setOnClickListener(v -> {
                    dbHelper.deleteHabit(id); // 데이터베이스에서 습관 삭제
                    habitContainer.removeView(habitBox); // 습관 컨테이너에서 삭제
                    habitMap.get(date).removeView(habitBox); // 습관 맵에서 삭제
                    if (habitMap.get(date).getChildCount() == 0) {
                        habitMap.remove(date); // 습관이 남아 있지 않으면 날짜 삭제
                    }
                    removeHabitLine(date, id); // 특정 습관 ID의 선 제거
                });

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(0, 0, 0, 16); // 아래쪽에 16dp 간격 추가
                habitBox.setLayoutParams(layoutParams); // 레이아웃 파라미터 설정

                if (!habitMap.containsKey(date)) {
                    LinearLayout dateHabitsContainer = new LinearLayout(this); // 날짜별 습관 컨테이너 생성
                    dateHabitsContainer.setOrientation(LinearLayout.VERTICAL); // 수직 방향 설정
                    habitMap.put(date, dateHabitsContainer); // 습관 맵에 추가
                }
                habitMap.get(date).addView(habitBox); // 습관 박스 추가
            } while (cursor.moveToNext());
        }
        cursor.close(); // 커서 닫기
        updateHabitLine(date); // 습관 라인 업데이트
    }

    private void removeHabitLine(String date, int habitId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth, 1); // 현재 년도와 월로 설정
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 월의 일 수

        for (int day = 1; day <= daysInMonth; day++) {
            String currentDate = currentYear + "-" + (currentMonth + 1) + "-" + day; // 현재 날짜
            if (currentDate.equals(date)) {
                String lineId = "lineContainerDay" + ((day + calendar.get(Calendar.DAY_OF_WEEK) - 2) / 7 + 1) + "_" + ((day + calendar.get(Calendar.DAY_OF_WEEK) - 2) % 7 + 1); // 선 컨테이너 ID 생성
                int lineResID = getResources().getIdentifier(lineId, "id", getPackageName()); // 리소스 ID 가져오기
                LinearLayout lineContainer = findViewById(lineResID); // 선 컨테이너 가져오기

                if (lineContainer != null) {
                    for (int i = 0; i < lineContainer.getChildCount(); i++) {
                        View line = lineContainer.getChildAt(i); // 선 가져오기
                        if ((int) line.getTag() == habitId) {
                            lineContainer.removeView(line); // 선 제거
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    private void setupRowClickListeners() {
        int[] rowIds = {
                R.id.linearLayoutDaysRow1, // 첫 번째 줄
                R.id.linearLayoutDaysRow2, // 두 번째 줄
                R.id.linearLayoutDaysRow3, // 세 번째 줄
                R.id.linearLayoutDaysRow4, // 네 번째 줄
                R.id.linearLayoutDaysRow5  // 다섯 번째 줄
        };
    }
}
