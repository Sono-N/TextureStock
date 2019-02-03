package com.example.admin.texturestock;

import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.Manifest;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import com.example.admin.texturestock.CustomOpenCVLoader;


//AppCompatActivityを継承したMainActivity
public class MainActivity extends AppCompatActivity {
    static final int REQUEST_CAPTURE_IMAGE = 100;
    static final int REQUEST_ACCESS_STRAGE = 200;
    static final int REQUEST_PERMISSION = 1002;

    public static final String EXTRA_URI = "com.example.admin.texturestock.URI";
    //private Bitmap bitmap;

    ImageButton button1;
    ImageButton button2;
    ImageButton button3;
    ImageView imageView1;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Filed OpenCVLoader.initDebug()");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //savedInstanceStateにActivityが変化する時のデータを一時保存
        super.onCreate(savedInstanceState);
        //super class
        //この子クラスから親クラスの(overrideする前の)onCreate()を実行
        setContentView(R.layout.activity_main); //UI表示

        //Permission Check
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
        findViews();
        setListeners();
    }

    protected void findViews(){
        button1 = (ImageButton)findViewById(R.id.button1);
        button2 = (ImageButton)findViewById(R.id.button2);
        button3 = (ImageButton)findViewById(R.id.button3);
        imageView1 = (ImageView)findViewById(R.id.imageView1);
    }

    private String filePath;
    private Uri imgUri;

    protected void setListeners(){
        //Camera起動
        button1.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                File cameraFolder = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM),"Camera");

                //ファイル名を設定
                String fileName = new SimpleDateFormat(
                        "ddHHmmss", Locale.US).format(new Date());
                filePath = String.format("%s/%s.jpg", cameraFolder.getPath(),fileName);

                File cameraFile = new File(filePath);
                imgUri = FileProvider.getUriForFile(
                        MainActivity.this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        cameraFile);


                Intent intent = new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE);
                //MediaStore.ACTION_IMAGE_CAPTUREはカメラで撮影した画像を取得
                //Intent.ACTION_GET_CONTENTはギャラリーから画像を選択する

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);

                startActivityForResult(
                        intent,
                        REQUEST_CAPTURE_IMAGE);
            }
        });

        //ギャラリーから選択
        button2.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intentGallery;

                if (Build.VERSION.SDK_INT < 19) {
                    intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
                    intentGallery.setType("image/*");
                } else {
                    intentGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
                    intentGallery.setType("image/jpeg");
                }
                //intentGallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(
                        intentGallery,
                        REQUEST_ACCESS_STRAGE);
            }
        });

        //Texture Stockのフォルダを開く
        button3.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StockActivity.class);
                startActivity(intent);
            }
        });
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    //mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
        //CustomOpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }


    @Override
    protected void onActivityResult(
            //受け取ったrequestCodeでActivityを判別
            //startActivityForResultで渡したものと同じならよい
            int requestCode,
            //RESULT_OKかRESULT_CANCELED
            int resultCode,
            //結果のデータ
            Intent data) {
        if(requestCode == REQUEST_CAPTURE_IMAGE
                && resultCode == Activity.RESULT_OK ){
            if(imgUri != null){
                registerDatabase(filePath);

                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("EXTRA_URI", imgUri.toString());
                startActivity(intent);
            }
            else{
            }
        }
        if(requestCode == REQUEST_ACCESS_STRAGE
                && resultCode == Activity.RESULT_OK ){
            /*if (data.getData() != null) {
                Uri uri = data.getData();
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("EXTRA_URI", uri.toString());
                startActivity(intent);
            }*/
            try {
                Uri uri = data.getData();

                ParcelFileDescriptor pfDescriptor = null;
                pfDescriptor = getContentResolver().openFileDescriptor(uri, "r");

                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                //startActivity(intent);
                //Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("EXTRA_URI", uri.toString());
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerDatabase(String file) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", file);
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
    }

    // Runtime Permission check
    private void checkPermission(){
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
        }
        // 拒否していた場合
        else{
            requestPermission();
        }
    }

    // 許可を求める
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this,
                    "アプリが実行できません",
                    Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された場合
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast toast = Toast.makeText(this,
                        "アプリを実行できませんでした", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}