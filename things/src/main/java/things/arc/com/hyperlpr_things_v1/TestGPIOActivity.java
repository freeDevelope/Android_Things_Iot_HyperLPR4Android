package things.arc.com.hyperlpr_things_v1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.List;

public class TestGPIOActivity extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = "TestGPIOActivity";
    private Button mBtnGpio1Low;
    private Button mBtnGpio1High;
    private Button mBtnGpio2Low;
    private Button mBtnGpio2High;
    private Button mBtnOpenPWM;
    private Button mBtnClosePWM;
    private TextView mTextViewGpio3State;


    private PeripheralManager mPeripheralManager;
    private Gpio mGpio1;
    private Gpio mGpio2;
    private Gpio mGpio3;
    private Pwm mPwm;
    private final String mGpio1Name = "BCM16";
    private final String mGpio2Name = "BCM26";
    private final String mGpio3Name = "BCM12";
    private final String mPwmName = "PWM0";
    private final double mPWMFreq = 120;
    private final double mPWMDuty = 25;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_gpio);

        initView();

        mPeripheralManager = PeripheralManager.getInstance();
        List<String> gpioList = mPeripheralManager.getGpioList();
        if (gpioList.isEmpty()){
            Toast.makeText(this, "没有可用的GPIO端口", Toast.LENGTH_SHORT).show();
        }else{

            //设置gpio1
            try {
                //打开
                mGpio1 = mPeripheralManager.openGpio(mGpio1Name);
                //设置为输出
                mGpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                //设置高电平有效
                mGpio1.setActiveType(Gpio.ACTIVE_HIGH);

            }catch (IOException e){
                e.printStackTrace();
            }

            //设置gpio2
            try {
                //打开
                mGpio2 = mPeripheralManager.openGpio(mGpio2Name);
                //设置为输出
                mGpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                //设置高电平有效
                mGpio2.setActiveType(Gpio.ACTIVE_HIGH);

            }catch (IOException e){
                e.printStackTrace();
            }

            //设置gpio3
            try {
                mGpio3 = mPeripheralManager.openGpio(mGpio3Name);
                //设置为输入
                mGpio3.setDirection(Gpio.DIRECTION_IN);
                //设置高电平有效
                mGpio3.setActiveType(Gpio.ACTIVE_HIGH);
                //设置触发方式
                mGpio3.setEdgeTriggerType(Gpio.EDGE_RISING);
                //注册回掉
                mGpio3.registerGpioCallback(new GpioCallback() {
                    @Override
                    public boolean onGpioEdge(Gpio gpio) {
                        Log.i(TAG,"电平发生变化");
                        try {
                            if (gpio.getValue()){
                                mTextViewGpio3State.setText("高电平");
                            }else {
                                mTextViewGpio3State.setText("低电平");
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                        return true;
                    }


                    @Override
                    public void onGpioError(Gpio gpio, int error) {
                        Toast.makeText(TestGPIOActivity.this, gpio.getName()+" Error,"+error, Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }

            //设置PWM
            try {
                List<String> pwmList = mPeripheralManager.getPwmList();
                if (pwmList.isEmpty()){
                    Toast.makeText(this, "没有可用的PWM设备", Toast.LENGTH_SHORT).show();
                }else{
                    Log.i(TAG,"PWM list:"+pwmList);
                    mPwm = mPeripheralManager.openPwm(mPwmName);
                    //占空比
                    mPwm.setPwmDutyCycle(mPWMDuty);
                    //频率
                    mPwm.setPwmFrequencyHz(mPWMFreq);
                    mPwm.setEnabled(true);
                }

            }catch (IOException e){
                e.printStackTrace();
            }

        }

        mBtnGpio1High.setOnClickListener(this);
        mBtnGpio1Low.setOnClickListener(this);
        mBtnGpio2High.setOnClickListener(this);
        mBtnGpio2Low.setOnClickListener(this);
        mBtnOpenPWM.setOnClickListener(this);
        mBtnClosePWM.setOnClickListener(this);
    }

    private void initView(){
       mBtnGpio1High = findViewById(R.id.btn1_high);
       mBtnGpio1Low = findViewById(R.id.btn1_low);
       mBtnGpio2Low = findViewById(R.id.btn2_low);
       mBtnGpio2High = findViewById(R.id.btn2_high);
       mBtnOpenPWM = findViewById(R.id.btn_pwm_on);
       mBtnClosePWM = findViewById(R.id.btn_pwm_close);
       mTextViewGpio3State = findViewById(R.id.text_view_gpio3);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn1_high:{
                try {
                    mGpio1.setValue(true);
                }catch (IOException e){
                    e.printStackTrace();
                }

                break;
            }
            case R.id.btn1_low:{
                try {
                    mGpio1.setValue(false);
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.btn2_high:{
                try {
                    mGpio2.setValue(true);
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.btn2_low:{
                try {
                    mGpio2.setValue(false);
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.btn_pwm_on:{
                try {
                    mPwm.setEnabled(true);
                }catch (IOException e){
                    e.printStackTrace();
                }

                break;
            }
            case R.id.btn_pwm_close:{
                try {
                    mPwm.setEnabled(false);
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mGpio1.close();
            mGpio2.close();
            mGpio3.close();
            mPwm.close();

            mGpio1 = null;
            mGpio2 = null;
            mGpio3 = null;
            mPwm = null;
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
