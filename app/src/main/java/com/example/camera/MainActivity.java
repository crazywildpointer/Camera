package com.example.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private PhotoDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new PhotoDatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoAdapter = new PhotoAdapter(getPhotoPathsFromDatabase());
        recyclerView.setAdapter(photoAdapter);

        Button btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查相机和存储权限
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                } else {
                    // 已经有权限，可以执行拍照操作
                    dispatchTakePictureIntent();
                }
            }
        });
    }

    private static final int REQUEST_PERMISSION = 1;


    private ArrayList<String> getPhotoPathsFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"photo_path"};
        String sortOrder = "timestamp DESC";
        Cursor cursor = db.query("photos", projection, null, null, null, null, sortOrder);

        ArrayList<String> photoPaths = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String photoPath = cursor.getString(cursor.getColumnIndex("photo_path"));
                photoPaths.add(photoPath);
            } while (cursor.moveToNext());
        }
        cursor.close();


        return photoPaths;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            savePhotoToDatabase(imageBitmap);
            photoAdapter.setPhotoPaths(getPhotoPathsFromDatabase());
        }
    }

    private void savePhotoToDatabase(Bitmap photo) {
        String photoPath = savePhotoToFile(photo); // 将照片保存到文件，并获取文件路径
        if (photoPath != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("photo_path", photoPath);
            values.put("latitude", 0.0); // 替换为实际的经纬度值
            values.put("longitude", 0.0);

            db.insert("photos", null, values);
            db.close();

        }
    }

    private String savePhotoToFile(Bitmap photo) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
        private ArrayList<String> photoPaths;

        public PhotoAdapter(ArrayList<String> photoPaths) {
            this.photoPaths = photoPaths;
        }

        public void setPhotoPaths(ArrayList<String> photoPaths) {
            this.photoPaths = photoPaths;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            String photoPath = photoPaths.get(position);
            Glide.with(MainActivity.this).load(photoPath).into(holder.photoImageView);
        }

        @Override
        public int getItemCount() {
            return photoPaths.size();
        }

        public class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView photoImageView;

            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                photoImageView = itemView.findViewById(R.id.photoImageView);
            }
        }
    }
}
