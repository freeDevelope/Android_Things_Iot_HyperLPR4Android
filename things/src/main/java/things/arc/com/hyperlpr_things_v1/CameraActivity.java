package things.arc.com.hyperlpr_things_v1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.List;

import test.xidian.com.camera.CameraTask;

public class CameraActivity extends AppCompatActivity {


    private static final String TAG = "CameraActivity";

    private SurfaceView mSurfaceView;
    private CameraTask mCameraTask;
    private Gpio mGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initView();

        //GPIO
        setGPIO("BCM19");

        //实例化一个相机，采用camera2
        mCameraTask = new CameraTask(getApplicationContext(),mSurfaceView);
        //开启预览
        mCameraTask.startPreview();


    }

    private void initView(){
        mSurfaceView = findViewById(R.id.surface_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraTask.shutDown();
        if (mGpio!=null){
            try {
                mGpio.close();
                mGpio=null;
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        
    }
    
    private void setGPIO(final String gpioName){
        
        //Managing the connection
        //实例化一个PeripheralManager外设管理对象，查询GPIO列表
        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()){
            Toast.makeText(this, "No GPIO port available on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //打开GPIO
            mGpio = manager.openGpio(gpioName);

            //设置GPIO
            //配置GPIO为输入口
            mGpio.setDirection(Gpio.DIRECTION_IN);
            //设置高电平为激活方式
            mGpio.setActiveType(Gpio.ACTIVE_HIGH);

            //....

            //为GPIO设置一个监听器，监听其状态改变
            //设置中断方式
            //EDGE_RASING上升沿触发，EDGE_FALLING下降沿触发 EDGE_BOTH 上升/下降沿都触发
            mGpio.setEdgeTriggerType(Gpio.EDGE_RISING);

            //为GPIO设置监听回掉
            mGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {

                    Toast.makeText(CameraActivity.this, "GPIO "+gpioName+ "state changed!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"GPIO state changed!");

                    return false;
                }
            });


        }catch (IOException e){
            e.printStackTrace();
        }




        
        
    }
    
    
    
}
