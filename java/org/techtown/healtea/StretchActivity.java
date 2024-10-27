package org.techtown.healtea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.Spinner;
import android.os.Handler;

public class StretchActivity extends AppCompatActivity{
    private VideoView videoView;
    private ImageButton nextButton;
    private ImageButton againButton;
    private Spinner spinner;
    private SeekBar seekBar;
    private SeekBar discreteSeekBar;
    boolean isPrepared = false;
    boolean isTouch = false;

    int currentIndex = 0;
    private int[] upperBodyVideos = {R.raw.upper_one, R.raw.upper_two, R.raw.upper_three};
    private int[] lowerBodyVideos = {R.raw.lower_one, R.raw.lower_two, R.raw.lower_three};


    @SuppressLint({"RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stretch);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        videoView = findViewById(R.id.videoView);
        nextButton = findViewById(R.id.nextButton);
        againButton = findViewById(R.id.againButton);
        spinner = findViewById(R.id.spinner);
        seekBar = findViewById(R.id.seekBar);
        discreteSeekBar = findViewById(R.id.seekBar2);

        discreteSeekBar.setEnabled(false);
        discreteSeekBar.getThumb().setColorFilter(getResources().getColor(R.color.your_color), PorterDuff.Mode.SRC_IN);
        discreteSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.your_color), PorterDuff.Mode.SRC_IN);

        Toast toast = new Toast(StretchActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,null);

        TextView text = layout.findViewById(R.id.text);
        text.setText("스트레칭 부위를 선택하세요!");

        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();

        // 스피너에 사용될 데이터 설정
        String[] stretchAreas = {"  스트레칭 부위 ▼", "  Upper Body", "  Lower Body"};
        ArrayAdapter<String> adapter = new StretchAdapter(this, android.R.layout.simple_spinner_dropdown_item, stretchAreas);
        spinner.setAdapter(adapter);

        // 스피너 선택 이벤트 처리
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Determine which array to use based on spinner selection
                int[] selectedVideos;
                if (position == 1) {
                    selectedVideos = upperBodyVideos;
                } else if (position == 2) {
                    selectedVideos = lowerBodyVideos;
                } else {
                    // If nothing selected, do nothing
                    return;
                }

                currentIndex = 0;
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + selectedVideos[currentIndex]));
                videoView.start();
                discreteSeekBar.setProgress(currentIndex);

                // Update the tag of videoView to reflect the current category
                if (position == 1) {
                    videoView.setTag("upper");
                } else if (position == 2) {
                    videoView.setTag("lower");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedItemPosition() == 0) {
                    // If not selected, display a toast message
                    Toast toast = new Toast(StretchActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,null);

                    TextView text = layout.findViewById(R.id.text);
                    text.setText("스트레칭 부위를 선택하세요!");

                    toast.setView(layout);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();
                    return; // Exit the method to prevent further execution
                }
                // Determine which array to use based on spinner selection
                int[] selectedVideos;
                if (spinner.getSelectedItemPosition() == 1) {
                    selectedVideos = upperBodyVideos;
                } else if (spinner.getSelectedItemPosition() == 2) {
                    selectedVideos = lowerBodyVideos;
                } else {
                    // If nothing selected, do nothing
                    return;
                }

                if (currentIndex == selectedVideos.length - 1) {
                    // If all videos are played, show toast and return
                    TextView text = layout.findViewById(R.id.text);
                    text.setText("스트레칭이 끝났습니다.");

                    toast.setView(layout);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                currentIndex = (currentIndex + 1) % selectedVideos.length;
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + selectedVideos[currentIndex]));
                videoView.start();

                discreteSeekBar.setProgress(currentIndex);
            }
        });

        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedItemPosition() == 0) {
                    // If not selected, display a toast message
                    Toast toast = new Toast(StretchActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_toast,null);

                    TextView text = layout.findViewById(R.id.text);
                    text.setText("스트레칭 부위를 선택하세요!");

                    toast.setView(layout);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.show();
                    return; // Exit the method to prevent further execution
                }
                videoView.seekTo(0); // 처음부터 다시 재생
                videoView.start();
            }
        });

        // VideoView의 재생 준비가 완료되었을 때 호출되는 리스너 설정
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // 재생 준비가 완료되었을 때
                isPrepared = true;
                seekBar.setMax(videoView.getDuration());

                // 재생 위치 변경 시 seekbar의 thumb 업데이트
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        // 재생이 끝났을 때
                    }
                });

                // SeekBar 설정
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            videoView.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        isTouch = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        videoView.seekTo(seekBar.getProgress());
                        isTouch = false;
                    }
                });

                // 일정 시간마다 VideoView의 현재 위치를 확인하고 seekbar를 업데이트
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isTouch) {
                            seekBar.setProgress(videoView.getCurrentPosition());
                        }
                        handler.postDelayed(this, 1000); // 1초마다 업데이트
                    }
                }, 1000);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // This will close the current activity and go back to the previous one
            return true;
        } else if (id == R.id.stretch) {
            // Handle stretch menu item
            Intent intent = new Intent(this, StretchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.habit) {
            // Handle habit menu item
            Intent intent = new Intent(this, HabitActivity.class); //여기에 가고 싶은 페이지 java 파일 이름으로 바꾸기. HabitActivity
            startActivity(intent);
            return true;
        } else if (id == R.id.setting) {
            // Handle setting menu item
            Intent intent = new Intent(this, ProfileActivity.class); // 이것도 마찬가지로 바꾸기. SettingActivity
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    // 스피너 어댑터 클래스
    private static class StretchAdapter extends ArrayAdapter<String> {
        public StretchAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public boolean isEnabled(int position) {
            // "스트레칭 부위"는 선택 불가능하도록 설정
            return position != 0;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // "스트레칭 부위" 항목을 드롭다운 목록에서 숨깁니다.
            View view = super.getDropDownView(position, convertView, parent);
            TextView textView = (TextView) view;
            if (position == 0) {
                // "스트레칭 부위" 항목은 비활성화되도록 설정
                textView.setTextColor(Color.GRAY);
            } else {
                textView.setTextColor(Color.BLACK);
            }
            return view;
        }
    }
}

