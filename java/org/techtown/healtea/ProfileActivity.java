package org.techtown.healtea;

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

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private EditText nameTextView, birthTextView, phoneTextView, emergencyTextView, heightTextView, weightTextView, bloodTextView, medicalTextView;
    private ImageView profileImageView;
    private Button alarmButton, galleryButton, defaultButton, saveButton;
    private static final int GALLERY_REQUEST_CODE = 100;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView textView = findViewById(R.id.text_view);
        nameTextView = findViewById(R.id.edit_text_name);
        birthTextView = findViewById(R.id.edit_text_birth);
        phoneTextView = findViewById(R.id.edit_text_phone);
        emergencyTextView = findViewById(R.id.edit_text_emergency);
        heightTextView = findViewById(R.id.edit_text_height);
        weightTextView = findViewById(R.id.edit_text_weight);
        bloodTextView = findViewById(R.id.edit_text_blood);
        medicalTextView = findViewById(R.id.edit_text_medical);
        profileImageView = findViewById(R.id.profile_img);
        galleryButton = findViewById(R.id.btn_gallery);
        defaultButton = findViewById(R.id.btn_basic);
//        alarmButton = findViewById(R.id.button_alarm);
        saveButton = findViewById(R.id.btn_save);

//        alarmButton.setOnClickListener(v -> openNotificationActivity());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            galleryButton.setOnClickListener(v -> openGallery());
            defaultButton.setOnClickListener(v -> showDefaultImageDialog());
            saveButton.setOnClickListener(v -> saveProfileData());

            String userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String userName = user.getDisplayName();
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String birth = dataSnapshot.child("birth").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String emergency = dataSnapshot.child("emergency").getValue(String.class);
                    String height = dataSnapshot.child("height").getValue(String.class);
                    String weight = dataSnapshot.child("weight").getValue(String.class);
                    String blood = dataSnapshot.child("blood").getValue(String.class);
                    String medical = dataSnapshot.child("medical").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    textView.setText("오늘도 좋은 하루, " + userName + "님!");
                    nameTextView.setText(name);
                    birthTextView.setText(birth);
                    phoneTextView.setText(phone);
                    emergencyTextView.setText(emergency);
                    heightTextView.setText(height);
                    weightTextView.setText(weight);
                    bloodTextView.setText(blood);
                    medicalTextView.setText(medical);

                    if (profileImageUrl != null) {
                        Glide.with(ProfileActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.user_profile)
                                .error(R.drawable.error)
                                .into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.profile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openNotificationActivity() {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                Glide.with(this).load(imageUri).into(profileImageView);
                uploadImageToFirebaseStorage(imageUri);
            } else {
                Toast.makeText(this, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String imageName = "profile_images/" + UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child(imageName);

            imageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToDatabase(imageUrl);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "이미지 URL을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            Toast.makeText(this, "이미지를 처리하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            userRef.child("profileImageUrl").setValue(imageUrl).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "프로필 이미지가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "프로필 이미지 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showDefaultImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("기본 사진 선택");

        // 기본 사진을 보여줄 이미지 배열
        final Integer[] defaultImages = {R.drawable.check, R.drawable.error, R.drawable.title};

        // 기본 사진을 보여줄 GridView 생성
        GridView gridView = new GridView(this);
        gridView.setNumColumns(3);
        gridView.setAdapter(new ImageAdapter(this, defaultImages));
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            // 선택한 기본 사진을 프로필 사진으로 설정
            int selectedImageResource = defaultImages[position];
            profileImageView.setImageResource(selectedImageResource);

            // Firebase Storage에 이미지 업로드 및 URL 저장
            uploadResourceImageToFirebaseStorage(selectedImageResource);

            // 다이얼로그 닫기
            alertDialog.dismiss();
        });

        builder.setView(gridView);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void uploadResourceImageToFirebaseStorage(int imageResource) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageResource);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String imageName = "profile_images/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(imageName);

        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveImageUrlToDatabase(imageUrl);
                }).addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "이미지 URL을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileData() {
        String name = nameTextView.getText().toString();
        String birth = birthTextView.getText().toString();
        String phone = phoneTextView.getText().toString();
        String emergency = emergencyTextView.getText().toString();
        String height = heightTextView.getText().toString();
        String weight = weightTextView.getText().toString();
        String blood = bloodTextView.getText().toString();
        String medical = medicalTextView.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            userRef.child("name").setValue(name);
            userRef.child("birth").setValue(birth);
            userRef.child("phone").setValue(phone);
            userRef.child("emergency").setValue(emergency);
            userRef.child("height").setValue(height);
            userRef.child("weight").setValue(weight);
            userRef.child("blood").setValue(blood);
            userRef.child("medical").setValue(medical).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "프로필 데이터가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "프로필 데이터 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}