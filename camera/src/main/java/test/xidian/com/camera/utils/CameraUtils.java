package test.xidian.com.camera.utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;

public class CameraUtils {

    private static String TAG = "CameraUtils";


    /**
     * @param context 上下文
     * @return 摄像头个数
     */
    public static int getCameraNumsOfDevice(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIdList;
        int cameraNum = 0;
        if (manager!=null){
            try {
                cameraIdList = manager.getCameraIdList();
                Log.i(TAG,"摄像头个数="+cameraIdList.length);
                cameraNum = cameraIdList.length;

            }catch (CameraAccessException e){
                e.printStackTrace();
            }

            return cameraNum;
        }else {
            return 0;
        }

    }



}
