package test.xidian.com.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 2018-4-13
 * Author:魏鹏辉
 * 功能：使用Android camera2，实现相机预览。
 * 此类实例化时候，只需要传入一个SurfaceView和Context实例，将视图与逻辑代码分离
 */
public class CameraTask {


    private static String TAG = "CameraTask";

    private ImageReader mImageReader;
    private int mPictureWidth = 1080;
    private int mPictureHeight = 1920;
    private Bitmap mPictureBitmap;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureRequest mCaptureRequest;
    private String mCameraId;
    private HandlerThread mHandlerThread = new HandlerThread("Camera2");  //创建一个子线程
    private Handler mHandlerChild; //子线程的Handler
    private Handler mHandlerMain;  //主线程的Handler
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Context mContext;
    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            //打开相机,预览模式
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCameraDevice!=null){
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
    };

    public interface TakePictrueCallback{
        void onTakePictureFinished(ImageReader imageReader);
    }

    private TakePictrueCallback mTakePictureCallback;

    /**
     * 拍照
     * @param callback 拍照回掉
     */
    public void takePicture(TakePictrueCallback callback){
        mTakePictureCallback = callback;    //注册回调

        final boolean preivew = false;
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            return;
        }
        try {
            String cameraIdList[] = mCameraManager.getCameraIdList();
            if (cameraIdList.length == 0) {
                return;
            }
            mCameraId = cameraIdList[0];
            //检查Camera权限
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }


            //打开相机
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    if (mCameraDevice ==null){
                        mCameraDevice = camera;
                    }
                    Log.i(TAG,"相机打开成功!");

                    if (preivew){
                        //开启预览
                        takePreview();
                    }else{

                        //设置拍照模式
                        if (mImageReader==null){
                            mImageReader = ImageReader.newInstance(1080,720,ImageFormat.JPEG,1);
                            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                                @Override
                                public void onImageAvailable(ImageReader reader) {
                                    mTakePictureCallback.onTakePictureFinished(reader);
                                }
                            },mHandlerMain);
                        }

                        if (mCameraDevice!=null){
                            try {

                                //建立相机捕获请求
                                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                builder.addTarget(mImageReader.getSurface());
                                mCaptureRequest = builder.build();
                                //创建一个相机-服务会话
                                //在会话中提交请求
                                mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(@NonNull CameraCaptureSession session) {
                                        mCameraCaptureSession = session;
                                        try {
                                            //提交拍照请求
                                            mCameraCaptureSession.capture(mCaptureRequest,null,mHandlerChild);
                                        }catch (CameraAccessException e){
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                                    }
                                },mHandlerChild);


                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    if (mCameraDevice!=null){
                        Log.i(TAG,"Camera disconnect!");
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG,"相机发生错误");
                }
            },mHandlerMain);   //StateCallback运行在主线程中
        }catch (CameraAccessException e){
            e.printStackTrace();
        }


    }

    /**
     * @param context 上下文
     * @param surfaceView 相机预览输出
     */
    public CameraTask(Context context, SurfaceView surfaceView) {
        mContext = context;
        mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();

        //开启子线程
        mHandlerThread.start();
        //子线程绑定Handler
        mHandlerChild = new Handler(mHandlerThread.getLooper());

        //主线程绑定Handler
        mHandlerMain = new Handler(mContext.getMainLooper());
    }

    public CameraTask(Context context) {
        mContext = context;
        //开启子线程
        mHandlerThread.start();
        //子线程绑定Handler
        mHandlerChild = new Handler(mHandlerThread.getLooper());

        //主线程绑定Handler
        mHandlerMain = new Handler(mContext.getMainLooper());
    }


    /**
     * 开始相机预览
     */
    public void startPreview(){
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);
    }



    /**
     * 关闭相机,释放资源，防止一直占用
     */
    public void shutDown(){
        if (mCameraCaptureSession!=null){
            mCameraCaptureSession.getDevice().close();
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
    }

    private void openCamera() {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            return;
        }
        try {
            String cameraIdList[] = mCameraManager.getCameraIdList();
            if (cameraIdList.length == 0) {
                return;
            }
            mCameraId = cameraIdList[0];
            //检查Camera权限
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }


            //打开相机
            mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    if (mCameraDevice ==null){
                        mCameraDevice = camera;
                    }
                    Log.i(TAG,"相机打开成功!");

                    if (true){
                        //开启预览
                        takePreview();
                    }else{

                        //设置拍照模式
                        if (mImageReader==null){
                            mImageReader = ImageReader.newInstance(1080,720,ImageFormat.JPEG,1);
                            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                                @Override
                                public void onImageAvailable(ImageReader reader) {
                                    mTakePictureCallback.onTakePictureFinished(reader);
                                }
                            },mHandlerMain);
                        }


                    }

                 }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    if (mCameraDevice!=null){
                        Log.i(TAG,"Camera disconnect!");
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG,"相机发生错误");
                }
            },mHandlerMain);   //StateCallback运行在主线程中
        }catch (CameraAccessException e){
            e.printStackTrace();
        }





    }







    /*
    预览
     */
    private void takePreview(){

        if (mCameraDevice ==null){
            return;
        }

       //相机设备创建一个请求
        CaptureRequest.Builder captureRequestBuilder = null;
        try {
            //相机预览请求
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置参数
            //...自动对焦，自动曝光

            //设置相机数据输出
            captureRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            mCaptureRequest = captureRequestBuilder.build();  //Android中Builder设计模式

            //创建一个相机-服务会话
            //在会话中提交请求
            //mImageReader.getSurface()
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest,null,mHandlerChild);
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },mHandlerChild);  //创建会话的回掉在子线程中进行，一般比较耗时，防止阻塞UI线程


        }catch (CameraAccessException e){
            e.printStackTrace();
        }

    }



}
