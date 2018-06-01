package things.arc.com.hyperlpr_things_v1;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TCPActivity";

    private EditText mEditTextIPAddr;
    private EditText mEditTextPort;
    private Button mBtnConnectTCP;
    private Button mBtnSendMessage;
    private EditText mEditTextMessage;

    private HandlerThread mHanderThreadChild = new HandlerThread("connect_tcp");
    private Handler mHanderChild;
    private Handler mHandlerMain;

    private Socket mSocket;
    private InetSocketAddress mSerAddr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);

        initView();

        //开启线程
        mHanderThreadChild.start();
        mHanderChild = new Handler(mHanderThreadChild.getLooper());



        mBtnSendMessage.setOnClickListener(this);
        mBtnConnectTCP.setOnClickListener(this);

        mHandlerMain = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1){
                    Toast.makeText(TCPActivity.this, "开始连接TCP", Toast.LENGTH_SHORT).show();
                }else if (msg.what ==2){
                    Toast.makeText(TCPActivity.this, "TCP连接成功！", Toast.LENGTH_SHORT).show();
                }else if (msg.what ==3){
                    Toast.makeText(TCPActivity.this, "TCP连接超时！", Toast.LENGTH_SHORT).show();
                }
            }
        };


    }

    private void initView(){
        mEditTextIPAddr = findViewById(R.id.edit_text_ip_addr);
        mEditTextPort = findViewById(R.id.edit_text_port);
        mEditTextMessage = findViewById(R.id.edit_text_message);
        mBtnConnectTCP = findViewById(R.id.btn_connect_tcp);
        mBtnSendMessage = findViewById(R.id.btn_send_meaasge);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_connect_tcp:{
                //在线程中执行TCP连接

                //获取IP地址,端口号
                if (mEditTextIPAddr.getText().toString().isEmpty()){
                    Toast.makeText(this, "IP地址为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mEditTextPort.getText().toString().isEmpty()){
                    Toast.makeText(this, "端口号为空！", Toast.LENGTH_SHORT).show();
                }

                //
                final String IPAddr = mEditTextIPAddr.getText().toString();
                final int port = Integer.valueOf(mEditTextPort.getText().toString());
                mSerAddr = new InetSocketAddress(IPAddr,port);

                //连接TCP,在子线程中进行
                mHanderChild.post(new Runnable() {
                    @Override
                    public void run() {
                        Message message = Message.obtain();
                        try {
                            message.what = 1;
                            mHandlerMain.sendMessage(message);

                            Log.i(TAG,"正在建立TCP连接...");
                            //初始化socket
                            mSocket = new Socket(IPAddr,port);

                            //超时设置为5s
                           // mSocket.connect(mSerAddr,10000);
                            if ( mSocket.isConnected()){
                                Log.i(TAG,"TCP连接成功");
                                message.what = 2;
                                mHandlerMain.sendMessage(message);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TCPActivity.this, "TCP连接成功！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                Log.i(TAG,"TCP连接失败！");
                                message.what = 3;
                                mHandlerMain.sendMessage(message);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TCPActivity.this, "TCP连接失败！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });

                break;
            }
            case R.id.btn_send_meaasge:{
                break;
            }
            default:
                break;
        }
    }
}
