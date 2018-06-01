package things.arc.com.hyperlpr_things_v1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {

    private static final String[] PERMISSION={
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private final int REQUEST_CODE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //动态授权
        requestPermissions(PERMISSION,REQUEST_CODE_PERMISSION);

        findViewById(R.id.btn_open_camera_mobile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this,TCPActivity.class));
                //startActivity(new Intent(WelcomeActivity.this,CameraActivity.class));
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int count = 0;
        if (requestCode==REQUEST_CODE_PERMISSION){
            for (String per:permissions){
                if (grantResults[count]!= PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "权限申请失败"+per, Toast.LENGTH_SHORT).show();
                }
            }
        }


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
