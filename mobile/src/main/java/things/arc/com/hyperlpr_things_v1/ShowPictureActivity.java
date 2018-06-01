package things.arc.com.hyperlpr_things_v1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class ShowPictureActivity extends AppCompatActivity {

    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        mImageView = findViewById(R.id.imageview_mobile);
        Intent intent = getIntent();
        if (intent.hasExtra("picturePath")){
            String path = intent.getStringExtra("picturePath");
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap!=null){
                mImageView.setImageBitmap(bitmap);
            }else{
                Toast.makeText(this, "照片加载失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
