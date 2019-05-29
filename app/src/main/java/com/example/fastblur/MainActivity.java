package com.example.fastblur;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    Bitmap bitmap=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageView imageView = (ImageView) findViewById(R.id.image);
        bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.a);

        Button addBtn=(Button)findViewById(R.id.addImage);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                intent.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setAction(Intent.ACTION_GET_CONTENT);
                /* 取得相片后返回本画面 */
                startActivityForResult(intent, 1);
            }
        });

        Bitmap blurBitmap=blur(bitmap,imageView,null);
        download(MainActivity.this,blurBitmap);

    }

    private Bitmap blur(Bitmap scaledBitmap, ImageView background,String picPath) {
        //        获取需要被模糊的原图bitmap
        if(picPath!=null){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(picPath);
                scaledBitmap  = BitmapFactory.decodeStream(fis);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            Resources res = getResources();
            scaledBitmap = BitmapFactory.decodeResource(res, R.drawable.a);
        }

        //        scaledBitmap为目标图像，10是缩放的倍数（越大模糊效果越高）
        Bitmap blurBitmap = FastBlurUtil.toBlur(scaledBitmap, 5);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        background.setImageBitmap(blurBitmap);
        return  blurBitmap;
    }

    public void download(Context context,Bitmap blurBitmap){
        String sdCardDir=Environment.getExternalStorageDirectory()+"/DCIM/";
        File appDir =new File(sdCardDir, "myPic");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = "MyPic"+System.currentTimeMillis() + ".jpg";
        File f = new File(appDir,fileName);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 通知图库更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(f);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());
            ContentResolver cr = this.getContentResolver();
            try {
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
//                ImageView imageView = (ImageView) findViewById(R.id.iv01);
//                /* 将Bitmap设定到ImageView */
//                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
