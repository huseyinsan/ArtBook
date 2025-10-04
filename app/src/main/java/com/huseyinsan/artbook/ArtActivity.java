package com.huseyinsan.artbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.huseyinsan.artbook.databinding.ActivityArtBinding;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.bumptech.glide.Glide;


import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> requestPermissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;
    private MetApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://collectionapi.metmuseum.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(MetApiService.class);

        registerLauncher();

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null );

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){
            //new art

            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);
        }else{
            int artID = intent.getIntExtra("artID",1);
            binding.button.setVisibility(View.INVISIBLE);

            try{

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artID)});
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }


            cursor.close();

                // Saved Data From Database can not be changed
                binding.nameText.setEnabled(false);
                binding.artistText.setEnabled(false);
                binding.yearText.setEnabled(false);
                binding.imageView.setClickable(false);
            }catch (Exception e){
                e.printStackTrace();
            }

        }


    }

    public void searchArt(View view) {
        String query = binding.searchText.getText().toString();
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.searchObjects(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().objectIDs != null && !response.body().objectIDs.isEmpty()) {
                    int firstObjectID = response.body().objectIDs.get(0);

                    fetchArtDetails(firstObjectID);
                }else{
                    Toast.makeText(ArtActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Toast.makeText(ArtActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchArtDetails(int objectID){
        apiService.getObjectDetails(objectID).enqueue(new Callback<ArtObject>() {

            @Override
            public void onResponse(Call<ArtObject> call, Response<ArtObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArtObject artObject = response.body();
                    binding.nameText.setText(artObject.title);
                    binding.artistText.setText(artObject.artistDisplayName);
                    binding.yearText.setText(artObject.objectDate);

                    Glide.with(ArtActivity.this).load(artObject.primaryImageSmall)
                            .placeholder(R.drawable.selectimage)
                            .into(binding.imageView);

                    binding.imageView.setClickable(false);
                }else{
                    Toast.makeText(ArtActivity.this, "Could not fetch details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArtObject> call, Throwable t) {
                Toast.makeText(ArtActivity.this,"Network Error: " + t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void save(View view) {

            Bitmap bitmapToSave = null;

            try {
                Drawable drawable = binding.imageView.getDrawable();

                if (drawable instanceof BitmapDrawable) {
                    bitmapToSave = ((BitmapDrawable) drawable).getBitmap();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bitmapToSave == null) {
                Toast.makeText(this, "Please search or select an image to save!", Toast.LENGTH_LONG).show();
                return;
            }

            String name = binding.nameText.getText().toString();
            String artistName = binding.artistText.getText().toString();
            String year = binding.yearText.getText().toString();

            Bitmap smallImage = makeSmallerImages(bitmapToSave, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            try {
                database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");

                String info = getIntent().getStringExtra("info");

                if (info.equals("new")) {

                    String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)";
                    SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
                    sqLiteStatement.bindString(1, name);
                    sqLiteStatement.bindString(2, artistName);
                    sqLiteStatement.bindString(3, year);
                    sqLiteStatement.bindBlob(4, byteArray);
                    sqLiteStatement.execute();
                } else {

                    int artID = getIntent().getIntExtra("artID", -1);
                    if (artID == -1) {
                        Toast.makeText(this, "ID for update not found!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String sqlString = "UPDATE arts SET artname = ?, paintername = ?, year = ?, image = ? WHERE id = ?";
                    SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
                    sqLiteStatement.bindString(1, name);
                    sqLiteStatement.bindString(2, artistName);
                    sqLiteStatement.bindString(3, year);
                    sqLiteStatement.bindBlob(4, byteArray);
                    sqLiteStatement.bindLong(5, artID);
                    sqLiteStatement.execute();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(ArtActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
    }

    public Bitmap makeSmallerImages(Bitmap image,int maximumSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float)height;
        if(bitmapRatio > 1){
            //landscape
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }else{
            //portrait
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return image.createScaledBitmap(image,width,height,true);
    }

    public void selectImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission Needed For Gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                        }
                    }).show();
                } else {
                    //request permission
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    private void registerLauncher() {


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = o.getData();
                    if (intentFromResult != null) {
                        Uri imageData = intentFromResult.getData();

                        try {
                            if (Build.VERSION.SDK_INT >= 33) {
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {


                if (o) {
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);


                }  else{
                    //Permission denied

                    Toast.makeText(ArtActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}