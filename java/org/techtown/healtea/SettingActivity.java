//package org.techtown.healtea;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class SettingActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setting);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 이거 액션바 이전 버튼임. 지우면 ㄴㄴ
//    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == android.R.id.home) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish(); // This will close the current activity and go back to the previous one
//            return true;
//        } else if (id == R.id.stretch) {
//            // Handle stretch menu item
//            Intent intent = new Intent(this, StretchActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (id == R.id.habit) {
//            // Handle habit menu item
//            Intent intent = new Intent(this, HabitActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (id == R.id.setting) {
//            // Handle setting menu item
//            Intent intent = new Intent(this, SettingActivity.class);
//            startActivity(intent);
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
//    }                                                   // 내가 추가해놓은것들 다 액션바야 !! 지우지 마삼 plz~
//}
//
