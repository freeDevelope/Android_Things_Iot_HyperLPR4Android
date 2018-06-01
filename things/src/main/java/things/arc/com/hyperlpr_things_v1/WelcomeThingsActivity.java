package things.arc.com.hyperlpr_things_v1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 *
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
public class WelcomeThingsActivity extends Activity {

    private final String PERMISSION[]={
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private final int REQUEST_CODE_PERMISSION = 1;

    static {

        System.loadLibrary("opencv_java3");
        System.loadLibrary("hyperlpr");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_things);

        //检查权限
        for (String permission:PERMISSION){
            if (checkSelfPermission(permission)!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "need "+ permission, Toast.LENGTH_SHORT).show();
            }
        }
        //动态申请权限
        requestPermissions(PERMISSION,REQUEST_CODE_PERMISSION);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode!=REQUEST_CODE_PERMISSION){
            return;
        }

        int count = 0;
        for (int result:grantResults){
            if (result!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, permissions[count]+ "was denied", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        //在SD卡目录创建文件夹，保存拍摄的照片
        HyperLprUtils.creatFolders();

        HyperLprUtils.creatJSONFile("陕A12345","12345","zh_shann","blue","white");

        //复制模型文件
        HyperLprUtils.copyModelToSdCard(WelcomeThingsActivity.this);

        //复制测试图片
        HyperLprUtils.copyTestCarPlatePictureToSdCard(WelcomeThingsActivity.this);

        Intent intent = new Intent(WelcomeThingsActivity.this,ConnectTCPActivity.class);
//
        //Intent intent = new Intent(WelcomeThingsActivity.this,TestGPIOActivity.class);
        startActivity(intent);

//
//        //Intent intent = new Intent(WelcomeThingsActivity.this,TakePictureActivity.class);
//        startActivity(intent);



        //如果权限满足，启动相机预界面
//        Intent intent = new Intent(WelcomeThingsActivity.this,CameraActivity.class);
//        startActivity(intent);


    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
