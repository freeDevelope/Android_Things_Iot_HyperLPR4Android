package things.arc.com.hyperlpr_things_v1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import test.xidian.com.camera.CameraTask;

public class CameraActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private  CameraTask mCameraTask;
    private String mSavePictureFolder = "Hyperlpr_Picture";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSurfaceView = findViewById(R.id.surface_view);
        mCameraTask = new CameraTask(getApplicationContext(),mSurfaceView);
        //开启相机预览
        mCameraTask.startPreview();

        //在sd卡新建一个文件夹，保存拍照的图片
        File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator+mSavePictureFolder);
        if (!file.exists()){
            file.mkdir();
        }

        //拍照
//        findViewById(R.id.btn_take_picture_mobile).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCameraTask.takePicture();
//                //保存照片
//                savePicture(
//                        Environment.getExternalStorageDirectory().toString()+File.separator+mSavePictureFolder,
//                        String.valueOf(new Date().getTime())+".jpg",
//                        mCameraTask.getTakePictureResult()
//                );
//
//                //跳转到另一个界面
//                String fullName = Environment.getExternalStorageDirectory().toString()+File.separator+mSavePictureFolder + File.separator+String.valueOf(new Date().getTime())+".jpg";
//                Intent intent = new Intent(CameraActivity.this,ShowPictureActivity.class);
//                intent.putExtra("piaturePath",fullName);
//                startActivity(intent);
//            }
//        });

    }

    /**
     * @param path 保存路径
     * @param pictureName 文件名
     * @param bitmap 图像
     */
    private void savePicture(String path, String pictureName, Bitmap bitmap){

        String fullName = path + File.separator + pictureName;
        File file = new File(fullName);
        if (file.exists()){
            file.delete();
        }else{
            try {
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }

            //将bitmap写入文件
            try{

                OutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraTask!=null){
            mCameraTask.shutDown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraTask!=null){
            mCameraTask.shutDown();
        }
    }
}
