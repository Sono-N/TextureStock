package com.example.admin.texturestock;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import android.graphics.Paint;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static org.opencv.core.CvType.CV_8UC1;

public class SubActivity extends AppCompatActivity {

    private LruCache<String, Bitmap> mMemoryCache;
    Bitmap beforebitmap;//元に戻す用
    Bitmap nowbitmap; //回転・変形前bitmap
    Bitmap displaybitmap; //今表示されているbitmap
    int imgW;
    int imgH;
    int rot;
    int nowrot;
    public Point lt;
    public Point rb;

    boolean left_to_right_bool = true; //mirroring
    boolean top_to_down_bool = true;

    int r = 20;

    private boolean isTrimtool = false;
    private boolean isRotate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Button returnButton = findViewById(R.id.save_button);
        ImageView imageViewE = findViewById(R.id.imageViewE);

        Intent intent = getIntent();
        Uri imgUri;
        imgUri = Uri.parse(intent.getStringExtra("EXTRA_URI"));
        //imageViewE.setImageURI(imgUri);

        int size = 512;
        try {
            ContentResolver cr = getContentResolver();
            InputStream in = cr.openInputStream(imgUri);
            BitmapFactory.Options imageOptions = new BitmapFactory.Options();
            float imageScaleWidth = (float)imageOptions.outWidth / size;
            float imageScaleHeight = (float)imageOptions.outHeight / size;
            imageOptions.inSampleSize = 4;
            if(imageScaleHeight > 4 || imageScaleWidth > 4){
                imageOptions.inSampleSize = 4;
            }else{
                imageOptions.inSampleSize = 2;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(in,null, imageOptions);
            imageViewE.setImageBitmap(bitmap);
            in.close();
        }catch (Exception e) {
            imageViewE.setImageResource(R.drawable.karitop);
            e.printStackTrace();
        }

        BitmapDrawable drawable = (BitmapDrawable) imageViewE.getDrawable();
        nowbitmap = drawable.getBitmap();

        displaybitmap = nowbitmap.copy(Bitmap.Config.RGB_565, true);
        Mat mat = new Mat();
        Utils.bitmapToMat(nowbitmap, mat);
        imgW = mat.width();
        imgH = mat.height();
        rot = 0;
        SeekBar bar2 = (SeekBar) findViewById(R.id.SeekBar2);
        bar2.setVisibility(View.INVISIBLE);
        float trans_x = imageViewE.getWidth();
        bar2.setTranslationX(400);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save();
            }
        });
        imageViewE.setImageBitmap(nowbitmap);
    }

    public void all_ui_clear() {
        SeekBar bar1 = (SeekBar) findViewById(R.id.SeekBar1);
        bar1.setVisibility(View.INVISIBLE);
        SeekBar bar2 = (SeekBar) findViewById(R.id.SeekBar2);
        bar2.setVisibility(View.INVISIBLE);
        Button buttonD = (Button) findViewById(R.id.buttonD);
        buttonD.setVisibility(View.INVISIBLE);

        Button button_menu1 = (Button) findViewById(R.id.button_menu1);
        button_menu1.setVisibility(View.INVISIBLE);
        Button button_menu2 = (Button) findViewById(R.id.button_menu2);
        button_menu2.setVisibility(View.INVISIBLE);
        SeekBar bar_menu2 = (SeekBar) findViewById(R.id.SeekBar_menu2);
        bar_menu2.setVisibility(View.INVISIBLE);

        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        imageViewE.setImageBitmap(nowbitmap);

        displaybitmap = nowbitmap.copy(Bitmap.Config.RGB_565, true);;
        rot = 0;
        isTrimtool = false;
    }


    //並べる
    public void tilingupdate(int num){
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        Mat mat = new Mat();
        Bitmap bitmap = nowbitmap.copy(Bitmap.Config.RGB_565, true);
        Utils.bitmapToMat(bitmap, mat);

        if(num > 0) {
            Mat row_mat = new Mat();
            List<Mat> row_mats = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                row_mats.add(mat);
            }
            Core.hconcat(row_mats, row_mat);
            Mat out_mat = new Mat();
            List<Mat> out_mats = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                out_mats.add(row_mat);
            }
            Core.vconcat(out_mats, out_mat);
            Imgproc.resize(out_mat, out_mat, new Size(mat.size().width, mat.size().height));
            Utils.matToBitmap(out_mat, displaybitmap);
        }
        imageViewE.setImageBitmap(displaybitmap);
    }

    public void tiling(View view){
        all_ui_clear();
        final SeekBar bar1 = (SeekBar) findViewById(R.id.SeekBar1);
        bar1.setVisibility(View.VISIBLE);
        Button buttonD = (Button)findViewById(R.id.buttonD);
        buttonD.setVisibility(View.VISIBLE);
        bar1.setProgress(72);
        tilingupdate(2);

        bar1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int tiling_num = 1;
                    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        int new_tiling_num = (int)(seekbar.getProgress()+72)/72 ; //360=5*73
                        if(tiling_num != new_tiling_num){
                            tilingupdate(new_tiling_num);
                            tiling_num = new_tiling_num;
                        }
                    }
                    public void onStartTrackingTouch(SeekBar seekbar) {
                    }
                    public void onStopTrackingTouch(SeekBar seekbar) {
                    }
                }
        );
        buttonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowbitmap = displaybitmap;
                all_ui_clear();
            }
        });
    }


    public void gammaupdate(double gamma){
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        Mat mat = new Mat();
        Bitmap bitmap = nowbitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(nowbitmap, mat);

        Mat lut = new Mat(1, 256, CV_8UC1);
        lut.setTo(new Scalar(0));
        for (int i = 0; i < 256; i++)
        {
            lut.put(0, i, Math.pow((double)(1.0 * i/255), 1/gamma) * 255);
        }
        Core.LUT(mat, lut, mat);
        Utils.matToBitmap(mat, bitmap);
        imageViewE.setImageBitmap(bitmap);
        displaybitmap = bitmap;
    }

    public void gamma(View v) {
        all_ui_clear();

        final SeekBar bar1 = (SeekBar) findViewById(R.id.SeekBar1);
        bar1.setVisibility(View.VISIBLE);
        bar1.setProgress(180);
        gammaupdate(1.0);
        Button buttonD = (Button) findViewById(R.id.buttonD);
        buttonD.setVisibility(View.VISIBLE);
        bar1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    double now_gamma;
                    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                        now_gamma = seekbar.getProgress()/180.0;
                        gammaupdate(now_gamma);
                    }
                    public void onStartTrackingTouch(SeekBar seekbar) {
                    }
                    public void onStopTrackingTouch(SeekBar seekbar) {
                    }
                }
        );
        buttonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowbitmap = displaybitmap;
                all_ui_clear();
            }
        });
    }


    public Mat blend(Mat mat1, Mat mat2, Mat alpha){
        if(mat1.cols()!=mat2.cols() && mat1.rows()!=mat2.rows()){
            //Error size mismatch
            return mat1;
        }

        List<Mat> bgr1 = new ArrayList<>();
        Core.split(mat1, bgr1);
        List<Mat> bgr2 = new ArrayList<>();
        Core.split(mat1, bgr2);

        Mat blended_mat = new Mat();
        mat1.copyTo(blended_mat);
        List<Mat> blended_mat_rgb = new ArrayList<>();
        Core.split(mat1, blended_mat_rgb);

        for(int ch=0; ch<3; ch++) {
            for (int r = 0; r < mat1.rows(); r++) {   //画像の横幅分for文を回す(img:画像1, img:画像2)
                for (int c = 0; c < mat1.cols(); c++) {
                    double alp_value = alpha.get(r,c)[0]/255;
                    double value = mat1.get(r,c)[ch] * alp_value + mat2.get(r,c)[ch] * (1-alp_value);
                    blended_mat_rgb.get(ch).put(r, c, value);
                }
            }
        }
        Core.merge(blended_mat_rgb, blended_mat);
        return blended_mat;
    }

    public void blend_x(int blend_width, double pos_x){
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        int x = (int)(pos_x * imgW);

        Mat mat = new Mat();
        Bitmap bitmap = nowbitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(nowbitmap, mat);

        Mat flip_mat_x = new Mat(); //x軸方向・水平反転
        Core.flip(mat, flip_mat_x, 1);

        Mat display_mirrored_mat  = new Mat();
        Utils.bitmapToMat(displaybitmap, display_mirrored_mat);

        if(blend_width > 0){
            Mat mat1 = new Mat(flip_mat_x, new Rect(x - blend_width, 0, blend_width * 2, imgH));
            Mat mat2 = new Mat(mat, new Rect(x - blend_width, 0, blend_width * 2, imgH));

            Mat alpha = new Mat(mat1.rows(), mat1.cols(), CV_8UC1);
            for (int r = 0; r < alpha.rows(); r++) {  //画像の横幅分for文を回す(img:画像1, img:画像2)
                for (int c = 0; c < alpha.cols(); c++) {
                    double value = 255 * c / alpha.cols();
                    alpha.put(r, c, value);
                }
            }
            Mat blend_mat = blend(mat1, mat2, alpha);

            Mat mat_roi = display_mirrored_mat.submat(new Rect(x - blend_width, 0, blend_width * 2, imgH));
            blend_mat.copyTo(mat_roi);
        }
        Utils.matToBitmap(display_mirrored_mat, bitmap);
        imageViewE.setImageBitmap(bitmap);
    }

    public void mirrorupdate_x(double pos_x, boolean left_to_right){
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        int x = (int)(pos_x * imgW);
        Mat mat = new Mat();
        Bitmap bitmap = nowbitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(nowbitmap, mat);

        Mat flip_mat_x = new Mat(); //x軸方向・水平反転
        Core.flip(mat, flip_mat_x, 1);

        if(left_to_right){
            Mat right_mat = new Mat(flip_mat_x, new Rect(x, 0, imgW-x, imgH));
            Mat mat_roi = mat.submat(new Rect(x, 0, imgW-x, imgH));
            right_mat.copyTo(mat_roi);
        }else{
            Mat right_mat = new Mat(mat, new Rect(x, 0, imgW-x, imgH));
            Mat mat_roi = flip_mat_x.submat(new Rect(x, 0, imgW-x, imgH));
            right_mat.copyTo(mat_roi);
            flip_mat_x.copyTo(mat);
        }

        Utils.matToBitmap(mat, bitmap);
        imageViewE.setImageBitmap(bitmap);
        displaybitmap = bitmap;
    }

    public void mirror_x(View v) {
        all_ui_clear();
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);
        imageViewE.setImageBitmap(nowbitmap);

        final SeekBar bar1 = (SeekBar) findViewById(R.id.SeekBar1);
        bar1.setVisibility(View.VISIBLE);

        final SeekBar bar_menu2 = (SeekBar) findViewById(R.id.SeekBar_menu2);
        bar_menu2.setVisibility(View.VISIBLE);
        Button button_menu1 = (Button) findViewById(R.id.button_menu1);
        button_menu1.setVisibility(View.VISIBLE);
        Button button_menu2 = (Button) findViewById(R.id.button_menu2);
        button_menu2.setVisibility(View.VISIBLE);

        mirrorupdate_x(0.5,true);
        bar1.setProgress(180);
        mirrorupdate_x(0.5,true);
        Button buttonD = (Button) findViewById(R.id.buttonD);
        buttonD.setVisibility(View.VISIBLE);

        bar1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    double x;
                    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                        x = seekbar.getProgress()/360.0;
                        mirrorupdate_x(x,left_to_right_bool);
                    }
                    public void onStartTrackingTouch(SeekBar seekbar) {
                    }
                    public void onStopTrackingTouch(SeekBar seekbar) {
                    }
                }
        );

        button_menu1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(left_to_right_bool){
                    left_to_right_bool = false;
                }else{
                    left_to_right_bool = true;
                }
                double x = bar1.getProgress()/360.0;
                mirrorupdate_x(x, left_to_right_bool);
            }
        });

        button_menu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double x = bar1.getProgress()/360.0;
                int blend_width = bar_menu2.getProgress();
                blend_x(blend_width, x);
            }
        });
        buttonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);
                BitmapDrawable drawable = (BitmapDrawable) imageViewE.getDrawable();
                displaybitmap = drawable.getBitmap();
                nowbitmap = displaybitmap;
                all_ui_clear();

            }
        });
    }

    public void blend_y(int blend_width, double pos_y){
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        int y = (int)((1-pos_y) * imgH);

        Mat mat = new Mat();
        Bitmap bitmap = nowbitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(nowbitmap, mat);

        Mat flip_mat_y = new Mat(); //x軸方向・水平反転
        Core.flip(mat, flip_mat_y, 0);

        Mat display_mirrored_mat  = new Mat();
        Utils.bitmapToMat(displaybitmap, display_mirrored_mat);

        if(blend_width > 0){
            Mat mat2 = new Mat(flip_mat_y, new Rect(0, y - blend_width, imgW, blend_width * 2));
            Mat mat1 = new Mat(mat, new Rect(0, y - blend_width, imgW, blend_width * 2));

            Mat alpha = new Mat(mat1.rows(), mat1.cols(), CV_8UC1);
            for (int r = 0; r < alpha.rows(); r++) {  //画像の横幅分for文を回す(img:画像1, img:画像2)
                for (int c = 0; c < alpha.cols(); c++) {
                    double value = 255 * r/ alpha.rows();
                    alpha.put(r, c, value);
                }
            }
            Mat blend_mat = blend(mat2, mat1, alpha);

            Mat mat_roi = display_mirrored_mat.submat(new Rect(0, y - blend_width, imgW, blend_width * 2));
            blend_mat.copyTo(mat_roi);
        }
        Utils.matToBitmap(display_mirrored_mat, bitmap);
        imageViewE.setImageBitmap(bitmap);
    }

    public void mirrorupdate_y(double pos_y, boolean top_to_bottom){
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        int y = (int)((1-pos_y) * imgH);
        Mat mat = new Mat();
        Bitmap bitmap = nowbitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(nowbitmap, mat);

        Mat flip_mat_y = new Mat(); //x軸方向・水平反転
        Core.flip(mat, flip_mat_y, 0);

        if(top_to_bottom){
            Mat top_mat = new Mat(flip_mat_y, new Rect(0, y, imgW, imgH-y));
            Mat mat_roi = mat.submat(new Rect(0, y, imgW, imgH-y));
            top_mat.copyTo(mat_roi);
        }else{
            Mat top_mat = new Mat(mat, new Rect(0, y, imgW, imgH-y));
            Mat mat_roi = flip_mat_y.submat(new Rect(0, y, imgW, imgH-y));
            top_mat.copyTo(mat_roi);
            flip_mat_y.copyTo(mat);
        }
        //blend(mat, flip_mat_x);

        Utils.matToBitmap(mat, bitmap);
        imageViewE.setImageBitmap(bitmap);
        displaybitmap = bitmap;
    }

    public void mirror_y(View v) {
        all_ui_clear();
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);
        imageViewE.setImageBitmap(nowbitmap);

        final SeekBar bar2 = (SeekBar) findViewById(R.id.SeekBar2);
        bar2.setVisibility(View.VISIBLE);
        float scale = (float)imageViewE.getHeight()/(float)bar2.getWidth();
        bar2.setScaleX(scale);

        Button button_menu1 = (Button) findViewById(R.id.button_menu1);
        button_menu1.setVisibility(View.VISIBLE);

        final SeekBar bar_menu2 = (SeekBar) findViewById(R.id.SeekBar_menu2);
        bar_menu2.setVisibility(View.VISIBLE);
        Button button_menu2 = (Button) findViewById(R.id.button_menu2);
        button_menu2.setVisibility(View.VISIBLE);

        bar2.setProgress(180);
        mirrorupdate_y(0.5,true);
        Button buttonD = (Button) findViewById(R.id.buttonD);
        buttonD.setVisibility(View.VISIBLE);
        bar2.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    double y;
                    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        y = seekbar.getProgress()/360.0;
                        mirrorupdate_y(y,top_to_down_bool);
                    }
                    public void onStartTrackingTouch(SeekBar seekbar) {
                    }
                    public void onStopTrackingTouch(SeekBar seekbar) {
                    }
                }
        );
        button_menu1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(top_to_down_bool){
                    top_to_down_bool = false;
                }else{
                    top_to_down_bool = true;
                }
                double y = bar2.getProgress()/360.0;
                mirrorupdate_y(y, top_to_down_bool);
            }
        });
        button_menu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double y = bar2.getProgress()/360.0;
                int blend_width = bar_menu2.getProgress();
                blend_y(blend_width, y);
            }
        });
        buttonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);
                BitmapDrawable drawable = (BitmapDrawable) imageViewE.getDrawable();
                displaybitmap = drawable.getBitmap();
                nowbitmap = displaybitmap;
                all_ui_clear();
            }
        });
    }

    public void rotateupdate(int degree){
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);

        Mat mat = new Mat();
        Bitmap bitmap = nowbitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(nowbitmap, mat);

        rot = degree;
        Point center = new Point(imgW / 2, imgH / 2);
        float scale = 1.0f;
        float cos = (float) abs(Math.cos(rot * Math.PI / 180));
        float sin = (float) abs(Math.sin(rot * Math.PI / 180));
        float l1 = imgW * cos + imgH * sin;
        float l2 = imgW * sin + imgH * cos;
        if (l1 / imgW > l2 / imgH)
            scale = (float) (imgW / l1);
        else
            scale = (float) (imgH / l2);
        Mat rotImage = Imgproc.getRotationMatrix2D(center, rot, scale);
        Mat dummy = mat.clone();
        Imgproc.warpAffine(mat, dummy, rotImage, mat.size());
        Mat rotateImage = dummy;

        Utils.matToBitmap(rotateImage, bitmap);
        imageViewE.setImageBitmap(bitmap);
        displaybitmap = bitmap;
    }

    public void rotate(View v) {
        all_ui_clear();

        SeekBar bar1 = (SeekBar) findViewById(R.id.SeekBar1);
        bar1.setVisibility(View.VISIBLE);
        Button buttonD = (Button) findViewById(R.id.buttonD);
        buttonD.setVisibility(View.VISIBLE);
        bar1.setProgress(180);
        rotateupdate(0);
        bar1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int degree;
                    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                        if(isRotate){
                            degree = nowrot+seekbar.getProgress() - 180;
                        }else {
                            degree = rot + seekbar.getProgress() - 180;
                            isRotate = true;
                        }
                        rotateupdate(degree);
                    }
                    public void onStartTrackingTouch(SeekBar seekbar) {
                    }
                    public void onStopTrackingTouch(SeekBar seekbar) {
                        isRotate = false;
                    }
                }
        );
        buttonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowbitmap = displaybitmap;
                all_ui_clear();
            }
        });
    }

    public void trim_area_uppdate(ImageView imageViewE) {
        Mat mat = new Mat();
        Bitmap tempbitmap = nowbitmap.copy(Bitmap.Config.ARGB_8888,true);
        Utils.bitmapToMat(nowbitmap, mat);

        if(imgW>imgH){
            if(imgH >40)
                r = imgH/20;
            else
                r = 3;
        }else{
            if(imgW > 40)
                r= imgH/20;
            else
                r = 3;
        }

        if (lt.x > rb.x) {
            double temp = rb.x;
            rb.x = lt.x;
            lt.x = temp;
        }
        if (lt.y > rb.y){
            double temp = rb.y;
            rb.y = lt.y;
            lt.y = temp;
        }
        if(lt.x < 0)
            lt.x = 0;
        if(lt.y < 0)
            lt.y = 0;
        if(rb.x > imgW)
            rb.x = imgW;
        if(rb.y > imgH)
            rb.y = imgH;

        int ltx = (int)lt.x;
        int lty = (int)lt.y;
        int rbx = (int)rb.x;
        int rby = (int)rb.y;

        Mat top_mat = new Mat(mat, new Rect(ltx, rby-lty, rbx-ltx, lty));
        Mat top_roi = mat.submat(new Rect(ltx, 0, rbx-ltx, lty));
        top_mat.copyTo(top_roi);

        Mat left_mat = new Mat(mat, new Rect(rbx-ltx, lty, ltx, rby-lty));
        Mat left_roi = mat.submat(new Rect(0, lty,ltx, rby-lty));
        left_mat.copyTo(left_roi);

        Mat right_mat = new Mat(mat, new Rect(ltx, lty, imgW-rbx, rby-lty));
        Mat right_roi = mat.submat(new Rect(rbx, lty,imgW-rbx, rby-lty));
        right_mat.copyTo(right_roi);

        Mat bottom_mat = new Mat(mat, new Rect(ltx, lty, rbx-ltx, imgH-rby));
        Mat bottom_roi = mat.submat(new Rect(ltx, rby,rbx-ltx, imgH-rby));
        bottom_mat.copyTo(bottom_roi);

        //black
        Imgproc.rectangle(mat, new Point(0,0), new Point(lt.x, lt.y), new Scalar(0,0,0), -1);
        Imgproc.rectangle(mat, new Point(rb.x, 0), new Point(imgW, lt.y), new Scalar(0,0,0), -1);
        Imgproc.rectangle(mat, new Point(0, rb.y), new Point(lt.x, imgH), new Scalar(0,0,0), -1);
        Imgproc.rectangle(mat, new Point(rb.x, rb.y), new Point(imgW, imgH), new Scalar(0,0,0), -1);

        Imgproc.circle(mat, lt, r, new Scalar(255,255,0),5);
        Imgproc.circle(mat, new Point(rb.x, lt.y), r, new Scalar(255,255,0),5);
        Imgproc.circle(mat, new Point(lt.x, rb.y), r, new Scalar(255,255,0),5);
        Imgproc.circle(mat, rb, r, new Scalar(255,255,0),5);
        Imgproc.rectangle(mat, lt, rb, new Scalar(0,0,0), 1);

        Utils.matToBitmap(mat, tempbitmap);
        imageViewE.setImageBitmap(tempbitmap);
    }

    public void trim_area(View v){
        all_ui_clear();
        isTrimtool = true;
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);
        Button buttonD = (Button) findViewById(R.id.buttonD);
        buttonD.setVisibility(View.VISIBLE);

        lt = new Point(r, r);
        rb = new Point(imgW-r, imgH-r);

        trim_area_uppdate(imageViewE);

        imageViewE.setOnTouchListener(new TrimAreaMove());

        buttonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trim(lt, rb);
            }
        });

    }


    public class TrimAreaMove implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int nowmove = 0;
            int threshold = 50;
            int bw=0, bh=0;
            int diffW, diffH, mx,my;
            float scalex = (float)v.getWidth()/imgW;
            float scaley = (float)v.getHeight()/imgH;
            if(scalex < scaley){
                bw = (int)(imgW*scalex);
                bh = (int)(imgH*scalex);
            }else{
                bw = (int)(imgW*scaley);
                bh = (int)(imgH*scaley);
            }
            if (isTrimtool) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // タッチしたとき」
                        diffW = v.getWidth()-bw;
                        diffH = v.getHeight()-bh;
                        mx = (int) ((event.getX()-diffW/2.0)/bw*imgW);
                        my = (int) ((event.getY()-diffH/2.0)/bh*imgH);
                        if (abs(mx - lt.x) <= threshold) {
                            if (abs(my - lt.y) <= threshold) {
                                nowmove = 1; //lt
                            } else if (abs(my - rb.y) <= threshold)
                                nowmove = 2; //lb
                        } else if (abs(mx - rb.x) <= threshold) {
                            if (abs(my - lt.y) <= threshold)
                                nowmove = 3; //rt
                            else if (abs(my - rb.y) <= threshold)
                                nowmove = 4; //rb
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // タッチしたまま動かす
                        diffW = v.getWidth()-bw;
                        diffH = v.getHeight()-bh;
                        mx = (int) ((event.getX()-diffW/2.0)/bw*imgW);
                        my = (int) ((event.getY()-diffH/2.0)/bh*imgH);
                        if (abs(mx - lt.x) <= threshold) {
                            if (abs(my - lt.y) <= threshold) {
                                nowmove = 1; //lt
                            } else if (abs(my - rb.y) <= threshold)
                                nowmove = 2; //lb
                        } else if (abs(mx - rb.x) <= threshold) {
                            if (abs(my - lt.y) <= threshold)
                                nowmove = 3; //rt
                            else if (abs(my - rb.y) <= threshold)
                                nowmove = 4; //rb
                        }
                        switch (nowmove) {
                            case 1:
                                lt.x = mx;
                                lt.y = my;
                                break;
                            case 2:
                                lt.x = mx;
                                rb.y = my;
                                break;
                            case 3:
                                rb.x = mx;
                                lt.y = my;
                                break;
                            case 4:
                                rb.x = mx;
                                rb.y = my;
                                break;
                            default:
                        }
                        trim_area_uppdate((ImageView) v);
                        break;
                    case MotionEvent.ACTION_UP:
                        // 指を離したとき

                        break;
                    default:
                        break;
                }
                return true;
            }
            return false;
        }
    }


    public void trim(Point lt, Point rb) {
        all_ui_clear();
        ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);
        Mat mat = new Mat();
        Utils.bitmapToMat(displaybitmap, mat);

        imgW = (int)(rb.x - lt.x);
        imgH = (int)(rb.y - lt.y);
        if(imgW%2 != 0){
            imgW -=1;
        }
        if(imgH%2 != 0){
            imgH -=1;
        }
        Rect roi = new Rect((int)lt.x, (int)lt.y, imgW, imgH);
        Mat new_mat = new Mat(mat, roi);

        rot = 0;
        nowbitmap = Bitmap.createBitmap(new_mat.width(), new_mat.height(), Bitmap.Config.ARGB_8888);
        displaybitmap = Bitmap.createBitmap(new_mat.width(), new_mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(new_mat, nowbitmap);
        Utils.matToBitmap(new_mat, displaybitmap);
        imageViewE.setImageBitmap(nowbitmap);
    }
    public void Save(){
        final String SAVE_DIR = "/TextureStock/";

        final java.io.File root_file = new File(Environment.getExternalStorageDirectory().getPath()+SAVE_DIR);
        try{
            if(!root_file.exists()){
                root_file.mkdir();
            }
        }catch(SecurityException e) {
            e.printStackTrace();
            throw e;
        }

        File[] textureFiles = root_file.listFiles();
        List<String> typeList = new ArrayList<String>();
        for(int i = 0; i < textureFiles.length; i++){
            if(textureFiles[i].isFile()){
                String type = textureFiles[i].getName().split("_",0)[0];
                if(!typeList.contains(type))
                    typeList.add(type);
            }
        }

        final AutoCompleteTextView actView = new AutoCompleteTextView(SubActivity.this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, typeList);
        actView.setAdapter(adapter);
        actView.setThreshold(1);
        actView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    actView.showDropDown();
                }
            }
        });

        final EditText editView = new EditText(SubActivity.this);
        final EditText editView2 = new EditText(SubActivity.this);
        TextView typeLabel = new TextView(getApplicationContext());
        typeLabel.setText("Type");
        TextView nameLabel = new TextView(getApplicationContext());
        nameLabel.setText("Name");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        //上から下にパーツを組み込む設定
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(typeLabel);
        layout.addView(actView);
        layout.addView(nameLabel);
        layout.addView(editView);

        new AlertDialog.Builder(SubActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("テキスト入力ダイアログ")
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String typename = actView.getText().toString();
                        String imgname = editView.getText().toString();

                        java.io.File save_file = new File(root_file,   typename + "_" + imgname + ".png");
                        if(save_file.exists()) {
                            int i=2;
                            while(true){
                                save_file = new File(root_file, typename + "_" + imgname +"("+ String.valueOf(i)+").png");
                                if(!save_file.exists())
                                    break;
                                i++;
                            }
                        }
                        try{
                            FileOutputStream fos = new FileOutputStream(save_file);
                            ImageView imageViewE = (ImageView) findViewById(R.id.imageViewE);
                            BitmapDrawable drawable = (BitmapDrawable) imageViewE.getDrawable();
                            Bitmap outbitmap = drawable.getBitmap();
                            outbitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        // save index
                        ContentValues values = new ContentValues();
                        ContentResolver contentResolver = getContentResolver();
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                        values.put(MediaStore.Images.Media.TITLE, imgname);
                        values.put("_data", save_file.getAbsolutePath());
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        finish();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }
}