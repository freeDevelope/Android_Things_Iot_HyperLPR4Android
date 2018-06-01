package things.arc.com.hyperlpr_things_v1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import pr.hyperlpr.util.DeepCarUtil;
import test.xidian.com.camera.CameraTask;

import static org.opencv.core.Core.flip;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class TakePictureActivity extends AppCompatActivity {


    private static final String TAG = "TakePictureActivity";
    private ImageView mImageView;
    private Button mBtnTakePicture;
    private TextView mTextViewPlateResult;
    private CameraTask mCameraTask;
    private String mPictureName;
    private Bitmap mPictureBitmap;
    private File mPictureFile;

    //车牌检测相关
    private long mHyperLprObjectAddr;
    private String mPlateStr="AY070Q";
    private String mPlateProvince="zh_shann";
    private String mPlateNum="";
    private String mBackgroundColor="blue";
    private String mTextColor = "white";
    private String mJsonStr="";

    //TCP通信
    private Socket mSocket;
    private String mIPAddr;
    private int mPort;
    private String mTCPSendMsg;
    private HandlerThread mHandlerThreadTCP = new HandlerThread("TCP_Clinet");
    private Handler mHandlerTCP;

    //GPIO口相关
    private Gpio mGpioRead;     //读取Gpio口的状态，检测是否有触发电平，然后开始车牌识别
    private Gpio mGpioWrite;    //向Gpio写状态，车牌识别结束后写一电平
    private Pwm  mPwm;          //输出PWM波
    private final String mNameGpioRead = "BCM12";
    private final String mNameGpioWrite = "BCM26";
    private PeripheralManager mPeripheralManager;
    private HandlerThread mHandlerThreadGpio = new HandlerThread("GPIO");
    private Handler mHandlerGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        //获取启动此activity的intent
        //获取传递过来的IP,Port
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        try {
            mIPAddr = bundle.getString("IPAddr");
            mPort = bundle.getInt("Port");
        }catch (NullPointerException e){
            e.printStackTrace();
        }


        initView();
        //--------------------开启TCP通信线程----------------------------------
        mHandlerThreadTCP.start();
        mHandlerTCP = new Handler(mHandlerThreadTCP.getLooper());
        //建立TCP连接
        mHandlerTCP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket(mIPAddr,mPort);
                }catch (IOException e){
                    Toast.makeText(TakePictureActivity.this, "TCP连接异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                if (mSocket!=null){
                    if (mSocket.isConnected()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TakePictureActivity.this, "TCP连接成功！", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TakePictureActivity.this, "TCP连接失败！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }

            }
        });

        mPeripheralManager = PeripheralManager.getInstance();
        //--------------------配置读取红外触发电平GPIO--------------------------
        try {
            //打开指定的GPIO
            mGpioRead = mPeripheralManager.openGpio(mNameGpioRead);
            //设置GPIO为输入
            mGpioRead.setDirection(Gpio.DIRECTION_IN);
            //设置高电平有效
            mGpioRead.setActiveType(Gpio.ACTIVE_HIGH);
            //设置电平变化的触发方式
            mGpioRead.setEdgeTriggerType(Gpio.EDGE_BOTH);
            //注册电平变化的回掉,中断处理
            //检测到电平变化之后执行车牌检测识别
            mGpioRead.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.i(TAG,"Gpio state changed!");
                    try {
                        if (gpio.getValue()){
                            Log.i(TAG,"Read Gpio is HIGH");
                        }else {
                            Log.i(TAG,"Read Gpio is LOW");
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    Log.i(TAG,"检测到GPIO(输入)口电平变化,开始执行车牌识别");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TakePictureActivity.this, "检测到触发电平！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //执行车牌检测
                    new PlateRecognizeAsyncTask().execute();

                    return false;
                }

                @Override
                public void onGpioError(Gpio gpio, int error) {
                    Log.w(TAG,gpio+ ": Error event" + error);
                }
            });

        }catch (IOException e){
            e.printStackTrace();
        }


        //----------------------------------开启GPIO口操作线程---------------------------
        mHandlerThreadGpio.start();
        mHandlerGpio = new Handler(mHandlerThreadGpio.getLooper());
//        mHandlerGpio.post(new Runnable() {
//            @Override
//            public void run() {
//                mPeripheralManager = PeripheralManager.getInstance();
//                List<String> portList = mPeripheralManager.getGpioList();
//                if (portList.isEmpty()){
//                    Log.i(TAG,"No Gpio port available");
//                }else {
//                    Log.i(TAG,"List of available ports: "+portList);
//                    //--------------------配置读取红外触发电平GPIO--------------------------
//                    try {
//                        //打开指定的GPIO
//                        mGpioRead = mPeripheralManager.openGpio(mNameGpioRead);
//                        //设置GPIO为输入
//                        mGpioRead.setDirection(Gpio.DIRECTION_IN);
//                        //设置高电平有效
//                        mGpioRead.setActiveType(Gpio.ACTIVE_HIGH);
//                        //设置电平变化的触发方式
//                        mGpioRead.setEdgeTriggerType(Gpio.EDGE_BOTH);
//                        //注册电平变化的回掉,中断处理
//                        //检测到电平变化之后执行车牌检测识别
//                        mGpioRead.registerGpioCallback(new GpioCallback() {
//                            @Override
//                            public boolean onGpioEdge(Gpio gpio) {
//                                Log.i(TAG,"Gpio state changed!");
//                                try {
//                                    if (gpio.getValue()){
//                                        Log.i(TAG,"Read Gpio is HIGH");
//                                    }else {
//                                        Log.i(TAG,"Read Gpio is LOW");
//                                    }
//                                }catch (IOException e){
//                                    e.printStackTrace();
//                                }
//
//                                Log.i(TAG,"检测到GPIO(输入)口电平变化,开始执行车牌识别");
//                                //执行车牌检测
//                                new PlateRecognizeAsyncTask().execute();
//
//                                return false;
//                            }
//
//                            @Override
//                            public void onGpioError(Gpio gpio, int error) {
//                                Log.w(TAG,gpio+ ": Error event" + error);
//                            }
//                        });
//
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//
//                    //-------------------------配置GPIO端口写电平-----------------------------
//                    //车牌识别结束后，向该GPIO端口写一个电平变化,初始化时该端口为低电平，识别结束之时产生一个1s的高电平脉冲
//                    try {
//                        //打开GPIO
//                        mGpioWrite = mPeripheralManager.openGpio(mNameGpioWrite);
//                        //设置为输出GPIO,初始电平为高电平
//                        mGpioWrite.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
//                        //设置高电平有效
//                        mGpioWrite.setActiveType(Gpio.ACTIVE_HIGH);
//                        //
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//
//                    //-----------------------配置PWM波端口------------------------------------
//                    try {
//                        List<String> pwmList =  mPeripheralManager.getPwmList();
//                        if (pwmList.isEmpty()){
//                            Log.i(TAG,"No available PWM!");
//                        }else {
//                            mPwm = mPeripheralManager.openPwm("PWM0");
//                            //设置PWM频率
//                            mPwm.setPwmFrequencyHz(1000);
//                            //设置PWM占空比
//                            mPwm.setPwmDutyCycle(70);
//                            mPwm.setEnabled(true);
//                        }
//
//
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//
//
//                }
//            }
//        });


        //开启一个线程，用于读取GPIO电平状态
//        new Thread(){
//            @Override
//            public void run() {
//                while (true){
//                    if (mGpioRead!=null){
//                        try{
//                            if (mGpioRead.getValue()){
//                                Log.i(TAG,"Gpio read state:"+"高电平");
//                            }else {
//                                Log.i(TAG,"Gpio read state:"+"低电平");
//                            }
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//
//                        try{
//                            sleep(500);
//                        }catch (InterruptedException e){
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                }
//            }
//        }.start();

        //开启一个线程，GPIO写电平
//        new Thread(){
//            @Override
//            public void run() {
//                if (mGpioWrite!=null){
//                    while (true){
//                        try{
//                            mGpioWrite.setValue(true);
//                            //等待2s
//                            try{
//                                sleep(2000);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            mGpioWrite.setValue(false);
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//
//                    }
//                }
//            }
//        }.start();


        //初始化车牌检测
        initPlateRecognizer();

        mBtnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开相机
                //mCameraTask.openCamera(false);
                //拍照
                mCameraTask = new CameraTask(TakePictureActivity.this);

                mCameraTask.takePicture(new CameraTask.TakePictrueCallback() {
                    @Override
                    public void onTakePictureFinished(ImageReader imageReader) {

                        //关闭相机
                        mCameraTask.shutDown();

                        Image image = imageReader.acquireNextImage();
                        Image.Plane plane = image.getPlanes()[0];
                        ByteBuffer byteBuffer = plane.getBuffer();

                        //ByteBufer.array()转byte[]错误用法
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes,0,bytes.length);
                        //解析为Bitmap
                        mPictureBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(mPictureBitmap);
                            }
                        });

                        //开启一个后台线程，用于车牌识别
                         new PlateRecognizeAsyncTask().execute();

                        //创建一个文件
                        //文件名
                        mPictureName = String.valueOf(new Date().getTime())+".jpg";
                        mPictureFile = new File(HyperLprUtils.pictureFolderPath+File.separator+mPictureName);
                        try {
                            mPictureFile.createNewFile();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        OutputStream out = null;
                        try {
                            out = new FileOutputStream(mPictureFile);
                            out.write(bytes);

                            //关闭文件
                            out.close();


                        }catch (IOException e){
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


    }

    private void initView(){
        mImageView = findViewById(R.id.image_view_take_pic_aty);
        mBtnTakePicture = findViewById(R.id.btn_take_picture);
        mTextViewPlateResult = findViewById(R.id.text_view_plate_result);

    }

    private void initPlateRecognizer(){
        //C++代码实例化一个PR对象，将指针返回
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHyperLprObjectAddr = DeepCarUtil.InitPlateRecognizer(HyperLprUtils.modelFolderPath+File.separator+HyperLprUtils.cascadeXML,
                        HyperLprUtils.modelFolderPath+File.separator+HyperLprUtils.horizonalFinemappingPrototxt,HyperLprUtils.modelFolderPath+File.separator+HyperLprUtils.horizonalFinemappingCaffeModel,
                        HyperLprUtils.modelFolderPath+File.separator+HyperLprUtils.segmentationPrototxt,HyperLprUtils.modelFolderPath+File.separator+HyperLprUtils.segmentationCaffeModel,
                        HyperLprUtils.modelFolderPath+File.separator+HyperLprUtils.characterRecognizationPrototxt,HyperLprUtils.modelFolderPath+File.separator+HyperLprUtils.characterRecognizationCaffeModel
                );
            }
        }).start();


    }


    class PlateRecognizeAsyncTask extends AsyncTask<Integer,Integer,Integer>{

       // private ProgressDialog progressDialog;

        @Override
        protected Integer doInBackground(Integer... integers) {
            //-----------------Bitmap转Mat------------------------
            if (mPictureBitmap==null){
                return null;
            }

            Mat imRGBA = new Mat();
           // mPictureBitmap = BitmapFactory.decodeFile(HyperLprUtils.testCarPlatePictureFolderPath+File.separator+"1.jpg");
            Utils.bitmapToMat(mPictureBitmap,imRGBA);
           // imRGBA = imread(HyperLprUtils.testCarPlatePictureFolderPath+File.separator+"0.jpg");
            //图像顺时针旋转90度
           // flip(imRGBA,imRGBA, Core.ROTATE_90_CLOCKWISE);
            //-------------------车牌定位，识别---------------------
            mPlateStr = DeepCarUtil.SimpleRecognization(imRGBA.getNativeObjAddr(),mHyperLprObjectAddr);
            return 0;
        }

        @Override
        protected void onPreExecute() {
//            progressDialog = new ProgressDialog(TakePictureActivity.this);
//            progressDialog.setTitle("Plate Recognize...");
//            progressDialog.show();

            Toast.makeText(TakePictureActivity.this, "Plate Recognize...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
         //   progressDialog.dismiss();
            if (mPlateStr.isEmpty()){
                Toast.makeText(TakePictureActivity.this, "未检测到车牌", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(TakePictureActivity.this, "检测到车牌,识别结果:"+mPlateStr, Toast.LENGTH_SHORT).show();
                mTextViewPlateResult.setText(mPlateStr);
            }

            //根据识别结果创建一个json,转为String
//            mPlateStr = "陕A12345";
//            mPlateNum = "12345";
//            mPlateProvince = "zh_shann";

            //String subStr = mPlateStr.substring(1);  //去除中文
            mJsonStr = "陕AY070Q";
            mPlateStr = "AY070Q";
            mJsonStr = HyperLprUtils.creatJSONStr(mPlateStr,mPlateNum,mPlateProvince,mBackgroundColor,mTextColor);


            //Button变得不可见
           // mBtnTakePicture.setVisibility(View.INVISIBLE);

            //通过TCP发送识别的结果
            mHandlerTCP.post(new Runnable() {
                @Override
                public void run() {
                    if (mSocket!=null){
                        if (mSocket.isConnected()){
                            try {
                                OutputStream outputStream = mSocket.getOutputStream();
                                //解决中文乱码问题，GB2312可以
                                //-------待改动---------------------------------------------
                                outputStream.write(mJsonStr.getBytes("utf-8"));
                                outputStream.flush();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TakePictureActivity.this, "识别结果已发送至服务端！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }catch (IOException e){
                                e.printStackTrace();
                            }

                        }
                    }
                }
            });


        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mGpioRead!=null){
                mGpioRead.close();
                mGpioRead = null;
            }
            if (mGpioWrite!=null){
                mGpioWrite.close();
                mGpioWrite = null;
            }
            if (mPwm!=null){
                mPwm.close();
                mPwm=null;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
