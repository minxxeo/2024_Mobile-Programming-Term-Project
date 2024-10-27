package org.techtown.healtea;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.techtown.healtea.AlarmReceiver;

import java.util.Calendar;

public class NotificationActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1; // 알림 권한 요청 코드
    private static final int REQUEST_CODE_SCHEDULE_EXACT_ALARM = 2; // 정확한 알람 스케줄링 권한 요청 코드

    private TextView selectedTimeTextView; // 선택된 시간 텍스트뷰
    private EditText labelEditText; // 라벨 입력 텍스트
    private Button selectTimeButton; // 시간 선택 버튼
    private Button saveButton; // 저장 버튼
    private Button cancelButton; // 취소 버튼
    private Calendar alarmTime; // 알람 시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        selectedTimeTextView = findViewById(R.id.selectedTime); // 선택된 시간 텍스트뷰 연결
        labelEditText = findViewById(R.id.labelEditText); // 라벨 입력 텍스트 연결
        selectTimeButton = findViewById(R.id.selectTimeBtn); // 시간 선택 버튼 연결
        saveButton = findViewById(R.id.saveBtn); // 저장 버튼 연결
        cancelButton = findViewById(R.id.cancelBtn); // 취소 버튼 연결

        selectTimeButton.setOnClickListener(v -> showTimePickerDialog()); // 시간 선택 버튼 클릭 리스너 설정
        saveButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!hasScheduleExactAlarmPermission()) { // 정확한 알람 스케줄링 권한이 없으면
                    requestScheduleExactAlarmPermission(); // 권한 요청
                } else {
                    saveAlarm(); // 알람 저장
                }
            } else {
                saveAlarm(); // 알람 저장
            }
        });

        cancelButton.setOnClickListener(v -> cancelAlarm()); // 취소 버튼 클릭 리스너 설정

        // 알림 권한 확인 및 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }

    // 정확한 알람 스케줄링 권한 확인
    private boolean hasScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // 정확한 알람 스케줄링 권한 요청
    private void requestScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                Toast.makeText(this, "정확한 알람 스케줄링 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM);
            }
        }
    }

    // 시간 선택 다이얼로그 표시
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    alarmTime = Calendar.getInstance();
                    alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    alarmTime.set(Calendar.MINUTE, minute1);
                    alarmTime.set(Calendar.SECOND, 0);

                    String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                    int hour12Format = (hourOfDay > 12) ? hourOfDay - 12 : hourOfDay;
                    if (hour12Format == 0) {
                        hour12Format = 12;
                    }

                    selectedTimeTextView.setText(String.format("%02d:%02d %s", hour12Format, minute1, amPm));
                },
                hour, minute, false);
        timePickerDialog.show();
    }

    // 알람 저장
    private void saveAlarm() {
        if (alarmTime == null) {
            // 시간이 선택되지 않은 경우 메시지 표시
            Toast.makeText(this, "먼저 시간을 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String label = labelEditText.getText().toString(); // 라벨 텍스트 가져오기
        long triggerAtMillis = alarmTime.getTimeInMillis() - System.currentTimeMillis() + SystemClock.elapsedRealtime();

        // AlarmManager를 사용하여 알람 설정
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("label", label); // 인텐트에 라벨 추가
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);

        // 알람이 설정되었음을 나타내는 토스트 메시지 표시
        Toast.makeText(this, "알람 설정됨: " + selectedTimeTextView.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    // 알람 취소
    private void cancelAlarm() {
        // AlarmManager를 사용하여 알람 취소
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);

        // 알람이 취소되었음을 나타내는 토스트 메시지 표시
        Toast.makeText(this, "알람 취소됨", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCHEDULE_EXACT_ALARM) {
            if (hasScheduleExactAlarmPermission()) {
                saveAlarm(); // 정확한 알람 스케줄링 권한이 있으면 알람 저장
            } else {
                Toast.makeText(this, "정확한 알람 스케줄링 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "알림 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
