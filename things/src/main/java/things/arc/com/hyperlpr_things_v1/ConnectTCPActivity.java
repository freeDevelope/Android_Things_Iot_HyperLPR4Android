package things.arc.com.hyperlpr_things_v1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConnectTCPActivity extends AppCompatActivity {

    private EditText mEditTextIPAddr;
    private EditText mEditTextPort;
    private Button mBtnNext;
    private String mIPAddr;
    private int mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_tcp);

        initView();

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIPAddr = mEditTextIPAddr.getText().toString();
                if (mIPAddr.isEmpty()){
                    Toast.makeText(ConnectTCPActivity.this, "IP地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                String port;
                if ((port = mEditTextPort.getText().toString()).isEmpty()){
                    Toast.makeText(ConnectTCPActivity.this, "端口号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                mPort = Integer.valueOf(port);
                Bundle bundle = new Bundle();
                bundle.putString("IPAddr",mIPAddr);
                bundle.putInt("Port",mPort);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(ConnectTCPActivity.this,TakePictureActivity.class);
                //启动拍照，车牌识别界面
                startActivity(intent);
            }
        });

    }

    private void initView(){
        mEditTextIPAddr = findViewById(R.id.edit_text_ip_addr);
        mEditTextPort = findViewById(R.id.edit_text_port);
        mBtnNext = findViewById(R.id.btn_next_aty);
    }


}
